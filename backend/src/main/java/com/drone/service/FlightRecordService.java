package com.drone.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.FlightRecordDto;
import com.drone.vo.FlightRecordVO;

/**
 * 飞行记录业务接口。
 *
 * <p>登记与删除都会影响无人机、电池的累计数据，故均为事务方法；
 * 查询与删除范围受当前用户角色限制，普通用户只能操作本人登记的记录。</p>
 */
public interface FlightRecordService {

    /**
     * 登记飞行记录。
     *
     * @param currentUserId 当前登录用户ID，普通用户只能登记本人的飞行记录
     * @param admin         当前用户是否为管理员
     */
    FlightRecordVO createFlightRecord(FlightRecordDto flightRecordDto, Long currentUserId, boolean admin);

    /**
     * 分页查询飞行记录。
     *
     * @param onlyUserId 只返回该使用人员的记录，为空表示不限制（管理员）
     */
    IPage<FlightRecordVO> pageFlightRecords(int pageNum, int pageSize, String droneCode, String userName,
                                            String flightDate, Long onlyUserId);

    /**
     * 查询详情，普通用户只能查看本人登记的记录。
     *
     * @param id            飞行记录ID
     * @param currentUserId 当前登录用户ID
     * @param admin         当前用户是否为管理员
     * @return 飞行记录详情（含电池使用明细）
     */
    FlightRecordVO getFlightRecordById(Long id, Long currentUserId, boolean admin);

    /**
     * 删除飞行记录。
     *
     * @param currentUserId 当前登录用户ID，普通用户只能删除本人登记的飞行记录
     * @param admin         当前用户是否为管理员
     */
    void deleteFlightRecord(Long id, Long currentUserId, boolean admin);
}
