package com.graftel.www.graftel;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

/**
 * Created by Shorabh on 7/21/2016.
 */
public class Graftel extends MultiDexApplication {

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

