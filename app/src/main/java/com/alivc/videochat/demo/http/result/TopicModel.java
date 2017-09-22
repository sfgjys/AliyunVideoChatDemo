package com.alivc.videochat.demo.http.result;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-7-28.
 */
public class TopicModel{
    @SerializedName("private")
    private String privateTopic;
    @SerializedName("public")
    private String publicTopic;

    public String getPrivateTopic() {
        return privateTopic;
    }

    public void setPrivateTopic(String privateTopic) {
        this.privateTopic = privateTopic;
    }

    public String getPublicTopic() {
        return publicTopic;
    }

    public void setPublicTopic(String publicTopic) {
        this.publicTopic = publicTopic;
    }
}
