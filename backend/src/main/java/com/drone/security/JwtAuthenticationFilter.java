package com.drone.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drone.entity.User;
import com.drone.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器：每个请求执行一次，校验 Authorization 请求头中的令牌并写入认证信息。
 *
 * <p>令牌有效且账号仍存在、状态为启用时，才向 SecurityContext 写入认证对象，
 * 后续的权限注解（@PreAuthorize）依赖该认证信息。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    /**
     * 解析令牌并设置认证信息，随后放行到后续过滤器链。
     *
     * <p>未携带令牌或令牌校验不通过时不设置认证信息，直接放行，由安全框架按未登录处理。</p>
     *
     * @param request     当前请求
     * @param response    当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);

            // 以数据库中的账号为准：账号已删除或被禁用时令牌立即失效，
            // 角色也取当前值，避免降权后旧令牌仍带管理员权限（令牌有效期内）
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username));

            if (user != null && Integer.valueOf(1).equals(user.getStatus())) {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer 令牌。
     *
     * @param request 当前请求
     * @return 去掉 "Bearer " 前缀的令牌；请求头缺失或格式不符时返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
