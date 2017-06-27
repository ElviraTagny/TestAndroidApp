package com.example.natixis_dev.test.DoYouDreamUpServices;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by natixis-dev on 27/06/2017.
 */

public interface DYDUInterface {

    @GET("servlet/chatHttp")
    public Call<DYDUTalkResponse> talk(@Query("data") String data);

    /*@GET("servlet/chatHttp")
    Call<TalkResponse> talkAsync(@Query("data") String data, Callback<TalkResponse> callback);*/

    @GET("servlet/chatHttp")
    Call<DYDUHistoryResponse> getHistory(@Query("data") String data);
}
