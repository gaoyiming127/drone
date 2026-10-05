package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数。
 */
@Data
public class LoginDto {

    /** 用户名，必填 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码（明文），必填，由业务层与数据库中的 BCrypt 密文比对 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
