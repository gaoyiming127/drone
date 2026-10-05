package com.drone.service.impl;

import com.drone.entity.Battery;
import com.drone.entity.Drone;
import com.drone.entity.MaintenanceRecord;
import com.drone.mapper.*;
import com.drone.service.DashboardService;
import com.drone.vo.DashboardVO;
import com.drone.vo.FlightRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页看板业务实现：统计指标、最近记录、图表数据与系统提醒。
 *
 * <p>提醒规则：SOH 低于 70% 的电池、出现鼓包的电池、循环次数达到 300 次的电池、
 * 超过 180 天未保养的无人机，以及从未保养过的无人机。</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DroneMapper droneMapper;
    private final BatteryMapper batteryMapper;
    private final FlightRecordMapper flightRecordMapper;
    private final FlightRecordBatteryMapper flightRecordBatteryMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    /**
     * 组装首页看板数据。
     *
     * <p>统计类指标（设备总数、本月飞行次数、待维修数量等）与图表数据始终为全系统口径；
     * 仅最近飞行记录明细受 onlyUserId 限制，普通用户只看到本人登记的记录。</p>
     *
     * @param onlyUserId 只返回该使用人员的飞行记录明细，为空表示不限制（管理员）
     * @return 首页看板数据
     */
    @Override
    public DashboardVO getDashboardData(Long onlyUserId) {
        DashboardVO vo = new DashboardVO();

        // 1. 基础统计数据
        vo.setTotalDrones(droneMapper.selectCount(null));
        vo.setIdleDrones(droneMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drone>()
                        .eq(Drone::getStatus, "IDLE")));
        vo.setTotalBatteries(batteryMapper.selectCount(null));
        vo.setDangerousBatteries(batteryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Battery>()
                        .eq(Battery::getHealthLevel, "DANGEROUS")));
        vo.setMonthlyFlightCount(flightRecordMapper.countCurrentMonth());
        vo.setMonthlyFlightMinutes(flightRecordMapper.sumCurrentMonthMinutes());
        vo.setPendingMaintenanceCount(maintenanceRecordMapper.countPending());

        // 2. 最近5条飞行记录（分页与电池明细均在数据库侧完成；普通用户只展示本人的记录）
        List<FlightRecordVO> recentFlights = flightRecordMapper.selectRecentFlightRecordVOs(5, onlyUserId);
        fillBatteryDetails(recentFlights);
        vo.setRecentFlights(recentFlights);

        // 3. 最近5条维修记录
        vo.setRecentMaintenanceRecords(maintenanceRecordMapper.selectRecentMaintenanceRecordVOs(5));

        // 4. 无人机状态饼图
        Map<String, Object> droneStatusPie = new HashMap<>();
        List<Drone> allDrones = droneMapper.selectList(null);
        Map<String, Long> statusCount = allDrones.stream()
                .collect(Collectors.groupingBy(Drone::getStatus, Collectors.counting()));
        droneStatusPie.put("data", statusCount);
        vo.setDroneStatusPie(droneStatusPie);

        // 5. 电池健康等级柱状图
        Map<String, Object> batteryHealthBar = new HashMap<>();
        List<Battery> allBatteries = batteryMapper.selectList(null);
        Map<String, Long> healthCount = allBatteries.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getHealthLevel() != null ? b.getHealthLevel() : "UNKNOWN",
                        Collectors.counting()));
        batteryHealthBar.put("data", healthCount);
        vo.setBatteryHealthBar(batteryHealthBar);

        // 6. 最近6个月飞行次数折线图
        Map<String, Object> monthlyFlightLine = new HashMap<>();
        List<Map<String, Object>> monthlyData = flightRecordMapper.getMonthlyFlightCount(6);
        monthlyFlightLine.put("data", monthlyData);
        vo.setMonthlyFlightLine(monthlyFlightLine);

        // 7. 提醒数据
        List<String> warnings = new ArrayList<>();

        // SOH低于70%的电池
        List<Battery> lowSohBatteries = batteryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Battery>()
                        .isNotNull(Battery::getSoh)
                        .lt(Battery::getSoh, new java.math.BigDecimal("70")));
        for (Battery b : lowSohBatteries) {
            warnings.add("电池 " + b.getBatteryCode() + " SOH仅为 " + b.getSoh() + "%，建议更换");
        }

        // 出现鼓包的电池
        List<Battery> swollenBatteries = batteryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Battery>()
                        .eq(Battery::getSwollen, 1));
        for (Battery b : swollenBatteries) {
            warnings.add("电池 " + b.getBatteryCode() + " 出现鼓包，请立即处理");
        }

        // 循环次数超过300次的电池
        List<Battery> highCycleBatteries = batteryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Battery>()
                        .ge(Battery::getCycleCount, 300));
        for (Battery b : highCycleBatteries) {
            warnings.add("电池 " + b.getBatteryCode() + " 循环次数已达 " + b.getCycleCount() + "次，接近使用寿命");
        }

        // 超过180天没有保养的无人机
        LocalDate sixMonthsAgo = LocalDate.now().minus(180, ChronoUnit.DAYS);
        List<Drone> noMaintenanceDrones = droneMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drone>()
                        .isNotNull(Drone::getLastMaintenanceDate)
                        .lt(Drone::getLastMaintenanceDate, sixMonthsAgo));
        for (Drone d : noMaintenanceDrones) {
            warnings.add("无人机 " + d.getDroneCode() + " 已超过180天未保养（上次保养：" + d.getLastMaintenanceDate() + "）");
        }

        // 从未保养过的无人机
        List<Drone> neverMaintainedDrones = droneMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Drone>()
                        .isNull(Drone::getLastMaintenanceDate));
        for (Drone d : neverMaintainedDrones) {
            warnings.add("无人机 " + d.getDroneCode() + " 从未进行过保养");
        }

        vo.setWarnings(warnings);

        return vo;
    }

    /**
     * 一次查询取出这几条记录的电池明细，避免逐条查询。
     *
     * @param records 待回填的飞行记录列表，结果直接写入各记录的 batteries 字段
     */
    private void fillBatteryDetails(List<FlightRecordVO> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> recordIds = records.stream().map(FlightRecordVO::getId).collect(Collectors.toList());
        Map<Long, List<FlightRecordVO.BatteryUsageVO>> grouped = flightRecordBatteryMapper
                .selectByFlightRecordIds(recordIds)
                .stream()
                .collect(Collectors.groupingBy(FlightRecordBatteryMapper.BatteryUsage::getFlightRecordId,
                        Collectors.mapping(d -> {
                            FlightRecordVO.BatteryUsageVO usage = new FlightRecordVO.BatteryUsageVO();
                            usage.setBatteryId(d.getBatteryId());
                            usage.setBatteryCode(d.getBatteryCode());
                            usage.setBatteryName(d.getBatteryName());
                            usage.setStartPower(d.getStartPower());
                            usage.setEndPower(d.getEndPower());
                            usage.setUseMinutes(d.getUseMinutes());
                            return usage;
                        }, Collectors.toList())));
        records.forEach(record -> record.setBatteries(grouped.getOrDefault(record.getId(), new ArrayList<>())));
    }
}
