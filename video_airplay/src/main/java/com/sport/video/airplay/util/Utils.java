package com.sport.video.airplay.util;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import java.util.Formatter;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * 说明：
 * 作者：zhouzhan
 * 日期：17/6/29 10:47
 */

public class Utils {

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    /**
     * 把时间戳转换成 00:00:00 格式
     *
     * @param timeMs 时间戳
     * @return 00:00:00 时间格式
     */
    public static String getStringTime(int timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        formatBuilder.setLength(0);
        return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    /**
     * 把 00:00:00 格式转成时间戳
     *
     * @param formatTime 00:00:00 时间格式
     * @return 时间戳(毫秒)
     */
    public static int getIntTime(String formatTime) {
        if (isNull(formatTime)) {
            return 0;
        }

        String[] tmp = formatTime.split(":");
        if (tmp.length < 3) {
            return 0;
        }
        int second = Integer.valueOf(tmp[0]) * 3600 + Integer.valueOf(tmp[1]) * 60 + Integer.valueOf(tmp[2]);

        return second * 1000;
    }

    public static ObjectAnimator getRotationAnimator(View v, long duration, int repeatCount) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "rotation", 0, 360);
        animator.setDuration(duration);
        animator.setRepeatCount(repeatCount);
        return animator;
    }


    /**
     * 判断WIFI网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static String wifiName(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.getExtraInfo().replace("\"", "");
            }
        }
        return "";
    }

    public static CharSequence foregroundColorSpan(String text, int color, int start, int end) {
        SpannableString sp = new SpannableString(text);
        sp.setSpan(new ForegroundColorSpan(color), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    public static void saveAirPlayDevice(Context context, String device) {
        SharedPreferences.Editor editor = context.getSharedPreferences("AirPlay", MODE_PRIVATE).edit();
        editor.putString("play_device", device);
        editor.apply();
    }

    public static String readAirPlayDevice(Context context) {
        SharedPreferences sp = context.getSharedPreferences("AirPlay", MODE_PRIVATE);
        String value = sp.getString("play_device", "");
        Utils.d("Utils readAirPlayDevice value:" + value);
        return value;
    }

    public static void d(String str) {
        System.out.println("com.sport.video.airplay  " + str);
    }
}

