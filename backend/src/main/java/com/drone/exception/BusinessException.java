package com.drone.exception;

import lombok.Getter;

/**
 * 业务异常，用于向调用方返回可读的业务提示。
 *
 * <p>由 {@link GlobalExceptionHandler} 统一捕获并转换为 {@link com.drone.util.Result} 响应；
 * 未指定状态码时默认为 500，未登录等场景可指定 401。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码：默认 500，未登录等场景为 401 */
    private final int code;

    /**
     * 构造状态码为 500 的业务异常。
     *
     * @param message 业务提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造指定状态码的业务异常。
     *
     * @param code    业务状态码
     * @param message 业务提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造携带根因、状态码为 500 的业务异常。
     *
     * @param message 业务提示信息
     * @param cause   引发该异常的原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
}
