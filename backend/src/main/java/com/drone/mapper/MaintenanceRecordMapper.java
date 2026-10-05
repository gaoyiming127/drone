package com.drone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.entity.MaintenanceRecord;
import com.drone.vo.MaintenanceRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 维修记录数据访问接口，列表类 SQL 定义在 MaintenanceRecordMapper.xml 中。
 */
@Mapper
public interface MaintenanceRecordMapper extends BaseMapper<MaintenanceRecord> {

    /**
     * 查询全部维修记录（不分页、无过滤条件），按登记时间倒序。
     *
     * <p>【待人工确认】当前项目内未发现调用（列表已改为按条件分页查询），
     * 因属于 Mapper 对外方法且 XML 中仍有对应语句，暂予保留，请人工确认后再决定是否删除。</p>
     *
     * @return 维修记录视图列表
     */
    List<MaintenanceRecordVO> selectMaintenanceRecordVOs();

    /** 首页看板：按录入时间取最近若干条 */
    List<MaintenanceRecordVO> selectRecentMaintenanceRecordVOs(@Param("limit") int limit);

    /** 按条件分页查询：过滤与分页均由数据库完成，避免全表加载到内存 */
    List<MaintenanceRecordVO> selectMaintenanceRecordVOsByPage(@Param("deviceType") String deviceType,
                                                               @Param("status") String status,
                                                               @Param("offset") int offset,
                                                               @Param("limit") int limit);

    /** 与分页查询同条件的总数统计 */
    long countMaintenanceRecordVOs(@Param("deviceType") String deviceType, @Param("status") String status);

    /**
     * 统计待维修（PENDING）工单数量，用于首页提醒指标。
     *
     * @return 待维修工单条数
     */
    @Select("SELECT COUNT(*) FROM maintenance_record WHERE status = 'PENDING'")
    long countPending();
}
