package com.drone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 电池新增/修改请求参数。
 *
 * <p>SOH 与健康等级不在此传入，由业务层依据标称容量、当前满充容量与鼓包情况计算。</p>
 */
@Data
public class BatteryDto {

    /** 电池编号，必填且全局唯一 */
    @NotBlank(message = "电池编号不能为空")
    private String batteryCode;

    /** 电池名称，必填 */
    @NotBlank(message = "电池名称不能为空")
    private String batteryName;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 序列号 */
    private String serialNumber;

    /** 标称容量（mAh），必填，须大于 0 才能参与 SOH 计算 */
    @NotNull(message = "标称容量不能为空")
    private BigDecimal ratedCapacity;

    /** 当前满充容量（mAh），必填；新增时录入初始值，修改时可录入最新检测值进行校准 */
    @NotNull(message = "当前满充容量不能为空")
    private BigDecimal fullChargeCapacity;

    /** 循环次数，为空时按 0 处理；可人工修正 */
    private Integer cycleCount;

    /** 使用次数，为空时按 0 处理；仅新增时生效，后续由飞行记录自动累计 */
    private Integer usageCount;

    /** 累计使用时长（分钟），为空时按 0 处理；仅新增时生效，后续由飞行记录自动累计 */
    private Integer totalUseMinutes;

    /** 是否鼓包：0-否，1-是，为空时按 0 处理 */
    private Integer swollen;

    /** 是否过热：0-否，1-是，为空时按 0 处理 */
    private Integer overheated;

    /** 电池状态，为空时按 NORMAL 处理 */
    private String status;

    /** 备注 */
    private String remark;
}
