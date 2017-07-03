package com.example.natixis_dev.test.Components;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.natixis_dev.test.Database.Message;
import com.example.natixis_dev.test.DoYouDreamUpServices.DYDUChatBotService;
import com.example.natixis_dev.test.NatServices.NatChatBotService;
import com.example.natixis_dev.test.R;
import com.example.natixis_dev.test.Services.ChatBotService;
import com.example.natixis_dev.test.Utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatBotActivity extends AppCompatActivity implements ChatBotService.DisplayMessageInterface, ChatInputField.IChatInputField {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 200;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 300;

    public static final String APP_TAG = "TestApp - ";
    private static final String ALLOW_KEY = "ALLOWED";
    protected final int REQ_CODE_TAKE_PHOTO = 1;
    private static Uri currentUriFile;

    @BindView(R.id.messagesRecyclerView)
    RecyclerView messagesRecyclerView;

    @BindView(R.id.sendmessageLayout)
    ChatInputField chatInputField;

    private List<Message> messages = new ArrayList<>();
    private DYDUChatBotService chatBotService;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        ButterKnife.bind(this);

        chatInputField.setListener(this);

        chatBotService = DYDUChatBotService.getInstance();
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

    @Override
    public void onMicroPressed(){
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

    @Override
    public void onSendPressed() {
        if (!chatInputField.getInput().isEmpty()) {
            //création d'un nouveau message
            displayMessage(chatInputField.getInput(), null, true);
            chatBotService.sendMessage(chatInputField.getInput());
            chatInputField.setInput("");
        }
    }

    @Override
    public void onCameraPressed(){
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
                    chatInputField.setInput(result.get(0));
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
        chatBotService.close();
        super.onDestroy();
    }

    @Override
    public void onDisplayMessage(Message aMessage) {
        displayMessage(aMessage);
    }

    /***** RECYCLER VIEW STACK *********/

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
        private List<Message> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public int mPosition;
            @BindView(R.id.bot_bubble)
            @Nullable
            BotBubble botBubble;
            @BindView(R.id.sender_bubble)
            @Nullable
            SenderBubble senderBubble;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
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
            String txt = mDataset.get(position).getTextMessage();
            if(holder.botBubble != null) {
                holder.botBubble.setTextMessage(chatBotService.getBotFormattedText(txt));
                holder.botBubble.setDateMessage(mDataset.get(position).getDateMessage());
            }
            if(holder.senderBubble != null) {
                holder.senderBubble.setTextMessage(SpannableString.valueOf(txt));
                holder.senderBubble.setDateMessage(mDataset.get(position).getDateMessage());
                holder.senderBubble.setImageToSend(mDataset.get(position).getImagePath());
            }
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

    protected boolean checkPermission(String permission, int requestCode, String featureName){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (Utils.getFromPreferences(this, ALLOW_KEY)) {
                showSettingsAlertForPermission(this, featureName);
            } else if (ContextCompat.checkSelfPermission(this,
                    permission)

                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        permission)) {
                    showAlertForPermission(this, permission, requestCode, featureName);
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

    protected void showAlertForPermission(final Activity activity, final String permission, final int requestCode, String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert_permission_title));
        alertDialog.setMessage(getString(R.string.alert_permission_text, featureName));

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_permission_negative_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_permission_positive_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(activity,
                                new String[]{permission},
                                requestCode);
                    }
                });
        alertDialog.show();
    }

    protected void showSettingsAlertForPermission(final Activity activity, String featureName) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.alert_permission_title));
        alertDialog.setMessage(getString(R.string.alert_permission_text, featureName));

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_permission_negative_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_permission_settings_button),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(activity);
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
                        showAlertForPermission(this, permission, requestCode, "Camera");
                    } else if (!showRationale) {
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        Utils.saveToPreferences(this, ALLOW_KEY, true);
                    }
                }
                else {
                    takePicture(REQ_CODE_TAKE_PHOTO);
                }
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
                        showAlertForPermission(this, permission, requestCode, "Files");
                    } else if (!showRationale) {
                        // user denied flagging NEVER ASK AGAIN
                        // you can either enable some fall back,
                        // disable features of your app
                        // or open another dialog explaining
                        // again the permission and directing to
                        // the app setting
                        Utils.saveToPreferences(this, ALLOW_KEY, true);
                    }
                }
                else {
                    takePicture(REQ_CODE_TAKE_PHOTO);
                }
                break;
            default:
                break;
        }
    }

    protected static void startInstalledAppDetailsActivity(final Activity activity) {
        if (activity == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + activity.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        activity.startActivity(i);
    }

    protected void takePicture(int requestCode) {
        if(checkPermission(Manifest.permission.CAMERA, MY_PERMISSIONS_REQUEST_CAMERA, "Camera")
                && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_WRITE_STORAGE, "Files")
                && checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_STORAGE, "Files")) {
            final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Natixis_ChatBot/";
            File newDir = new File(dir);
            newDir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new Date());
            String filename = getString(R.string.file_prefix) + timeStamp + getString(R.string.file_extension);
            File newfile = new File(newDir, filename);
            try {
                newfile.createNewFile();
            } catch (IOException e) {
                Log.e(APP_TAG + ChatBotActivity.class.getSimpleName(), "takePicture - ", e);
            }
            currentUriFile = Uri.fromFile(newfile);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentUriFile);
            startActivityForResult(cameraIntent, requestCode);
        }
    }

    protected Uri getCurrentUriFile(){
        return currentUriFile;
    }
}
