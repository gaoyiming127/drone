package com.drone.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 首页统计视图对象，一次性返回首页所需的统计指标、最近记录、图表数据与系统提醒。
 */
@Data
public class DashboardVO {

    // 统计数据
    /** 无人机总数 */
    private long totalDrones;
    /** 空闲无人机数量（状态为 IDLE） */
    private long idleDrones;
    /** 电池总数 */
    private long totalBatteries;
    /** 危险电池数量（健康等级为 DANGEROUS） */
    private long dangerousBatteries;
    /** 本月飞行次数 */
    private long monthlyFlightCount;
    /** 本月飞行总时长（分钟） */
    private long monthlyFlightMinutes;
    /** 待维修工单数量（状态为 PENDING） */
    private long pendingMaintenanceCount;

    // 最近记录
    /** 最近若干条飞行记录，普通用户仅包含本人登记的记录 */
    private List<FlightRecordVO> recentFlights;
    /** 最近若干条维修记录 */
    private List<MaintenanceRecordVO> recentMaintenanceRecords;

    // 图表数据
    /** 无人机状态分布饼图数据，data 为“状态 -> 数量”的映射 */
    private Map<String, Object> droneStatusPie;
    /** 电池健康等级柱状图数据，data 为“健康等级 -> 数量”的映射 */
    private Map<String, Object> batteryHealthBar;
    /** 近 6 个月飞行次数折线图数据，data 为按月统计的列表 */
    private Map<String, Object> monthlyFlightLine;

    // 提醒数据
    /** 系统提醒文案列表（低 SOH、鼓包、高循环次数、超期未保养等） */
    private List<String> warnings;
}
