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
import java.util.Objects;

/***
 * Service to manage the Media Player and start a notification.
 */
public class MusicService extends Service {
    //Actions for the service
    public static String START_FOREGROUND_ACTION = "START_SERVICE";
    public static String STOP_FOREGROUND_ACTION = "STOP_SERVICE";
    public static String PLAY_FOREGROUND_ACTION = "PLAY_SERVICE";
    public static String MUTE_FOREGROUND_ACTION = "MUTE_SERVICE";
    public static String PAUSE_FOREGROUND_ACTION = "PAUSE_SERVICE";
    //Chanel name for the notification.
    private static final String CHANEL_NAME = "RADIO_NOTIFICATION_CHANEL";
    //Process ID
    private static final int FOREGROUND_ID = 1001;
    //Constants for the pending indent generator
    private static final int PENDING_TYPE_MAIN = 1;
    private static final int PENDING_TYPE_PAUSE = 2;
    private static final int PENDING_TYPE_PLAY = 3;
    private static final int PENDING_TYPE_MUTE = 4;
    private static final int PENDING_TYPE_CLOSE = 5;

    //Enum for the MediaPlayer status
    public enum PlayerStates {
        PAUSED, STOPPED
    }

    PlayerStates statePlayer;
    AudioManager am;
    public static MediaPlayer mediaPlayer;
    public MediaPlayerStateListener mListenerMediaPlayerChanges;
    private IBinder mBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /***
     * Function of the servce, we can use this to manage the mediaplayer, using the constants for xxxx_FREGROUND_ACTION
     * @param intent intent with the actions for the services
     * @param flags flags for services
     * @param startId int as process id
     * @return Return the START_STICKY in case the service fails to restart
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (intent != null) {
            if (START_FOREGROUND_ACTION.equals(Objects.requireNonNull(intent.getAction()))) {
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
                            mListenerMediaPlayerChanges.stateChangePlay();
                        }
                    });
                    mediaPlayer.prepareAsync();
                    //new AsyncPrepareRadio().execute(item.getUrl());
                    initChanel(getApplicationContext(), CHANEL_NAME);
                    startForeground(FOREGROUND_ID, GenerateNotification(item, CHANEL_NAME));
                } else {
                    mediaPlayer.pause();
                    mListenerMediaPlayerChanges.stateChangePause();
                    statePlayer = PlayerStates.PAUSED;
                }
            } else if (intent.getAction().equals(PLAY_FOREGROUND_ACTION)) {
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                mListenerMediaPlayerChanges.stateChangePlay();
            } else if (intent.getAction().equals(PAUSE_FOREGROUND_ACTION)) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                mListenerMediaPlayerChanges.stateChangePause();

                statePlayer = PlayerStates.PAUSED;
            } else if (intent.getAction().equals(STOP_FOREGROUND_ACTION)) {
                if (isMediaPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    statePlayer = PlayerStates.STOPPED;
                    stopForeground(true);
                    stopSelf();
                }
            } else if (intent.getAction().equals(MUTE_FOREGROUND_ACTION)) {
                if (isMediaPlaying()) {
                    mListenerMediaPlayerChanges.stateChangeMute();
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

    /***
     * Function in case of ON BIND.
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /***
     * This function is to generate a notification with 3 actions
     * @param item item with the data for the notification
     * @param chanelId chanel ID in case of the SDK>26
     * @return returns a notification object
     */
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
                        getString(R.string.notification_button_play), pendingGenerator(PENDING_TYPE_PLAY))
                .addAction(android.R.drawable.ic_media_pause, getString(R.string.notification_pause),
                        pendingGenerator(PENDING_TYPE_PAUSE))
                .addAction(R.drawable.ic_mute_gray, getString(R.string.notification_mute),
                        pendingGenerator(PENDING_TYPE_MUTE))
                .build();
    }

    /***
     * Function to init the chanel for the notification
     * @param context Application context
     * @param ch chanel name
     */
    public void initChanel(Context context, String ch) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notiChanel = new NotificationChannel(ch, ch, NotificationManager.IMPORTANCE_DEFAULT);
        notiChanel.setDescription(getString(R.string.notificacion_description));
        if (notiManager != null) {
            notiManager.createNotificationChannel(notiChanel);
        }
    }

    /***
     * Function to generate the pendingIntents for the notification.
     * @param typePending Cosntant with the name PENDING_TYPE_xxxx
     * @return this returns a pending intent object
     */
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

    /***
     * function to check if the media player is playing music
     * @return
     */
    public boolean isMediaPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /***
     * check if the system STREAM_MUSIC is mute
     * @return
     */
    public boolean isMediaMute() {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return am != null && am.isStreamMute(AudioManager.STREAM_MUSIC);
    }

    /***
     * Binder class to bind the service with the activity
     */
    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }


    /***
     * Function for the media player listener to make a contract on the activity to get the
     * status of the MediaPlayer in the service
     * @param listenerStates Mediaplayer listener to set the local listener
     */
    public void setMediaPlayerStateChangeLister(MediaPlayerStateListener listenerStates) {
        mListenerMediaPlayerChanges = listenerStates;
    }

    /***
     * Interface for the contract to check the Service Media Player in the MainActivity
     */
    public interface MediaPlayerStateListener {
        void stateChangePlay();

        void stateChangePause();

        void stateChangeMute();
    }


}
