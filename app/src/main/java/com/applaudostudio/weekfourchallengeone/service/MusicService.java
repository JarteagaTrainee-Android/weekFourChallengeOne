package com.applaudostudio.weekfourchallengeone.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.applaudostudio.weekfourchallengeone.MainActivity;
import com.applaudostudio.weekfourchallengeone.R;
import com.applaudostudio.weekfourchallengeone.model.RadioItem;

import java.io.IOException;

public class MusicService extends Service {
    public static String START_FOREGROUND_ACTION = "START_SERVICE";
    public static String STOP_FOREGROUND_ACTION = "STOP_SERVICE";
    public static String PLAY_FOREGROUND_ACTION = "PLAY_SERVICE";
    public static String MUTE_FOREGROUND_ACTION = "MUTE_SERVICE";
    public static String PAUSE_FOREGROUND_ACTION = "PAUSE_SERVICE";

    private static final String CHANEL_NAME = "RADIO_NOTIFICATION_CHANEL";
    private static final int FOREGROND_ID=1001;
    private static final int PENDING_TYPE_MAIN = 1;
    private static final int PENDING_TYPE_PAUSE = 2;
    private static final int PENDING_TYPE_PLAY = 3;
    private static final int PENDING_TYPE_MUTE = 4;
    private static final int PENDING_TYPE_CLOSE = 5;

    public enum PlayerStates{PAUSED,STOPPED};
    PlayerStates statePlayer;
    AudioManager am;

    private static MediaPlayer mediaPlayer;


    private IBinder mBinder = new MyBinder();


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (intent != null) {
            if (intent.getAction().equals(START_FOREGROUND_ACTION)) {
                if (!isMediaPlaying()) {
                    RadioItem item = intent.getParcelableExtra(MainActivity.ARG_ITEM_PLAY_ON_SERVICE);
                    // might take long! (for buffering, etc)
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setDataSource(item.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.prepareAsync();
                    //new AsyncPrepareRadio().execute(item.getUrl());
                    initChanel(getApplicationContext(),CHANEL_NAME);
                    startForeground(FOREGROND_ID, GenerateNotification(item, CHANEL_NAME));
                } else {
                    mediaPlayer.pause();
                    statePlayer=PlayerStates.PAUSED;
                }
            } else if (intent.getAction().equals(PLAY_FOREGROUND_ACTION)) {
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
            } else if (intent.getAction().equals(PAUSE_FOREGROUND_ACTION)) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                    statePlayer=PlayerStates.PAUSED;
            } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
                if (isMediaPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    statePlayer=PlayerStates.STOPPED;
                    stopForeground(true);
                    stopSelf();
                }
            } else if (intent.getAction().equals(MUTE_FOREGROUND_ACTION)) {
                if (isMediaPlaying()) {
                    am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    // Change the stream to your stream of choice.
                    if (am != null) {
                        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0);
                    }
                }
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification GenerateNotification(RadioItem item, String chanelId) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.music_sign);

        return new NotificationCompat.Builder(this, chanelId)
                .setContentTitle(item.getSubTitle())
                .setTicker(item.getSubTitle())
                .setContentText(item.getUrl())
                .setSmallIcon(R.drawable.music_sign)
                .setDeleteIntent(pendingGenerator(PENDING_TYPE_CLOSE))
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingGenerator(PENDING_TYPE_MAIN))
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_play,
                        "Play", pendingGenerator(PENDING_TYPE_PLAY))
                .addAction(android.R.drawable.ic_media_pause, "Pause",
                        pendingGenerator(PENDING_TYPE_PAUSE))
                .addAction(R.drawable.ic_mute_gray, "Mute",
                        pendingGenerator(PENDING_TYPE_MUTE))
                .build();
    }

    private PendingIntent pendingGenerator(int typePending) {
        PendingIntent pendingIntent;
        Intent notificationIntent;
        switch (typePending) {
            case PENDING_TYPE_MAIN:
                notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
            case PENDING_TYPE_PAUSE:
                notificationIntent = new Intent(this, MusicService.class);
                notificationIntent.setAction(PAUSE_FOREGROUND_ACTION);
                pendingIntent = PendingIntent.getService(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
            case PENDING_TYPE_PLAY:
                notificationIntent = new Intent(this, MusicService.class);
                notificationIntent.setAction(PLAY_FOREGROUND_ACTION);
                pendingIntent = PendingIntent.getService(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
            case PENDING_TYPE_MUTE:
                notificationIntent = new Intent(this, MusicService.class);
                notificationIntent.setAction(MUTE_FOREGROUND_ACTION);
                pendingIntent = PendingIntent.getService(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
            case PENDING_TYPE_CLOSE:
                notificationIntent = new Intent(this, MusicService.class);
                notificationIntent.setAction(STOP_FOREGROUND_ACTION);
                pendingIntent = PendingIntent.getService(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
            default:
                return null;
        }
    }

    public boolean isMediaPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void initChanel(Context context,String ch){
            if(Build.VERSION.SDK_INT<26){
                return;
            }
        NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notiChanel = new NotificationChannel(ch,ch,NotificationManager.IMPORTANCE_DEFAULT);
        notiChanel.setDescription("Notifications for Radio Applaudo");
        if(notiManager!=null){
            notiManager.createNotificationChannel(notiChanel);
        }

    }


    public interface MediaPlayerStateListener{

    }



}
