package com.example.natixis_dev.test;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.natixis_dev.test.Database.Message;
import com.example.natixis_dev.test.Database.MessageDataSource;
import com.example.natixis_dev.test.ServicesREST.ChatBotService;
import com.example.natixis_dev.test.ServicesREST.TalkResponse;
import com.example.natixis_dev.test.Utils.CustomTagHandler;
import com.example.natixis_dev.test.Utils.TopActivity;
import com.example.natixis_dev.test.Utils.Utils;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends TopActivity implements View.OnClickListener, Callback<TalkResponse> {

    @BindView(R.id.messagesRecyclerView)
    RecyclerView messagesRecyclerView;

    @BindView(R.id.inputMessage)
    EditText inputMessage;

    @BindView(R.id.sendBtn)
    View sendButton;

    @BindView(R.id.btnSpeak)
    View speakButton;

    @BindView(R.id.btnCamera)
    View cameraButton;

    private List<Message> messages = new ArrayList<>();
    private ChatBotService chatBotService;
    private TextToSpeech tts;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private MessageDataSource datasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        sendButton = findViewById(R.id.sendBtn);
        sendButton.setOnClickListener(this);
        speakButton = findViewById(R.id.btnSpeak);
        speakButton.setOnClickListener(this);
        cameraButton = findViewById(R.id.btnCamera);
        cameraButton.setOnClickListener(this);

        inputMessage = (EditText) findViewById(R.id.inputMessage);
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    sendButton.setVisibility(View.VISIBLE);
                    speakButton.setVisibility(View.GONE);
                }
                else {
                    sendButton.setVisibility(View.GONE);
                    speakButton.setVisibility(View.VISIBLE);
                }
            }
        });

        //Recuperer les messages de la database
        datasource = new MessageDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        messages = datasource.getAllMessages();
        MessageAdapter messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(mLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);
        if (Build.VERSION.SDK_INT >= 11) {
            messagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        messagesRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if(messagesRecyclerView.getAdapter().getItemCount() > 0) messagesRecyclerView.smoothScrollToPosition(
                                        messagesRecyclerView.getAdapter().getItemCount() - 1);
                            }
                        });
                    }
                }
            });
        }
        if(messages.isEmpty()) {
            //welcome message
            addMessage(getString(R.string.dudy_welcome_message), null, false);
        }


        initChatBot();

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.getDefault());
                    }
                }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chatbot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(getString(R.string.button_delete))
                    .setMessage(getString(R.string.alert_delete_text))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            datasource.deleteAllMessages();
                            messages.clear();
                            messagesRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSpeak:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.sendBtn:
                if(!inputMessage.getText().toString().isEmpty()){
                    //creation d'un nouveau message
                    final String textToSend = inputMessage.getText().toString();
                    addMessage(textToSend, null, true);
                    inputMessage.setText("");
                }
                break;

            case R.id.btnCamera:
                takePicture(REQ_CODE_TAKE_PHOTO);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputMessage.setText(result.get(0));
                }
                break;
            case REQ_CODE_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    addMessage("", getCurrentUriFile().getPath(), true);

                }
                break;
        }
    }

    public void onDestroy(){
        datasource.close();
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /***** RECYCLER VIEW STACK *********/

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> mDataset;
        SimpleDateFormat mFormatter = new SimpleDateFormat("hh:mm");
        Calendar mCalendar = Calendar.getInstance();
        BitmapFactory.Options options = new BitmapFactory.Options();
        private Bitmap bitmap;

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.textmessage)
            public TextView messageTextView;
            @BindView(R.id.image)
            public ImageView imageView;
            @BindView(R.id.datemessage)
            public TextView dateTextView;
            public int mPosition;
            @BindView(R.id.btnRead)
            public ImageButton btnRead;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
                //messageTextView = (TextView) v.findViewById(R.id.textmessage);
                //imageView = (ImageView) v.findViewById(R.id.image);
                //dateTextView = (TextView) v.findViewById(R.id.datemessage);
                //btnRead = (ImageButton) v.findViewById(R.id.btnRead);
                if(btnRead != null) btnRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        read(messageTextView.getText().toString());
                    }
                });
            }

            public void setPosition(int position) {
                this.mPosition = position;
            }
        }

        public MessageAdapter(List<Message> myDataset) {
            mDataset = myDataset;
            options.inJustDecodeBounds = false;
            options.inSampleSize = 4;
            //options.inPreferredConfig = Bitmap.Config.RGB_565;
            //options.inDither = true;
        }

        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
            View v = null;
            if(viewType == 1){
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.messagesrecyclerview_sender_row, parent, false);
            }
            else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.messagesrecyclerview_item_row, parent, false);
            }
            MessageAdapter.ViewHolder vh = new MessageAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
            holder.setPosition(position);
            String txt = mDataset.get(position).getTextMessage();
            if(!txt.isEmpty()){
                holder.messageTextView.setVisibility(View.VISIBLE);
                holder.messageTextView.setText(txt);
                holder.messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else holder.messageTextView.setVisibility(View.GONE);
            if(holder.imageView != null) {
                if (mDataset.get(position).getImagePath() != null && !mDataset.get(position).getImagePath().isEmpty()) {
                    bitmap = BitmapFactory.decodeFile(mDataset.get(position).getImagePath(), options);
                    holder.imageView.setVisibility(View.VISIBLE);
                    holder.imageView.setImageBitmap(bitmap);
                } else {
                    holder.imageView.setVisibility(View.GONE);
                }
            }
            mCalendar.setTimeInMillis(mDataset.get(position).getDateMessage());
            holder.dateTextView.setText(mFormatter.format(mCalendar.getTime()));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mDataset.get(position).isSender() ? 1: 0;
        }
    }

    private void addMessage(final String sTextMessage, final String bImagePath, boolean bSend) {
        Message message = new Message(sTextMessage, bImagePath, 0, bSend);
        messages.add(message);
        if(messages.size() > 0) {
            messagesRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
            messagesRecyclerView.smoothScrollToPosition(
                    messagesRecyclerView.getAdapter().getItemCount() - 1);
        }
        datasource.createMessage(message);

        if(bSend){ // on envoie pas les images pour l'instant
            //envoi de la requete
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable(){
                public void run() {
                    Call<TalkResponse> call = chatBotService.talk(getTalkRequestParameters(sTextMessage, bImagePath));
                    call.enqueue(ChatBotActivity.this);
                }
            }, 300);
        }
    }

    /***** CHATBOT STACK *******/

    public void initChatBot(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ChatBotService.ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        chatBotService = retrofit.create(ChatBotService.class);
    }

    @Override
    public void onResponse(Call<TalkResponse> call, Response<TalkResponse> response) {
        if(response.errorBody() != null){
            Log.e(APP_TAG + ChatBotActivity.class.getSimpleName(), "An error occured: " + response.errorBody().toString());
            //Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
            addMessage("Oups ! Un problème est survenu...", null, false);
        }
        else if(response.body() != null) {
            String messageText = getCleanText(response.body().getValues().getText());
            addMessage(messageText, null, false);
        }
    }

    @Override
    public void onFailure(Call<TalkResponse> call, Throwable t) {
        Log.e(APP_TAG + ChatBotActivity.class.getSimpleName(), "An error occured: " + t.getLocalizedMessage());
    }

    private Map<String, Object> getHistoryRequestParameters(){

        Map<String, Object> data = new HashMap<>();

        return data;
    }

    private String getTalkRequestParameters(String userInput, String bImagePath){
        StringBuilder data = new StringBuilder();
        data.append("{\"type\":\"talk\",");
        data.append("\"parameters\":{");
        //data.append("\"userUrl\":\"" + Utils.encodeFromUtf8ToBase64("http://front1.doyoudreamup.com/TestRecipe/30bfa404-279b-48c7-b8b7-ba8d4adf498e/de8f0b26-4a18-4051-8573-cf1430626d97/sample.debug.html") + "\",");
        //data.append("\"alreadyCame\":true,");
        data.append("\"clientId\":\"" + Utils.encodeFromUtf8ToBase64("USERID_123") + "\","); //USERID_123
        //data.append("\"os\":\"Linux x86_64\",");
        //data.append("\"browser\":\"Firefox 45.0\",");
        //data.append("\"disableLanguageDetection\":\"" + Utils.encodeFromUtf8ToBase64("true") + "\",");
        //data.append("\"contextType\":\"Web\",");
        //data.append("\"mode\":\"Synchrone\",");
        data.append("\"botId\":\"" + Utils.encodeFromUtf8ToBase64("972f1264-6d85-4a58-b5ac-da31481dda63") + "\","); //30bfa404-279b-48c7-b8b7-ba8d4adf498e
        //data.append("\"qualificationMode\":true,");
        data.append("\"language\":\"" + Utils.encodeFromUtf8ToBase64("fr") + "\",");
        data.append("\"space\":\"" + Utils.encodeFromUtf8ToBase64("Defaut") + "\",");
        data.append("\"solutionUsed\":\"" + Utils.encodeFromUtf8ToBase64("ASSISTANT") + "\",");
        data.append("\"pureLivechat\":false,");
        String encodedPicture = "";
        if(bImagePath != null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                bitmap = BitmapFactory.decodeFile(bImagePath, options);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            }
            catch (OutOfMemoryError err){
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
                Log.e(APP_TAG+ ChatBotActivity.class.getSimpleName(), "Out of memory error catched");
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            encodedPicture = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        data.append("\"userInput\":\"" + encodedPicture + Utils.encodeFromUtf8ToBase64(userInput) + "\",");
        data.append("\"contextId\":\"" + Utils.encodeFromUtf8ToBase64("1982637c-1ff2-4224-8c63-c0ced20cc8ca") + "\"");
        data.append("}}");

        return data.toString();
    }

    private String getCleanText(String sEncodedHtmlText) {
        // Base 64 decode
        String text = Utils.decodeFromBase64ToUtf8(sEncodedHtmlText);
        // Get text to underline
/*
        Pattern pattern = Pattern.compile("onclick=\"reword.*',");
        Matcher matcher = pattern.matcher(text);
        for (int i = 1; i < matcher.groupCount(); i++){
            Log.d("PARSE", matcher.group(i) +" ");
        }
*/

        // Html to Text
        text = Html.fromHtml(text, null, new CustomTagHandler()).toString();
        //String[] und_text = text.split("•");

        return text;
    }
}
