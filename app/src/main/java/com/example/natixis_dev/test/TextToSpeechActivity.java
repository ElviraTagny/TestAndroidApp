package com.example.natixis_dev.test;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class TextToSpeechActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btnRead)
    Button readButton;

    @BindView(R.id.inputText)
    EditText textToRead;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.getDefault());
                }
            }
        });
        textToRead = (EditText) findViewById(R.id.inputText);
        readButton = (Button) findViewById(R.id.btnRead);
        readButton.setOnClickListener(this);
    }

    @OnClick(R.id.btnRead)
    public void readInputText(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(textToRead.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, "REQUEST_CODE");
        }
        else tts.speak(textToRead.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRead:
                readInputText();
                break;
            default:
                break;
        }
    }

    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }
}
