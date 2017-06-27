package com.example.natixis_dev.test.DoYouDreamUpServices;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;

import com.example.natixis_dev.test.ChatBotActivity;
import com.example.natixis_dev.test.Database.Message;
import com.example.natixis_dev.test.Services.ChatBotService;
import com.example.natixis_dev.test.Utils.Utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by natixis-dev on 09/06/2017.
 */

public class DYDUChatBotService extends ChatBotService implements Callback<DYDUTalkResponse> {

    public static final String ENDPOINT = "https://jp.createmyassistant.com";
    //public static final String TALK_SUFFIX_URL = "https://jp.createmyassistant.com";

    private DYDUInterface dyduInterface;
    private static DYDUChatBotService ourInstance = new DYDUChatBotService();

    public static DYDUChatBotService getInstance() {
        return ourInstance;
    }

    @Override
    public void sendMessage(String sData, String sUrl) {
        super.sendMessage(sData, sUrl);
        Call<DYDUTalkResponse> call = dyduInterface.talk(sData);
        call.enqueue(this);
    }

    @Override
    public void sendMessage(String sData) {
        super.sendMessage(sData);
        Call<DYDUTalkResponse> call = dyduInterface.talk(getTalkRequestParameters(sData));
        call.enqueue(this);
    }

    @Override
    public void init(Context aContext, DisplayMessageInterface iDisplayMessageInterface) {
        super.init(aContext, iDisplayMessageInterface);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(DYDUChatBotService.ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dyduInterface = retrofit.create(DYDUInterface.class);
    }

    @Override
    public SpannableString getBotFormattedText(String sText) {
        if(sText.contains("<p")|| sText.contains("<br") || sText.contains("&")) {
            // Html to Text
            sText = Html.fromHtml(sText, null, new CustomTagHandler()).toString();

            if(sText.contains("•") || sText.contains("reword")) {
                SpannableString clickableText = new SpannableString(sText);

                String[] choices = sText.split("•");
                if (choices.length > 1) {
                    choices[0] = null;
                    for (String choice : choices) {
                        if (choice != null) {
                            choice = choice.replace("?", "");
                            final String choiceF = choice.trim();
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                @Override
                                public void onClick(View textView) {
                                    sendMessage(choiceF);
                                    if(iDisplayMessageInterface != null) iDisplayMessageInterface.onDisplayMessage(new Message(choiceF, null, 0, true));
                                }
                            };
                            int startIndex = sText.lastIndexOf(choiceF);
                            int endIndex = startIndex + choiceF.length();
                            clickableText.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            //For UnderLine
                            clickableText.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            //For Bold
                            clickableText.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                return clickableText;
            }
        }
        return SpannableString.valueOf(sText);
    }

    /*****************************/
    /*** CUSTOM CALLBACK STACK ***/
    /*****************************/

    @Override
    public void onResponse(Call<DYDUTalkResponse> call, Response<DYDUTalkResponse> response) {
        if(response.errorBody() != null){
            Log.e(MODULE_TAG + ChatBotActivity.class.getSimpleName(), "An error occured: " + response.errorBody().toString());
            Message message = new Message("Oups ! Un problème est survenu...", null, 0, false);
            getDataSource().createMessage(message);
            if(iDisplayMessageInterface != null) iDisplayMessageInterface.onDisplayMessage(message);
        }
        else if(response.body() != null) {

            String messageText = Utils.decodeFromBase64ToUtf8(response.body().getValues().getText());
            Message message = new Message(messageText, null, 0, false);
            getDataSource().createMessage(message);
            if(iDisplayMessageInterface != null) iDisplayMessageInterface.onDisplayMessage(message);
        }
    }

    @Override
    public void onFailure(Call<DYDUTalkResponse> call, Throwable t) {
        Log.e(MODULE_TAG + ChatBotActivity.class.getSimpleName(), "An error occured: " + t.getLocalizedMessage());
    }

    /****************************/
    /*** CUSTOM METHODS STACK ***/
    /****************************/

    private Map<String, Object> getHistoryRequestParameters(){

        Map<String, Object> data = new HashMap<>();

        return data;
    }

    public String getTalkRequestParameters(String userInput/*, String bImagePath*/){
        StringBuilder data = new StringBuilder();
        data.append("{\"type\":\"talk\",");
        data.append("\"parameters\":{");
        //data.append("\"userUrl\":\"" + Utils.encodeFromUtf8ToBase64("http://front1.doyoudreamup.com/TestRecipe/30bfa404-279b-48c7-b8b7-ba8d4adf498e/de8f0b26-4a18-4051-8573-cf1430626d97/sample.debug.html") + "\",");
        //data.append("\"alreadyCame\":true,");
        data.append("\"clientId\":\"" + Utils.encodeFromUtf8ToBase64("USERID_123") + "\","); //USERID_123
        //data.append("\"os\":\"Linux x86_64\",");
        //data.append("\"browser\":\"Firefox 45.0\",");
        //data.append("\"disableLanguageDetection\":\"" + Utils.encodeFromUtf8ToBase64("true") + "\",");
        //data.append("\"contextType\":\"Web\",");
        //data.append("\"mode\":\"Synchrone\",");
        data.append("\"botId\":\"" + Utils.encodeFromUtf8ToBase64("972f1264-6d85-4a58-b5ac-da31481dda63") + "\","); //30bfa404-279b-48c7-b8b7-ba8d4adf498e
        //data.append("\"qualificationMode\":true,");
        data.append("\"language\":\"" + Utils.encodeFromUtf8ToBase64("fr") + "\",");
        data.append("\"space\":\"" + Utils.encodeFromUtf8ToBase64("Defaut") + "\",");
        data.append("\"solutionUsed\":\"" + Utils.encodeFromUtf8ToBase64("ASSISTANT") + "\",");
        data.append("\"pureLivechat\":false,");
        String encodedPicture = "";
        /*if(bImagePath != null){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = null;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                bitmap = BitmapFactory.decodeFile(bImagePath, options);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            }
            catch (OutOfMemoryError err){
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
                Log.e(MODULE_TAG+ ChatBotActivity.class.getSimpleName(), "Out of memory error catched");
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            encodedPicture = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }*/
        data.append("\"userInput\":\"" + encodedPicture + Utils.encodeFromUtf8ToBase64(userInput) + "\",");
        data.append("\"contextId\":\"" + Utils.encodeFromUtf8ToBase64("1982637c-1ff2-4224-8c63-c0ced20cc8ca") + "\"");
        data.append("}}");

        return data.toString();
    }

}
