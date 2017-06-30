package com.example.natixis_dev.test.NatServices;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;

import com.example.natixis_dev.test.Services.ChatBotService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.WebSocket;

import rx.Observable;
import rx.schedulers.Schedulers;
//import rx.android.schedulers.AndroidSchedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

/**
 * Created by natixis-dev on 27/06/2017.
 */

public class NatChatBotService extends ChatBotService {

    private static NatChatBotService ourInstance = new NatChatBotService();

    public static NatChatBotService getInstance() {
        return ourInstance;
    }

    private StompClient mStompClient;
    private Gson mGson = new GsonBuilder().create();

    @Override
    public void init(Context aContext, DisplayMessageInterface iDisplayMessageInterface) {
        super.init(aContext, iDisplayMessageInterface);

        mStompClient = Stomp.over(WebSocket.class, "ws://chatbot-89c3.zapto.org/chatbot-box/gs-guide-websocket");
        //mStompClient = Stomp.over(WebSocket.class, "ws://" + ANDROID_EMULATOR_LOCALHOST + ":" + RestClient.SERVER_PORT + "/example-endpoint/websocket");
        mStompClient.topic("/topic/greetings")
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(MODULE_TAG, topicMessage.getPayload());
                    mGson.fromJson(topicMessage.getPayload(), NatBotResponse.class);
        });
        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                //.observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {

                case OPENED:
                    Log.d(MODULE_TAG, "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e(MODULE_TAG, "Error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    Log.d(MODULE_TAG, "Stomp connection closed");
                    break;
            }
        });

        mStompClient.connect(true);
    }

    protected <T> Observable.Transformer<T, T> applySchedulers() {
        return rObservable -> rObservable
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io());
                //.observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void close() {
        super.close();
        mStompClient.disconnect();
    }

    @Override
    public void sendMessage(String sUrl, String sData) {
        super.sendMessage(sUrl, sData);
        //mStompClient.send(sUrl, sData).subscribe();
        mStompClient.send(sUrl, sData)
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(MODULE_TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(MODULE_TAG, "Error send STOMP echo", throwable);
                    Log.e(MODULE_TAG, throwable.getMessage());
                });
    }

    @Override
    public void sendMessage(String sData) {
        super.sendMessage(sData);
        if(mStompClient.isConnected()){
            mStompClient.send("/app/hello", getJson(sData)).subscribe();
            //new StompMessage("/app/hello", null, "Slut");
        }
        else {
            mStompClient.connect(true);
        }
    }

    private String getJson(String sData) {
        return mGson.toJson(new NatBotRequest(sData));
        //"{name: sData}"
    }

    @Override
    public SpannableString getBotFormattedText(String txt) {
        return SpannableString.valueOf(txt);
    }
}
