package com.applaudostudio.weekfourchallengeone.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/***
 * Class with the functons to check the internet connection
 */
public class ConnectionManager {

    private Context mContext;

    public ConnectionManager(Context context){
        mContext=context;
    }
    public boolean isNetworkAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }


}
