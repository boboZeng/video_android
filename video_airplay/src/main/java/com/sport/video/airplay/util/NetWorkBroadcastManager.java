package com.sport.video.airplay.util;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import androidx.lifecycle.Observer;

import static android.content.Context.CONNECTIVITY_SERVICE;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-12-02.
 **/
public class NetWorkBroadcastManager {
    private Observer<Boolean> observer;

    public NetWorkBroadcastManager(Observer<Boolean> observer) {
        this.observer = observer;
    }

    private ConnectivityManager getConnectivityManager(Context context) {
        return (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    }

    private ConnectivityManager.NetworkCallback connectivityManagerCallback;

    public void registerReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getConnectivityManager(context).registerDefaultNetworkCallback(getConnectivityManagerCallback());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lollipopNetworkAvailableRequest(context);
        } else {
            context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void unregisterReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getConnectivityManager(context).unregisterNetworkCallback(connectivityManagerCallback);
        } else {
            context.unregisterReceiver(networkReceiver);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void lollipopNetworkAvailableRequest(Context context) {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        getConnectivityManager(context).registerNetworkCallback(
                builder.build(), getConnectivityManagerCallback());
    }


    private ConnectivityManager.NetworkCallback getConnectivityManagerCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManagerCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                }


                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                }

                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);

                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    if (observer != null) {
                        observer.onChanged(true);
                    }
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                }
            };
        }
        return connectivityManagerCallback;
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (observer != null) {
                    NetworkInfo activeNetwork = getConnectivityManager(context).getActiveNetworkInfo();
                    observer.onChanged(activeNetwork != null && activeNetwork.isConnected());
                }
            }
        }
    };
}
