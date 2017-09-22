package com.alivc.videochat.demo.http.result;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by liujianghao on 16-8-9.
 */
public class LiveItemResult implements Serializable {
    /**
     * 变量的描述: 状态为流没有创建
     */
    public static final int STATUS_CREATE_NO_STREAM = 0;
    /**
     * 变量的描述: 状态为正常直播
     */
    public static final int STATUS_NORMAL_LIVE_STREAM = 1;
    /**
     * 变量的描述: 状态为打断了流
     */
    public static final int STATUS_INTERRUPTED_STREAM = 2;

    @SerializedName(HttpConstant.KEY_ROOM_ID)
    private String roomID;
    @SerializedName(HttpConstant.KEY_UID)
    private String uid;
    @SerializedName(HttpConstant.KEY_NAME)
    private String name;
    @SerializedName(HttpConstant.KEY_DESC)
    private String desc;
    @SerializedName(HttpConstant.KEY_RTMP_URL)
    private String rtmpUrl;
    @SerializedName(HttpConstant.KEY_PLAY_URL)
    private String flvPlayUrl;
    @SerializedName(HttpConstant.KEY_STATUS)
    private int status;
    @SerializedName(HttpConstant.KEY_M3U8_PLAY_URL)
    private String m3u8PlayUrl;
    @SerializedName(HttpConstant.KEY_RTMP_PLAY_URL)
    private String rtmpPlayUrl;
    @SerializedName(HttpConstant.KEY_IS_MIX_READY)
    private boolean mIsMixReady;
    @SerializedName(HttpConstant.KEY_IS_MIXED)
    private boolean mIsMixed;
    @SerializedName(HttpConstant.KEY_MNS)
    private MNSModel mMns;

    private int mUserType;   //用户类型 FeedbackForm.INVITE_TYPE_WATCHER or Feedback.INVITE_TYPE_ANCHOR


    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getFlvPlayUrl() {
        return flvPlayUrl;
    }

    public void setFlvPlayUrl(String flvPlayUrl) {
        this.flvPlayUrl = flvPlayUrl;
    }

    public String getM3u8PlayUrl() {
        return m3u8PlayUrl;
    }

    public void setM3u8PlayUrl(String m3u8PlayUrl) {
        this.m3u8PlayUrl = m3u8PlayUrl;
    }

    public String getRtmpPlayUrl() {
        return rtmpPlayUrl;
    }

    public void setRtmpPlayUrl(String rtmpPlayUrl) {
        this.rtmpPlayUrl = rtmpPlayUrl;
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

    public MNSModel getMns() {
        return mMns;
    }

    public void setMns(MNSModel mns) {
        mMns = mns;
    }

    public int getUserType() {
        return mUserType;
    }

    public void setUserType(int userType) {
        mUserType = userType;
    }
}
