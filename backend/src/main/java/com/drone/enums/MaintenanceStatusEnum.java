package com.drone.enums;

import lombok.Getter;

/**
 * 维修状态枚举：与 maintenance_record.status 字段取值一一对应。
 *
 * <p>【待人工确认】当前项目内业务代码均直接使用字符串字面量（如 "COMPLETED"）判断，
 * 本枚举未被任何文件引用；因无法确认是否存在外部依赖或后续使用计划，故本次重构予以保留，
 * 请人工确认后再决定是否删除。</p>
 */
@Getter
public enum MaintenanceStatusEnum {
    PENDING("PENDING", "待维修"),
    PROCESSING("PROCESSING", "维修中"),
    COMPLETED("COMPLETED", "已完成");

    /** 维修状态编码，与数据库中存储的取值一致 */
    private final String code;

    /** 维修状态中文描述，用于界面展示 */
    private final String description;

    MaintenanceStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按维修状态编码查找枚举项。
     *
     * @param code 维修状态编码，如 "PENDING"
     * @return 匹配的枚举项；无匹配时返回 null
     */
    public static MaintenanceStatusEnum getByCode(String code) {
        for (MaintenanceStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
