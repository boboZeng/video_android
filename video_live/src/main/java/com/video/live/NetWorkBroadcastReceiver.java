package com.video.live;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-12-02.
 **/
public abstract class NetWorkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        VideoUtils.d("NetWorkBroadcastReceiver onReceive action:" + intent.getAction());
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            onConnect(context);
        }
    }

    public abstract void onConnect(Context context);
}
