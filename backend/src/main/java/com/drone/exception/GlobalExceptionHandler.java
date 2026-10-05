package com.drone.exception;

import com.drone.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器，把各类异常统一转换为前端可识别的 {@link Result} 响应。
 *
 * <p>业务异常保留原始状态码与提示；数据完整性、重复键等数据库异常转换为友好提示；
 * 未预期的异常记录日志后返回统一的 500 提示，避免把堆栈信息暴露给前端。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，按异常自带的状态码与提示返回。
     *
     * @param e 业务异常
     * @return 携带业务状态码与提示的失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理认证失败异常，统一提示“用户名或密码错误”。
     *
     * @param e 认证失败异常
     * @return 401 失败响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        return Result.error(401, "用户名或密码错误");
    }

    /**
     * 处理权限不足异常。
     *
     * @param e 权限不足异常
     * @return 403 失败响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.forbidden();
    }

    /**
     * 处理请求参数校验失败异常，把各字段的校验提示以逗号拼接后返回。
     *
     * @param e 参数校验异常
     * @return 400 失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(400, message);
    }

    /**
     * 处理唯一键冲突异常，提示编号或名称重复。
     *
     * @param e 唯一键冲突异常
     * @return 500 失败响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        return Result.error("数据重复，请检查编号或名称是否已存在");
    }

    /**
     * 处理数据完整性约束异常（外键引用、字段超长等）。
     *
     * @param e 数据完整性约束异常
     * @return 500 失败响应
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性约束: {}", e.getMessage());
        return Result.error("该数据已被其它业务记录引用，操作未完成");
    }

    /**
     * 处理非法参数异常。
     *
     * @param e 非法参数异常
     * @return 400 失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 兜底处理未预期的异常，记录完整堆栈并返回统一提示。
     *
     * @param e 未预期的异常
     * @return 500 失败响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("服务器内部错误，请联系管理员");
    }
}
