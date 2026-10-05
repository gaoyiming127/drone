package com.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.dto.BatteryDto;
import com.drone.entity.Battery;
import com.drone.entity.FlightRecordBattery;
import com.drone.entity.MaintenanceRecord;
import com.drone.exception.BusinessException;
import com.drone.mapper.BatteryMapper;
import com.drone.mapper.FlightRecordBatteryMapper;
import com.drone.mapper.MaintenanceRecordMapper;
import com.drone.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 电池管理业务实现。
 *
 * <p>容量衰减幅度与容量下限通过配置项控制：
 * app.battery.decay-rate-per-cycle（每循环衰减率）、app.battery.min-capacity-ratio（容量下限比例）。</p>
 */
@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    private final BatteryMapper batteryMapper;
    private final FlightRecordBatteryMapper flightRecordBatteryMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    /** 每循环容量衰减率，默认 0.05% */
    @Value("${app.battery.decay-rate-per-cycle:0.0005}")
    private BigDecimal decayRatePerCycle;

    /** 容量衰减下限（相对标称容量的比例），默认 20% */
    @Value("${app.battery.min-capacity-ratio:0.2}")
    private BigDecimal minCapacityRatio;

    /**
     * 分页查询电池列表，过滤条件均按需拼接，结果按创建时间倒序。
     *
     * @param pageNum     页码，从 1 开始
     * @param pageSize    每页条数
     * @param batteryCode 电池编号，模糊匹配，可为空
     * @param model       型号，模糊匹配，可为空
     * @param healthLevel 健康等级，精确匹配，可为空
     * @param status      电池状态，精确匹配，可为空
     * @return 分页结果
     */
    @Override
    public IPage<Battery> pageBatteries(int pageNum, int pageSize, String batteryCode, String model, String healthLevel, String status) {
        Page<Battery> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Battery> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(batteryCode)) {
            wrapper.like(Battery::getBatteryCode, batteryCode);
        }
        if (StringUtils.hasText(model)) {
            wrapper.like(Battery::getModel, model);
        }
        if (StringUtils.hasText(healthLevel)) {
            wrapper.eq(Battery::getHealthLevel, healthLevel);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Battery::getStatus, status);
        }

        wrapper.orderByDesc(Battery::getCreatedAt);
        return batteryMapper.selectPage(page, wrapper);
    }

    /**
     * 按ID查询电池。
     *
     * @param id 电池ID
     * @return 电池信息
     * @throws BusinessException 电池不存在时抛出
     */
    @Override
    public Battery getBatteryById(Long id) {
        Battery battery = batteryMapper.selectById(id);
        if (battery == null) {
            throw new BusinessException("电池不存在");
        }
        return battery;
    }

    /**
     * 新增电池：先校验编号唯一，再写入各字段（数值型字段为空时取默认值），
     * 最后计算 SOH 与健康等级。
     *
     * @param batteryDto 电池信息
     * @return 新增后的电池
     * @throws BusinessException 电池编号已存在时抛出
     */
    @Override
    public Battery createBattery(BatteryDto batteryDto) {
        Long count = batteryMapper.selectCount(new LambdaQueryWrapper<Battery>()
                .eq(Battery::getBatteryCode, batteryDto.getBatteryCode()));
        if (count > 0) {
            throw new BusinessException("电池编号已存在");
        }

        Battery battery = new Battery();
        battery.setBatteryCode(batteryDto.getBatteryCode());
        battery.setBatteryName(batteryDto.getBatteryName());
        battery.setBrand(batteryDto.getBrand());
        battery.setModel(batteryDto.getModel());
        battery.setSerialNumber(batteryDto.getSerialNumber());
        battery.setRatedCapacity(batteryDto.getRatedCapacity());
        battery.setFullChargeCapacity(batteryDto.getFullChargeCapacity());
        battery.setCycleCount(batteryDto.getCycleCount() != null ? batteryDto.getCycleCount() : 0);
        battery.setUsageCount(batteryDto.getUsageCount() != null ? batteryDto.getUsageCount() : 0);
        battery.setTotalUseMinutes(batteryDto.getTotalUseMinutes() != null ? batteryDto.getTotalUseMinutes() : 0);
        battery.setSwollen(batteryDto.getSwollen() != null ? batteryDto.getSwollen() : 0);
        battery.setOverheated(batteryDto.getOverheated() != null ? batteryDto.getOverheated() : 0);
        battery.setStatus(batteryDto.getStatus() != null ? batteryDto.getStatus() : "NORMAL");
        battery.setRemark(batteryDto.getRemark());

        // 计算SOH和健康等级
        calculateSoh(battery);

        batteryMapper.insert(battery);
        return battery;
    }

    /**
     * 修改电池：编号变更时校验唯一性，容量、鼓包、过热、循环次数、状态等字段为空时保持原值，
     * 修改后重新计算 SOH 与健康等级。
     *
     * @param id         电池ID
     * @param batteryDto 待修改的电池信息
     * @return 修改后的电池
     * @throws BusinessException 电池不存在或新编号已被占用时抛出
     */
    @Override
    public Battery updateBattery(Long id, BatteryDto batteryDto) {
        Battery battery = getBatteryById(id);

        if (StringUtils.hasText(batteryDto.getBatteryCode()) && !batteryDto.getBatteryCode().equals(battery.getBatteryCode())) {
            Long count = batteryMapper.selectCount(new LambdaQueryWrapper<Battery>()
                    .eq(Battery::getBatteryCode, batteryDto.getBatteryCode())
                    .ne(Battery::getId, id));
            if (count > 0) {
                throw new BusinessException("电池编号已存在");
            }
            battery.setBatteryCode(batteryDto.getBatteryCode());
        }

        battery.setBatteryName(batteryDto.getBatteryName());
        battery.setBrand(batteryDto.getBrand());
        battery.setModel(batteryDto.getModel());
        battery.setSerialNumber(batteryDto.getSerialNumber());

        if (batteryDto.getRatedCapacity() != null) {
            battery.setRatedCapacity(batteryDto.getRatedCapacity());
        }
        if (batteryDto.getFullChargeCapacity() != null) {
            battery.setFullChargeCapacity(batteryDto.getFullChargeCapacity());
        }
        if (batteryDto.getSwollen() != null) {
            battery.setSwollen(batteryDto.getSwollen());
        }
        if (batteryDto.getOverheated() != null) {
            battery.setOverheated(batteryDto.getOverheated());
        }
        // 循环次数允许在新增时录入初始值并可在编辑时修正，使用次数与累计使用时长由飞行记录自动累计，不接受修改
        if (batteryDto.getCycleCount() != null) {
            battery.setCycleCount(batteryDto.getCycleCount());
        }
        if (StringUtils.hasText(batteryDto.getStatus())) {
            battery.setStatus(batteryDto.getStatus());
        }
        battery.setRemark(batteryDto.getRemark());

        // 重新计算SOH
        calculateSoh(battery);

        batteryMapper.updateById(battery);
        return battery;
    }

    /**
     * 删除电池：已被飞行记录使用或存在维修记录时拒绝删除。
     *
     * @param id 电池ID
     * @throws BusinessException 电池不存在、已被飞行记录使用或存在维修记录时抛出
     */
    @Override
    public void deleteBattery(Long id) {
        getBatteryById(id);

        // 存在业务记录时禁止删除，避免外键报错与维修记录中的设备信息悬空
        Long usageCount = flightRecordBatteryMapper.selectCount(new LambdaQueryWrapper<FlightRecordBattery>()
                .eq(FlightRecordBattery::getBatteryId, id));
        if (usageCount > 0) {
            throw new BusinessException("该电池已被飞行记录使用，无法删除");
        }
        Long maintenanceCount = maintenanceRecordMapper.selectCount(new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getDeviceType, "BATTERY")
                .eq(MaintenanceRecord::getDeviceId, id));
        if (maintenanceCount > 0) {
            throw new BusinessException("该电池存在维修记录，无法删除");
        }

        batteryMapper.deleteById(id);
    }

    /**
     * 循环次数增加时对当前满充容量做衰减。
     *
     * <p>衰减幅度 = 标称容量 × 衰减率（保留两位小数）；衰减结果不低于
     * 标称容量 × 容量下限比例。标称容量或当前容量缺失、标称容量非正数时不处理。</p>
     *
     * @param battery 待衰减的电池对象，结果直接写回该对象
     */
    @Override
    public void applyCycleDecay(Battery battery) {
        BigDecimal rated = battery.getRatedCapacity();
        BigDecimal current = battery.getFullChargeCapacity();
        if (rated == null || current == null || rated.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // 每增加一次循环，容量按标称容量的固定比例线性衰减
        BigDecimal decay = rated.multiply(decayRatePerCycle).setScale(2, RoundingMode.HALF_UP);
        BigDecimal minCapacity = rated.multiply(minCapacityRatio).setScale(2, RoundingMode.HALF_UP);

        BigDecimal decayed = current.subtract(decay);
        battery.setFullChargeCapacity(decayed.compareTo(minCapacity) < 0 ? minCapacity : decayed);
    }

    /**
     * 依据标称容量与当前满充容量计算健康度 SOH 与健康等级。
     *
     * <p>SOH = 当前满充容量 / 标称容量 × 100（保留两位小数），超过 100% 按 100% 处理；
     * 健康等级判定：SOH ≥ 85% 且未鼓包为 GOOD，70% ≤ SOH &lt; 85% 为 ATTENTION，
     * 其余（含鼓包）为 DANGEROUS。标称容量或当前容量缺失、标称容量非正数时不计算。</p>
     *
     * @param battery 待计算的电池对象，结果直接写回该对象
     */
    @Override
    public void calculateSoh(Battery battery) {
        if (battery.getRatedCapacity() == null || battery.getFullChargeCapacity() == null
                || battery.getRatedCapacity().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // SOH = 当前满充容量 / 标称容量 × 100
        BigDecimal soh = battery.getFullChargeCapacity()
                .divide(battery.getRatedCapacity(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        // 满充容量高于标称容量的异常录入按 100% 计，避免超出 soh 字段的取值范围导致保存失败
        if (soh.compareTo(new BigDecimal("100")) > 0) {
            soh = new BigDecimal("100.00");
        }

        battery.setSoh(soh);

        // 健康等级判定
        boolean isSwollen = battery.getSwollen() != null && battery.getSwollen() == 1;

        if (soh.compareTo(new BigDecimal("85")) >= 0 && !isSwollen) {
            battery.setHealthLevel("GOOD");
        } else if (soh.compareTo(new BigDecimal("70")) >= 0 && soh.compareTo(new BigDecimal("85")) < 0) {
            battery.setHealthLevel("ATTENTION");
        } else {
            battery.setHealthLevel("DANGEROUS");
        }
    }
}
