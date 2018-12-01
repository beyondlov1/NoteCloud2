package com.beyond.exception;

/**
 * @author beyondlov1
 * @date 2018/12/01
 */
public class RemotePullException extends RuntimeException {
    @Override
    public String getMessage() {
        return "远程数据拉取失败";
    }
}
