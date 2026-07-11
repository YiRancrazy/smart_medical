package com.yirancrazy.smartmedical.exception;

import com.yirancrazy.smartmedical.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * @Author: YiRanCrazy@gmail.com
 * @Description: 把业务异常和系统异常统一转为 Result.fail
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("[Biz] code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public Result<Void> handleOptimisticLock(OptimisticLockingFailureException e) {
        log.warn("[Lock] {}", e.getMessage());
        return Result.fail(BizErrorCode.CONCURRENT_OPERATION.getCode(), BizErrorCode.CONCURRENT_OPERATION.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleAny(Exception e) {
        log.error("[SYS]", e);
        return Result.fail(500, "系统异常");
    }
}