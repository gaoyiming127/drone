package com.drone.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.PasswordDto;
import com.drone.dto.UserDto;
import com.drone.entity.User;
import com.drone.vo.UserOptionVO;

import java.util.List;

/**
 * 系统用户业务接口。
 *
 * <p>负责登录校验、用户增删改查、状态与密码维护；系统须至少保留一个启用的管理员。</p>
 */
public interface UserService {

    /**
     * 登录校验。
     *
     * @param username 登录名
     * @param password 明文密码
     * @return 校验通过的用户实体（含角色与状态）
     * @throws com.drone.exception.BusinessException 用户不存在、账号被禁用或密码错误时抛出
     */
    User login(String username, String password);

    /**
     * 分页查询用户列表。
     *
     * @param pageNum  页码，从 1 开始
     * @param pageSize 每页条数
     * @param keyword  关键词，对用户名与真实姓名模糊匹配，可为空
     * @return 分页结果，按创建时间倒序
     */
    IPage<User> pageUsers(int pageNum, int pageSize, String keyword);

    /**
     * 按ID查询用户。
     *
     * @param id 用户ID
     * @return 用户实体
     * @throws com.drone.exception.BusinessException 用户不存在时抛出
     */
    User getUserById(Long id);

    /**
     * 按登录名精确查询用户。
     *
     * @param username 登录名
     * @return 用户实体
     * @throws com.drone.exception.BusinessException 用户不存在时抛出
     */
    User getByUsername(String username);

    /**
     * 新增用户：校验用户名唯一与密码非空，密码加密后存储。
     *
     * @param userDto 用户信息
     * @return 新增后的用户
     */
    User createUser(UserDto userDto);

    /**
     * 修改用户：用户名变更时校验唯一性，密码留空表示不修改。
     *
     * @param id      用户ID
     * @param userDto 待修改的用户信息
     * @return 修改后的用户
     */
    User updateUser(Long id, UserDto userDto);

    /**
     * 删除用户：存在飞行记录或该用户是最后一个启用的管理员时不允许删除。
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 启用/禁用用户，禁用最后一个启用的管理员时拒绝操作。
     *
     * @param id     用户ID
     * @param status 目标状态：0-禁用，1-启用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 修改密码：校验旧密码后写入新密码的 BCrypt 密文。
     *
     * @param userId      用户ID
     * @param passwordDto 旧密码与新密码
     */
    void updatePassword(Long userId, PasswordDto passwordDto);

    /**
     * 查询用户下拉选项，供业务表单选择使用人员。
     *
     * @return 仅含ID、登录名与真实姓名的用户列表，按ID升序
     */
    List<UserOptionVO> listOptions();
}
