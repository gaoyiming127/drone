package com.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.dto.FlightRecordBatteryDto;
import com.drone.dto.FlightRecordDto;
import com.drone.entity.*;
import com.drone.exception.BusinessException;
import com.drone.mapper.*;
import com.drone.service.FlightRecordService;
import com.drone.service.BatteryService;
import com.drone.vo.FlightRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 飞行记录业务实现。
 *
 * <p>登记记录时会连带更新无人机与电池的累计数据：无人机的累计飞行次数、累计飞行分钟并置为使用中；
 * 每块电池的使用次数、累计使用时长与最近使用时间；当某块电池本次耗电量达到 70% 时循环次数加一，
 * 并触发满充容量衰减与 SOH 重算。</p>
 */
@Service
@RequiredArgsConstructor
public class FlightRecordServiceImpl implements FlightRecordService {

    private final FlightRecordMapper flightRecordMapper;
    private final FlightRecordBatteryMapper flightRecordBatteryMapper;
    private final DroneMapper droneMapper;
    private final BatteryMapper batteryMapper;
    private final UserMapper userMapper;
    private final BatteryService batteryService;

    /**
     * 登记飞行记录。
     *
     * <p>流程：校验无人机与电池可用性 → 写入飞行记录 → 写入电池明细 →
     * 更新无人机累计数据并置为使用中 → 更新每块电池的累计数据（必要时衰减容量）→ 返回完整记录。
     * 全过程在同一事务内，任一步失败都会回滚。</p>
     *
     * @param dto           飞行记录信息（含电池使用明细）
     * @param currentUserId 当前登录用户ID，普通用户只能登记本人的飞行记录
     * @param admin         当前用户是否为管理员
     * @return 新增后的飞行记录详情
     * @throws BusinessException 无人机或电池不存在、已停用、维修中，或越权登记时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlightRecordVO createFlightRecord(FlightRecordDto dto, Long currentUserId, boolean admin) {
        // 1. 校验无人机和电池
        Drone drone = droneMapper.selectById(dto.getDroneId());
        if (drone == null) {
            throw new BusinessException("无人机不存在");
        }
        if ("DISABLED".equals(drone.getStatus())) {
            throw new BusinessException("无人机 " + drone.getDroneCode() + " 已停用，无法登记飞行记录");
        }
        if ("MAINTENANCE".equals(drone.getStatus())) {
            throw new BusinessException("无人机 " + drone.getDroneCode() + " 正在维修中，无法登记飞行记录");
        }

        // 普通用户只能登记本人执行的飞行
        if (!admin && !dto.getUserId().equals(currentUserId)) {
            throw new BusinessException("普通用户只能登记本人的飞行记录");
        }
        if (userMapper.selectById(dto.getUserId()) == null) {
            throw new BusinessException("使用人员不存在");
        }

        // 2. 创建飞行记录
        FlightRecord record = new FlightRecord();
        record.setRecordCode(generateRecordCode());
        record.setDroneId(dto.getDroneId());
        record.setUserId(dto.getUserId());
        record.setFlightLocation(dto.getFlightLocation());
        record.setFlightDate(dto.getFlightDate());
        record.setFlightMinutes(dto.getFlightMinutes());
        record.setTakeoffCount(dto.getTakeoffCount() != null ? dto.getTakeoffCount() : 1);
        record.setHasException(dto.getHasException() != null ? dto.getHasException() : 0);
        record.setExceptionDescription(dto.getExceptionDescription());
        record.setRemark(dto.getRemark());

        flightRecordMapper.insert(record);

        // 3. 保存电池使用记录
        if (dto.getBatteries() != null && !dto.getBatteries().isEmpty()) {
            for (FlightRecordBatteryDto batteryDto : dto.getBatteries()) {
                Battery battery = batteryMapper.selectById(batteryDto.getBatteryId());
                if (battery == null) {
                    throw new BusinessException("电池ID " + batteryDto.getBatteryId() + " 不存在");
                }
                if ("DISABLED".equals(battery.getStatus())) {
                    throw new BusinessException("电池 " + battery.getBatteryCode() + " 已停用，无法用于飞行记录");
                }

                FlightRecordBattery frb = new FlightRecordBattery();
                frb.setFlightRecordId(record.getId());
                frb.setBatteryId(batteryDto.getBatteryId());
                frb.setStartPower(batteryDto.getStartPower());
                frb.setEndPower(batteryDto.getEndPower());
                frb.setUseMinutes(batteryDto.getUseMinutes());
                flightRecordBatteryMapper.insert(frb);
            }
        }

        // 4. 更新无人机累计数据
        drone.setTotalFlightCount(drone.getTotalFlightCount() + 1);
        drone.setTotalFlightMinutes(drone.getTotalFlightMinutes() + dto.getFlightMinutes());
        drone.setStatus("IN_USE");
        droneMapper.updateById(drone);

        // 5. 更新每块电池的累计数据
        if (dto.getBatteries() != null && !dto.getBatteries().isEmpty()) {
            for (FlightRecordBatteryDto batteryDto : dto.getBatteries()) {
                Battery battery = batteryMapper.selectById(batteryDto.getBatteryId());

                battery.setUsageCount(battery.getUsageCount() + 1);
                battery.setTotalUseMinutes(battery.getTotalUseMinutes() + batteryDto.getUseMinutes());

                // 当本次耗电量大于等于70%时，循环次数增加1，并据此衰减当前满充容量
                BigDecimal powerDrop = batteryDto.getStartPower().subtract(batteryDto.getEndPower());
                if (powerDrop.compareTo(new BigDecimal("70")) >= 0) {
                    battery.setCycleCount(battery.getCycleCount() + 1);
                    batteryService.applyCycleDecay(battery);
                }

                battery.setLastUseTime(LocalDateTime.now());

                // 重新计算SOH
                batteryService.calculateSoh(battery);
                batteryMapper.updateById(battery);
            }
        }

        // 6. 返回完整记录
        return loadFlightRecordVO(record.getId());
    }

    /**
     * 分页查询飞行记录。
     *
     * <p>页码与每页条数会先做边界修正（页码最小 1，每页 1~100 条）；
     * 日期条件按"年"、"年-月"、"年-月-日"解析为区间交由数据库按索引过滤，
     * 无法识别的输入退化为模糊匹配。</p>
     *
     * @param pageNum    页码，从 1 开始
     * @param pageSize   每页条数，上限 100
     * @param droneCode  无人机编号，模糊匹配，可为空
     * @param userName   使用人员（登录名或姓名），模糊匹配，可为空
     * @param flightDate 飞行日期条件，可为空
     * @param onlyUserId 只返回该使用人员的记录，为空表示不限制（管理员）
     * @return 分页结果，含电池使用明细
     */
    @Override
    public IPage<FlightRecordVO> pageFlightRecords(int pageNum, int pageSize, String droneCode, String userName,
                                                    String flightDate, Long onlyUserId) {
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(Math.max(1, pageSize), 100);

        // 日期条件解析为区间，交由数据库按索引过滤；无法识别的输入退化为模糊匹配
        LocalDate startDate = null;
        LocalDate endDate = null;
        String dateLike = null;
        if (StringUtils.hasText(flightDate)) {
            String date = flightDate.trim();
            if (date.matches("\\d{4}")) {
                startDate = LocalDate.parse(date + "-01-01");
                endDate = LocalDate.parse(date + "-12-31");
            } else if (date.matches("\\d{4}-\\d{2}")) {
                LocalDate month = LocalDate.parse(date + "-01");
                startDate = month.withDayOfMonth(1);
                endDate = month.withDayOfMonth(month.lengthOfMonth());
            } else if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                startDate = LocalDate.parse(date);
                endDate = startDate;
            } else {
                dateLike = date;
            }
        }

        long total = flightRecordMapper.countFlightRecordVOs(droneCode, userName, startDate, endDate, dateLike, onlyUserId);
        List<FlightRecordVO> records = total == 0
                ? new ArrayList<>()
                : flightRecordMapper.selectFlightRecordVOsByPage(droneCode, userName, startDate, endDate, dateLike,
                        onlyUserId, (pageNum - 1) * pageSize, pageSize);
        fillBatteryDetails(records);

        Page<FlightRecordVO> resultPage = new Page<>(pageNum, pageSize, total);
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 查询飞行记录详情，含无人机、使用人员与电池使用明细。
     *
     * @param id            飞行记录ID
     * @param currentUserId 当前登录用户ID
     * @param admin         当前用户是否为管理员
     * @return 飞行记录详情
     * @throws BusinessException 记录不存在或普通用户查看他人记录时抛出
     */
    @Override
    public FlightRecordVO getFlightRecordById(Long id, Long currentUserId, boolean admin) {
        FlightRecord record = flightRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("飞行记录不存在");
        }
        // 普通用户只能查看本人登记的记录
        if (!admin && !record.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能查看本人登记的飞行记录");
        }
        return loadFlightRecordVO(id);
    }

    /**
     * 按ID组装飞行记录视图对象。
     *
     * @param id 飞行记录ID
     * @return 飞行记录详情（含无人机、使用人员与电池明细）
     * @throws BusinessException 记录不存在时抛出
     */
    private FlightRecordVO loadFlightRecordVO(Long id) {
        FlightRecord record = flightRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("飞行记录不存在");
        }

        FlightRecordVO vo = new FlightRecordVO();
        vo.setId(record.getId());
        vo.setRecordCode(record.getRecordCode());
        vo.setDroneId(record.getDroneId());
        vo.setUserId(record.getUserId());
        vo.setFlightLocation(record.getFlightLocation());
        vo.setFlightDate(record.getFlightDate());
        vo.setFlightMinutes(record.getFlightMinutes());
        vo.setTakeoffCount(record.getTakeoffCount());
        vo.setHasException(record.getHasException());
        vo.setExceptionDescription(record.getExceptionDescription());
        vo.setRemark(record.getRemark());
        vo.setCreatedAt(record.getCreatedAt());

        // 无人机信息
        Drone drone = droneMapper.selectById(record.getDroneId());
        if (drone != null) {
            vo.setDroneCode(drone.getDroneCode());
            vo.setDroneName(drone.getDroneName());
        }

        // 用户信息
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setUserRealName(user.getRealName());
        }

        // 电池信息
        loadBatteryDetails(vo);

        return vo;
    }

    /**
     * 删除飞行记录：仅删除记录本身与电池明细，不回滚无人机与电池的累计数据。
     *
     * @param id            飞行记录ID
     * @param currentUserId 当前登录用户ID，普通用户只能删除本人登记的飞行记录
     * @param admin         当前用户是否为管理员
     * @throws BusinessException 记录不存在或普通用户删除他人记录时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFlightRecord(Long id, Long currentUserId, boolean admin) {
        FlightRecord record = flightRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("飞行记录不存在");
        }
        // 普通用户只能删除本人的飞行记录
        if (!admin && !record.getUserId().equals(currentUserId)) {
            throw new BusinessException("只能删除本人登记的飞行记录");
        }
        // 明细行由数据库外键级联删除，此处显式删除以兼容未建外键的环境
        flightRecordBatteryMapper.delete(new LambdaQueryWrapper<FlightRecordBattery>()
                .eq(FlightRecordBattery::getFlightRecordId, id));
        flightRecordMapper.deleteById(id);
    }

    /**
     * 查询并回填单条记录的电池明细。
     *
     * @param vo 待回填的飞行记录视图对象
     */
    private void loadBatteryDetails(FlightRecordVO vo) {
        vo.setBatteries(flightRecordBatteryMapper.selectByFlightRecordId(vo.getId())
                .stream()
                .map(this::toBatteryUsageVO)
                .collect(Collectors.toList()));
    }

    /** 一次查询取出本页所有记录的电池明细，按记录分组后回填，避免逐条查询 */
    private void fillBatteryDetails(List<FlightRecordVO> records) {
        if (records.isEmpty()) {
            return;
        }
        List<Long> recordIds = records.stream().map(FlightRecordVO::getId).collect(Collectors.toList());
        Map<Long, List<FlightRecordVO.BatteryUsageVO>> grouped = flightRecordBatteryMapper
                .selectByFlightRecordIds(recordIds)
                .stream()
                .collect(Collectors.groupingBy(FlightRecordBatteryMapper.BatteryUsage::getFlightRecordId,
                        Collectors.mapping(this::toBatteryUsageVO, Collectors.toList())));
        records.forEach(record -> record.setBatteries(grouped.getOrDefault(record.getId(), new ArrayList<>())));
    }

    /**
     * 把数据库查询结果转换为电池使用明细视图对象。
     *
     * @param detail 电池明细查询结果
     * @return 电池使用明细视图对象
     */
    private FlightRecordVO.BatteryUsageVO toBatteryUsageVO(FlightRecordBatteryMapper.BatteryUsage detail) {
        FlightRecordVO.BatteryUsageVO usage = new FlightRecordVO.BatteryUsageVO();
        usage.setBatteryId(detail.getBatteryId());
        usage.setBatteryCode(detail.getBatteryCode());
        usage.setBatteryName(detail.getBatteryName());
        usage.setStartPower(detail.getStartPower());
        usage.setEndPower(detail.getEndPower());
        usage.setUseMinutes(detail.getUseMinutes());
        return usage;
    }

    /**
     * 生成飞行记录编号，格式为 "FL-日期-6位随机数"。
     *
     * @return 记录编号
     */
    private String generateRecordCode() {
        // 原实现取时间戳后四位，每 10 秒就会重复一次，会与记录编号唯一约束冲突导致登记失败
        return "FL-" + java.time.LocalDate.now() + "-" + String.format("%06d", java.util.concurrent.ThreadLocalRandom.current().nextInt(1000000));
    }
}
