package com.drone.enums;

import lombok.Getter;

/**
 * 电池健康等级枚举：由 SOH 与鼓包情况计算得出，对应 battery.health_level 字段。
 *
 * <p>【待人工确认】当前项目内业务代码均直接使用字符串字面量（如 "GOOD"）判断，
 * 本枚举未被任何文件引用；因无法确认是否存在外部依赖或后续使用计划，故本次重构予以保留，
 * 请人工确认后再决定是否删除。</p>
 */
@Getter
public enum HealthLevelEnum {
    GOOD("GOOD", "良好"),
    ATTENTION("ATTENTION", "注意"),
    DANGEROUS("DANGEROUS", "危险");

    /** 健康等级编码，与数据库中存储的取值一致 */
    private final String code;

    /** 健康等级中文描述，用于界面展示 */
    private final String description;

    HealthLevelEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按健康等级编码查找枚举项。
     *
     * @param code 健康等级编码，如 "GOOD"
     * @return 匹配的枚举项；无匹配时返回 null
     */
    public static HealthLevelEnum getByCode(String code) {
        for (HealthLevelEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
