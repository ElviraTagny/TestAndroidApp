package com.example.natixis_dev.test;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import com.example.natixis_dev.test.DoYouDreamUpServices.DYDUChatBotService;
import com.example.natixis_dev.test.NatServices.NatChatBotService;
import com.example.natixis_dev.test.Services.ChatBotService;
import com.example.natixis_dev.test.Utils.TopActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatBotActivity extends TopActivity implements ChatBotService.DisplayMessageInterface {

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
    private NatChatBotService chatBotService;
    private TextToSpeech tts;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chat_bot);
        super.onCreate(savedInstanceState);
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

        chatBotService = NatChatBotService.getInstance();
        chatBotService.init(this, this);
        messages = chatBotService.getDataSource().getAllMessages();
        MessageAdapter messageAdapter = new MessageAdapter(messages);
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
            Message message = new Message(getString(R.string.dudy_welcome_message), null, 0, false);
            displayMessage(message);
            chatBotService.getDataSource().createMessage(message);
        }

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
                            chatBotService.getDataSource().deleteAllMessages();
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

    @OnClick(R.id.btnSpeak)
    public void speak() {
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
    }

    @OnClick(R.id.sendBtn)
    public void send() {
        if (!inputMessage.getText().toString().isEmpty()) {
            //creation d'un nouveau message
            final String textToSend = inputMessage.getText().toString();
            displayMessage(textToSend, null, true);
            chatBotService.sendMessage(textToSend);
            inputMessage.setText("");
        }
    }

    @OnClick(R.id.btnCamera)
    public void takePicture(){
        takePicture(REQ_CODE_TAKE_PHOTO);
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
                    displayMessage("", getCurrentUriFile().getPath(), true);
                    //chatBotService.sendMessage(""); //on n'envoie pas encore les photos
                }
                break;
        }
    }

    public void onDestroy(){
        chatBotService.closeDatabase();
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onDisplayMessage(Message aMessage) {
        displayMessage(aMessage);
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

            //@BindView(R.id.image)
            public ImageView imageView;

            @BindView(R.id.datemessage)
            public TextView dateTextView;

            public int mPosition;

            //@BindView(R.id.btnRead)
            public ImageButton btnRead;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
                imageView = (ImageView) v.findViewById(R.id.image);
                btnRead = (ImageButton) v.findViewById(R.id.btnRead);
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
                holder.messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
                if(holder.btnRead != null) { //means it's a bot message //TODO find another way to show it
                    holder.messageTextView.setText(chatBotService.getBotFormattedText(txt), TextView.BufferType.SPANNABLE);
                }
                else holder.messageTextView.setText(txt);
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

    private void displayMessage(final Message aMessage) {
        messages.add(aMessage);
        if(messages.size() > 0) {
            messagesRecyclerView.getAdapter().notifyItemInserted(messages.size() - 1);
            messagesRecyclerView.smoothScrollToPosition(
                    messagesRecyclerView.getAdapter().getItemCount() - 1);
        }
    }

    private void displayMessage(final String sTextMessage, final String bImagePath, boolean bSend) {
        Message message = new Message(sTextMessage, bImagePath, 0, bSend);
        displayMessage(message);
    }

    /*private void sendMessage(final String sTextMessage){
        //envoi de la requete
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
            public void run() {

                chatBotService.sendMessage(sTextMessage);
            }
        }, 300);
    }*/
}
