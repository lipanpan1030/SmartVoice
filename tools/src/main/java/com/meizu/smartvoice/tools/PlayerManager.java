package com.meizu.smartvoice.tools;

/**
 * Created by lipan on 17-7-21.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
/**
 * 音乐播放管理类
 * Created by Administrator on 2015/8/27 0027.
 */
public class PlayerManager {
    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;
    /**
     * 耳机模式
     */
    public static final int MODE_HEADSET = 1;
    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;
    private static PlayerManager playerManager;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private PlayCallback callback;
    private Context context;
    private boolean isPause = false;
    private String filePath;
    private int currentMode = MODE_SPEAKER;
    public static PlayerManager getInstance(Context context){
        if (playerManager == null){
            synchronized (PlayerManager.class){
                playerManager = new PlayerManager(context);
            }
        }
        return playerManager;
    }
    private PlayerManager(Context context){
        this.context = context;
        initMediaPlayer();
        initAudioManager();
    }
    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
    }
    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(false);			//默认为扬声器播放
    }
    /**
     * 播放回调接口
     */
    public interface PlayCallback{
        /**
         * 音乐准备完毕
         */
        void onPlayerPrepared();
        /**
         * 音乐播放完成
         */
        void onPlayerComplete();
        /**
         * 音乐停止播放
         */
        void onPlayerStop();
    }
    /**
     * 播放音乐
     * @param path 音乐文件路径
     * @param callback 播放回调函数
     */
    public void play(String path, @Nullable final PlayCallback callback){
        this.filePath = path;
        this.callback = callback;
        try {
            mediaPlayer.reset();
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = context.getAssets().openFd("weixin_message.mp3");
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(),
                    descriptor.getStartOffset(), descriptor.getLength());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (callback != null) {
                        callback.onPlayerPrepared();
                    }
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    //mediaPlayer.release();
                    if (callback != null) {
                        callback.onPlayerComplete();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isPause(){
        return isPause;
    }
    public void pause(){
        if (isPlaying()){
            isPause = true;
            mediaPlayer.pause();
        }
    }
    public void resume(){
        if (isPause){
            isPause = false;
            mediaPlayer.start();
        }
    }
    /**
     * 获取当前播放模式
     * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }
    /**
     * 切换到听筒模式
     */
    /**
     * 切换到听筒
     */
    public void changeToReceiver(){
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }
    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode(){
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }
    /**
     * 切换到外放模式
     */
    public void changeToSpeaker() {
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }
    public void resetPlayMode(){
        if (audioManager.isWiredHeadsetOn()){
            changeToHeadsetMode();
        } else {
            changeToSpeaker();
        }
    }

    /**
     * 耳机是否插入
     * @return 插入耳机返回true,否则返回false
     */
    @SuppressWarnings("deprecation")
    public boolean isWiredHeadsetOn(){
        return audioManager.isWiredHeadsetOn();
    }

    /**
     * 调大音量
     */
    public void raiseVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }
    /**
     * 调小音量
     */
    public void lowerVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }
    /**
     * 停止播放
     */
    public void stop(){
        if (isPlaying()){
            try {
                mediaPlayer.stop();
                if (callback != null) {
                    callback.onPlayerStop();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 是否正在播放
     * @return 正在播放返回true,否则返回false
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

}
