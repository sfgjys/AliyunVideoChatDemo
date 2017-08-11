package com.alivc.videochat.demo.exception;

/**
 * Created by liujianghao on 16-8-2.
 */
public class APIException extends BaseException {

    public APIException(String message, int errorCode) {
        super(message, errorCode);
    }
}
