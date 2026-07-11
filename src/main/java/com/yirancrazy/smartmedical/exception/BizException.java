package com.yirancrazy.smartmedical.exception;

import lombok.Getter;

/**
 * 业务异常
 * @Author: YiRanCrazy@gmail.com
 * @Description: 业务逻辑层抛出此异常，由 GlobalExceptionHandler 转为 Result.fail
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(BizErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.code = errorCode.getCode();
    }

    public BizException(BizErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}