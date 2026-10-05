package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求参数。
 */
@Data
public class PasswordDto {

    /** 旧密码，必填，须与当前密码一致 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码，必填，保存前使用 BCrypt 加密 */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
