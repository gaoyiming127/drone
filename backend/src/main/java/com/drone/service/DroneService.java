package com.drone.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.DroneDto;
import com.drone.entity.Drone;

/**
 * 无人机管理业务接口。
 */
public interface DroneService {

    /**
     * 分页查询无人机列表。
     *
     * @param pageNum   页码，从 1 开始
     * @param pageSize  每页条数
     * @param droneCode 设备编号，模糊匹配，可为空
     * @param droneName 设备名称，模糊匹配，可为空
     * @param brand     品牌，模糊匹配，可为空
     * @param status    设备状态，精确匹配，可为空
     * @return 分页结果
     */
    IPage<Drone> pageDrones(int pageNum, int pageSize, String droneCode, String droneName, String brand, String status);

    /**
     * 按ID查询无人机详情。
     *
     * @param id 无人机ID
     * @return 无人机信息
     * @throws com.drone.exception.BusinessException 无人机不存在时抛出
     */
    Drone getDroneById(Long id);

    /**
     * 新增无人机：校验编号唯一后写入，累计飞行数据初始化为 0。
     *
     * @param droneDto 无人机信息
     * @return 新增后的无人机
     */
    Drone createDrone(DroneDto droneDto);

    /**
     * 修改无人机：编号变更时校验唯一性，字段留空表示保持原值。
     *
     * @param id       无人机ID
     * @param droneDto 待修改的无人机信息
     * @return 修改后的无人机
     */
    Drone updateDrone(Long id, DroneDto droneDto);

    /**
     * 删除无人机：存在飞行记录或维修记录时不允许删除。
     *
     * @param id 无人机ID
     */
    void deleteDrone(Long id);

    /**
     * 结束使用：把"使用中"的无人机恢复为"空闲"，供飞行任务结束后回库。
     *
     * @param id 无人机ID
     * @return 状态更新后的无人机
     */
    Drone releaseDrone(Long id);
}
