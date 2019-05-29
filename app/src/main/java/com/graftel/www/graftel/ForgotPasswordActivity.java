package com.graftel.www.graftel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

/**
 * Created by Shorabh on 7/18/2016.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private WebView mWebview ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebview  = new WebView(this);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview .loadUrl("https://www.graftel.com/portal/forgot-password/");
        setContentView(mWebview);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
