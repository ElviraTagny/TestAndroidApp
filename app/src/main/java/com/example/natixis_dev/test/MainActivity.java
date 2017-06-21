package com.example.natixis_dev.test;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.natixis_dev.test.Utils.TopActivity;
import com.example.natixis_dev.test.Utils.Utils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends TopActivity implements RecyclerView.RecyclerListener, RecyclerView.OnItemTouchListener {

    @BindView(R.id.menuRecyclerView)
    RecyclerView menuRecyclerView;

    @BindView(R.id.language_button)
    Button languageButton;

    private String[] menuList = {"Speech to text", "Text to Speech", "TouchID feature", "Send a SMS/Email", "Take a picture",
            "Open a webview", "ChatBot", "NFC", "Scan code", "Watch",
            "Glasses", "Twitter", "LinkedIn", "NFC Payment", "OCR", "Realite augmentee"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        // hide the action bar
        try {
            getActionBar().hide();
        }
        catch (NullPointerException e){
        }
        Log.d(APP_TAG, "Locale " + Locale.getDefault().toString());
        menuRecyclerView.setRecyclerListener(this);
        menuRecyclerView.addOnItemTouchListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        menuRecyclerView.setLayoutManager(mLayoutManager);
        MenuAdapter menuAdapter = new MenuAdapter(menuList);
        menuRecyclerView.setAdapter(menuAdapter);

        if(Locale.getDefault().toString().contains(Locale.FRENCH.toString())){
            languageButton.setBackgroundResource(R.drawable.uk_flag);
        }
        else if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString())){
            languageButton.setBackgroundResource(R.drawable.fr_flag);
        }
    }

    @OnClick(R.id.language_button)
    public void changeLanguage(){
        Log.d(APP_TAG, "Locale " + Locale.getDefault().toString());
        if(Locale.getDefault().toString().contains(Locale.FRENCH.toString())){
            //languageButton.setBackgroundResource(R.drawable.fr_flag);
            //Locale.setDefault(Locale.UK);
            changeLanguage(Locale.UK);
        }
        else if(Locale.getDefault().toString().contains(Locale.ENGLISH.toString())){
            //languageButton.setBackgroundResource(R.drawable.uk_flag);
            //Locale.setDefault(Locale.FRANCE);
            changeLanguage(Locale.FRANCE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        Log.w(APP_TAG + MainActivity.class.getSimpleName() , "onViewRecycled");
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        Log.w(APP_TAG + MainActivity.class.getSimpleName() , "onTouchEvent");
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        Log.w(APP_TAG + MainActivity.class.getSimpleName() , "onRequestDisallowInterceptTouchEvent");
    }

    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private String[] mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.label)
            public TextView mTextView;
            public int mPosition;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }

            @OnClick(R.id.label)
            public void goToNextPage(){
                Intent intent = null;
                switch ((int) mTextView.getTag()){
                    case 0:
                        intent = new Intent(MainActivity.this, SpeechToTextActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, TextToSpeechActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, FingerPrintActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, MessageActivity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        intent = new Intent(MainActivity.this, PhotoVideoActivity.class);
                        startActivity(intent);
                        break;
                    case 5:
                        intent = new Intent(MainActivity.this, WebviewActivity.class);
                        startActivity(intent);
                        break;
                    case 6:
                        intent = new Intent(MainActivity.this, ChatBotActivity.class);
                        startActivity(intent);
                        break;
                    case 7:
                        intent = new Intent(MainActivity.this, NfcActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

            public void setPosition(int position) {
                this.mPosition = position;
                mTextView.setTag(mPosition);
            }
        }

        public MenuAdapter(String[] myDataset) {
            mDataset = myDataset;
        }

        @Override
        public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.menurecyclerview_item_row, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setPosition(position);
            holder.mTextView.setText(mDataset[position]);
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }
}
