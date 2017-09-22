package com.alivc.videochat.demo.http.result;

import com.alivc.videochat.demo.http.HttpConstant;
import com.alivc.videochat.demo.im.model.ParterInfo;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by liujianghao on 16-7-28.
 */
public class InviteFeedbackResult {

    @SerializedName(HttpConstant.KEY_MAIN_PLAY_URL)
    private String mMainPlayUrl; //主流播放地址
    @SerializedName(HttpConstant.KEY_RTMP_URL)
    private String rtmpUrl; // 本观众副流推流地址
    @SerializedName(HttpConstant.KEY_PLAY_URLS)
    private List<ParterInfo> mOtherParterInfos;


    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public List<ParterInfo> getOtherParterInfos() {
        return mOtherParterInfos;
    }

    public void setOtherParterInfos(List<ParterInfo> otherParterInfos) {
        mOtherParterInfos = otherParterInfos;
    }

    public String getMainPlayUrl() {
        return mMainPlayUrl;
    }

    public void setMainPlayUrl(String mainPlayUrl) {
        mMainPlayUrl = mainPlayUrl;
    }
}
