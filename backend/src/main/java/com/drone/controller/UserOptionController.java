package com.drone.controller;

import com.drone.service.UserService;
import com.drone.util.Result;
import com.drone.vo.UserOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 供普通用户使用的用户下拉选项接口。
 * 用户管理接口仅管理员可访问，而登记飞行记录需要选择使用人员，故单独提供只含编号与姓名的选项接口。
 */
@RestController
@RequestMapping("/api/user-options")
@RequiredArgsConstructor
public class UserOptionController {

    private final UserService userService;

    /**
     * 查询用户下拉选项。
     *
     * @return 仅含ID、登录名与真实姓名的用户列表
     */
    @GetMapping
    public Result<List<UserOptionVO>> list() {
        return Result.success(userService.listOptions());
    }
}
