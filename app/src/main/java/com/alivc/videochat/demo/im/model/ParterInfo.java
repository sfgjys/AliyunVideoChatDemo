package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

public class ParterInfo {
    @SerializedName(HttpConstant.KEY_UID)
    private String mUID;// 其他连麦观众uid
    @SerializedName(HttpConstant.KEY_URL)
    private String mPlayUrl;// 其他连麦观众短延迟播放地址

    public String getUID() {
        return mUID;
    }

    public void setUID(String UID) {
        mUID = UID;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public void setPlayUrl(String playUrl) {
        mPlayUrl = playUrl;
    }
}
