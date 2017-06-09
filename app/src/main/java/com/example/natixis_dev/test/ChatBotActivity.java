package com.example.natixis_dev.test;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.TextView;

import com.example.natixis_dev.test.ServicesREST.ChatBotService;
import com.example.natixis_dev.test.ServicesREST.TalkResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
        ChatBotActivity.MessageAdapter messageAdapter = new ChatBotActivity.MessageAdapter(messages);
        messagesRecyclerView.setAdapter(messageAdapter);

        initChatBot();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSpeak:
                //implement speech to text here
                break;
            case R.id.sendBtn:
                if(!inputMessage.getText().toString().isEmpty()){
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

    /***** RECYCLER VIEW STACK *********/

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        ChatBotActivity.MessageAdapter.ViewHolder vh = (ChatBotActivity.MessageAdapter.ViewHolder) rv.findContainingViewHolder(child);

        Intent intent = null;
        switch (vh.getAdapterPosition()) {
            case 0:
                break;
            default:
                break;
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

    public class MessageAdapter extends RecyclerView.Adapter<ChatBotActivity.MessageAdapter.ViewHolder> {
        private List<Message> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView messageTextView;
            public TextView dateTextView;
            public int mPosition;

            public ViewHolder(View v) {
                super(v);
                messageTextView = (TextView) v.findViewById(R.id.textmessage);
                dateTextView = (TextView) v.findViewById(R.id.datemessage);
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
        public ChatBotActivity.MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.messagesrecyclerview_item_row, parent, false);
            ChatBotActivity.MessageAdapter.ViewHolder vh = new ChatBotActivity.MessageAdapter.ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ChatBotActivity.MessageAdapter.ViewHolder holder, int position) {
            holder.setPosition(position);
            holder.messageTextView.setText(mDataset.get(position).getTextMessage());
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mDataset.get(position).getDateMessage());
            holder.dateTextView.setText(formatter.format(calendar.getTime()));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
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
