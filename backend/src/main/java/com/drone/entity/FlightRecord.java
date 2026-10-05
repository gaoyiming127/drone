package com.drone.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 飞行记录实体，对应数据库表 flight_record。
 *
 * <p>一条飞行记录对应一次飞行任务，可同时使用多块电池，电池明细保存在
 * {@link FlightRecordBattery} 中。</p>
 */
@Data
@TableName("flight_record")
public class FlightRecord {

    /** 主键ID，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 记录编号，由系统按 "FL-日期-6位随机数" 规则生成 */
    private String recordCode;

    /** 使用的无人机ID */
    private Long droneId;

    /** 使用人员（登记人）ID */
    private Long userId;

    /** 飞行地点 */
    private String flightLocation;

    /** 飞行日期 */
    private LocalDate flightDate;

    /** 飞行时长（分钟） */
    private Integer flightMinutes;

    /** 起飞次数 */
    private Integer takeoffCount;

    /** 是否发生异常：0-否，1-是 */
    private Integer hasException;

    /** 异常描述，仅在发生异常时填写 */
    private String exceptionDescription;

    /** 备注 */
    private String remark;

    /** 创建时间，插入时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
