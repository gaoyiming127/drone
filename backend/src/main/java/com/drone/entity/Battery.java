package com.drone.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 电池实体，对应数据库表 battery。
 *
 * <p>SOH（健康度）与健康等级由业务层根据标称容量、当前满充容量与鼓包情况实时计算后写入，
 * 使用次数、累计使用时长在登记飞行记录时自动累计。</p>
 */
@Data
@TableName("battery")
public class Battery {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 电池编号，全局唯一 */
    private String batteryCode;

    /** 电池名称 */
    private String batteryName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 序列号 */
    private String serialNumber;

    /** 标称容量（mAh） */
    private BigDecimal ratedCapacity;

    /** 当前满充容量（mAh），随循环次数按配置的衰减率递减，可人工录入最新检测值校准 */
    private BigDecimal fullChargeCapacity;

    /** 循环次数 */
    private Integer cycleCount;

    /** 使用次数，由飞行记录累计，不接受人工修改 */
    private Integer usageCount;

    /** 累计使用时长（分钟），由飞行记录累计，不接受人工修改 */
    private Integer totalUseMinutes;

    /** 健康度 SOH（%）= 当前满充容量 / 标称容量 × 100，上限 100 */
    private BigDecimal soh;

    /** 健康等级：GOOD-良好，ATTENTION-注意，DANGEROUS-危险 */
    private String healthLevel;

    /** 是否鼓包：0-否，1-是；鼓包时健康等级直接判为危险 */
    private Integer swollen;

    /** 是否过热：0-否，1-是 */
    private Integer overheated;

    /** 电池状态：NORMAL-正常，IN_USE-使用中，ABNORMAL-异常，DISABLED-停用 */
    private String status;

    /** 最近一次使用时间，登记飞行记录时更新 */
    private LocalDateTime lastUseTime;

    /** 备注 */
    private String remark;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
