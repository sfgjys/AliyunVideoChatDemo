package com.alivc.videochat.demo.http.result;

/**
 * 类的描述:
 */
public enum MixStatusCode {
    /**
     * 变量的描述: 成功推流
     */
    SUCCESS("Success"),
    /**
     * 变量的描述: 主播放流不存在
     */
    MAIN_STREAM_NOT_EXIST("MainStreamNotExist"),
    /**
     * 变量的描述: 混流(连麦观众流)不存在
     */
    MIX_STREAM_NOT_EXIST("MixStreamNotExist"),
    /**
     * 变量的描述: 网络异常
     */
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
