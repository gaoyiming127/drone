package com.drone.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 飞行记录视图对象，在记录本体的基础上补充无人机编号/名称、使用人员姓名与电池使用明细，
 * 供列表查询、详情查询与首页最近记录共用。
 */
@Data
public class FlightRecordVO {

    /** 飞行记录ID */
    private Long id;
    /** 记录编号 */
    private String recordCode;
    /** 无人机ID */
    private Long droneId;
    /** 无人机编号（关联查询得到，无人机被删除时为空） */
    private String droneCode;
    /** 无人机名称（关联查询得到） */
    private String droneName;
    /** 使用人员ID */
    private Long userId;
    /** 使用人员登录名（关联查询得到） */
    private String userName;
    /** 使用人员真实姓名（关联查询得到） */
    private String userRealName;
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
    /** 异常描述 */
    private String exceptionDescription;
    /** 备注 */
    private String remark;
    /** 记录创建时间 */
    private LocalDateTime createdAt;

    /** 本次飞行使用的电池明细 */
    private List<BatteryUsageVO> batteries;

    /**
     * 飞行记录中的单块电池使用明细。
     */
    @Data
    public static class BatteryUsageVO {
        /** 电池ID */
        private Long batteryId;
        /** 电池编号 */
        private String batteryCode;
        /** 电池名称 */
        private String batteryName;
        /** 开始电量（%） */
        private BigDecimal startPower;
        /** 结束电量（%） */
        private BigDecimal endPower;
        /** 本次使用时长（分钟） */
        private Integer useMinutes;
    }
}
