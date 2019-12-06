package com.sport.video;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-11-29.
 **/
public class AppApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
