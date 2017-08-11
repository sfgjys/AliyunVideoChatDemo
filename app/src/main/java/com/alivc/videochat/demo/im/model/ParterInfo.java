package com.alivc.videochat.demo.im.model;

import com.alivc.videochat.demo.http.HttpConstant;
import com.google.gson.annotations.SerializedName;

/**
 * Created by apple on 2017/1/11.
 */

public class ParterInfo {
    @SerializedName(HttpConstant.KEY_UID)
    private String mUID;
    @SerializedName(HttpConstant.KEY_URL)
    private String mPlayUrl;

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
