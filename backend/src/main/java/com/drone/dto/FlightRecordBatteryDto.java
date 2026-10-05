package com.drone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 飞行记录中单块电池的使用信息，作为 {@link FlightRecordDto#getBatteries()} 的元素。
 */
@Data
public class FlightRecordBatteryDto {

    /** 使用的电池ID，必填 */
    @NotNull(message = "电池ID不能为空")
    private Long batteryId;

    /** 开始电量（%），必填 */
    @NotNull(message = "开始电量不能为空")
    private BigDecimal startPower;

    /** 结束电量（%），必填；与开始电量之差达到 70% 时该电池循环次数加一并触发容量衰减 */
    @NotNull(message = "结束电量不能为空")
    private BigDecimal endPower;

    /** 本次使用时长（分钟），必填，同时累加到电池的累计使用时长 */
    @NotNull(message = "使用分钟数不能为空")
    private Integer useMinutes;
}
