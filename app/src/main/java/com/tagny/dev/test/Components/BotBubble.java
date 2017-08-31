package com.tagny.dev.test.Components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tagny.dev.test.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tagny on 29/06/2017.
 */

public class BotBubble extends RelativeLayout {

    @BindView(R.id.textmessage)
    TextView mTextMessage;
    @BindView(R.id.datemessage)
    TextView mDateMessage;
    @BindView(R.id.profilePicture)
    ImageView mProfilePicture;
    @BindView(R.id.btnRead)
    ImageButton mSpeaker;

    private Context mContext;
    private TextToSpeech tts;
    private boolean showProfilePicture = true;
    private boolean showSpeaker = true;
    private Drawable profilePicture = null;

    public BotBubble(Context context) {
        super(context);
        init(context, null);
    }

    public BotBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BotBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BotBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context aContext, AttributeSet attrs) {
        this.mContext = aContext;

        if(attrs != null) {
            TypedArray a = aContext.obtainStyledAttributes(attrs,
                    R.styleable.BotBubble, 0, 0);
            showProfilePicture = a.getBoolean(R.styleable.BotBubble_showProfilePicture, true);
            showSpeaker = a.getBoolean(R.styleable.BotBubble_showSpeaker, true);
            profilePicture = a.getDrawable(R.styleable.BotBubble_profilePicture);
            a.recycle();
        }

        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bot_bubble, this);

        ButterKnife.bind(this);
        // layout is inflated, assign local variables to components
        /*mProfilePicture = (ImageView) findViewById(R.id.profilePicture);
        mTextMessage = (TextView) findViewById(R.id.textmessage);
        mDateMessage = (TextView) findViewById(R.id.datemessage);
        mSpeaker = (ImageButton) findViewById(R.id.btnRead);
        if(mSpeaker != null) mSpeaker.setOnClickListener(v ->
                read(mTextMessage.getText().toString()));*/

        if(showProfilePicture) {
            if(profilePicture != null) mProfilePicture.setImageDrawable(profilePicture);
        } else {
            mProfilePicture.setVisibility(View.GONE);
        }
        mTextMessage.setVisibility(View.GONE);
        if(!showSpeaker) mSpeaker.setVisibility(View.GONE);
    }

    @OnClick(R.id.btnRead)
    public void read(){
        String textToRead = mTextMessage.getText().toString();
        if(tts == null){
            tts =new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.getDefault());
                    }
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "REQUEST_CODE");
        }
        else tts.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void setTextMessage(SpannableString sText){
        mTextMessage.setVisibility(View.VISIBLE);
        mTextMessage.setMovementMethod(LinkMovementMethod.getInstance());
        mTextMessage.setText(sText, TextView.BufferType.SPANNABLE);
    }

    public void setDateMessage(long sDate){
        SimpleDateFormat mFormatter = new SimpleDateFormat("hh:mm");
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(sDate);
        mDateMessage.setText(mFormatter.format(mCalendar.getTime()));
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if(tts !=null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}
