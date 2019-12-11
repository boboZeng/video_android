package com.video.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.util.Formatter;
import java.util.Locale;


public class VideoUtils {
    public static final String TAG = "JZVD";
    public static int SYSTEM_UI = 0;
    public static boolean enableDebug = false;

    /**
     * 是否可以调试
     *
     * @param enableDebug
     */
    public static void setEnableDebug(boolean enableDebug) {
        VideoUtils.enableDebug = enableDebug;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().equals("");
    }

    public static void d(String str) {
        if (enableDebug) {
            Log.d("VideoUtils", str);
        }
    }

    public static String stringForTime(long timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = timeMs / 1000;
        int seconds = (int) (totalSeconds % 60);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int hours = (int) (totalSeconds / 3600);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    /**
     * Get AppCompatActivity from context
     *
     * @param context context
     * @return AppCompatActivity if it's not null
     */
    public static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    public static void setRequestedOrientation(Context context, int orientation) {
        if (VideoUtils.getAppCompActivity(context) != null) {
            VideoUtils.getAppCompActivity(context).setRequestedOrientation(
                    orientation);
        } else {
            VideoUtils.scanForActivity(context).setRequestedOrientation(
                    orientation);
        }
    }

    public static Window getWindow(Context context) {
        if (VideoUtils.getAppCompActivity(context) != null) {
            return VideoUtils.getAppCompActivity(context).getWindow();
        } else {
            return VideoUtils.scanForActivity(context).getWindow();
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * if url == null, clear all progress
     *
     * @param context context
     * @param url     if url!=null clear this url progress
     */
    public static void clearSavedProgress(Context context, Object url) {
        if (url == null) {
            SharedPreferences spn = context.getSharedPreferences("JZVD_PROGRESS",
                    Context.MODE_PRIVATE);
            spn.edit().clear().apply();
        } else {
            SharedPreferences spn = context.getSharedPreferences("JZVD_PROGRESS",
                    Context.MODE_PRIVATE);
            spn.edit().putLong("newVersion:" + url.toString(), 0).apply();
        }
    }

    @SuppressLint("RestrictedApi")
    public static void showStatusBar(Context context) {
        VideoUtils.getWindow(context).clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    //如果是沉浸式的，全屏前就没有状态栏
    @SuppressLint("RestrictedApi")
    public static void hideStatusBar(Context context) {
        VideoUtils.getWindow(context).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @SuppressLint("NewApi")
    public static void hideSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        SYSTEM_UI = VideoUtils.getWindow(context).getDecorView().getSystemUiVisibility();
        VideoUtils.getWindow(context).getDecorView().setSystemUiVisibility(uiOptions);

    }

    @SuppressLint("NewApi")
    public static void showSystemUI(Context context) {
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        VideoUtils.getWindow(context).getDecorView().setSystemUiVisibility(SYSTEM_UI);
    }

    /**
     * 屏幕宽
     *
     * @param context
     * @return
     */
    public static int getscreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        //API 17之后使用，获取的像素宽高包含虚拟键所占空间，在API 17之前通过反射获取
        //        context.getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        //获取的像素宽高不包含虚拟键所占空间
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;  // 宽度（像素）
    }

    /**
     * 屏幕高
     *
     * @param context
     * @return
     */
    public static int getscreenhHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        //API 17之后使用，获取的像素宽高包含虚拟键所占空间，在API 17之前通过反射获取
        //        context.getWindowManager().getDefaultDisplay().getRealMetrics(metric);
        //获取的像素宽高不包含虚拟键所占空间
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;  // 宽度（像素）
    }

    public static int getNavigetionHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Keep Screen on
     */
    public static void keepScreenOn(Activity activity) {
        if (activity != null)
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Cancel keep Screen on
     *
     * @param activity
     */
    public static void cancelScreenOn(Activity activity) {
        if (activity != null)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isConnected();
            }
        }
        return false;
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

    /**
     * 判断MOBILE网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isConnected();
            }
        }
        return false;
    }

    /**
     * 获取当前网络连接的类型信息
     *
     * @param context
     * @return
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isConnected()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    /**
     * 获取手机网络类型；
     * @param context
     * @return
     */
    public static int getMoblieNetWorkType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null && mMobileNetworkInfo.isConnected()) {
                return getNetworkClass(mMobileNetworkInfo.getSubtype());
            }
        }
        return VideoConstants.NETWORK_CLASS.NETWORK_CLASS_UNKNOWN;
    }

    /**
     *
     * 获取手机网络类型；2G/3G/4G
     * @param networkType
     * @return
     */
    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_GPRS:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_GSM:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_EDGE:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_CDMA:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_1xRTT:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_IDEN:
                return VideoConstants.NETWORK_CLASS.NETWORK_CLASS_2_G;
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_UMTS:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_EVDO_0:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_EVDO_A:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_HSDPA:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_HSUPA:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_HSPA:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_EVDO_B:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_EHRPD:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_HSPAP:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_TD_SCDMA:
                return VideoConstants.NETWORK_CLASS.NETWORK_CLASS_3_G;
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_LTE:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_IWLAN:
            case VideoConstants.NETWORK_TYPE.NETWORK_TYPE_LTE_CA:
                return VideoConstants.NETWORK_CLASS.NETWORK_CLASS_4_G;
            default:
                return VideoConstants.NETWORK_CLASS.NETWORK_CLASS_UNKNOWN;
        }
    }
}
