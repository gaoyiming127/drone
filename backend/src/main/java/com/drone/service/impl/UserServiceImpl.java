package com.drone.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drone.dto.PasswordDto;
import com.drone.dto.UserDto;
import com.drone.entity.FlightRecord;
import com.drone.entity.User;
import com.drone.exception.BusinessException;
import com.drone.mapper.FlightRecordMapper;
import com.drone.mapper.UserMapper;
import com.drone.service.UserService;
import com.drone.vo.UserOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户业务实现。
 *
 * <p>口令统一使用 BCrypt 加密存储；状态为 0 的账号无法登录；
 * 为避免无人可管理系统，系统始终保留至少一个启用的管理员。</p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final FlightRecordMapper flightRecordMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录校验：账号存在、状态为启用、密码匹配三者缺一不可。
     *
     * @param username 登录名
     * @param password 明文密码
     * @return 校验通过的用户实体
     * @throws BusinessException 用户不存在、账号被禁用或密码错误时抛出（均返回 401）
     */
    @Override
    public User login(String username, String password) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 与界面口径保持一致：只有状态为 1 的账号可以登录
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(401, "账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        return user;
    }

    /**
     * 分页查询用户列表，关键词对用户名与真实姓名做模糊匹配，结果按创建时间倒序。
     *
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  关键词，可为空
     * @return 分页结果
     */
    @Override
    public IPage<User> pageUsers(int pageNum, int pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }

        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, wrapper);
    }

    /**
     * 按ID查询用户。
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 按登录名精确查询用户。
     *
     * @param username 登录名
     * @return 用户实体
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    public User getByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 新增用户：校验用户名唯一、密码非空后写入，角色与状态为空时分别默认 USER 与 1。
     *
     * @param userDto 用户信息
     * @return 新增后的用户
     * @throws BusinessException 用户名已存在或密码为空时抛出
     */
    @Override
    public User createUser(UserDto userDto) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 密码仅在新增时必填，修改时留空表示不修改
        if (!StringUtils.hasText(userDto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRealName(userDto.getRealName());
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : "USER");
        user.setStatus(userDto.getStatus() != null ? userDto.getStatus() : 1);

        userMapper.insert(user);
        return user;
    }

    /**
     * 修改用户：用户名变更时校验唯一性，密码留空表示不修改，
     * 姓名与角色留空表示保持原值，邮箱与手机号直接覆盖。
     *
     * @param id      用户ID
     * @param userDto 待修改的用户信息
     * @return 修改后的用户
     * @throws BusinessException 用户不存在或新用户名已被占用时抛出
     */
    @Override
    public User updateUser(Long id, UserDto userDto) {
        User user = getUserById(id);

        if (StringUtils.hasText(userDto.getUsername()) && !userDto.getUsername().equals(user.getUsername())) {
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, userDto.getUsername())
                    .ne(User::getId, id));
            if (count > 0) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(userDto.getUsername());
        }

        if (StringUtils.hasText(userDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        if (StringUtils.hasText(userDto.getRealName())) {
            user.setRealName(userDto.getRealName());
        }
        user.setEmail(userDto.getEmail());
        user.setPhone(userDto.getPhone());
        if (StringUtils.hasText(userDto.getRole())) {
            user.setRole(userDto.getRole());
        }
        if (userDto.getStatus() != null) {
            user.setStatus(userDto.getStatus());
        }

        userMapper.updateById(user);
        return user;
    }

    /**
     * 删除用户：存在飞行记录或为最后一个启用的管理员时拒绝删除。
     *
     * @param id 用户ID
     * @throws BusinessException 用户不存在、存在飞行记录或为最后一个启用管理员时抛出
     */
    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);

        Long flightCount = flightRecordMapper.selectCount(new LambdaQueryWrapper<FlightRecord>()
                .eq(FlightRecord::getUserId, id));
        if (flightCount > 0) {
            throw new BusinessException("该用户存在飞行记录，无法删除");
        }
        ensureNotLastAdmin(user, "删除");

        userMapper.deleteById(id);
    }

    /**
     * 启用/禁用用户，禁用最后一个启用的管理员时拒绝操作。
     *
     * @param id     用户ID
     * @param status 目标状态：0-禁用，1-启用
     * @throws BusinessException 用户不存在或禁用最后一个启用管理员时抛出
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        User user = getUserById(id);
        if (Integer.valueOf(0).equals(status)) {
            ensureNotLastAdmin(user, "禁用");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 系统须保留至少一个启用的管理员，避免管理员可被……全部管理系统。
     *
     * <p>非管理员或本就未启用的账号直接放行，不需要校验。</p>
     *
     * @param user   待操作的用户
     * @param action 操作名称（删除/禁用），用于拼接异常提示
     * @throws BusinessException 系统将不再有启用的管理员时抛出
     */
    private void ensureNotLastAdmin(User user, String action) {
        if (!"ADMIN".equals(user.getRole()) || !Integer.valueOf(1).equals(user.getStatus())) {
            return;
        }
        Long otherEnabledAdmins = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, "ADMIN")
                .eq(User::getStatus, 1)
                .ne(User::getId, user.getId()));
        if (otherEnabledAdmins == 0) {
            throw new BusinessException("系统至少需要保留一个启用的管理员，无法" + action);
        }
    }

    /**
     * 修改密码：校验旧密码后写入新密码的 BCrypt 密文。
     *
     * @param userId      用户ID
     * @param passwordDto 旧密码与新密码
     * @throws BusinessException 用户不存在或旧密码不正确时抛出
     */
    @Override
    public void updatePassword(Long userId, PasswordDto passwordDto) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 查询用户下拉选项，按ID升序返回，仅含ID、登录名与真实姓名。
     *
     * @return 用户选项列表
     */
    @Override
    public List<UserOptionVO> listOptions() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByAsc(User::getId))
                .stream()
                .map(u -> new UserOptionVO(u.getId(), u.getUsername(), u.getRealName()))
                .collect(Collectors.toList());
    }
}
