package com.tagny.dev.test;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.tagny.dev.test.Utils.TopActivity;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class YoutubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    //private YouTube mYouTubeAPI;
    private YouTubePlayerView mYouTubePlayerView;
    private YouTubePlayer mYouTubePlayer;
    @BindView(R.id.loadButton)
    public ImageView mLoadButton;
    private static final String[] SCOPES = { YouTubeScopes.YOUTUBE_READONLY };
    public static final String DEVELOPER_KEY = "AIzaSyAEKo9Zli1GM4QOQVo8jZh9h4R2oR2tiLQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        ButterKnife.bind(this);

        mYouTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        mYouTubePlayerView.initialize(DEVELOPER_KEY, this);
        // Initialize credentials and service object.
        /*GoogleAccountCredential mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mYouTubeAPI = new com.google.api.services.youtube.YouTube.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("TagnyTest")
                .build();*/
    }

    @BindView(R.id.inputUrl)
    public EditText mInputUrl;

    @OnClick(R.id.loadButton)
    public void loadVideo(){

        if(mInputUrl.getText() != null) {
            String url = mInputUrl.getText().toString();
            String videoId = url.split("youtu.be/")[1];
            mYouTubePlayer.loadVideo(videoId);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if(!wasRestored){
            mYouTubePlayer = youTubePlayer;
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Log.e(TopActivity.APP_TAG, "An error occured !");
        youTubeInitializationResult.getErrorDialog(this, 1);
    }
}
