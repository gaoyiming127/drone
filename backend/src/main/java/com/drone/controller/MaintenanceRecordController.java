package com.drone.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drone.dto.MaintenanceRecordDto;
import com.drone.entity.User;
import com.drone.mapper.UserMapper;
import com.drone.service.MaintenanceRecordService;
import com.drone.util.PageResult;
import com.drone.util.Result;
import com.drone.vo.MaintenanceRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 维修工单接口。
 *
 * <p>查询对全部登录用户开放；新增、修改、完成、删除仅管理员可用。</p>
 */
@RestController
@RequestMapping("/api/maintenance-records")
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;
    private final UserMapper userMapper;

    /**
     * 分页查询维修记录。
     *
     * @param pageNum    页码，默认 1
     * @param pageSize   每页条数，默认 10
     * @param deviceType 设备类型，精确匹配，可为空
     * @param status     维修状态，精确匹配，可为空
     * @return 分页结果
     */
    @GetMapping
    public PageResult<MaintenanceRecordVO> page(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String deviceType,
                                                @RequestParam(required = false) String status) {
        com.baomidou.mybatisplus.core.metadata.IPage<MaintenanceRecordVO> page =
                maintenanceRecordService.pageMaintenanceRecords(pageNum, pageSize, deviceType, status);
        return PageResult.success(page);
    }

    /**
     * 查询维修记录详情。
     *
     * @param id 维修记录ID
     * @return 维修记录详情
     */
    @GetMapping("/{id}")
    public Result<MaintenanceRecordVO> getById(@PathVariable Long id) {
        return Result.success(maintenanceRecordService.getMaintenanceRecordById(id));
    }

    /**
     * 新增维修工单（仅管理员）。
     *
     * @param dto            工单信息
     * @param authentication 当前登录身份，由安全框架注入，作为工单创建人
     * @return 新增后的工单详情
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MaintenanceRecordVO> create(@Valid @RequestBody MaintenanceRecordDto dto,
                                               Authentication authentication) {
        String username = authentication.getName();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return Result.success(maintenanceRecordService.createMaintenanceRecord(dto, user.getId()));
    }

    /**
     * 修改未完成的维修工单（仅管理员）。
     *
     * @param id  工单ID
     * @param dto 待修改的工单信息
     * @return 修改后的工单详情
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MaintenanceRecordVO> update(@PathVariable Long id,
                                               @Valid @RequestBody MaintenanceRecordDto dto) {
        return Result.success(maintenanceRecordService.updateMaintenanceRecord(id, dto));
    }

    /**
     * 完成维修并登记维修结果（仅管理员）。
     *
     * @param id                 工单ID
     * @param result             维修结果：RESTORED-恢复，SCRAPPED-报废
     * @param maintenanceContent 维修内容，可为空
     * @param maintenanceCost    维修费用，可为空
     * @return 完成后的工单详情
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<MaintenanceRecordVO> complete(@PathVariable Long id,
                                                 @RequestParam String result,
                                                 @RequestParam(required = false) String maintenanceContent,
                                                 @RequestParam(required = false) BigDecimal maintenanceCost) {
        return Result.success(maintenanceRecordService.completeMaintenance(id, result, maintenanceContent, maintenanceCost));
    }

    /**
     * 删除维修工单（仅管理员）。
     *
     * @param id 工单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        maintenanceRecordService.deleteMaintenanceRecord(id);
        return Result.success();
    }
}
