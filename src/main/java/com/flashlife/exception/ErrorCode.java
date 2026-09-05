package com.flashlife.exception;

import org.springframework.http.HttpStatus;
/*
 * ErrorCode FlashLife 项目统一业务错误定义。
 * 每一种错误包含：1. 业务错误码2. 错误提示3. HTTP 状态码
 */
public enum ErrorCode {
    /*
     * 参数错误。
     * HTTP：400 Bad Request
     */
    INVALID_PARAMETER(
            40001,
            "请求参数不合法",
            HttpStatus.BAD_REQUEST
    ),
    /*
     * 用户不存在。
     * HTTP：404 Not Found
     */
    USER_NOT_FOUND(
            40401,
            "用户不存在",
            HttpStatus.NOT_FOUND
    ),
    /*
     * username 已经被占用。
     * HTTP： 409 Conflict
     */
    USERNAME_ALREADY_EXISTS(
            40901,
            "用户名已存在",
            HttpStatus.CONFLICT
    ),
    /*
     * 未知服务器错误。
     */
    INTERNAL_ERROR(
            50000,
            "服务器内部错误",
            HttpStatus.INTERNAL_SERVER_ERROR
    );
    /*
     * FlashLife 自己定义的业务错误码。
     */
    private final Integer code;
    /*
     * 用户能够理解的错误信息。
     */
    private final String message;
    /*
     * HTTP 协议层状态码。
     */
    private final HttpStatus httpStatus;
    /*
     * Enum 构造方法。
     */
    ErrorCode(
            Integer code,
            String message,
            HttpStatus httpStatus
    ) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}