package com.example.natixis_dev.test;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.natixis_dev.test.Utils.TopActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class TextToSpeechActivity extends TopActivity/* implements View.OnClickListener*/ {

    @BindView(R.id.btnRead)
    Button readButton;

    @BindView(R.id.inputText)
    EditText textToRead;

    private AudioManager audioManager;
    @BindView(R.id.volume_bar)
    SeekBar volumeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_text_to_speech);
        super.onCreate(savedInstanceState);

        audioManager =
                (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        volumeBar.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));


        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar arg0)
            {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0)
            {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
            {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        progress, 0);
            }
        });
    }

    @OnClick(R.id.btnRead)
    public void read(){
        read(textToRead.getText().toString());
    }
}
