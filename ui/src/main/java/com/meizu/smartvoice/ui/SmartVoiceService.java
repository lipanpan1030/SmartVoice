package com.meizu.smartvoice.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.meizu.smartvoice.R;
import com.meizu.smartvoice.tools.PlayerManager;
import com.meizu.smartvoice.tools.ScreenUtil;

public class SmartVoiceService extends Service implements SensorEventListener, PlayerManager.PlayCallback{

    public static final String TAG = "SmartVoice";

    private final static String PATH = "/sdcard/smartvoice/weixin_message.mp3";
    private static final String BUTTON_INDEX = "btn";
    private static final String BUTTON_PLAY = "play";
    private static final String BUTTON_CANCEL = "cancel";
    private static final String BUTTON_REPLY = "reply";
    RemoteViews mRemoteViews;

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private ScreenUtil mScreenUtil;
    private PlayerManager mPlayerManager;

    private boolean mActive;

    private static SmartVoiceService sInstance = null;

    public void onCreate() {

        mPlayerManager = PlayerManager.getInstance(this);
        mScreenUtil = ScreenUtil.getInstance(this);
        initSensor();

        sInstance = this;
    }

    public static SmartVoiceService getInstance() {
        return sInstance;
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
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.noti_remote);
        mRemoteViews.setViewVisibility(R.id.reply, View.INVISIBLE);
        mRemoteViews.setViewVisibility(R.id.cancel, View.INVISIBLE);

        //实例化一个指向MusicService的intent
        Intent intent = new Intent(this, SmartVoiceService.class);
        intent.setAction("com.meizu.voice");

        //设置play按钮的点击事件
        intent.putExtra(BUTTON_INDEX, BUTTON_PLAY);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);

        //设置next按钮的点击事件
//        intent.putExtra(BUTTON_INDEX, BUTTON_CANCEL);
//        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.cancel, pendingIntent);
//
//        //设置prev按钮的点击事件
//        intent.putExtra(BUTTON_INDEX, BUTTON_REPLY);
//        pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setOnClickPendingIntent(R.id.reply, pendingIntent);


        builder.setContent(mRemoteViews);
        Notification notification = builder.build();
        notification.flags |= 0x04000000;
        notification.extras.putInt("flyme.showHeadUpInOccluded", 1);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
