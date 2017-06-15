package com.example.natixis_dev.test;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @BindView(R.id.btnCamera)
    View cameraButton;

    private List<Message> messages = new ArrayList<>();
    private ChatBotService chatBotService;
    private TextToSpeech tts;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_TAKE_PHOTO = 1;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 200;
    public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 300;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    private MessageDataSource datasource;
    private static Uri currentPictureUri;

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
            addMessage("DYDU à votre disposition ! Que puis-je faire pour vous?", null, false);
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
                    addMessage(textToSend, null, true);
                    inputMessage.setText("");
                }
                break;

            case R.id.btnCamera:
                if(checkPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA, "Camera")
                        && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_STORAGE, "Files")
                        && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_STORAGE, "Files")){
                    openCamera();
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
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputMessage.setText(result.get(0));
                }
                break;
            case REQ_CODE_TAKE_PHOTO:
                if(resultCode == RESULT_OK) {
                    addMessage("", currentPictureUri.getPath(), true);

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
            public TextView messageTextView;
            public ImageView imageView;
            public TextView dateTextView;
            public int mPosition;
            public ImageButton btnRead;

            public ViewHolder(View v) {
                super(v);
                messageTextView = (TextView) v.findViewById(R.id.textmessage);
                imageView = (ImageView) v.findViewById(R.id.image);
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
            //Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
            addMessage("Oups ! Un problème est survenu...", null, false);
        }
        else if(response.body() != null) {
            String messageText = getCleanText(response.body().getValues().getText());
            addMessage(messageText, null, false);
        }
    }

    private String getCleanText(String sEncodedHtmlText) {
        // Base 64 decode
        String text = decodeFromBase64ToUtf8(sEncodedHtmlText);
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

    @Override
    public void onFailure(Call<TalkResponse> call, Throwable t) {
        Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "An error occured: " + t.getLocalizedMessage());
    }

    private Map<String, Object> getHistoryRequestParameters(){

        Map<String, Object> data = new HashMap<>();

        return data;
    }

    private String getTalkRequestParameters(String userInput, String bImagePath){
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
                Log.e("TestApp - "+ ChatBotActivity.class.getSimpleName(), "Out of memory error catched");
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            encodedPicture = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        data.append("\"userInput\":\"" + encodedPicture + encodeFromUtf8ToBase64(userInput) + "\",");
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

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    private void showAlert(final String permission, final int requestCode, String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Permission required");
        alertDialog.setMessage("TestApp needs to access the " + featureName + ". Please grant permission.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(ChatBotActivity.this,
                                new String[]{permission},
                                requestCode);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert(String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Permission required");
        alertDialog.setMessage("TestApp needs to access the " + featureName + ". Please grant permission.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DON'T ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(ChatBotActivity.this);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        String permission = null;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                //for (int i = 0, len = permissions.length; i < len; i++) {
                permission = permissions[0];
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean
                            showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                    this, permission);
                     if (showRationale) {
                         showAlert(permission, requestCode, "Camera");
                     } else if (!showRationale) {
                         // user denied flagging NEVER ASK AGAIN
                         // you can either enable some fall back,
                         // disable features of your app
                         // or open another dialog explaining
                         // again the permission and directing to
                         // the app setting
                         saveToPreferences(this, ALLOW_KEY, true);
                     }
                }
                else {
                    openCamera();
                }
                //}
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE:
            case MY_PERMISSIONS_REQUEST_READ_STORAGE:
                //for (int i = 0, len = permissions.length; i < len; i++) {
                permission = permissions[0];
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale =
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                    if (showRationale) {
                        showAlert(permission, requestCode, "Files");
                    } else if (!showRationale) {
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        saveToPreferences(this, ALLOW_KEY, true);
                    }
                }
                else {
                    openCamera();
                }
                //}
            break;
            default:
                break;
        }
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void openCamera() {

        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/TestApp/";
        File newDir = new File(dir);
        newDir.mkdirs();

        long currentTimeInMS = (new Date()).getTime();
        String filename = "chat_picture(" + currentTimeInMS + ").jpg";
        File newfile = new File(newDir, filename);
        try {
            newfile.createNewFile();
        }
        catch (IOException e)
        {
            Log.e("TestApp - " + ChatBotActivity.class.getSimpleName(), "openCamera - ", e);
        }

        currentPictureUri = Uri.fromFile(newfile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPictureUri);

        startActivityForResult(cameraIntent, REQ_CODE_TAKE_PHOTO);
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

    private boolean checkPermission(String permission, int requestCode, String featureName){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (getFromPref(this, ALLOW_KEY)) {
                showSettingsAlert(featureName);
            } else if (ContextCompat.checkSelfPermission(this,
                    permission)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permission)) {
                    showAlert(permission, requestCode, featureName);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{permission},
                            requestCode);
                }
            }
            return false;
        } else {
            return true;
        }
    }

}
