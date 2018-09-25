package com.applaudostudio.weekfourchallengeone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.applaudostudio.weekfourchallengeone.manager.ConnectionManager;

/***
 * Broadcast Receiver to listen the changes on the network status
 */
public class InternetReceiver extends BroadcastReceiver {
    private InternetConnectionListener mInternetContract;


    public InternetReceiver(InternetConnectionListener callback) {
        mInternetContract = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (checkInternet(context)) {
            //Toast toast = Toast.makeText(context, "NETWORK AVAILABLE", Toast.LENGTH_LONG);
            //toast.show();
            mInternetContract.onInternetAvailable(true);
        } else {
            Toast toast = Toast.makeText(context, "NETWORK UNAVAILABLE", Toast.LENGTH_LONG);
            toast.show();
            mInternetContract.onInternetAvailable(false);
        }
    }

    /***
     * Function to check internet
     * @param context context as param
     * @return returns tru if network available
     */
    boolean checkInternet(Context context) {
        ConnectionManager serviceManager = new ConnectionManager(context);
        return serviceManager.isNetworkAvailable();
    }

    /***
     * Interface to make a contract with the Main Activity
     */
    public interface InternetConnectionListener {
        void onInternetAvailable(boolean status);
    }

}
