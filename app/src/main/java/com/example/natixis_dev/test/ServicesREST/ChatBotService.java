package com.example.natixis_dev.test.ServicesREST;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by natixis-dev on 09/06/2017.
 */

public interface ChatBotService {

    public static final String ENDPOINT = "https://jp.createmyassistant.com";

    @GET("servlet/chatHttp")
    Call<TalkResponse> talk(@Query("data") String data);

    /*@GET("servlet/chatHttp")
    Call<TalkResponse> talkAsync(@Query("data") String data, Callback<TalkResponse> callback);*/

    @GET("servlet/chatHttp")
    Call<HistoryResponse> getHistory(@Query("data") String data);

}
