package com.drone.controller;

import com.drone.dto.LoginDto;
import com.drone.dto.PasswordDto;
import com.drone.dto.UserDto;
import com.drone.entity.User;
import com.drone.exception.BusinessException;
import com.drone.security.JwtUtil;
import com.drone.service.UserService;
import com.drone.util.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：登录、注册与修改密码。
 *
 * <p>登录与注册无需认证（在安全配置中放行 /api/auth/**）；修改密码需要携带有效令牌，
 * 并按用户名精确查询当前用户，避免修改到他人密码。</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 登录并签发令牌。
     *
     * @param loginDto 用户名与密码
     * @return 包含 token、userId、username、realName、role 的数据体
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDto loginDto) {
        User user = userService.login(loginDto.getUsername(), loginDto.getPassword());

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());

        return Result.success(data);
    }

    /**
     * 注册新账号，角色固定为普通用户（USER）、状态固定为启用。
     *
     * <p>返回值为 Result&lt;Void&gt;：Void 大写，和基础类型 void 小写区分，
     * 这里表示接口不返回业务数据，只返回操作结果提示。</p>
     *
     * @param userDto 注册信息
     * @return 注册结果提示
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserDto userDto) {
        userDto.setRole("USER");
        userDto.setStatus(1);
        userService.createUser(userDto);
        return Result.successMsg("注册成功");
    }

    /**
     * 修改当前登录用户的密码。
     *
     * @param authentication 当前登录身份，由安全框架注入
     * @param passwordDto    旧密码与新密码
     * @return 修改结果提示
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(Authentication authentication,
                                       @Valid @RequestBody PasswordDto passwordDto) {
        if (authentication == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        // 必须按用户名精确查询，不能用模糊分页取第一条，否则会改到他人的密码
        User user = userService.getByUsername(authentication.getName());
        userService.updatePassword(user.getId(), passwordDto);
        return Result.successMsg("密码修改成功");
    }
}
