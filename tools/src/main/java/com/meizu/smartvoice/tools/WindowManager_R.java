package com.meizu.smartvoice.tools;


import android.app.Activity;
import android.util.Log;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class WindowManager_R {
    private static final String TAG = "WindowManager_R";
    private static final String ERROR_TAG = "RE:" + TAG;

    public static final int MEIZU_FLAG_DARK_STATUS_BAR_ICON = get_MEIZU_FLAG_DARK_STATUS_BAR_ICON();
    private static Field sField_mMEIZU_FLAG_DARK_STATUS_BAR_ICON;
    private static Field sField_mMeizuFlags;


    private static int get_MEIZU_FLAG_DARK_STATUS_BAR_ICON() {
        if(null == sField_mMEIZU_FLAG_DARK_STATUS_BAR_ICON){
            try {
                sField_mMEIZU_FLAG_DARK_STATUS_BAR_ICON =
                        WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            } catch (Exception e) {
                Log.w(ERROR_TAG, "", e);
            }
        }

        try {
            return (int) sField_mMEIZU_FLAG_DARK_STATUS_BAR_ICON.get(null);
        } catch (Exception e) {
            Log.w(ERROR_TAG, "", e);
        }
        return 1 << 9;
    }

    public static int getMeizuFlags(WindowManager.LayoutParams params) {
        if(null == sField_mMeizuFlags){
            try {
                sField_mMeizuFlags =
                        WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            } catch (Exception e) {
                Log.w(ERROR_TAG, "", e);
            }
        }

        try {
            return (int) sField_mMeizuFlags.get(params);
        } catch (Exception e) {
            Log.w(ERROR_TAG, "", e);
        }
        return 0;
    }

    public static void setMeizuFlags(WindowManager.LayoutParams params, int value) {
        if(null == sField_mMeizuFlags){
            try {
                sField_mMeizuFlags =
                        WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            } catch (Exception e) {
                Log.w(ERROR_TAG, "", e);
            }
        }

        try {
            sField_mMeizuFlags.setInt(params, value);
        } catch (Exception e) {
            Log.w(ERROR_TAG, "", e);
        }
    }

    public static void doTest(Activity activity) {
        get_MEIZU_FLAG_DARK_STATUS_BAR_ICON();

        getMeizuFlags(activity.getWindow().getAttributes());
    }

}