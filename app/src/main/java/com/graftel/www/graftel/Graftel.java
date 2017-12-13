package com.graftel.www.graftel;

import android.app.Application;

/**
 * Created by Shorabh on 7/21/2016.
 */
public class Graftel extends Application {

    private static Graftel mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static synchronized Graftel getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}

