package com.applaudostudio.weekfourchallengeone.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.applaudostudio.weekfourchallengeone.MainActivity;
import com.applaudostudio.weekfourchallengeone.R;
import com.applaudostudio.weekfourchallengeone.model.RadioItem;

import java.io.IOException;

public class MusicService extends Service {
    public static String START_FOREGROUND_ACTION = "START_SERVICE";
    public static String STOP_FOREGROUND_ACTION = "STOP_SERVICE";
    private static final int PENDING_TYPE_MAIN = 1;
    private static final int PENDING_TYPE_PAUSE=2;
    private static final int PENDING_TYPE_PLAY=3;
    private static final int PENDING_TYPE_MUTE=4;
    private static final String LOG_TAG = "ForegroundService";
    private static MediaPlayer mediaPlayer;

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
        if (intent.getAction().equals(START_FOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "ON START");
            RadioItem item = intent.getParcelableExtra(MainActivity.ARG_ITEM_PLAY_ON_SERVICE);
            // might take long! (for buffering, etc)
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
            startForeground(1010, GenerateNotification(item));
        } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "ON STOP");
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Notification GenerateNotification(RadioItem item) {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.music_sign);

        Intent intent = new Intent();
        Notification notification = new NotificationCompat.Builder(this, "")
                .setContentTitle(item.getSubTitle())
                .setTicker(item.getSubTitle())
                .setContentText(item.getUrl())
                .setSmallIcon(R.drawable.music_sign)
                .setLargeIcon(
                        Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingGenerator(PENDING_TYPE_MAIN))
                .setOngoing(true)
                .addAction(android.R.drawable.ic_media_previous,
                        "Play", pendingGenerator(PENDING_TYPE_MAIN))
                .addAction(android.R.drawable.ic_media_play, "Pause",
                        pendingGenerator(PENDING_TYPE_MAIN))
                .build();

        return notification;
    }

    private PendingIntent pendingGenerator(int typePending) {
        PendingIntent pendingIntent = null;
        switch (typePending) {
            case PENDING_TYPE_MAIN:
                Intent notificationIntent = new Intent(this, MainActivity.class);
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);
                return pendingIntent;
        }
        return pendingIntent;
    }

    public boolean isPlayingMusic() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
