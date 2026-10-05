package com.drone.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.DroneDto;
import com.drone.entity.Drone;
import com.drone.service.DroneService;
import com.drone.util.PageResult;
import com.drone.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 无人机管理接口。
 *
 * <p>查询对全部登录用户开放；新增、修改、结束使用、删除仅管理员可用。</p>
 */
@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {

    private final DroneService droneService;

    /**
     * 分页查询无人机列表。
     *
     * @param pageNum   页码，默认 1
     * @param pageSize  每页条数，默认 10
     * @param droneCode 设备编号，模糊匹配，可为空
     * @param droneName 设备名称，模糊匹配，可为空
     * @param brand     品牌，模糊匹配，可为空
     * @param status    设备状态，精确匹配，可为空
     * @return 分页结果
     */
    @GetMapping
    public PageResult<Drone> page(@RequestParam(defaultValue = "1") int pageNum,
                                  @RequestParam(defaultValue = "10") int pageSize,
                                  @RequestParam(required = false) String droneCode,
                                  @RequestParam(required = false) String droneName,
                                  @RequestParam(required = false) String brand,
                                  @RequestParam(required = false) String status) {
        IPage<Drone> page = droneService.pageDrones(pageNum, pageSize, droneCode, droneName, brand, status);
        return PageResult.success(page);
    }

    /**
     * 查询无人机详情。
     *
     * @param id 无人机ID
     * @return 无人机信息
     */
    @GetMapping("/{id}")
    public Result<Drone> getById(@PathVariable Long id) {
        return Result.success(droneService.getDroneById(id));
    }

    /**
     * 新增无人机（仅管理员）。
     *
     * @param droneDto 无人机信息
     * @return 新增后的无人机
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Drone> create(@Valid @RequestBody DroneDto droneDto) {
        return Result.success(droneService.createDrone(droneDto));
    }

    /**
     * 修改无人机（仅管理员）。
     *
     * @param id       无人机ID
     * @param droneDto 待修改的无人机信息
     * @return 修改后的无人机
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Drone> update(@PathVariable Long id, @Valid @RequestBody DroneDto droneDto) {
        return Result.success(droneService.updateDrone(id, droneDto));
    }

    /**
     * 结束使用：把使用中的无人机恢复为空闲（仅管理员）。
     *
     * @param id 无人机ID
     * @return 状态更新后的无人机
     */
    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Drone> release(@PathVariable Long id) {
        return Result.success(droneService.releaseDrone(id));
    }

    /**
     * 删除无人机（仅管理员）。
     *
     * @param id 无人机ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        droneService.deleteDrone(id);
        return Result.success();
    }
}
