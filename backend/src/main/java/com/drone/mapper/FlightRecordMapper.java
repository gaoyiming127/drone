package com.drone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.entity.FlightRecord;
import com.drone.vo.FlightRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 飞行记录数据访问接口，列表与统计类 SQL 定义在 FlightRecordMapper.xml 中。
 */
@Mapper
public interface FlightRecordMapper extends BaseMapper<FlightRecord> {

    /**
     * 查询全部飞行记录（不分页、无过滤条件），按登记时间倒序。
     *
     * <p>【待人工确认】当前项目内未发现调用（列表已改为按条件分页查询），
     * 因属于 Mapper 对外方法且 XML 中仍有对应语句，暂予保留，请人工确认后再决定是否删除。</p>
     *
     * @return 飞行记录视图列表
     */
    List<FlightRecordVO> selectFlightRecordVOs();

    /**
     * 按条件分页查询：过滤与分页均由数据库完成，避免全表加载到内存。
     *
     * @param onlyUserId 只查询该使用人员的记录，为空表示不限制（管理员）
     */
    List<FlightRecordVO> selectFlightRecordVOsByPage(@Param("droneCode") String droneCode,
                                                     @Param("userName") String userName,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("dateLike") String dateLike,
                                                     @Param("onlyUserId") Long onlyUserId,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit);

    /** 与分页查询同条件的总数统计 */
    long countFlightRecordVOs(@Param("droneCode") String droneCode,
                              @Param("userName") String userName,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("dateLike") String dateLike,
                              @Param("onlyUserId") Long onlyUserId);

    /** 首页看板：按录入时间取最近若干条，onlyUserId 为空表示不限制 */
    List<FlightRecordVO> selectRecentFlightRecordVOs(@Param("limit") int limit,
                                                     @Param("onlyUserId") Long onlyUserId);

    /**
     * 统计近若干个月每月的飞行次数，用于首页折线图。
     *
     * @param months 统计的月份跨度
     * @return 每项包含 month（yyyy-MM）与 count（飞行次数）
     */
    @Select("SELECT DATE_FORMAT(flight_date, '%Y-%m') as month, COUNT(*) as count " +
            "FROM flight_record " +
            "WHERE flight_date >= DATE_SUB(CURDATE(), INTERVAL #{months} MONTH) " +
            "GROUP BY DATE_FORMAT(flight_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> getMonthlyFlightCount(@Param("months") int months);

    /**
     * 统计本月飞行次数。
     *
     * @return 本月（自然月）的飞行记录条数
     */
    // 使用日期区间代替对列做 DATE_FORMAT，便于命中 idx_flight_date 索引
    @Select("SELECT COUNT(*) FROM flight_record " +
            "WHERE flight_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
            "AND flight_date < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01')")
    long countCurrentMonth();

    /**
     * 统计本月飞行总时长。
     *
     * @return 本月（自然月）的飞行分钟数合计，无记录时返回 0
     */
    @Select("SELECT COALESCE(SUM(flight_minutes), 0) FROM flight_record " +
            "WHERE flight_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
            "AND flight_date < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01')")
    long sumCurrentMonthMinutes();
}
