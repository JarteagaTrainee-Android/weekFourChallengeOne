package com.applaudostudio.weekfourchallengeone;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.applaudostudio.weekfourchallengeone.model.RadioItem;

public class RadioDetailActivity extends AppCompatActivity {

    TextView txtTitle;
    TextView txtDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_detail);

        txtTitle=findViewById(R.id.textViewTitleDetail);
        txtDescription =findViewById(R.id.textViewDescriptionDetail);
        RadioItem item = getIntent().getParcelableExtra(MainActivity.KEY_RADIO_DETAIL);

        txtTitle.setText(item.getSubTitle());
        txtDescription.setText(item.getDescription());
    }




}
