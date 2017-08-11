package com.alivc.videochat.demo.im.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-8-11.
 */
public class BaseMessage {
    @SerializedName("type")
    private int type;
    @SerializedName("timestamp")
    private long timestamp;
    @SerializedName("data")
    private JsonObject data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
