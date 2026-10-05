package com.drone.enums;

import lombok.Getter;

/**
 * 无人机状态枚举：与 drone.status 字段取值一一对应。
 *
 * <p>【待人工确认】当前项目内业务代码均直接使用字符串字面量（如 "IDLE"）判断，
 * 本枚举未被任何文件引用；因无法确认是否存在外部依赖或后续使用计划，故本次重构予以保留，
 * 请人工确认后再决定是否删除。</p>
 */
@Getter
public enum DroneStatusEnum {
    IDLE("IDLE", "空闲"),
    IN_USE("IN_USE", "使用中"),
    MAINTENANCE("MAINTENANCE", "维修中"),
    DISABLED("DISABLED", "停用");

    /** 状态编码，与数据库中存储的取值一致 */
    private final String code;

    /** 状态中文描述，用于界面展示 */
    private final String description;

    DroneStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按状态编码查找枚举项。
     *
     * @param code 状态编码，如 "IDLE"
     * @return 匹配的枚举项；无匹配时返回 null
     */
    public static DroneStatusEnum getByCode(String code) {
        for (DroneStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
