package com.example.natixis_dev.test;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.natixis_dev.test.Database.Message;
import com.example.natixis_dev.test.Database.MessageDataSource;
import com.example.natixis_dev.test.ServicesREST.ChatBotService;
import com.example.natixis_dev.test.ServicesREST.TalkResponse;
import com.example.natixis_dev.test.Utils.CustomTagHandler;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity implements View.OnClickListener, Callback<TalkResponse> {

    @BindView(R.id.messagesRecyclerView)
    RecyclerView messagesRecyclerView;

    @BindView(R.id.inputMessage)
    EditText inputMessage;

    @BindView(R.id.sendBtn)
    View sendButton;

    @BindView(R.id.btnSpeak)
    View speakButton;

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

        inputMessage = (EditText) findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendBtn);
        sendButton.setOnClickListener(this);
        speakButton = findViewById(R.id.btnSpeak);
        speakButton.setOnClickListener(this);

        //Recuperer les messages de la database
        datasource = new MessageDataSource(this);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        messages = datasource.getAllMessages();
        if(messages.isEmpty()) {
            //welcome message
            Message message = new Message("DYDU à votre disposition ! Que puis-je faire pour vous?", 0, false);
            messages.add(message);
            datasource.createMessage(message);
        }
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
            builder.setTitle("Supprimer")
                    .setMessage("Voulez-vous supprimer entièrement cette conversation ?")
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
                    Message message = new Message(textToSend, 0, true);
                    messages.add(message);
                    messagesRecyclerView.getAdapter().notifyItemInserted(messages.size() -1);
                    messagesRecyclerView.smoothScrollToPosition(
                            messagesRecyclerView.getAdapter().getItemCount() - 1);
                    datasource.createMessage(message);
                    inputMessage.setText("");
                    //envoi de la requete
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable(){
                        public void run() {
                            Call<TalkResponse> call = chatBotService.talk(getTalkRequestParameters(textToSend));
                            call.enqueue(ChatBotActivity.this);
                        }
                    }, 300);


                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputMessage.setText(result.get(0));
                }
                break;
            }

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView messageTextView;
            public TextView dateTextView;
            public int mPosition;
            public ImageButton btnRead;

            public ViewHolder(View v) {
                super(v);
                messageTextView = (TextView) v.findViewById(R.id.textmessage);
                dateTextView = (TextView) v.findViewById(R.id.datemessage);
                btnRead = (ImageButton) v.findViewById(R.id.btnRead);
                if(btnRead != null) btnRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            tts.speak(messageTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "REQUEST_CODE");
                        }
                        else tts.speak(messageTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }

            public void setPosition(int position) {
                this.mPosition = position;
            }
        }

        public MessageAdapter(List<Message> myDataset) {
            mDataset = myDataset;
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
            holder.messageTextView.setText(mDataset.get(position).getTextMessage());
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
            Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "An error occured: " + response.errorBody().toString());
            Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
        }
        else if(response.body() != null) {
            Log.w("TAGHANDLER", response.body().getValues().getText());
            Message message = new Message(Html.fromHtml(decodeFromBase64ToUtf8(response.body().getValues().getText()), null, new CustomTagHandler()).toString(), 0, false);
            messages.add(message);
            messagesRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
            messagesRecyclerView.smoothScrollToPosition(
                    messagesRecyclerView.getAdapter().getItemCount() - 1);
            datasource.createMessage(message);
        }
    }

    @Override
    public void onFailure(Call<TalkResponse> call, Throwable t) {
        Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "An error occured: " + t.getLocalizedMessage());
    }

    private Map<String, Object> getHistoryRequestParameters(){

        Map<String, Object> data = new HashMap<>();

        return data;
    }

    private String getTalkRequestParameters(String userInput){
        StringBuilder data = new StringBuilder();
        data.append("{\"type\":\"talk\",");
        data.append("\"parameters\":{");
        //data.append("\"userUrl\":\"" + encodeFromUtf8ToBase64("http://front1.doyoudreamup.com/TestRecipe/30bfa404-279b-48c7-b8b7-ba8d4adf498e/de8f0b26-4a18-4051-8573-cf1430626d97/sample.debug.html") + "\",");
        //data.append("\"alreadyCame\":true,");
        data.append("\"clientId\":\"" + encodeFromUtf8ToBase64("USERID_123") + "\","); //USERID_123
        //data.append("\"os\":\"Linux x86_64\",");
        //data.append("\"browser\":\"Firefox 45.0\",");
        //data.append("\"disableLanguageDetection\":\"" + encodeFromUtf8ToBase64("true") + "\",");
        //data.append("\"contextType\":\"Web\",");
        //data.append("\"mode\":\"Synchrone\",");
        data.append("\"botId\":\"" + encodeFromUtf8ToBase64("972f1264-6d85-4a58-b5ac-da31481dda63") + "\","); //30bfa404-279b-48c7-b8b7-ba8d4adf498e
        //data.append("\"qualificationMode\":true,");
        data.append("\"language\":\"" + encodeFromUtf8ToBase64("fr") + "\",");
        data.append("\"space\":\"" + encodeFromUtf8ToBase64("Defaut") + "\",");
        data.append("\"solutionUsed\":\"" + encodeFromUtf8ToBase64("ASSISTANT") + "\",");
        data.append("\"pureLivechat\":false,");
        data.append("\"userInput\":\"" + encodeFromUtf8ToBase64(userInput) + "\",");
        data.append("\"contextId\":\"" + encodeFromUtf8ToBase64("1982637c-1ff2-4224-8c63-c0ced20cc8ca") + "\"");
        data.append("}}");


        return data.toString();
    }

    private String encodeFromUtf8ToBase64(String sDataToEncode){
        byte[] data = new byte[0];
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                data = sDataToEncode.getBytes(StandardCharsets.UTF_8);
            } else data = sDataToEncode.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "Encode error", e);
        }
        String base64Str = Base64.encodeToString(data, Base64.DEFAULT);
        return base64Str;
    }

    private String decodeFromBase64ToUtf8(String sDataToDecode){
        byte[] data = Base64.decode(sDataToDecode, Base64.DEFAULT);
        String utf8Str = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                utf8Str = new String(data, StandardCharsets.UTF_8);
            } else utf8Str = new String(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e){
            Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "Decode error", e);
        }
        return utf8Str;
    }

}
