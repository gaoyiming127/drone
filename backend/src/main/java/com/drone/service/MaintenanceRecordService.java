package com.drone.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.MaintenanceRecordDto;
import com.drone.vo.MaintenanceRecordVO;

/**
 * 维修工单业务接口。
 *
 * <p>新增工单会把设备置为维修中/异常，完成或删除工单会按结果恢复或停用设备，
 * 因此相关方法均为事务方法。</p>
 */
public interface MaintenanceRecordService {

    /**
     * 分页查询维修记录。
     *
     * @param pageNum    页码，从 1 开始
     * @param pageSize   每页条数
     * @param deviceType 设备类型，精确匹配，可为空
     * @param status     维修状态，精确匹配，可为空
     * @return 分页结果
     */
    IPage<MaintenanceRecordVO> pageMaintenanceRecords(int pageNum, int pageSize, String deviceType, String status);

    /**
     * 按ID查询维修记录详情。
     *
     * @param id 维修记录ID
     * @return 维修记录详情（含设备编号名称与创建人姓名）
     * @throws com.drone.exception.BusinessException 记录不存在时抛出
     */
    MaintenanceRecordVO getMaintenanceRecordById(Long id);

    /**
     * 新增维修工单：状态固定为待维修，并把设备置为维修中/异常。
     *
     * @param dto    工单信息
     * @param userId 创建人（当前登录用户）ID
     * @return 新增后的工单详情
     */
    MaintenanceRecordVO createMaintenanceRecord(MaintenanceRecordDto dto, Long userId);

    /**
     * 修改未完成的维修工单。
     *
     * @param id  工单ID
     * @param dto 待修改的工单信息
     * @return 修改后的工单详情
     */
    MaintenanceRecordVO updateMaintenanceRecord(Long id, MaintenanceRecordDto dto);

    /**
     * 完成维修：记录维修结果并据此更新设备状态，恢复时同时回填无人机的最近保养日期。
     *
     * @param id                 工单ID
     * @param result             维修结果：RESTORED-恢复，SCRAPPED-报废
     * @param maintenanceContent 维修内容，为空时不覆盖原值
     * @param cost               维修费用，为空时不覆盖原值
     * @return 完成后的工单详情
     */
    MaintenanceRecordVO completeMaintenance(Long id, String result, String maintenanceContent, java.math.BigDecimal cost);

    /**
     * 删除维修工单；删除未完成工单且该设备已无其它未完成工单时恢复设备状态。
     *
     * @param id 工单ID
     */
    void deleteMaintenanceRecord(Long id);
}
