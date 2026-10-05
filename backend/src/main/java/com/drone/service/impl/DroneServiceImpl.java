package com.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.dto.DroneDto;
import com.drone.entity.Drone;
import com.drone.entity.FlightRecord;
import com.drone.entity.MaintenanceRecord;
import com.drone.exception.BusinessException;
import com.drone.mapper.DroneMapper;
import com.drone.mapper.FlightRecordMapper;
import com.drone.mapper.MaintenanceRecordMapper;
import com.drone.service.DroneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 无人机管理业务实现。
 */
@Service
@RequiredArgsConstructor
public class DroneServiceImpl implements DroneService {

    private final DroneMapper droneMapper;
    private final FlightRecordMapper flightRecordMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    /**
     * 分页查询无人机列表，过滤条件均按需拼接，结果按创建时间倒序。
     *
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数
     * @param droneCode 设备编号，模糊匹配，可为空
     * @param droneName 设备名称，模糊匹配，可为空
     * @param brand     品牌，模糊匹配，可为空
     * @param status    设备状态，精确匹配，可为空
     * @return 分页结果
     */
    @Override
    public IPage<Drone> pageDrones(int pageNum, int pageSize, String droneCode, String droneName, String brand, String status) {
        Page<Drone> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Drone> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(droneCode)) {
            wrapper.like(Drone::getDroneCode, droneCode);
        }
        if (StringUtils.hasText(droneName)) {
            wrapper.like(Drone::getDroneName, droneName);
        }
        if (StringUtils.hasText(brand)) {
            wrapper.like(Drone::getBrand, brand);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Drone::getStatus, status);
        }

        wrapper.orderByDesc(Drone::getCreatedAt);
        return droneMapper.selectPage(page, wrapper);
    }

    /**
     * 按ID查询无人机。
     *
     * @param id 无人机ID
     * @return 无人机信息
     * @throws BusinessException 无人机不存在时抛出
     */
    @Override
    public Drone getDroneById(Long id) {
        Drone drone = droneMapper.selectById(id);
        if (drone == null) {
            throw new BusinessException("无人机不存在");
        }
        return drone;
    }

    /**
     * 新增无人机：校验编号唯一后写入，状态默认为空闲，累计飞行数据初始化为 0。
     *
     * @param droneDto 无人机信息
     * @return 新增后的无人机
     * @throws BusinessException 设备编号已存在时抛出
     */
    @Override
    public Drone createDrone(DroneDto droneDto) {
        // 检查编号是否已存在
        Long count = droneMapper.selectCount(new LambdaQueryWrapper<Drone>()
                .eq(Drone::getDroneCode, droneDto.getDroneCode()));
        if (count > 0) {
            throw new BusinessException("设备编号已存在");
        }

        Drone drone = new Drone();
        drone.setDroneCode(droneDto.getDroneCode());
        drone.setDroneName(droneDto.getDroneName());
        drone.setBrand(droneDto.getBrand());
        drone.setModel(droneDto.getModel());
        drone.setSerialNumber(droneDto.getSerialNumber());
        drone.setPurchaseDate(droneDto.getPurchaseDate());
        drone.setStatus(droneDto.getStatus() != null ? droneDto.getStatus() : "IDLE");
        drone.setTotalFlightCount(0);
        drone.setTotalFlightMinutes(0);
        drone.setRemark(droneDto.getRemark());

        droneMapper.insert(drone);
        return drone;
    }

    /**
     * 修改无人机：编号变更时校验唯一性，状态为空时保持原值。
     *
     * @param id       无人机ID
     * @param droneDto 待修改的无人机信息
     * @return 修改后的无人机
     * @throws BusinessException 无人机不存在或新编号已被占用时抛出
     */
    @Override
    public Drone updateDrone(Long id, DroneDto droneDto) {
        Drone drone = getDroneById(id);

        if (StringUtils.hasText(droneDto.getDroneCode()) && !droneDto.getDroneCode().equals(drone.getDroneCode())) {
            Long count = droneMapper.selectCount(new LambdaQueryWrapper<Drone>()
                    .eq(Drone::getDroneCode, droneDto.getDroneCode())
                    .ne(Drone::getId, id));
            if (count > 0) {
                throw new BusinessException("设备编号已存在");
            }
            drone.setDroneCode(droneDto.getDroneCode());
        }

        drone.setDroneName(droneDto.getDroneName());
        drone.setBrand(droneDto.getBrand());
        drone.setModel(droneDto.getModel());
        drone.setSerialNumber(droneDto.getSerialNumber());
        drone.setPurchaseDate(droneDto.getPurchaseDate());
        if (StringUtils.hasText(droneDto.getStatus())) {
            drone.setStatus(droneDto.getStatus());
        }
        drone.setRemark(droneDto.getRemark());

        droneMapper.updateById(drone);
        return drone;
    }

    /**
     * 结束使用：把状态为"使用中"的无人机恢复为"空闲"。
     *
     * @param id 无人机ID
     * @return 状态更新后的无人机
     * @throws BusinessException 无人机不存在或当前不是使用中状态时抛出
     */
    @Override
    public Drone releaseDrone(Long id) {
        Drone drone = getDroneById(id);
        if (!"IN_USE".equals(drone.getStatus())) {
            throw new BusinessException("该无人机当前不是使用中状态，无需结束使用");
        }
        drone.setStatus("IDLE");
        droneMapper.updateById(drone);
        return drone;
    }

    /**
     * 删除无人机：存在飞行记录或维修记录时拒绝删除。
     *
     * @param id 无人机ID
     * @throws BusinessException 无人机不存在、存在飞行记录或存在维修记录时抛出
     */
    @Override
    public void deleteDrone(Long id) {
        getDroneById(id);

        // 存在业务记录时禁止删除，避免外键报错与维修记录中的设备信息悬空
        Long flightCount = flightRecordMapper.selectCount(new LambdaQueryWrapper<FlightRecord>()
                .eq(FlightRecord::getDroneId, id));
        if (flightCount > 0) {
            throw new BusinessException("该无人机存在飞行记录，无法删除");
        }
        Long maintenanceCount = maintenanceRecordMapper.selectCount(new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getDeviceType, "DRONE")
                .eq(MaintenanceRecord::getDeviceId, id));
        if (maintenanceCount > 0) {
            throw new BusinessException("该无人机存在维修记录，无法删除");
        }

        droneMapper.deleteById(id);
    }
}
