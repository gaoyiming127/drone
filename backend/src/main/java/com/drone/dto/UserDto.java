package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户新增/修改请求参数。
 *
 * <p>新增用户与注册时用户名与密码必填；修改用户时密码留空表示不修改，
 * 其余字段留空表示保持原值。</p>
 */
@Data
public class UserDto {

    /** 用户名，必填；修改时若变更需保证不与其它用户重复 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 新增用户时由业务层校验必填；修改时留空表示不修改密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 角色：ADMIN-管理员，USER-普通用户；新增时为空按 USER 处理 */
    private String role;

    /** 状态：0-禁用，1-启用；新增时为空按 1 处理 */
    private Integer status;
}
