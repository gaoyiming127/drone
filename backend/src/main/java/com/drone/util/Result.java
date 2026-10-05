package com.drone.util;

import lombok.Data;

/**
 * 统一响应结果封装，非分页接口的返回体。
 *
 * <p>构造器私有：外部不能直接 new Result(...)，也不能手动实例化这个类。
 * 目的是强制使用者只能调用静态方法 success() / error() 等工厂方法创建对象，控制对象创建方式。</p>
 *
 * @param <T> 业务数据类型
 */
@Data
public class Result<T> {

    /** 业务状态码：200-成功，401-未登录或登录过期，403-无权限，其它为失败 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 业务数据，无数据时为 null */
    private T data;

    /**
     * 私有构造器：外部不能直接 new Result(...)，外部不能手动实例化这个类。
     * 目的：强制使用者只能调用静态方法 success() / error() 等工厂方法创建对象，控制对象创建方式。
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造不带业务数据的成功响应。
     *
     * <p>泛型符号 &lt;T&gt; 写在 static 后面：声明这个方法是泛型方法，定义符号 T。
     * 方法体中的 new Result<>(...) 调用上面的私有构造器创建对象；
     * 虽然构造器是 private，但 success() 方法就在本类内部，本类内部可以访问 private 构造器，外部类访问不了。</p>
     *
     * @param <T> 业务数据类型
     * @return 成功响应，data 为 null
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 构造带业务数据的成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 构造自定义提示信息的成功响应。
     *
     * <p>【待人工确认】当前项目内未发现调用，因属于对外响应工具方法，暂予保留，请人工确认。</p>
     *
     * @param message 提示信息
     * @param data    业务数据
     * @param <T>     业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 构造只带提示信息、不带业务数据的成功响应，用于新增/修改/删除等操作的结果提示。
     *
     * @param message 提示信息
     * @return 成功响应
     */
    public static Result<Void> successMsg(String message) {
        return new Result<>(200, message, null);
    }

    /**
     * 构造指定状态码的失败响应。
     *
     * @param code    业务状态码
     * @param message 错误提示
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 构造状态码为 500 的失败响应。
     *
     * @param message 错误提示
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 构造未登录（401）响应，供安全框架在令牌缺失或失效时写出响应体。
     *
     * @param <T> 业务数据类型
     * @return 401 响应
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或token已过期", null);
    }

    /**
     * 构造无权限（403）响应。
     *
     * @param <T> 业务数据类型
     * @return 403 响应
     */
    public static <T> Result<T> forbidden() {
        return new Result<>(403, "无权限访问", null);
    }
}
