package com.drone.vo;

import lombok.Data;

/**
 * 供业务表单选择使用人员时的轻量用户信息，不包含联系方式等敏感字段。
 */
@Data
public class UserOptionVO {

    /** 用户ID */
    private Long id;

    /** 登录名 */
    private String username;

    /** 真实姓名，界面优先展示 */
    private String realName;

    /**
     * 构造用户下拉选项。
     *
     * @param id       用户ID
     * @param username 登录名
     * @param realName 真实姓名
     */
    public UserOptionVO(Long id, String username, String realName) {
        this.id = id;
        this.username = username;
        this.realName = realName;
    }
}
