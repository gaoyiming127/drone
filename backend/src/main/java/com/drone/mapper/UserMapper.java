package com.drone.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.drone.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问接口。
 *
 * <p>增删改查与分页均由 MyBatis-Plus 的 {@link BaseMapper} 提供，无自定义 SQL。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
