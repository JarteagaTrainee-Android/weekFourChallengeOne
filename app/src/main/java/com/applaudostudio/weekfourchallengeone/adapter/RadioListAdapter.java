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

    /***
     * Constructor to set dataset and a colback for the SelectedItem listener
     * @param mDataSet Data with the radio items
     * @param callback callback for the item selected.
     */
    public RadioListAdapter(List<RadioItem> mDataSet, ItemSelectedListener callback) {
        this.mDataSet = mDataSet;
        mCallback=callback;
    }

    /***
     * Constructor for the view Holder of the recyclerview
     * @param parent the parent viewGroup
     * @param viewType Type of view to be render
     * @return returns a RadioViewHolder
     */
    @NonNull
    @Override
    public RadioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_radio_item_old, parent, false);
        return new RadioViewHolder(view);
    }

    /***
     * bindin for the view holder
     * @param viewHolder view holder for radio Adapter
     * @param i item index
     */
    @Override
    public void onBindViewHolder(@NonNull RadioListAdapter.RadioViewHolder viewHolder,int i) {
        viewHolder.bindData(mDataSet.get(i));
    }

    /***
     * function to get the data set size.
     * @return  number of item of the dataset
     */
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    /***
     * Class for the Radio View holder
     */
    class RadioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgPlayButton;
        TextView txtTitle;
        private ConstraintLayout itemElements;
        RadioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle=itemView.findViewById(R.id.textViewTitleDetail);
            imgPlayButton = itemView.findViewById(R.id.imageViewButtonPlay);
            itemElements=itemView.findViewById(R.id.layoutContainer);
            itemElements.setOnClickListener(this);
        }

        /***
         * On click for all the container
         * @param view the whole view as parameter
         */
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

        /***
         * Function to bind the data to the view element
         * @param item
         */
        private void bindData(RadioItem item){
                txtTitle.setText(item.getSubTitle());
        }

    }

    /***
     * Interface for the click on the items to send the data to the activity
     */
    public interface ItemSelectedListener {
        void onClickPlayButton(RadioItem item);
    }


}
