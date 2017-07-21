package com.meizu.smartvoice.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.meizu.smartvoice.R;
import com.meizu.smartvoice.tools.PlayerManager;
import com.meizu.smartvoice.tools.ScreenUtil;

public class SmartVoiceService extends Service implements SensorEventListener, PlayerManager.PlayCallback{

    public static final String TAG = "SmartVoice";

    private final static String PATH = "/sdcard/smartvoice/weixin_message.mp3";

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private ScreenUtil mScreenUtil;
    private PlayerManager mPlayerManager;

    private boolean mActive;

    public void onCreate() {

        mPlayerManager = PlayerManager.getInstance(this);
        mScreenUtil = ScreenUtil.getInstance(this);
        initSensor();

    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float distance = event.values[0];
        Log.d(TAG, "distance:" + distance);
        mActive = (distance >= 0.0 && distance < Math.min(mProximitySensor.getMaximumRange(), 5.0));
        if (mPlayerManager.isWiredHeadsetOn()){
            return;
        }

        // 没有播放的时候 靠近屏幕播放
        if (mPlayerManager.isPlaying()){
            // 播放的时候,离开屏幕, 外放
            if (!mActive) {
                mScreenUtil.setScreenOn();
                mPlayerManager.changeToSpeaker();
            } else {
                mPlayerManager.changeToReceiver();
                mScreenUtil.setScreenOff();
            }
        } else {
            if (mActive) {
                mPlayerManager.changeToReceiver();
                mScreenUtil.setScreenOff();
                mPlayerManager.play(PATH, this);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPlayerPrepared() {
        Log.d(TAG, "音乐准备完毕,开始播放");
    }
    @Override
    public void onPlayerComplete() {
        Log.d(TAG, "音乐播放完毕");
        if (mActive) {
            showNotification("路人甲", "正在回复", 1);
        }

    }
    @Override
    public void onPlayerStop() {
        Log.d(TAG, "音乐停止播放");
    }


    public void showNotification(String title, String text, int notificationId) {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable(R.drawable.notification_big);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.notification)
                .setLargeIcon(drawable.getBitmap())
                .setOngoing(false)
                .setAutoCancel(false);

        Notification notification = builder.build();
        notification.flags |= 0x04000000;
        notification.extras.putInt("flyme.showHeadUpInOccluded", 1);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, notification);
    }
}
