package com.alivc.videochat.demo.im.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liujianghao on 16-8-28.
 */
public class MsgDataMergeStream {
    @SerializedName("mainRoomId")
    private String mMainRoomID;
    @SerializedName("userName")
    private String mUsername;
    @SerializedName("mainMixPlayUrls")
    private List<String> mMainMixPlayUrls;

    public String getMainRoomID() {
        return mMainRoomID;
    }

    public void setMainRoomID(String mainRoomID) {
        mMainRoomID = mainRoomID;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public List<String> getMainMixPlayUrls() {
        return mMainMixPlayUrls;
    }

    public void setMainMixPlayUrls(List<String> mainMixPlayUrls) {
        mMainMixPlayUrls = mainMixPlayUrls;
    }
}
