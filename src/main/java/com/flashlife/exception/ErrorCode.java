package com.flashlife.exception;

import org.springframework.http.HttpStatus;
/*
 * ErrorCode
 * FlashLife 项目统一业务错误码。
 * 每一个错误包含：
 * 1. code
 *    FlashLife 自定义业务错误码
 * 2. message
 *    返回给客户端看的错误信息
 * 3. httpStatus
 *    HTTP 协议状态码
 */
public enum ErrorCode {
    /*
     * ========================================
     * 400 - 请求参数错误
     * ========================================
     * 例如：
     * username 为空
     * username 长度不合法
     * nickname 为空
     */
    INVALID_PARAMETER(
            40001,
            "请求参数不合法",
            HttpStatus.BAD_REQUEST
    ),
    /*
     * ========================================
     * 401 - 用户名或密码错误
     * ========================================
     * 登录时使用。
     * 我们故意不区分： 用户名不存在和密码错误避免暴露账号是否存在。
     */
    INVALID_CREDENTIALS(
            40101,
            "用户名或密码错误",
            HttpStatus.UNAUTHORIZED
    ),
    /*
     * ========================================
     * 401 - 没有有效登录状态
     * ========================================
     * 例如：
     * 没有 Access Token
     * Access Token 无效
     * Access Token 已过期
     */
    UNAUTHORIZED(
            40102,
            "请先登录或登录状态已失效",
            HttpStatus.UNAUTHORIZED
    ),
    /*
     * ========================================
     * 401 - Refresh Token 无效
     * ========================================
     * Day7 使用。
     * 例如：
     * Refresh Token 不存在
     * Refresh Token 已过期
     * Refresh Token 已撤销
     * Refresh Token 已被 Rotation 消费
     */
    INVALID_REFRESH_TOKEN(
            40103,
            "Refresh Token无效或已过期",
            HttpStatus.UNAUTHORIZED
    ),
    /*
     * ========================================
     * 404 - 用户不存在
     * ========================================
     * 例如：GET /api/users/99999
     */
    USER_NOT_FOUND(
            40401,
            "用户不存在",
            HttpStatus.NOT_FOUND
    ),
    /*
     * ========================================
     * 409 - 用户名已经存在
     * ========================================
     * 注册时： username 已经被别人使用。
     */
    USERNAME_ALREADY_EXISTS(
            40901,
            "用户名已存在",
            HttpStatus.CONFLICT
    ),
    /*
     * ========================================
     * 500 - 未知服务器错误
     * ========================================
     */
    INTERNAL_ERROR(
            50000,
            "服务器内部错误",
            HttpStatus.INTERNAL_SERVER_ERROR
    );
    /*
     * ========================================
     * 从这里开始已经不是枚举常量
     * ========================================
     */
    /*
     * FlashLife 自定义业务错误码。
     * 例如：
     * 40001
     * 40101
     * 40401
     * 40901
     */
    private final Integer code;
    /*
     * 返回给客户端看的错误信息。
     */
    private final String message;
    /*
     * HTTP 状态码。
     * 例如：
     * 400
     * 401
     * 404
     * 409
     * 500
     */
    private final HttpStatus httpStatus;
    /*
     * 枚举构造方法。
     * 例如：
     * USER_NOT_FOUND(
     *     40401,
     *     "用户不存在",
     *     HttpStatus.NOT_FOUND
     * )
     * 就会调用这个构造器。
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
    /*
     * 获取业务错误码。
     */
    public Integer getCode() {
        return code;
    }
    /*
     * 获取错误信息。
     */
    public String getMessage() {
        return message;
    }
    /*
     * 获取 HTTP 状态码。
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}