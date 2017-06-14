package com.example.natixis_dev.test.Database;

import android.graphics.Bitmap;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by natixis-dev on 08/06/2017.
 */

public class Message {

    private long id;
    private String textMessage;
    private String imagePath;
    private long dateMessage;
    private boolean isSender;

    public Message() {
    }

    public Message(String textMessage, String imagePath, int minutesAgo, boolean isSender) {
        this.textMessage = textMessage;
        this.imagePath = imagePath;
        this.dateMessage = Calendar.getInstance().getTimeInMillis() - (minutesAgo * 60 * 1000);
        this.isSender = isSender;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public long getDateMessage() {
        return dateMessage;
    }

    public boolean isSender() {
        return isSender;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public void setDateMessage(long dateMessage) {
        this.dateMessage = dateMessage;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
