package com.drone.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 无人机实体，对应数据库表 drone。
 *
 * <p>累计飞行次数与累计飞行分钟在登记飞行记录时自动累加；最近保养日期在维修工单完成并判定为
 * "已恢复"时回填，用于首页"超过180天未保养"提醒。</p>
 */
@Data
@TableName("drone")
public class Drone {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备编号，全局唯一 */
    private String droneCode;

    /** 设备名称 */
    private String droneName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 序列号 */
    private String serialNumber;

    /** 购买日期 */
    private LocalDate purchaseDate;

    /** 设备状态：IDLE-空闲，IN_USE-使用中，MAINTENANCE-维修中，DISABLED-停用 */
    private String status;

    /** 累计飞行次数，由飞行记录自动累计 */
    private Integer totalFlightCount;

    /** 累计飞行分钟数，由飞行记录自动累计 */
    private Integer totalFlightMinutes;

    /** 最近一次保养日期，维修完成并恢复时回填 */
    private LocalDate lastMaintenanceDate;

    /** 备注 */
    private String remark;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
