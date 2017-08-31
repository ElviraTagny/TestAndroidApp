package com.tagny.dev.test;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tagny.dev.test.Utils.TopActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class WebviewActivity extends TopActivity/* implements View.OnClickListener*/ {

    private static final CharSequence WWW_PREFIX = "www.";
    private static final CharSequence HTTP_PREFIX = "http://";
    private static final CharSequence HTTPS_PREFIX = "https://";

    @BindView(R.id.webview)
    WebView myBrowser;

    @BindView(R.id.go_button)
    Button goButton;

    @BindView(R.id.inputUrl)
    EditText inputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_webview);
        super.onCreate(savedInstanceState);

        myBrowser.setWebViewClient(new MyBrowser());
        myBrowser.getSettings().setLoadsImagesAutomatically(true);
        myBrowser.getSettings().setJavaScriptEnabled(true);
    }

    @OnClick(R.id.go_button)
    public void goToPage() {
        if(!inputUrl.getText().toString().isEmpty()) {
            String url = inputUrl.getText().toString();
            myBrowser.loadUrl(reformat(url));
        }
    }

    private String reformat(String p_Url) {
        if(!p_Url.contains(HTTP_PREFIX) && !p_Url.contains(HTTPS_PREFIX)){
           p_Url = HTTP_PREFIX + p_Url;
        }
        return p_Url;
    }

    private class MyBrowser extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.loadUrl(request.getUrl().toString());
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            findViewById(R.id.loading_textview).setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }
    }
}
