package com.alivc.videochat.demo.exception;

public class BaseException extends RuntimeException{
    private String message;
    private int errorCode;

    public BaseException(String message, int errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
