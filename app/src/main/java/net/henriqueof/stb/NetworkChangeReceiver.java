package net.henriqueof.stb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Carlos Henrique on 23/02/2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;

        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();

            Log.d(TAG, "connectivity info:" + networkInfo);
        }

        if(networkInfo != null && networkInfo.isConnected()) {
            //TODO: see why this is called multiple times and handle schedule reloading
            Log.d(TAG, "have Wifi connection and is connected");
        }else
            Log.d(TAG, "don't have Wifi connect or it isn't connected");
    }
}
