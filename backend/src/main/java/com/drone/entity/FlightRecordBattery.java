package com.drone.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 飞行记录-电池使用明细实体，对应数据库表 flight_record_battery。
 *
 * <p>一条记录描述某块电池在本次飞行中的电量与使用时长；本次耗电量（开始电量 - 结束电量）
 * 达到 70% 时，业务层会为该电池的循环次数加一并触发容量衰减。</p>
 */
@Data
@TableName("flight_record_battery")
public class FlightRecordBattery {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属飞行记录ID */
    private Long flightRecordId;

    /** 使用的电池ID */
    private Long batteryId;

    /** 开始电量（%） */
    private BigDecimal startPower;

    /** 结束电量（%） */
    private BigDecimal endPower;

    /** 本次使用时长（分钟） */
    private Integer useMinutes;
}
