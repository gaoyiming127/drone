package com.drone.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.UserDto;
import com.drone.entity.User;
import com.drone.service.UserService;
import com.drone.util.PageResult;
import com.drone.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口，整个控制器仅管理员可访问。
 *
 * <p>普通用户需要用户下拉选项时使用 {@link UserOptionController} 提供的接口。</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表。
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 10
     * @param keyword  关键词，对用户名与真实姓名模糊匹配，可为空
     * @return 分页结果
     */
    @GetMapping
    public PageResult<User> page(@RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) String keyword) {
        IPage<User> page = userService.pageUsers(pageNum, pageSize, keyword);
        return PageResult.success(page);
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户ID
     * @return 用户信息（密码不参与序列化）
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 新增用户。
     *
     * @param userDto 用户信息
     * @return 新增后的用户
     */
    @PostMapping
    public Result<User> create(@Valid @RequestBody UserDto userDto) {
        return Result.success(userService.createUser(userDto));
    }

    /**
     * 修改用户，密码留空表示不修改。
     *
     * @param id      用户ID
     * @param userDto 待修改的用户信息
     * @return 修改后的用户
     */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        return Result.success(userService.updateUser(id, userDto));
    }

    /**
     * 启用或禁用用户。
     *
     * @param id     用户ID
     * @param status 目标状态：0-禁用，1-启用
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除用户。
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
