package com.example.natixis_dev.test;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.natixis_dev.test.ServicesREST.ChatBotService;
import com.example.natixis_dev.test.ServicesREST.TalkResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity implements View.OnClickListener, RecyclerView.RecyclerListener, RecyclerView.OnItemTouchListener {

    private List<Message> messages = new ArrayList<>();
    private ChatBotService chatBotService;

    @BindView(R.id.messagesRecyclerView)
    RecyclerView messagesRecyclerView;

    @BindView(R.id.inputMessage)
    EditText inputMessage;

    @BindView(R.id.sendBtn)
    View sendButton;

    @BindView(R.id.btnSpeak)
    View speakButton;

    private TextToSpeech tts;
    private final int REQ_CODE_SPEECH_INPUT = 100;

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

        messagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setRecyclerListener(this);
        messagesRecyclerView.addOnItemTouchListener(this);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(mLayoutManager);

        messages.add(new Message("DYDU à votre disposition ! Que puis-je faire pour vous?", 10, false));
        MessageAdapter messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setAdapter(messageAdapter);

        tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });

        initChatBot();
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
                    //disparition du clavier
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputMessage.getWindowToken(), 0);
                    //creation d'un nouveau message
                    messages.add(new Message(inputMessage.getText().toString(), 0, true));
                    //rafraichissement de la recyclerView
                    messagesRecyclerView.getAdapter().notifyDataSetChanged();

                    //envoi de la requete
                    Call<TalkResponse> call = chatBotService.talk(getTalkRequestParameters(inputMessage.getText().toString()));
                    call.enqueue(new Callback<TalkResponse>() {
                        @Override
                        public void onResponse(Call<TalkResponse> call, Response<TalkResponse> response) {
                            if(response.errorBody() != null){
                                Log.e("CHATBOT", "An error occured: " + response.errorBody().toString());
                                messages.add(new Message("Ah, je crois qu'un problème est survenu", 0, false));
                                messagesRecyclerView.getAdapter().notifyDataSetChanged();
                            }
                            else if(response.body() != null) {
                                messages.add(new Message(response.body().getValues().getText(), 0, false));
                                messagesRecyclerView.getAdapter().notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<TalkResponse> call, Throwable t) {
                            Log.e("CHATBOT", "An error occured: " + t.getLocalizedMessage());
                        }
                    });

                    inputMessage.setText("");
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

    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    /***** RECYCLER VIEW STACK *********/

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if(child != null) {
            MessageAdapter.ViewHolder vh = (MessageAdapter.ViewHolder) rv.findContainingViewHolder(child);

            Intent intent = null;
            switch (vh.getAdapterPosition()) {
                case 0:
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {

    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> mDataset;

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
            }

//            public int getPosition(){
//                return mPosition;
//            }

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
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDataset.get(position).getDateMessage());
            holder.dateTextView.setText(formatter.format(calendar.getTime()));
            final ViewHolder vh = holder;
            if(holder.btnRead != null) holder.btnRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(vh.messageTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "REQUEST_CODE");
                    }
                    else tts.speak(vh.messageTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(mDataset.get(position).isSender()){
                return 1;
            }
            else {
                return 0;
            }
        }
    }

    /***** CHATBOT STACK *******/

    public void initChatBot(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ChatBotService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        chatBotService = retrofit.create(ChatBotService.class);
    }

    private Map<String, Object> getHistoryRequestParameters(){

        Map<String, Object> data = new HashMap<>();

        return data;
    }

    private String getTalkRequestParameters(String userInput){
        StringBuilder data = new StringBuilder();
        data.append("{\"type\":\"talk\",");
        data.append("\"parameters\": {");
        data.append("\"userUrl\": \"http://front1.doyoudreamup.com/TestRecipe/30bfa404-279b-48c7-b8b7-ba8d4adf498e/de8f0b26-4a18-4051-8573-cf1430626d97/sample.debug.html\",");
        data.append("\"alreadyCame\": true,");
        data.append("\"clientId\": \"2Hb8pJKEA20gdKX\",");
        data.append("\"os\": \"Linux x86_64\",");
        data.append("\"browser\": \"Firefox 45.0\",");
        data.append("\"disableLanguageDetection\": \"true\",");
        data.append("\"contextType\": \"Web\",");
        data.append("\"mode\": \"Synchrone\",");
        data.append("\"botId\": \"30bfa404-279b-48c7-b8b7-ba8d4adf498e\",");
        data.append("\"qualificationMode\": true,");
        data.append("\"language\": \"fr\",");
        data.append("\"space\": \"Defaut\",");
        data.append("\"solutionUsed\": \"ASSISTANT\",");
        data.append("\"pureLivechat\": true,");
        data.append("\"userInput\":\"" + userInput + "\",");
        data.append("\"contextId\": \"1982637c-1ff2-4224-8c63-c0ced20cc8ca\"");
        data.append("}}");

        return data.toString();
    }
}
