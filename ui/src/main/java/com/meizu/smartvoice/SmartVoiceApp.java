package com.meizu.smartvoice;

import android.app.Application;
import android.content.Context;

/**
 * Created by lipan on 17-7-21.
 */

public class SmartVoiceApp extends Application {

    private static Context mAppContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
    }


    public static Context getContext() {
        return mAppContext;
    }
}
