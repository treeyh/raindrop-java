package com.treeyh.raindrop.exception;


public class RaindropException extends Exception {
    private Integer code;

    public RaindropException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
