package com.alivc.videochat.demo.http.model;

/**
 * Created by apple on 2016/12/13.
 */

public enum  MixStatusCode {
    SUCCESS("Success"),
    MAIN_STREAM_NOT_EXIST("MainStreamNotExist"),
    MIX_STREAM_NOT_EXIST("MixStreamNotExist"),
    INTERNAL_ERROR("InternalError");

    String value;
    MixStatusCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
