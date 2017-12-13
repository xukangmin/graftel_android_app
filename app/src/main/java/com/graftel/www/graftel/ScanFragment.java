package com.graftel.www.graftel;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Shorabh on 6/10/2016.
 */
public class ScanFragment extends Fragment
{
    Button b1;

    @TargetApi(23)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.scan, container, false);
        b1 = (Button) rootView.findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener(){
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                int hasCameraPermission = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA);
                if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.CAMERA},1);
                    return;
                }
                startActivity(new Intent(getActivity(),ScannerActivity.class));
            }
        });
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity(), "Scan Access Denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
