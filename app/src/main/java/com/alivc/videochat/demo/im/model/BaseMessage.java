package com.alivc.videochat.demo.im.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * 类的描述: 这个类承载了从MNS服务器中获取的消息，但是这只是个基础Bean类，其变量data还等这进一步的具体解析
 */
public class BaseMessage {
    @SerializedName("type")
    private int type;
    @SerializedName("timestamp")// 时间戳
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
