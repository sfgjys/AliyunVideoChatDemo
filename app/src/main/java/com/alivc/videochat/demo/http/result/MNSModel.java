package com.alivc.videochat.demo.http.result;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by liujianghao on 16-10-21.
 */

public class MNSModel implements Serializable{
    @SerializedName(HttpConstant.KEY_TOPIC)
    private String mTopic;
    @SerializedName(HttpConstant.KEY_TOPIC_LOCATION)
    private String mTopicLocation;
    @SerializedName(HttpConstant.KEY_ROOM_TAG)
    private String mRoomTag;
    @SerializedName(HttpConstant.KEY_USER_ROOM_TAG)
    private String mUserTag;

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public String getTopicLocation() {
        return mTopicLocation;
    }

    public void setTopicLocation(String topicLocation) {
        mTopicLocation = topicLocation;
    }

    public String getRoomTag() {
        return mRoomTag;
    }

    public void setRoomTag(String roomTag) {
        mRoomTag = roomTag;
    }

    public String getUserTag() {
        return mUserTag;
    }

    public void setUserTag(String userTag) {
        mUserTag = userTag;
    }

    @Override
    public String toString() {
        return "MNSModel{" +
                "mTopic='" + mTopic + '\'' +
                ", mTopicLocation='" + mTopicLocation + '\'' +
                ", mRoomTag='" + mRoomTag + '\'' +
                ", mUserTag='" + mUserTag + '\'' +
                '}';
    }
}
