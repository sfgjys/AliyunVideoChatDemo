package com.alivc.videochat.demo.http.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-7-28.
 */
public class MqttModel {
    @SerializedName("ip")
    private String ip;
    @SerializedName("port")
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
