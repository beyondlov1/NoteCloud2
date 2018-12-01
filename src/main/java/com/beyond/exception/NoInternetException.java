package com.beyond.exception;

/**
 * @author beyondlov1
 * @date 2018/12/01
 */
public class NoInternetException extends RuntimeException {
    @Override
    public String getMessage() {
        return "不能连接网络";
    }
}
