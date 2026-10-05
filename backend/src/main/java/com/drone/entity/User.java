package com.drone.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体，对应数据库表 sys_user。
 *
 * <p>密码使用 BCrypt 加密存储，并通过 {@link JsonIgnore} 禁止序列化到接口响应中。</p>
 */
@Data
@TableName("sys_user")
public class User {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，登录账号，全局唯一 */
    private String username;

    /** 密码（BCrypt 密文），不参与 JSON 序列化 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 角色：ADMIN-管理员，USER-普通用户 */
    private String role;

    /** 状态：0-禁用，1-启用；仅启用状态的账号可登录 */
    private Integer status;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
