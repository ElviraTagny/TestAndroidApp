package com.example.natixis_dev.test;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.natixis_dev.test.Utils.TopActivity;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class TextToSpeechActivity extends TopActivity implements View.OnClickListener {

    @BindView(R.id.btnRead)
    Button readButton;

    @BindView(R.id.inputText)
    EditText textToRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);

        textToRead = (EditText) findViewById(R.id.inputText);
        readButton = (Button) findViewById(R.id.btnRead);
        readButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRead:
                read(textToRead.getText().toString());
                break;
            default:
                break;
        }
    }
}
