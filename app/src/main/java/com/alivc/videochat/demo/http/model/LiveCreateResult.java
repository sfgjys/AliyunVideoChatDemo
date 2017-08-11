package com.alivc.videochat.demo.http.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by liujianghao on 16-7-28.
 */
public class LiveCreateResult implements Serializable{

    @SerializedName(HttpConstant.KEY_UID)
    private String mUid;
    @SerializedName(HttpConstant.KEY_NAME)
    private String mName;
    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String mRoomID;
    @SerializedName(HttpConstant.KEY_RTMP_URL)
    private String mRtmpUrl;
    @SerializedName(HttpConstant.KEY_MNS)
    private MNSModel mMNSModel;
    @SerializedName(HttpConstant.KEY_IS_MIX_READY)
    private boolean mIsMixReady;
    @SerializedName(HttpConstant.KEY_IS_MIXED)
    private boolean mIsMixed;
    @SerializedName(HttpConstant.KEY_PLAY_URL)
    private String mPlayUrl;
    @SerializedName(HttpConstant.KEY_M3U8_PLAY_URL)
    private String mM3u8PlayUrl;
    @SerializedName(HttpConstant.KEY_RTMP_PLAY_URL)
    private String mRtmpPlayUrl;
    @SerializedName(HttpConstant.KEY_STATUS)
    private int mStatus;
    @SerializedName(HttpConstant.KEY_TYPE)
    private int mUserType;

    private MNSConnectModel mMnsConnectModel;


    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getRoomID() {
        return mRoomID;
    }

    public void setRoomID(String roomID) {
        this.mRoomID = roomID;
    }


    public String getRtmpUrl() {
        return mRtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.mRtmpUrl = rtmpUrl;
    }

    public MNSModel getMNSModel() {
        return mMNSModel;
    }

    public void setMNSModel(MNSModel MNSModel) {
        mMNSModel = MNSModel;
    }

    public boolean isMixReady() {
        return mIsMixReady;
    }

    public void setMixReady(boolean mixReady) {
        mIsMixReady = mixReady;
    }

    public boolean isMixed() {
        return mIsMixed;
    }

    public void setMixed(boolean mixed) {
        mIsMixed = mixed;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public void setPlayUrl(String playUrl) {
        mPlayUrl = playUrl;
    }

    public String getM3u8PlayUrl() {
        return mM3u8PlayUrl;
    }

    public void setM3u8PlayUrl(String m3u8PlayUrl) {
        mM3u8PlayUrl = m3u8PlayUrl;
    }

    public String getRtmpPlayUrl() {
        return mRtmpPlayUrl;
    }

    public void setRtmpPlayUrl(String rtmpPlayUrl) {
        mRtmpPlayUrl = rtmpPlayUrl;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getUserType() {
        return mUserType;
    }

    public void setUserType(int userType) {
        mUserType = userType;
    }

    public MNSConnectModel getMnsConnectModel() {
        return mMnsConnectModel;
    }

    public void setMnsConnectModel(MNSConnectModel mnsConnectModel) {
        mMnsConnectModel = mnsConnectModel;
    }

    @Override
    public String toString() {
        return "LiveCreateResult{" +
                "mUid='" + mUid + '\'' +
                ", mName='" + mName + '\'' +
                ", mRoomID='" + mRoomID + '\'' +
                ", mRtmpUrl='" + mRtmpUrl + '\'' +
                ", mMNSModel=" + mMNSModel +
                ", mIsMixReady=" + mIsMixReady +
                ", mIsMixed=" + mIsMixed +
                ", mPlayUrl='" + mPlayUrl + '\'' +
                ", mM3u8PlayUrl='" + mM3u8PlayUrl + '\'' +
                ", mRtmpPlayUrl='" + mRtmpPlayUrl + '\'' +
                ", mStatus=" + mStatus +
                ", mUserType=" + mUserType +
                ", mMnsConnectModel=" + mMnsConnectModel +
                '}';
    }
}
