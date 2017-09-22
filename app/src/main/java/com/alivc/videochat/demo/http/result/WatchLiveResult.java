package com.alivc.videochat.demo.http.result;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-7-28.
 */
public class WatchLiveResult {
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;
    @SerializedName(HttpConstant.KEY_NAME)
    private String name;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;
    @SerializedName(HttpConstant.KEY_PLAY_URL)
    private String playUrl;
    @SerializedName(HttpConstant.KEY_MNS)
    private MNSModel mMNSModel;

    private MNSConnectModel mConnectModel;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public MNSModel getMNSModel() {
        return mMNSModel;
    }

    public void setMNSModel(MNSModel MNSModel) {
        mMNSModel = MNSModel;
    }

    public MNSConnectModel getConnectModel() {
        return mConnectModel;
    }

    public void setConnectModel(MNSConnectModel connectModel) {
        mConnectModel = connectModel;
    }

    @Override
    public String toString() {
        return "WatchLiveResult{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", roomID='" + roomID + '\'' +
                ", playUrl='" + playUrl + '\'' +
                ", mMNSModel=" + mMNSModel +
                ", mConnectModel=" + mConnectModel +
                '}';
    }
}
