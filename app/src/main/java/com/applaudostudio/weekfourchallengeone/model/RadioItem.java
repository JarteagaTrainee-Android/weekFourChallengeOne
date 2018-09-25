package com.applaudostudio.weekfourchallengeone.model;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * model of each radio with the parcelable implementation.
 */
public class RadioItem implements Parcelable {
    private String url;
    private String subTitle;
    private String description;

    public RadioItem() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.subTitle);
        dest.writeString(this.description);
    }

    private RadioItem(Parcel in) {
        this.url = in.readString();
        this.subTitle = in.readString();
        this.description = in.readString();
    }

    public static final Creator<RadioItem> CREATOR = new Creator<RadioItem>() {
        @Override
        public RadioItem createFromParcel(Parcel source) {
            return new RadioItem(source);
        }

        @Override
        public RadioItem[] newArray(int size) {
            return new RadioItem[size];
        }
    };
}
