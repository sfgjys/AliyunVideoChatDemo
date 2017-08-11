package com.alivc.videochat.demo.http.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liujianghao on 16-7-28.
 */
public class HttpResponse<T> {
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
