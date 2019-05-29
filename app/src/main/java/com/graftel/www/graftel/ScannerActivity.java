package com.graftel.www.graftel;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;

import com.google.zxing.Result;
import com.graftel.www.graftel.database.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Shorabh on 7/22/16.
 */
public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    static ZXingScannerView mScannerView;
    String result="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner);
        if (savedInstanceState != null) {
            return;
        }
        mScannerView = (ZXingScannerView) findViewById(R.id.scanner_view);
        mScannerView.startCamera();
        mScannerView.setResultHandler(this);
    }

    @Override
    @TargetApi(19)
    public void onResume() {
        super.onResume();
        //if (mScannerView.isAttachedToWindow()) {
        if (ViewCompat.isAttachedToWindow(mScannerView)) {

            mScannerView.startCamera();
            mScannerView.setResultHandler(this);
        } else
            mScannerView.resumeCameraPreview(ScannerActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String res = rawResult.getText();
        if(res.startsWith("GT:"))
            if(!result.contains(res.substring(res.indexOf(":")+1,res.indexOf("|"))))
                result = result + res.substring(res.indexOf(":")+1,res.indexOf("|"))+",";
        if(res.startsWith("GTPACK:")) {
            QID = res.substring(res.indexOf("=") + 1, res.indexOf("|"));
            new Load().execute();
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String temp[] = line.split(",");
            for(int i=0;i<temp.length;i++)
                if(!result.contains(temp[i]))
                    result = result + temp[i]+ ",";
        }
        new AlertDialog.Builder(this)
                .setMessage(result)
                .setPositiveButton("Continue Scanning!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mScannerView.resumeCameraPreview(ScannerActivity.this);
                    }
                })
                .setNegativeButton("Done!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(),DeviceInfoActivity.class).putExtra("result",result));
                    }
                })
                .create()
                .show();
    }

    JSONParser jParser = new JSONParser();
    String QID = null;
    JSONArray products = null;
    String line = "";

    class Load extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("quoteId",QID));
            JSONObject json = jParser.makeHttpRequest("https://www.graftel.com/appport/webGetCalInfoProd.php", "GET", params);
            if(json!=null) {
                //Log.d("All Products: ", json.toString());
                try {
                    int success = json.getInt("success");
                    if (success == 1) {
                        products = json.getJSONArray("temp");
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject c = products.getJSONObject(i);
                            if(!line.contains(c.getString("Calibration ID #")))
                                line += c.getString("Calibration ID #")+",";
                        }
                    } else {
                    }
                } catch (JSONException e) {
                }
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
        }
    }

}

