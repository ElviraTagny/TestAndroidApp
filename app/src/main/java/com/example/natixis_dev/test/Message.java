package com.example.natixis_dev.test;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by natixis-dev on 08/06/2017.
 */

public class Message {

    private String textMessage;
    private long dateMessage;
    private boolean isSender;

    public Message(String textMessage, int minutesAgo, boolean isSender) {
        this.textMessage = textMessage;
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
}
