package com.alivc.videochat.demo.logic;

/**
 * Created by apple on 2017/1/8.
 */

public class ChatSessionInfo {
    private String mPublisherUID;
    private String mPlayerUID;
    /**
     * 变量的描述: 推流地址
     */
    private String mRtmpUrl;        //
    /**
     * 变量的描述: 短延时播放地址
     */
    private String mPlayUrl;       //

    protected ChatSessionInfo() {
    }

    public String getPublisherUID() {
        return mPublisherUID;
    }

    public void setPublisherUID(String publisherUID) {
        mPublisherUID = publisherUID;
    }

    public String getPlayerUID() {
        return mPlayerUID;
    }

    public void setPlayerUID(String playerUID) {
        mPlayerUID = playerUID;
    }

    public String getRtmpUrl() {
        return mRtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        mRtmpUrl = rtmpUrl;
    }

    public String getPlayUrl() {
        return mPlayUrl;
    }

    public void setPlayUrl(String playUrl) {
        mPlayUrl = playUrl;
    }

    /**
     * 类的描述: 貌似没用
     */
    public static class Builder {
        private ChatSessionInfo mInstance = new ChatSessionInfo();

        public ChatSessionInfo build() {
            return mInstance;
        }

        public Builder publisherUID(String uid) {
            mInstance.mPublisherUID = uid;
            return this;
        }

        public Builder playerUID(String uid) {
            mInstance.mPlayerUID = uid;
            return this;
        }

        public Builder rtmpUrl(String url) {
            mInstance.mRtmpUrl = url;
            return this;
        }

        public Builder playUrl(String url) {
            mInstance.mPlayUrl = url;
            return this;
        }
    }
}
