package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liujianghao on 16-8-19.
 */
public class MsgDataAgreeVideoCall {
    @SerializedName(HttpConstant.KEY_INVITEE_UID)
    private String inviteeUID;
    @SerializedName(HttpConstant.KEY_INVITEE_NAME)
    private String inviteeName;
    @SerializedName(HttpConstant.KEY_RTMP_URL)          //副麦推流地址
    private String rtmpUrl;
    @SerializedName(HttpConstant.KEY_INVITEE_ROOM_ID)
    private String inviteeRoomID;
    @SerializedName(HttpConstant.KEY_INVITER_ROOM_ID)
    private String inviterRoomID;
    @SerializedName(HttpConstant.KEY_MAIN_PLAY_URL)  //主流播放地址
    private String mMainPlayUrl;
    @SerializedName(HttpConstant.KEY_PLAY_URLS)
    private List<ParterInfo> mParterInfos;

    public String getInviteeUID() {
        return inviteeUID;
    }

    public void setInviteeUID(String inviteeUID) {
        this.inviteeUID = inviteeUID;
    }

    public String getInviteeName() {
        return inviteeName;
    }

    public void setInviteeName(String inviteeName) {
        this.inviteeName = inviteeName;
    }

    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public String getInviteeRoomID() {
        return inviteeRoomID;
    }

    public void setInviteeRoomID(String inviteeRoomID) {
        this.inviteeRoomID = inviteeRoomID;
    }

    public String getInviterRoomID() {
        return inviterRoomID;
    }

    public void setInviterRoomID(String inviterRoomID) {
        this.inviterRoomID = inviterRoomID;
    }

    public String getMainPlayUrl() {
        return mMainPlayUrl;
    }

    public void setMainPlayUrl(String mainPlayUrl) {
        mMainPlayUrl = mainPlayUrl;
    }

    public List<ParterInfo> getParterInfos() {
        return mParterInfos;
    }

    public void setParterInfos(List<ParterInfo> parterInfos) {
        mParterInfos = parterInfos;
    }
}
