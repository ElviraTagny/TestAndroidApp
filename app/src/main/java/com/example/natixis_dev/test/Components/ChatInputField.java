package com.example.natixis_dev.test.Components;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.natixis_dev.test.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by natixis-dev on 30/06/2017.
 */

public class ChatInputField extends RelativeLayout {

    public interface IChatInputField {

        public void onSendPressed();
        public void onMicroPressed();
        public void onCameraPressed();

    }

    private IChatInputField iChatInputField;

    private Context mContext;
    @BindView(R.id.inputMessage)
    EditText mInputField;

    @BindView(R.id.sendBtn)
    View mSendButton;

    @BindView(R.id.btnSpeak)
    View mMicroButton;

    @BindView(R.id.btnCamera)
    View mCameraButton;

    public ChatInputField(Context context) {
        super(context);
        init(context, null);
    }

    public ChatInputField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ChatInputField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ChatInputField(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context aContext, AttributeSet attrs) {
        this.mContext = aContext;

        if(attrs != null) {
            TypedArray a = aContext.obtainStyledAttributes(attrs,
                    R.styleable.BotBubble, 0, 0);
//            showProfilePicture = a.getBoolean(R.styleable.BotBubble_showProfilePicture, true);
            a.recycle();
        }

        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.chat_input_field, this);

        ButterKnife.bind(this);

        mInputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    mSendButton.setVisibility(View.VISIBLE);
                    mMicroButton.setVisibility(View.GONE);
                }
                else {
                    mSendButton.setVisibility(View.GONE);
                    mMicroButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @OnClick(R.id.btnSpeak)
    public void speak() {
        if(iChatInputField != null) iChatInputField.onMicroPressed();
    }

    @OnClick(R.id.sendBtn)
    public void send() {
        if(iChatInputField != null) iChatInputField.onSendPressed();
    }

    @OnClick(R.id.btnCamera)
    public void takePicture(){
        if(iChatInputField != null) iChatInputField.onCameraPressed();
    }

    public void setListener(IChatInputField aListener) {
        iChatInputField = aListener;
    }

    public void setInput(String aInputText){
        mInputField.setText(aInputText);
    }

    public String getInput(){
        return mInputField.getText().toString();
    }
}
