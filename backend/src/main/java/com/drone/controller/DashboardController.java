package com.drone.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drone.entity.User;
import com.drone.exception.BusinessException;
import com.drone.mapper.UserMapper;
import com.drone.service.DashboardService;
import com.drone.util.Result;
import com.drone.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页看板接口。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserMapper userMapper;

    /**
     * 查询首页统计数据。
     *
     * <p>普通用户只返回本人的飞行记录明细，管理员返回全部；统计类指标始终为全系统口径。</p>
     *
     * @param authentication 当前登录身份，由安全框架注入
     * @return 首页统计、最近记录、图表数据与系统提醒
     */
    @GetMapping("/statistics")
    public Result<DashboardVO> getStatistics(Authentication authentication) {
        if (authentication == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        User current = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, authentication.getName()));
        if (current == null) {
            throw new BusinessException(401, "账号不存在或已被删除");
        }
        // 普通用户首页只展示本人的飞行记录明细，管理员展示全部
        Long onlyUserId = "ADMIN".equals(current.getRole()) ? null : current.getId();
        return Result.success(dashboardService.getDashboardData(onlyUserId));
    }
}
