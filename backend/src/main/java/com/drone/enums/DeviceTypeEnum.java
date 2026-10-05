package com.drone.enums;

import lombok.Getter;

/**
 * 设备类型枚举：维修记录 maintenance_record.device_type 的取值。
 *
 * <p>【待人工确认】当前项目内业务代码均直接使用字符串字面量（如 "DRONE"）判断，
 * 本枚举未被任何文件引用；因无法确认是否存在外部依赖或后续使用计划，故本次重构予以保留，
 * 请人工确认后再决定是否删除。</p>
 */
@Getter
public enum DeviceTypeEnum {
    DRONE("DRONE", "无人机"),
    BATTERY("BATTERY", "电池");

    /** 设备类型编码，与数据库中存储的取值一致 */
    private final String code;

    /** 设备类型中文描述，用于界面展示 */
    private final String description;

    DeviceTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按类型编码查找枚举项。
     *
     * @param code 类型编码，如 "DRONE"
     * @return 匹配的枚举项；无匹配时返回 null
     */
    public static DeviceTypeEnum getByCode(String code) {
        for (DeviceTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
