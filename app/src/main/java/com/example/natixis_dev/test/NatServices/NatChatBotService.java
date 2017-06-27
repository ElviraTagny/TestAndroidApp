package com.example.natixis_dev.test.NatServices;

import android.content.Context;
import android.text.SpannableString;

import com.example.natixis_dev.test.Services.ChatBotService;

/**
 * Created by natixis-dev on 27/06/2017.
 */

public class NatChatBotService extends ChatBotService {

    private static NatChatBotService ourInstance = new NatChatBotService();

    public static NatChatBotService getInstance() {
        return ourInstance;
    }

    @Override
    public void init(Context aContext, DisplayMessageInterface iDisplayMessageInterface) {
        super.init(aContext, iDisplayMessageInterface);
    }

    @Override
    public void sendMessage(String sUrl, String sData) {

    }

    @Override
    public void sendMessage(String sData) {

    }

    @Override
    public SpannableString getBotFormattedText(String txt) {
        return SpannableString.valueOf(txt);
    }
}
