package com.graftel.www.graftel;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by graftel on 10/13/16.
 */

public class PDFView {

    private static CognitoCachingCredentialsProvider credentialsProvider;
    private static File temp = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    private static AmazonS3 s3;
    private static TransferUtility transferUtility;
    private static TransferObserver observer;
    private static ProgressDialog pDialog;

    public void PDFView(final Context context, String bucket, String key) {
        credentialsProvider = new CognitoCachingCredentialsProvider(context,"us-east-1:d4f153ee-c628-4497-974b-af7d5be33054",Regions.US_EAST_1);
        s3 = new AmazonS3Client(credentialsProvider);
        temp  = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+key);
        transferUtility = new TransferUtility(s3, context);
        observer = transferUtility.download(bucket,key,temp);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                pDialog = new ProgressDialog(context);
                if (state != com.amazonaws.mobileconnectors.s3.transferutility.TransferState.COMPLETED) {
                    pDialog.setMessage("Loading. Please wait...");
                    pDialog.setIndeterminate(false);
                    pDialog.setCancelable(false);
                    pDialog.show();
                }
                if (state == com.amazonaws.mobileconnectors.s3.transferutility.TransferState.COMPLETED) {
                    pDialog.dismiss();
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(temp), "application/pdf");
                        context.startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No Application to view PDF", Toast.LENGTH_SHORT).show();
                    }
                }
                System.out.println(state);
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent / bytesTotal * 100);
                if(percentage == 100) {
                    pDialog.dismiss();
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(temp), "application/pdf");
                        context.startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No Application to view PDF", Toast.LENGTH_SHORT).show();
                    }
                }
               //System.out.println(percentage);
            }

            @Override
            public void onError(int id, Exception ex) {
                System.out.println(ex);
            }

        });
    }
}
