package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by apple on 2016/12/15.
 */

public class MsgDataStartPublishStream implements Serializable{
    @SerializedName(HttpConstant.KEY_UID)
    private String mUid;
    @SerializedName(HttpConstant.KEY_NAME)
    private String mName;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String mRoomId;
    @SerializedName(HttpConstant.KEY_PLAY_URL)
    private String mPlayUrl;

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public void setPlayUrl(String playUrl) {
        mPlayUrl = playUrl;
    }

}
