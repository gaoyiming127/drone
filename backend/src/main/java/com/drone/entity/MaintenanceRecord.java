package com.drone.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维修记录实体，对应数据库表 maintenance_record。
 *
 * <p>维修工单同时适用于无人机（device_type = DRONE）与电池（device_type = BATTERY），
 * 通过 device_type + device_id 定位设备；工单完成后按维修结果恢复或停用设备。</p>
 */
@Data
@TableName("maintenance_record")
public class MaintenanceRecord {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 维修编号，由系统按 "MT-日期-6位随机数" 规则生成 */
    private String maintenanceCode;

    /** 设备类型：DRONE-无人机，BATTERY-电池 */
    private String deviceType;

    /** 设备ID，与设备类型共同确定具体设备 */
    private Long deviceId;

    /** 故障描述 */
    private String faultDescription;

    /** 维修内容 */
    private String maintenanceContent;

    /** 维修日期，工单完成时更新为当天 */
    private LocalDate maintenanceDate;

    /** 维修费用，未填写时记为 0 */
    private BigDecimal maintenanceCost;

    /** 维修状态：PENDING-待维修，PROCESSING-维修中，COMPLETED-已完成 */
    private String status;

    /** 维修结果：RESTORED-已恢复，SCRAPPED-已报废，未完成时为空 */
    private String result;

    /** 备注 */
    private String remark;

    /** 创建人（登记工单的用户）ID */
    private Long createdBy;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
