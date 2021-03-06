package com.tagny.dev.test.Services;

import android.content.Context;
import android.text.SpannableString;

import com.tagny.dev.test.Database.Message;
import com.tagny.dev.test.Database.MessageDataSource;

import java.sql.SQLException;

/**
 * Created by tagny on 27/06/2017.
 */

public abstract class ChatBotService {

    public static final String MODULE_TAG = "ChatBotService";
    private MessageDataSource datasource;
    public DisplayMessageInterface iDisplayMessageInterface;

    public interface DisplayMessageInterface {
        void onDisplayMessage(Message aMessage);
    }

    public void init(Context aContext, DisplayMessageInterface iDisplayMessageInterface){
        openDatabase(aContext);
        this.iDisplayMessageInterface = iDisplayMessageInterface;
    }

    public void close(){
        closeDatabase();
    };

    private void openDatabase(Context aContext){
        datasource = new MessageDataSource(aContext);
        try {
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closeDatabase() {
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
