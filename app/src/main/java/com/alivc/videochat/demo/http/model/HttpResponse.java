package com.alivc.videochat.demo.http.model;

import com.google.gson.annotations.SerializedName;

/**
 * 用与Call<T>的泛型，而HttpResponse<T>的泛型则用于网络请求结果监听回调接口Callback<T>的泛型
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
