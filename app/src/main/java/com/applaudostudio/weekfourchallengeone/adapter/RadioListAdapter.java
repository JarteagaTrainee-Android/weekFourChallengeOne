package com.applaudostudio.weekfourchallengeone.adapter;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.applaudostudio.weekfourchallengeone.R;
import com.applaudostudio.weekfourchallengeone.model.RadioItem;

import java.util.List;

public class RadioListAdapter extends RecyclerView.Adapter<RadioListAdapter.RadioViewHolder> {
    private List<RadioItem> mDataSet;
    private final ItemSelectedListener mCallback;


    public RadioListAdapter(List<RadioItem> mDataSet, ItemSelectedListener callback) {
        this.mDataSet = mDataSet;
        mCallback=callback;
    }

    @NonNull
    @Override
    public RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_radio_item_old, parent, false);
        return new RadioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RadioListAdapter.RadioViewHolder viewHolder,int i) {
        viewHolder.bindData(mDataSet.get(i));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }


    class RadioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgPlayButton;
        TextView txtTitle;
        private ConstraintLayout itemElements;

        public RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle=itemView.findViewById(R.id.textViewTitleDetail);
            imgPlayButton = itemView.findViewById(R.id.imageViewButtonPlay);
            itemElements=itemView.findViewById(R.id.layoutContainer);
            itemElements.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layoutContainer:
                    if (mCallback != null) {
                        mCallback.onClickPlayButton(mDataSet.get(getAdapterPosition()));
                    }
                    break;
            }
        }

        private void bindData(RadioItem item){
                txtTitle.setText(item.getSubTitle());
        }

    }

    public interface ItemSelectedListener {
        void onClickPlayButton(RadioItem item);
    }

}
