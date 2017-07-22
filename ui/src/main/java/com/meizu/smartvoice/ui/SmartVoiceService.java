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

    private static final String PLAY_END = "playEnd";

    RemoteViews mRemoteViews;

    public final static String ACTION_NEW_MESSAGE = "com.meizu.voice.action.NEW_MESSAGE";

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private ScreenUtil mScreenUtil;
    private PlayerManager mPlayerManager;
    NotificationManager mNotification;

    private boolean mActive;
    public static boolean sRecord = false;


    public void onCreate() {

        mPlayerManager = PlayerManager.getInstance(this);
        mScreenUtil = ScreenUtil.getInstance(this);

        mNotification =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        showNotification(1, PLAY_END);
    }
    @Override
    public void onPlayerStop() {
        Log.d(TAG, "音乐停止播放");
    }


    public void showNotification(int notificationId, String action) {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable(R.drawable.notification_big);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentText("智能语音助手")
                .setContentTitle("智能语音助手")
                .setLargeIcon(drawable.getBitmap())
                .setOngoing(false)
                .setAutoCancel(false);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.noti_remote);

        switch (action) {
            case BUTTON_PLAY:
                mRemoteViews.setViewVisibility(R.id.play, View.GONE);
                mRemoteViews.setViewVisibility(R.id.reply, View.GONE);
                mRemoteViews.setViewVisibility(R.id.cancel, View.GONE);
                mRemoteViews.setTextViewText(R.id.remote_txt, "正在播放");
                // 没有播放的时候 靠近屏幕播放
                if (!mPlayerManager.isPlaying()){
                    mPlayerManager.changeToSpeaker();
                    mPlayerManager.play(PATH, this);
                }
                break;
            case BUTTON_CANCEL:
                mNotification.cancel(1);
                return;
            case BUTTON_REPLY:
                mNotification.cancel(1);
                sRecord = true;
                return;
                // 发一条信息出去
            case PLAY_END:
                mRemoteViews.setViewVisibility(R.id.play, View.GONE);
                mRemoteViews.setViewVisibility(R.id.reply, View.VISIBLE);
                mRemoteViews.setViewVisibility(R.id.cancel, View.VISIBLE);
                mRemoteViews.setTextViewText(R.id.remote_txt, "正在录音");
                break;
            default:
                mRemoteViews.setViewVisibility(R.id.play, View.VISIBLE);
                mRemoteViews.setViewVisibility(R.id.reply, View.GONE);
                mRemoteViews.setViewVisibility(R.id.cancel, View.GONE);
                break;
        }

        //实例化一个指向MusicService的intent
        Intent intent = new Intent(ACTION_NEW_MESSAGE);

        //设置play按钮的点击事件
        intent.putExtra(BUTTON_INDEX, BUTTON_PLAY);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);

        //设置next按钮的点击事件
        intent.putExtra(BUTTON_INDEX, BUTTON_CANCEL);
        pendingIntent = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.cancel, pendingIntent);

        //设置prev按钮的点击事件
        intent.putExtra(BUTTON_INDEX, BUTTON_REPLY);
        pendingIntent = PendingIntent.getService(this, 3, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.reply, pendingIntent);

        builder.setContent(mRemoteViews);
        Notification notification = builder.build();
        notification.flags |= 0x04000000;
        notification.extras.putInt("headsup", 0);
        notification.extras.putInt("flyme.showHeadUpInOccluded", 1);

        mNotification.notify(notificationId, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String extra = intent.getStringExtra(BUTTON_INDEX);
        showNotification(1, extra);
        return START_STICKY;
    }
}
