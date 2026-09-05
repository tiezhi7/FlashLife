package com.flashlife.exception;

import com.flashlife.common.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/*
 * @RestControllerAdvice
 * 可以理解：“所有 Controller 的统一异常处理中心。”
 * Controller 出现异常以后，可以集中来到这里处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /*
     * Logger：用来记录程序日志。
     * 真正无法预料的错误，我们应该在服务器日志中保留下来。
     */
    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );
    /*
     * ========================================
     * 1. 处理 FlashLife 自己的 BusinessException
     * ========================================
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>>
    handleBusinessException(
            BusinessException e
    ) {
        /*
         * Result.error()
         * 创建：
         * {
         *     code,
         *     message,
         *     data: null
         * }
         */
        Result<Void> result =
                Result.error(
                        e.getCode(),
                        e.getMessage()
                );
        /*
         * HTTP Status：
         * 例如：
         * 404
         * 409
         */
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(result);
    }
    /*
     * ========================================
     * 2. 处理 @Valid 参数校验错误
     * ========================================
     */
    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<Result<Void>>
    handleValidationException(
            MethodArgumentNotValidException e
    ) {
        /*
         * 获取第一个字段错误。
         * 例如 username = ""
         * 就可能得到：用户名不能为空
         */
        String message =
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(error ->
                                error.getDefaultMessage()
                        )
                        .orElse(
                                ErrorCode.INVALID_PARAMETER
                                        .getMessage()
                        );
        Result<Void> result =
                Result.error(
                        ErrorCode.INVALID_PARAMETER
                                .getCode(),
                        message
                );
        /*
         * 参数错误： HTTP 400
         */
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(result);
    }
    /*
     * ========================================
     * 3. 处理没有预料到的异常
     * ========================================
     * 这是系统最后的安全网。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>>
    handleUnknownException(
            Exception e
    ) {
        /*
         * 把真正异常记录到服务器日志。
         * 但是不要把 Java 堆栈原封不动返回给前端。
         */
        log.error(
                "Unhandled exception",
                e
        );
        Result<Void> result =
                Result.error(
                        ErrorCode.INTERNAL_ERROR
                                .getCode(),
                        ErrorCode.INTERNAL_ERROR
                                .getMessage()
                );
        return ResponseEntity
                .status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                )
                .body(result);
    }
}