package com.drone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.entity.Battery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电池数据访问接口。
 *
 * <p>增删改查与分页均由 MyBatis-Plus 的 {@link BaseMapper} 提供，无自定义 SQL。</p>
 */
@Mapper
public interface BatteryMapper extends BaseMapper<Battery> {
}
