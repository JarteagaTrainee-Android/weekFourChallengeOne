package com.applaudostudio.weekfourchallengeone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.applaudostudio.weekfourchallengeone.adapter.RadioListAdapter;
import com.applaudostudio.weekfourchallengeone.model.RadioItem;
import com.applaudostudio.weekfourchallengeone.receiver.InternetReceiver;
import com.applaudostudio.weekfourchallengeone.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RadioListAdapter.ItemSelectedListener, View.OnClickListener, InternetReceiver.InternetConnectionListener, MusicService.MediaPlayerStateListener {
        public static boolean PLAYING_MUSIC;
        public static boolean MUTE_MUSIC;
        //for broadcast network status
        private RadioItem RADIO_PLAYING_ITEM;
        private static final int INTENT_TYPE_RADIO_DETAIL_ACTIVITY = 0;
        private static final int INTENT_TYPE_RADIO_ACTION_PLAY = 1;
        private static final int INTENT_TYPE_RADIO_ACTION_PAUSE = 2;
        private static final int INTENT_TYPE_RADIO_ACTION_STOP = 3;
        private static final int INTENT_TYPE_RADIO_ACTION_MUTE = 4;

        public static final String KEY_RADIO_DETAIL = "RADIO_DETAIL_DATA";
        public static final String ARG_ITEM_PLAY_ON_SERVICE = "RADIO_TO_PLAY";
        public static final String SAVE_ITEM_RADIO = "RADIO_SAVED";

        private static final int TOGGLE_TYPE_PLAY = 1;
        private static final int TOGGLE_TYPE_PAUSE = 2;
        private static final int TOGGLE_TYPE_STOP = 3;
        private static final int TOGGLE_TYPE_MUTE = 4;

        //View Elements
        private List<RadioItem> mDataSet;
        private RecyclerView mRecyclerView;
        private ImageView mButtonPlay;
        private ImageView mButtonStop;
        private ImageView mButtonInfo;
        private ImageView mButtonMute;
        private TextView mTxtPlaying;
        //Service Elements
        Intent intentService;
        private InternetReceiver mInternetReceiver;
        MusicService mBoundMusicService;
        boolean mServiceBound = false;

    /***
     * start the data and initialice services an Recycler View
     * @param savedInstanceState
     */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            RADIO_PLAYING_ITEM = null;
            intentService = null;
            mRecyclerView = findViewById(R.id.recyclerViewRadios);
            initData();
            //Recycler View Section
            mRecyclerView.setHasFixedSize(true);
            // use a linear layout manager
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            // specify an adapter (see also next example)
            RadioListAdapter mAdapter = new RadioListAdapter(mDataSet, this);
            mRecyclerView.setAdapter(mAdapter);
            mInternetReceiver = new InternetReceiver(this);
            //UI SET CLICK LISTENERS
            mButtonPlay.setOnClickListener(this);
            mButtonStop.setOnClickListener(this);
            mButtonMute.setOnClickListener(this);
            mButtonInfo.setOnClickListener(this);
            mTxtPlaying.setText("");

        }

    /***
     * Check the reciver information about the internet connection.
     */
    @Override
    protected void onResume() {
            super.onResume();
            //RECEIVERS
            IntentFilter intentFilter = new IntentFilter();
            // Add network connectivity change action.
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.registerReceiver(mInternetReceiver, intentFilter);

        }

        /***
         * For the service Bind
         */
        @Override
        protected void onStart() {
            super.onStart();
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction("");
            //startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

        /***
         * unregister the receiver
         */
        @Override
        protected void onDestroy() {
            this.unregisterReceiver(mInternetReceiver);
            super.onDestroy();

        }

        /***
         * On click for the interface OnClickListener
         * @param v the whoel view as param
         */
        @Override
        public void onClick(View v) {
            if (mBoundMusicService != null) {
                switch (v.getId()) {
                    case R.id.imageViewPlay:
                        if (RADIO_PLAYING_ITEM != null && !mBoundMusicService.isMediaPlaying()) {
                            startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_PLAY));
                            toggleButtonsClick(TOGGLE_TYPE_PLAY);
                        } else if (mBoundMusicService.isMediaPlaying()) {
                            startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_PAUSE));
                            toggleButtonsClick(TOGGLE_TYPE_PAUSE);
                        }
                        break;
                    case R.id.imageViewStop:
                        if (mBoundMusicService.isMediaPlaying()) {
                            startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_STOP));
                            toggleButtonsClick(TOGGLE_TYPE_STOP);
                        }
                        break;
                    case R.id.imageViewMute:
                        startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_MUTE));
                        toggleButtonsClick(TOGGLE_TYPE_MUTE);
                        break;
                    case R.id.imageViewInfo:
                        if (RADIO_PLAYING_ITEM != null) {
                            startActivity(communicationGenerator(INTENT_TYPE_RADIO_DETAIL_ACTIVITY));
                        }
                        break;
                }
            }
        }

        /***
         * Function to manage the play and pause on the same button
         * @param item selecte item of the recyclerView
         */
        @Override
        public void onClickPlayButton(RadioItem item) {
            RADIO_PLAYING_ITEM = item;
            bindData();
            if (!mBoundMusicService.isMediaPlaying()) {
                startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_PLAY));
            } else {
                startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_STOP));
                startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_PLAY));
            }
            toggleButtonsClick(TOGGLE_TYPE_PLAY);

        }

        /***
         * save the selected item data
         * @param outState bundle to be saved
         */
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            outState.putParcelable(SAVE_ITEM_RADIO, RADIO_PLAYING_ITEM);
            super.onSaveInstanceState(outState);
        }

        /***
         * Restore the selected item playing
         * @param savedInstanceState bundle with the data
         */
        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            RADIO_PLAYING_ITEM = savedInstanceState.getParcelable(SAVE_ITEM_RADIO);
            if (RADIO_PLAYING_ITEM != null) {
                mTxtPlaying.setText(RADIO_PLAYING_ITEM.getSubTitle());
            }
        }

        /***
         * Function to init the radio list and get the set the view elements to the objects
         */
        private void initData() {
            PLAYING_MUSIC = false;
            MUTE_MUSIC = false;
            mButtonPlay = findViewById(R.id.imageViewPlay);
            mButtonInfo = findViewById(R.id.imageViewInfo);
            mButtonStop = findViewById(R.id.imageViewStop);
            mButtonMute = findViewById(R.id.imageViewMute);
            mTxtPlaying = findViewById(R.id.textViewPlaying);
            mDataSet = new ArrayList<>();
            RadioItem rdItem = new RadioItem();
            rdItem.setUrl("http://us5.internet-radio.com:8110/listen.pls&t=.m3u");
            rdItem.setSubTitle("Radio Applaudo");
            rdItem.setDescription(getString(R.string.radio_description));
            mDataSet.add(rdItem);
            rdItem = new RadioItem();
            rdItem.setUrl("http://uk7.internet-radio.com:8119/listen.pls&t=.m3u");
            rdItem.setSubTitle("Radio Applaudo2");
            rdItem.setDescription(getString(R.string.radio_description));
            mDataSet.add(rdItem);
            rdItem = new RadioItem();
            rdItem.setUrl("http://198.178.123.17:10922/listen.pls?sid=1&t=.m3u");
            rdItem.setSubTitle("Radio Applaudo3");
            rdItem.setDescription(getString(R.string.radio_description));
            mDataSet.add(rdItem);
            rdItem = new RadioItem();
            rdItem.setUrl("http://us5.internet-radio.com:8110/listen.pls&t=.m3u");
            rdItem.setSubTitle("Radio Applaudo4");
            rdItem.setDescription(getString(R.string.radio_description));
            mDataSet.add(rdItem);
            rdItem = new RadioItem();
            rdItem.setUrl("http://us5.internet-radio.com:8110/listen.pls&t=.m3u");
            rdItem.setSubTitle("Radio Applaudo5");
            rdItem.setDescription(getString(R.string.radio_description));
            mDataSet.add(rdItem);
            mDataSet.add(rdItem);
            mDataSet.add(rdItem);
        }

        /***
         * function to bind the data of the item selected on the Recycler view  on each element of the view,
         */
        private void bindData() {
            mTxtPlaying.setText(RADIO_PLAYING_ITEM.getSubTitle());
        }

        /***
         * Function to send intent actions for the service, this functions makes the intents for the
         * service
         * @param activityType Intent type to generate, using the constants INTENT_TYPE_RADIO_xxxx_XXXX
         * @return returns a intent to be start in a button
         */
        private Intent communicationGenerator(int activityType) {
            Intent intent;
            switch (activityType) {
                case INTENT_TYPE_RADIO_DETAIL_ACTIVITY:
                    intent = new Intent(this, RadioDetailActivity.class);
                    intent.putExtra(KEY_RADIO_DETAIL, RADIO_PLAYING_ITEM);
                    return intent;
                case INTENT_TYPE_RADIO_ACTION_PLAY:
                    intentService = new Intent(this, MusicService.class);
                    intentService.putExtra(ARG_ITEM_PLAY_ON_SERVICE, RADIO_PLAYING_ITEM);
                    intentService.setAction(MusicService.START_FOREGROUND_ACTION);
                    return intentService;
                case INTENT_TYPE_RADIO_ACTION_PAUSE:
                    intentService = new Intent(this, MusicService.class);
                    intentService.setAction(MusicService.PAUSE_FOREGROUND_ACTION);
                    return intentService;
                case INTENT_TYPE_RADIO_ACTION_STOP:
                    intentService = new Intent(this, MusicService.class);
                    intentService.setAction(MusicService.STOP_FOREGROUND_ACTION);
                    return intentService;
                case INTENT_TYPE_RADIO_ACTION_MUTE:
                    intentService = new Intent(this, MusicService.class);
                    intentService.setAction(MusicService.MUTE_FOREGROUND_ACTION);
                    return intentService;
                default:
                    return null;
            }
        }

        /***
         * Function for toggle buttons, change the view colors and icons
         * @param enableType Using the Constants TOGGLE_TYPE_XXXX to change the view
         */
        private void toggleButtonsClick(int enableType) {
            switch (enableType) {
                case TOGGLE_TYPE_PLAY:
                    mButtonPlay.setImageResource(R.drawable.ic_pause_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    break;
                case TOGGLE_TYPE_PAUSE:
                    mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    break;
                case TOGGLE_TYPE_STOP:
                    mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_red);
                    break;
                case TOGGLE_TYPE_MUTE:
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (am != null) {
                        if (am.isStreamMute(AudioManager.STREAM_MUSIC)) {
                            mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                        } else {
                            mButtonMute.setImageResource(R.drawable.ic_mute_yellow);
                        }
                    }

                    break;
            }
        }

        /***
         * Function to update the view status using the media player status, for the configuration
         * changes and the on resume for the Mainactivity
         */
        public void checkMediaPlayerStatus() {
            if (mBoundMusicService != null) {
                if (mBoundMusicService.isMediaPlaying()) {
                    mButtonPlay.setImageResource(R.drawable.ic_pause_gray);
                } else {
                    mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                }

                if (mBoundMusicService.isMediaMute()) {
                    mButtonMute.setImageResource(R.drawable.ic_mute_yellow);
                } else {
                    mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                }

                if (MusicService.mediaPlayer == null) {
                    mButtonStop.setImageResource(R.drawable.ic_stop_red);
                } else {
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                }
            }

        }

    /***
     * Override the up volumen key press function to change the mute icons and system music stream mute
     * @param event
     * @return
     */
    @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            // What key was pressed
            int keyCode = event.getKeyCode();

            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    // Check your event code (KeyEvent.ACTION_DOWN, KeyEvent.ACTION_UP etc)
                    if (mBoundMusicService.isMediaMute()) {
                        mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                    }
                    return super.dispatchKeyEvent(event);

                default:
                    // Let the system do what it wanted to do
                    return super.dispatchKeyEvent(event);
            }
        }


        @Override
        public void onInternetAvailable(boolean status) {
            //default and internet status buttons on/off
        }

        /***
         * Nested class to get the service Conection with the binder.
         */
        private ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceBound = false;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
                mBoundMusicService = myBinder.getService();
                mBoundMusicService.setMediaPlayerStateChangeLister(MainActivity.this);
                mServiceBound = true;
                checkMediaPlayerStatus();
            }
        };

        //Implementations for the service MediaPlayerStateListener contact
        @Override
        public void stateChangePlay() {
            this.toggleButtonsClick(TOGGLE_TYPE_PLAY);
        }

        @Override
        public void stateChangePause() {
            this.toggleButtonsClick(TOGGLE_TYPE_PAUSE);
        }

        @Override
        public void stateChangeMute() {
            this.toggleButtonsClick(TOGGLE_TYPE_MUTE);
        }


}
