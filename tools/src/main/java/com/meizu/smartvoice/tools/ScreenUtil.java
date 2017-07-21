package com.meizu.smartvoice.tools;

import android.content.Context;
import android.os.PowerManager;

import static android.content.ContentValues.TAG;

/**
 * Created by lipan on 17-7-21.
 */

public class ScreenUtil {

    PowerManager mPowerManager;
    PowerManager.WakeLock mWakeLock;
    private static ScreenUtil screenUtil;

    public static ScreenUtil getInstance(Context context) {
        if (screenUtil == null) {
            screenUtil = new ScreenUtil(context);
        }
        return screenUtil;
    }

    private ScreenUtil(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }


    public void setScreenOff() {
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
        }
        mWakeLock.acquire();
    }

    public void setScreenOn() {
        if (mWakeLock != null) {
            mWakeLock.setReferenceCounted(false);
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
