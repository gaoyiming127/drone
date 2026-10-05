package com.drone.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.BatteryDto;
import com.drone.entity.Battery;
import com.drone.service.BatteryService;
import com.drone.util.PageResult;
import com.drone.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 电池管理接口。
 *
 * <p>查询对全部登录用户开放；新增、修改、删除仅管理员可用。</p>
 */
@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService batteryService;

    /**
     * 分页查询电池列表。
     *
     * @param pageNum     页码，默认 1
     * @param pageSize    每页条数，默认 10
     * @param batteryCode 电池编号，模糊匹配，可为空
     * @param model       型号，模糊匹配，可为空
     * @param healthLevel 健康等级，精确匹配，可为空
     * @param status      电池状态，精确匹配，可为空
     * @return 分页结果
     */
    @GetMapping
    public PageResult<Battery> page(@RequestParam(defaultValue = "1") int pageNum,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    @RequestParam(required = false) String batteryCode,
                                    @RequestParam(required = false) String model,
                                    @RequestParam(required = false) String healthLevel,
                                    @RequestParam(required = false) String status) {
        IPage<Battery> page = batteryService.pageBatteries(pageNum, pageSize, batteryCode, model, healthLevel, status);
        return PageResult.success(page);
    }

    /**
     * 查询电池详情。
     *
     * @param id 电池ID
     * @return 电池信息
     */
    @GetMapping("/{id}")
    public Result<Battery> getById(@PathVariable Long id) {
        return Result.success(batteryService.getBatteryById(id));
    }

    /**
     * 新增电池（仅管理员）。
     *
     * @param batteryDto 电池信息
     * @return 新增后的电池
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Battery> create(@Valid @RequestBody BatteryDto batteryDto) {
        return Result.success(batteryService.createBattery(batteryDto));
    }

    /**
     * 修改电池（仅管理员）。
     *
     * @param id         电池ID
     * @param batteryDto 待修改的电池信息
     * @return 修改后的电池
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Battery> update(@PathVariable Long id, @Valid @RequestBody BatteryDto batteryDto) {
        return Result.success(batteryService.updateBattery(id, batteryDto));
    }

    /**
     * 删除电池（仅管理员）。
     *
     * @param id 电池ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        batteryService.deleteBattery(id);
        return Result.success();
    }
}
