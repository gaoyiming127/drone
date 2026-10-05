package com.drone.enums;

import lombok.Getter;

/**
 * 用户角色枚举：与 sys_user.role 字段取值一一对应。
 *
 * <p>【待人工确认】当前项目内业务代码均直接使用字符串字面量（如 "ADMIN"）判断，
 * 本枚举未被任何文件引用；因无法确认是否存在外部依赖或后续使用计划，故本次重构予以保留，
 * 请人工确认后再决定是否删除。</p>
 */
@Getter
public enum RoleEnum {
    ADMIN("ADMIN", "管理员"),
    USER("USER", "普通用户");

    /** 角色编码，与数据库中存储的取值一致 */
    private final String code;

    /** 角色中文描述，用于界面展示 */
    private final String description;

    RoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按角色编码查找枚举项。
     *
     * @param code 角色编码，如 "ADMIN"
     * @return 匹配的枚举项；无匹配时返回 null
     */
    public static RoleEnum getByCode(String code) {
        for (RoleEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
