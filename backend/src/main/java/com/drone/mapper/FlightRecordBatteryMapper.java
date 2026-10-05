package com.drone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.entity.FlightRecordBattery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 飞行记录-电池明细数据访问接口，SQL 以注解方式定义在方法上。
 */
@Mapper
public interface FlightRecordBatteryMapper extends BaseMapper<FlightRecordBattery> {

    /**
     * 查询单条飞行记录的电池明细，并关联带出电池编号与名称。
     *
     * @param flightRecordId 飞行记录ID
     * @return 该记录的电池使用明细列表
     */
    @Select("SELECT frb.*, b.battery_code, b.battery_name " +
            "FROM flight_record_battery frb " +
            "LEFT JOIN battery b ON frb.battery_id = b.id " +
            "WHERE frb.flight_record_id = #{flightRecordId}")
    List<BatteryUsage> selectByFlightRecordId(Long flightRecordId);

    /** 批量查询多条飞行记录的电池明细，避免列表逐条查询 */
    @Select("<script>" +
            "SELECT frb.*, b.battery_code, b.battery_name " +
            "FROM flight_record_battery frb " +
            "LEFT JOIN battery b ON frb.battery_id = b.id " +
            "WHERE frb.flight_record_id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach> " +
            "ORDER BY frb.id" +
            "</script>")
    List<BatteryUsage> selectByFlightRecordIds(@Param("ids") List<Long> ids);

    /**
     * 电池使用明细的查询结果载体：在明细表字段基础上补充电池编号与名称。
     */
    class BatteryUsage {

        /** 明细行主键ID */
        private Long id;

        /** 所属飞行记录ID */
        private Long flightRecordId;

        /** 电池ID */
        private Long batteryId;

        /** 电池编号（关联 battery 表得到，电池被删除时为空） */
        private String batteryCode;

        /** 电池名称（关联 battery 表得到） */
        private String batteryName;

        /** 开始电量（%） */
        private BigDecimal startPower;

        /** 结束电量（%） */
        private BigDecimal endPower;

        /** 本次使用时长（分钟） */
        private Integer useMinutes;

        /**
         * 获取明细行主键ID。
         *
         * @return 明细行主键ID
         */
        public Long getId() { return id; }

        /**
         * 设置明细行主键ID。
         *
         * @param id 明细行主键ID
         */
        public void setId(Long id) { this.id = id; }

        /**
         * 获取所属飞行记录ID。
         *
         * @return 飞行记录ID
         */
        public Long getFlightRecordId() { return flightRecordId; }

        /**
         * 设置所属飞行记录ID。
         *
         * @param flightRecordId 飞行记录ID
         */
        public void setFlightRecordId(Long flightRecordId) { this.flightRecordId = flightRecordId; }

        /**
         * 获取电池ID。
         *
         * @return 电池ID
         */
        public Long getBatteryId() { return batteryId; }

        /**
         * 设置电池ID。
         *
         * @param batteryId 电池ID
         */
        public void setBatteryId(Long batteryId) { this.batteryId = batteryId; }

        /**
         * 获取电池编号。
         *
         * @return 电池编号
         */
        public String getBatteryCode() { return batteryCode; }

        /**
         * 设置电池编号。
         *
         * @param batteryCode 电池编号
         */
        public void setBatteryCode(String batteryCode) { this.batteryCode = batteryCode; }

        /**
         * 获取电池名称。
         *
         * @return 电池名称
         */
        public String getBatteryName() { return batteryName; }

        /**
         * 设置电池名称。
         *
         * @param batteryName 电池名称
         */
        public void setBatteryName(String batteryName) { this.batteryName = batteryName; }

        /**
         * 获取开始电量。
         *
         * @return 开始电量（%）
         */
        public BigDecimal getStartPower() { return startPower; }

        /**
         * 设置开始电量。
         *
         * @param startPower 开始电量（%）
         */
        public void setStartPower(BigDecimal startPower) { this.startPower = startPower; }

        /**
         * 获取结束电量。
         *
         * @return 结束电量（%）
         */
        public BigDecimal getEndPower() { return endPower; }

        /**
         * 设置结束电量。
         *
         * @param endPower 结束电量（%）
         */
        public void setEndPower(BigDecimal endPower) { this.endPower = endPower; }

        /**
         * 获取本次使用时长。
         *
         * @return 使用时长（分钟）
         */
        public Integer getUseMinutes() { return useMinutes; }

        /**
         * 设置本次使用时长。
         *
         * @param useMinutes 使用时长（分钟）
         */
        public void setUseMinutes(Integer useMinutes) { this.useMinutes = useMinutes; }
    }
}
