package com.tagny.dev.test.Components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tagny.dev.test.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tagny on 29/06/2017.
 */

public class SenderBubble extends RelativeLayout {

    @BindView(R.id.textmessage)
    TextView mTextMessage;
    @BindView(R.id.datemessage)
    TextView mDateMessage;
    @BindView(R.id.profilePicture)
    ImageView mProfilePicture;
    @BindView(R.id.image)
    ImageView mSendImage;

    private Context mContext;
    private boolean showProfilePicture = true;
    private Drawable profilePicture = null;

    public SenderBubble(Context context) {
        super(context);
        init(context, null);
    }

    public SenderBubble(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SenderBubble(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SenderBubble(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context aContext, AttributeSet attrs) {
        this.mContext = aContext;

        if(attrs != null) {
            TypedArray a = aContext.obtainStyledAttributes(attrs,
                    R.styleable.SenderBubble, 0, 0);
            showProfilePicture = a.getBoolean(R.styleable.SenderBubble_showProfilePicture, true);
            profilePicture = a.getDrawable(R.styleable.SenderBubble_profilePicture);
            a.recycle();
        }

        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sender_bubble, this);

        ButterKnife.bind(this);

        if(showProfilePicture) {
            if(profilePicture != null) mProfilePicture.setImageDrawable(profilePicture);
        } else {
            mProfilePicture.setVisibility(View.GONE);
        }
        mTextMessage.setVisibility(View.GONE);
        mSendImage.setVisibility(View.GONE);
    }

    public void setTextMessage(SpannableString sText){
        if(sText != null) {
            mTextMessage.setVisibility(View.VISIBLE);
            mTextMessage.setMovementMethod(LinkMovementMethod.getInstance());
            mTextMessage.setText(sText, TextView.BufferType.SPANNABLE);
        }
        else mTextMessage.setVisibility(View.GONE);
    }

    public void setDateMessage(long sDate){
        SimpleDateFormat mFormatter = new SimpleDateFormat("hh:mm");
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(sDate);
        mDateMessage.setText(mFormatter.format(mCalendar.getTime()));
    }

    public void setImageToSend(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 4;
        //options.inPreferredConfig = Bitmap.Config.RGB_565;
        //options.inDither = true;

        if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                mSendImage.setVisibility(View.VISIBLE);
                mSendImage.setImageBitmap(bitmap);
            } else {
                mSendImage.setVisibility(View.GONE);
            }
        }

}
