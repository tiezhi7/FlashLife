package com.flashlife.exception;

import org.springframework.http.HttpStatus;
/*
 * BusinessException FlashLife 的“业务异常”。
 * 例如： 用户不存在 用户名重复 优惠券不存在 库存不足
 * 这些不是 Java 程序崩了，
 * 而是：业务规则告诉我们：
 * 当前操作无法完成。
 */
public class BusinessException
        extends RuntimeException {
    /*
     * FlashLife 业务错误码。
     */
    private final Integer code;
    /*
     * HTTP Status。
     */
    private final HttpStatus httpStatus;
    /*
     * 创建 BusinessException 时，传入一个 ErrorCode。
     */
    public BusinessException(
            ErrorCode errorCode
    ) {
        /*
         * RuntimeException 本身已经拥有 message。
         * 所以把：errorCode.getMessage() 交给父类。
         */
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }
    public Integer getCode() {
        return code;
    }
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
