package com.applaudostudio.weekfourchallengeone.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;


import com.applaudostudio.weekfourchallengeone.R;

public class MusicService extends Service {
    private static final String LOG_TAG = "ForegroundService";


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
        Log.i(LOG_TAG, "ON START");
        Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mute_yellow);

        RemoteViews remoteView = new RemoteViews(this.getPackageName(), R.layout.notification_player);

        Intent intentPlay = new Intent(this,MusicService.class);

        PendingIntent intentPendit = PendingIntent.getService(this, 0, intentPlay, 0);
        remoteView.setOnClickPendingIntent(R.id.imageButton, intentPendit);
        remoteView.setTextViewText(R.id.textViewUrlNotification, "RADIO APPLAUDO");


        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("RADIO PLAYER")
                .setTicker("Truiton Music Player")
                .setContentText("My Music")
                .setSmallIcon(R.drawable.ic_stop_red)
                .setLargeIcon(Bitmap.createScaledBitmap(map, 128, 128, false))
                .setOngoing(true)
                .build();
        noti.contentView = remoteView;

        startForeground(1010,noti);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
