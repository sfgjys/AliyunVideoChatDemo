package com.alivc.videochat.demo.exception;

public class APIException extends BaseException {

    public APIException(String message, int errorCode) {
        super(message, errorCode);
    }
}
