package com.example.natixis_dev.test.Services;

import android.content.Context;
import android.text.SpannableString;

import com.example.natixis_dev.test.Database.Message;
import com.example.natixis_dev.test.Database.MessageDataSource;

import java.sql.SQLException;

/**
 * Created by natixis-dev on 27/06/2017.
 */

public abstract class ChatBotService {

    public static final String MODULE_TAG = "ChatBotService";
    private MessageDataSource datasource;
    public DisplayMessageInterface iDisplayMessageInterface;

    public interface DisplayMessageInterface {
        public void onDisplayMessage(Message aMessage);
    }

    public void init(Context aContext, DisplayMessageInterface iDisplayMessageInterface){
        openDatabase(aContext);
        this.iDisplayMessageInterface = iDisplayMessageInterface;
    }

    public void close(){
        closeDatabase();
    };

    public void openDatabase(Context aContext){
        datasource = new MessageDataSource(aContext);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeDatabase() {
        datasource.close();
    }

    public MessageDataSource getDataSource() {
        return datasource;
    }

    public void sendMessage(String sUrl, String sData){
        datasource.createMessage(new Message(sData, null, 0, true));
    }

    public void sendMessage(String sData){
        datasource.createMessage(new Message(sData, null, 0, true));
    }

    public abstract SpannableString getBotFormattedText(String txt);
}
