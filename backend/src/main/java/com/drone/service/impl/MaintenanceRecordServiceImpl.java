package com.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.dto.MaintenanceRecordDto;
import com.drone.entity.Battery;
import com.drone.entity.Drone;
import com.drone.entity.MaintenanceRecord;
import com.drone.exception.BusinessException;
import com.drone.mapper.BatteryMapper;
import com.drone.mapper.DroneMapper;
import com.drone.mapper.MaintenanceRecordMapper;
import com.drone.mapper.UserMapper;
import com.drone.service.MaintenanceRecordService;
import com.drone.vo.MaintenanceRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 维修工单业务实现。
 *
 * <p>工单状态与设备状态联动：新增工单把设备置为维修中/异常；完成工单时按维修结果
 * 恢复设备（无人机置空闲、电池置正常，并回填无人机的最近保养日期）或停用设备；
 * 删除未完成工单且设备无其它未完成工单时恢复设备状态。</p>
 */
@Service
@RequiredArgsConstructor
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordMapper maintenanceRecordMapper;
    private final DroneMapper droneMapper;
    private final BatteryMapper batteryMapper;
    private final UserMapper userMapper;

    /**
     * 分页查询维修记录。
     *
     * <p>页码与每页条数会先做边界修正（页码最小 1，每页 1~100 条），
     * 过滤与分页由数据库完成，避免全表加载到内存。</p>
     *
     * @param pageNum    页码，从 1 开始
     * @param pageSize   每页条数，上限 100
     * @param deviceType 设备类型，精确匹配，可为空
     * @param status     维修状态，精确匹配，可为空
     * @return 分页结果
     */
    @Override
    public IPage<MaintenanceRecordVO> pageMaintenanceRecords(int pageNum, int pageSize, String deviceType, String status) {
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(Math.max(1, pageSize), 100);

        // 过滤与分页由数据库完成，避免全表加载到内存
        long total = maintenanceRecordMapper.countMaintenanceRecordVOs(deviceType, status);
        List<MaintenanceRecordVO> pageList = total == 0
                ? List.of()
                : maintenanceRecordMapper.selectMaintenanceRecordVOsByPage(deviceType, status,
                        (pageNum - 1) * pageSize, pageSize);

        Page<MaintenanceRecordVO> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pageList);
        return page;
    }

    /**
     * 按ID查询维修记录详情。
     *
     * @param id 维修记录ID
     * @return 维修记录详情
     * @throws BusinessException 记录不存在时抛出
     */
    @Override
    public MaintenanceRecordVO getMaintenanceRecordById(Long id) {
        MaintenanceRecord record = maintenanceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("维修记录不存在");
        }
        return convertToVO(record);
    }

    /**
     * 新增维修工单：状态固定为待维修、费用为空时记 0，并把设备置为维修中/异常。
     *
     * @param dto    工单信息
     * @param userId 创建人（当前登录用户）ID
     * @return 新增后的工单详情
     * @throws BusinessException 设备类型不正确或设备不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceRecordVO createMaintenanceRecord(MaintenanceRecordDto dto, Long userId) {
        // 校验设备
        validateDevice(dto.getDeviceType(), dto.getDeviceId());

        MaintenanceRecord record = new MaintenanceRecord();
        record.setMaintenanceCode(generateMaintenanceCode());
        record.setDeviceType(dto.getDeviceType());
        record.setDeviceId(dto.getDeviceId());
        record.setFaultDescription(dto.getFaultDescription());
        record.setMaintenanceContent(dto.getMaintenanceContent());
        record.setMaintenanceDate(dto.getMaintenanceDate());
        record.setMaintenanceCost(dto.getMaintenanceCost() != null ? dto.getMaintenanceCost() : BigDecimal.ZERO);
        record.setStatus("PENDING");
        record.setRemark(dto.getRemark());
        record.setCreatedBy(userId);

        maintenanceRecordMapper.insert(record);

        // 更新设备状态
        updateDeviceStatus(dto.getDeviceType(), dto.getDeviceId(), "MAINTENANCE", "ABNORMAL");

        return convertToVO(record);
    }

    /**
     * 修改未完成的维修工单，设备类型与设备本身不可更改。
     *
     * @param id  工单ID
     * @param dto 待修改的工单信息
     * @return 修改后的工单详情
     * @throws BusinessException 记录不存在或工单已完成时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceRecordVO updateMaintenanceRecord(Long id, MaintenanceRecordDto dto) {
        MaintenanceRecord record = maintenanceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("维修记录不存在");
        }
        if ("COMPLETED".equals(record.getStatus())) {
            throw new BusinessException("已完成维修无法修改");
        }

        record.setFaultDescription(dto.getFaultDescription());
        record.setMaintenanceContent(dto.getMaintenanceContent());
        record.setMaintenanceCost(dto.getMaintenanceCost() != null ? dto.getMaintenanceCost() : BigDecimal.ZERO);
        record.setMaintenanceDate(dto.getMaintenanceDate());
        record.setRemark(dto.getRemark());

        maintenanceRecordMapper.updateById(record);
        return convertToVO(record);
    }

    /**
     * 完成维修：状态置为已完成，维修日期取当天，并按维修结果更新设备状态。
     *
     * <p>结果为恢复时，无人机置为空闲、电池置为正常，同时回填无人机的最近保养日期；
     * 结果为报废时设备置为停用。</p>
     *
     * @param id                 工单ID
     * @param result             维修结果：RESTORED-恢复，SCRAPPED-报废
     * @param maintenanceContent 维修内容，为空时不覆盖原值
     * @param cost               维修费用，为空时不覆盖原值
     * @return 完成后的工单详情
     * @throws BusinessException 记录不存在、工单已完成或维修结果不合法时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceRecordVO completeMaintenance(Long id, String result, String maintenanceContent, BigDecimal cost) {
        MaintenanceRecord record = maintenanceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("维修记录不存在");
        }
        if ("COMPLETED".equals(record.getStatus())) {
            throw new BusinessException("该维修记录已完成");
        }
        // 结果必须是系统支持的处置结果，否则会出现"工单已完成但设备状态无人处理"
        if (!"RESTORED".equals(result) && !"SCRAPPED".equals(result)) {
            throw new BusinessException("维修结果只能是恢复或报废");
        }

        record.setStatus("COMPLETED");
        record.setResult(result);
        record.setMaintenanceDate(LocalDate.now());
        if (StringUtils.hasText(maintenanceContent)) {
            record.setMaintenanceContent(maintenanceContent);
        }
        if (cost != null) {
            record.setMaintenanceCost(cost);
        }

        maintenanceRecordMapper.updateById(record);

        // 根据维修结果更新设备状态
        if ("RESTORED".equals(result)) {
            // 恢复为正常状态
            String normalStatus = "DRONE".equals(record.getDeviceType()) ? "IDLE" : "NORMAL";
            updateDeviceStatus(record.getDeviceType(), record.getDeviceId(), normalStatus, "NORMAL");

            if ("DRONE".equals(record.getDeviceType())) {
                Drone drone = droneMapper.selectById(record.getDeviceId());
                if (drone != null) {
                    // 回填最近保养日期，供首页"超过180天未保养"提醒使用
                    drone.setLastMaintenanceDate(record.getMaintenanceDate());
                    droneMapper.updateById(drone);
                }
            }
        } else if ("SCRAPPED".equals(result)) {
            // 报废 - 设置为停用
            updateDeviceStatus(record.getDeviceType(), record.getDeviceId(), "DISABLED", "DISABLED");
        }

        return convertToVO(record);
    }

    /**
     * 删除维修工单。
     *
     * <p>删除未完成的工单后，若该设备已无其它未完成工单，则恢复设备状态，
     * 避免设备一直停留在"维修中/异常"而无法再次安排使用。</p>
     *
     * @param id 工单ID
     * @throws BusinessException 记录不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaintenanceRecord(Long id) {
        MaintenanceRecord record = maintenanceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("维修记录不存在");
        }
        maintenanceRecordMapper.deleteById(id);

        // 删除未完成的工单后，若该设备已无其它未完成工单，则恢复设备状态，
        // 避免设备一直停留在"维修中/异常"而无法再次安排使用
        if (!"COMPLETED".equals(record.getStatus())) {
            Long openCount = maintenanceRecordMapper.selectCount(new LambdaQueryWrapper<MaintenanceRecord>()
                    .eq(MaintenanceRecord::getDeviceType, record.getDeviceType())
                    .eq(MaintenanceRecord::getDeviceId, record.getDeviceId())
                    .ne(MaintenanceRecord::getStatus, "COMPLETED"));
            if (openCount == 0) {
                String normalStatus = "DRONE".equals(record.getDeviceType()) ? "IDLE" : "NORMAL";
                updateDeviceStatus(record.getDeviceType(), record.getDeviceId(), normalStatus, "NORMAL");
            }
        }
    }

    /**
     * 校验设备类型与设备是否存在。
     *
     * @param deviceType 设备类型：DRONE-无人机，BATTERY-电池
     * @param deviceId   设备ID
     * @throws BusinessException 设备类型不正确或对应设备不存在时抛出
     */
    private void validateDevice(String deviceType, Long deviceId) {
        if ("DRONE".equals(deviceType)) {
            Drone drone = droneMapper.selectById(deviceId);
            if (drone == null) {
                throw new BusinessException("无人机不存在");
            }
        } else if ("BATTERY".equals(deviceType)) {
            Battery battery = batteryMapper.selectById(deviceId);
            if (battery == null) {
                throw new BusinessException("电池不存在");
            }
        } else {
            throw new BusinessException("设备类型不正确");
        }
    }

    /**
     * 按设备类型更新设备状态，设备已不存在时静默跳过。
     *
     * @param deviceType    设备类型：DRONE-无人机，BATTERY-电池
     * @param deviceId      设备ID
     * @param droneStatus   无人机目标状态（仅设备类型为 DRONE 时使用）
     * @param batteryStatus 电池目标状态（仅设备类型为 BATTERY 时使用）
     */
    private void updateDeviceStatus(String deviceType, Long deviceId, String droneStatus, String batteryStatus) {
        if ("DRONE".equals(deviceType)) {
            Drone drone = droneMapper.selectById(deviceId);
            if (drone != null) {
                drone.setStatus(droneStatus);
                droneMapper.updateById(drone);
            }
        } else if ("BATTERY".equals(deviceType)) {
            Battery battery = batteryMapper.selectById(deviceId);
            if (battery != null) {
                battery.setStatus(batteryStatus);
                batteryMapper.updateById(battery);
            }
        }
    }

    /**
     * 把工单实体转换为视图对象，并补充设备编号名称与创建人姓名。
     *
     * @param record 工单实体
     * @return 工单视图对象
     */
    private MaintenanceRecordVO convertToVO(MaintenanceRecord record) {
        MaintenanceRecordVO vo = new MaintenanceRecordVO();
        vo.setId(record.getId());
        vo.setMaintenanceCode(record.getMaintenanceCode());
        vo.setDeviceType(record.getDeviceType());
        vo.setDeviceId(record.getDeviceId());
        vo.setFaultDescription(record.getFaultDescription());
        vo.setMaintenanceContent(record.getMaintenanceContent());
        vo.setMaintenanceDate(record.getMaintenanceDate());
        vo.setMaintenanceCost(record.getMaintenanceCost());
        vo.setStatus(record.getStatus());
        vo.setResult(record.getResult());
        vo.setRemark(record.getRemark());
        vo.setCreatedBy(record.getCreatedBy());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());

        // 设备名称和编号
        if ("DRONE".equals(record.getDeviceType())) {
            Drone drone = droneMapper.selectById(record.getDeviceId());
            if (drone != null) {
                vo.setDeviceCode(drone.getDroneCode());
                vo.setDeviceName(drone.getDroneName());
            }
        } else if ("BATTERY".equals(record.getDeviceType())) {
            Battery battery = batteryMapper.selectById(record.getDeviceId());
            if (battery != null) {
                vo.setDeviceCode(battery.getBatteryCode());
                vo.setDeviceName(battery.getBatteryName());
            }
        }

        // 创建人姓名
        if (record.getCreatedBy() != null) {
            com.drone.entity.User user = userMapper.selectById(record.getCreatedBy());
            if (user != null) {
                vo.setCreatedByName(user.getRealName());
            }
        }

        return vo;
    }

    /**
     * 生成维修编号，格式为 "MT-日期-6位随机数"。
     *
     * @return 维修编号
     */
    private String generateMaintenanceCode() {
        // 原实现取时间戳后四位，每 10 秒就会重复一次，会与维修编号唯一约束冲突导致新增失败
        return "MT-" + LocalDate.now() + "-" + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
}
