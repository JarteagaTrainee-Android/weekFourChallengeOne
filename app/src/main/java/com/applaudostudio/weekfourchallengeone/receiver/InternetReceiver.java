package com.applaudostudio.weekfourchallengeone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.applaudostudio.weekfourchallengeone.manager.ConnectionManager;

public class InternetReceiver extends BroadcastReceiver {
    private InternetConnectionListener mInternetContract;


    public InternetReceiver() {
    }

    public InternetReceiver(InternetConnectionListener callback){
        mInternetContract=callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(checkInternet(context)){
            //Toast toast = Toast.makeText(context, "NETWORK AVAILABLE", Toast.LENGTH_LONG);
            //toast.show();
            mInternetContract.onInternetAvailable(true);
        }else{
            Toast toast = Toast.makeText(context, "NETWORK UNAVAILABLE", Toast.LENGTH_LONG);
            toast.show();
            mInternetContract.onInternetAvailable(false);
        }
    }

    boolean checkInternet(Context context) {
        ConnectionManager serviceManager = new ConnectionManager(context);
        return serviceManager.isNetworkAvailable();
    }

    public interface InternetConnectionListener{
        void onInternetAvailable(boolean status);
    }

}
