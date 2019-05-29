package com.graftel.www.graftel;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * Created by Shorabh on 6/28/2016.
 */
public class ServicesActivity extends AppCompatActivity {

    private WebView mWebview ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebview  = new WebView(this);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview .loadUrl("https://www.graftel.com/services/");
        setContentView(mWebview);
    }
}