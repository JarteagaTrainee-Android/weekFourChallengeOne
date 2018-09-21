package com.applaudostudio.weekfourchallengeone;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.applaudostudio.weekfourchallengeone.adapter.RadioListAdapter;
import com.applaudostudio.weekfourchallengeone.model.RadioItem;
import com.applaudostudio.weekfourchallengeone.receiver.InternetReceiver;
import com.applaudostudio.weekfourchallengeone.service.MusicService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RadioListAdapter.ItemSelectedListener, View.OnClickListener, InternetReceiver.InternetConnectionListener {
    public static boolean PLAYING_MUSIC;
    public static boolean MUTE_MUSIC;
    //for broadcast network status
    private static boolean internetStatus;
    private RadioItem RADIO_PLAYING_ITEM;
    private static final int INTENT_TYPE_RADIO_DETAIL_ACTIVITY = 0;
    private static final int INTENT_TYPE_RADIO_ACTION_PLAY = 1;
    private static final int INTENT_TYPE_RADIO_ACTION_STOP=2;

    public static final String KEY_RADIO_DETAIL = "RADIO_DETAIL_DATA";
    public static final String ARG_ITEM_PLAY_ON_SERVICE = "RADIO_TO_PLAY";
    //View Elements
    private List<RadioItem> mDataSet;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RadioListAdapter mAdapter;
    private ImageView mButtonPlay;
    private ImageView mButtonStop;
    private ImageView mButtonInfo;
    private ImageView mButtonMute;
    private TextView mTxtPlaying;

    Intent intentService;

    private InternetReceiver mInternetReceiver;

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
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new RadioListAdapter(mDataSet, this);
        mRecyclerView.setAdapter(mAdapter);
        mInternetReceiver = new InternetReceiver(this);
        //UI SET CLICK LISTENERS
        mButtonPlay.setOnClickListener(this);
        mButtonStop.setOnClickListener(this);
        mButtonMute.setOnClickListener(this);
        mButtonInfo.setOnClickListener(this);
        mTxtPlaying.setText("");


        toggleButtons(0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleButtons(0);//To Execute Default case.

        //RECEIVERS
        IntentFilter intentFilter = new IntentFilter();
        // Add network connectivity change action.
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(mInternetReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mInternetReceiver);
        super.onDestroy();
    }

    @Override
    public void onClickPlayButton(RadioItem item) {
        RADIO_PLAYING_ITEM = item;
        bindData();
    }


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
        rdItem.setUrl("http://us5.internet-radio.com:8110/listen.pls&t=.m3u");
        rdItem.setSubTitle("Radio Applaudo2");
        rdItem.setDescription(getString(R.string.radio_description));
        mDataSet.add(rdItem);

        rdItem = new RadioItem();
        rdItem.setUrl("http://us5.internet-radio.com:8110/listen.pls&t=.m3u");
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
        mDataSet.add(rdItem);
        mDataSet.add(rdItem);
        mDataSet.add(rdItem);
        mDataSet.add(rdItem);

    }

    private void toggleButtons(int idButton) {
        if (internetStatus) {
            switch (idButton) {
                case R.id.imageViewPlay:
                    mButtonPlay.setImageResource(R.drawable.ic_play_blue);
                    mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    break;
                case R.id.imageViewStop:
                    mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                    mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_red);
                    break;
                case R.id.imageViewMute:
                    if (PLAYING_MUSIC) {
                        mButtonPlay.setImageResource(R.drawable.ic_play_blue);
                    } else {
                        mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                    }
                    mButtonMute.setImageResource(R.drawable.ic_mute_yellow);
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    break;
                case R.id.imageViewInfo:
                    if (PLAYING_MUSIC) {
                        mButtonPlay.setImageResource(R.drawable.ic_play_blue);
                    } else {
                        mButtonPlay.setImageResource(R.drawable.ic_play_gray);
                    }
                    mButtonMute.setImageResource(R.drawable.ic_mute_gray);
                    mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    break;
                default:
                    if (PLAYING_MUSIC) {
                        mButtonPlay.setImageResource(R.drawable.ic_play_blue);
                        mButtonStop.setImageResource(R.drawable.ic_stop_gray);
                    }
                    break;
            }
        } else { //End if Internet Status
            mButtonPlay.setImageResource(R.drawable.ic_play_gray);
            mButtonMute.setImageResource(R.drawable.ic_mute_gray);
            mButtonStop.setImageResource(R.drawable.ic_stop_gray);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.imageViewPlay:
                if (RADIO_PLAYING_ITEM != null && PLAYING_MUSIC!=true) {
                    PLAYING_MUSIC = true;
                    startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_PLAY));
                    toggleButtons(v.getId());
                }
                break;
            case R.id.imageViewStop:
                if(PLAYING_MUSIC==true) {
                    PLAYING_MUSIC = false;
                    startService(communicationGenerator(INTENT_TYPE_RADIO_ACTION_STOP));
                    toggleButtons(v.getId());
                }
                break;
            case R.id.imageViewMute:
                toggleButtons(v.getId());
                break;
            case R.id.imageViewInfo:
                if (RADIO_PLAYING_ITEM != null)
                    startActivity(communicationGenerator(INTENT_TYPE_RADIO_DETAIL_ACTIVITY));
                toggleButtons(v.getId());
                break;
            default:

                break;


        }
    }

    private Intent communicationGenerator(int activityType) {
        Intent intent = null;
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
            case INTENT_TYPE_RADIO_ACTION_STOP:
                intentService = new Intent(this, MusicService.class);
                intentService.setAction(MusicService.STOP_FOREGROUND_ACTION);
                return intentService;
        }
        return intent;
    }

    private void bindData() {
        mTxtPlaying.setText(RADIO_PLAYING_ITEM.getSubTitle());
    }

    @Override
    public void onInternetAvailable(boolean status) {
        internetStatus = status;
        //default and internet status buttons on/off
        toggleButtons(0);
    }
}
