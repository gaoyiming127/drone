package com.drone.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维修记录视图对象，在工单本体的基础上补充设备编号/名称与创建人姓名，
 * 供列表查询、详情查询与首页最近记录共用。
 */
@Data
public class MaintenanceRecordVO {

    /** 维修记录ID */
    private Long id;
    /** 维修编号 */
    private String maintenanceCode;
    /** 设备类型：DRONE-无人机，BATTERY-电池 */
    private String deviceType;
    /** 设备ID */
    private Long deviceId;
    /** 设备编号（按设备类型关联查询得到） */
    private String deviceCode;
    /** 设备名称（按设备类型关联查询得到） */
    private String deviceName;
    /** 故障描述 */
    private String faultDescription;
    /** 维修内容 */
    private String maintenanceContent;
    /** 维修日期 */
    private LocalDate maintenanceDate;
    /** 维修费用 */
    private BigDecimal maintenanceCost;
    /** 维修状态：PENDING-待维修，PROCESSING-维修中，COMPLETED-已完成 */
    private String status;
    /** 维修结果：RESTORED-已恢复，SCRAPPED-已报废 */
    private String result;
    /** 备注 */
    private String remark;
    /** 创建人ID */
    private Long createdBy;
    /** 创建人真实姓名（关联查询得到） */
    private String createdByName;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
