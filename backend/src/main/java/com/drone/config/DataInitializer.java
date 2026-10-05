package com.drone.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drone.entity.User;
import com.drone.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 初始账号初始化。
 * 仅在用户表为空（全新数据库）时创建初始账号，账号被删除后重启不会自动重建；
 * 初始口令来自配置项 app.init-admin-password / app.init-user-password（可用环境变量覆盖），日志中不打印口令。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-admin-password:admin123}")
    private String initAdminPassword;

    @Value("${app.init-user-password:user123}")
    private String initUserPassword;

    /**
     * 应用启动后执行：用户表为空时创建初始管理员与普通用户，否则只修补信息为空的账号。
     *
     * @param args 启动参数，本方法未使用
     */
    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            // 已存在账号：不创建、不重置密码。仅修补极少数情况下为空的账号信息（如脚本导入的空口令行）
            repairBlankAccounts();
            return;
        }

        createUser("admin", initAdminPassword, "系统管理员", "ADMIN");
        createUser("user", initUserPassword, "普通用户", "USER");
        log.info("首次启动，已创建初始账号 admin 与 user，初始口令取自配置项 app.init-admin-password / app.init-user-password");
    }

    /**
     * 创建一个启用状态的账号，口令经 BCrypt 加密后存储。
     *
     * @param username    登录名
     * @param rawPassword 明文初始口令
     * @param realName    真实姓名
     * @param role        角色：ADMIN-管理员，USER-普通用户
     */
    private void createUser(String username, String rawPassword, String realName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus(1);
        userMapper.insert(user);
        log.info("已创建初始账号: {}", username);
    }

    /**
     * 仅处理口令为空的账号（由初始化脚本导入的空口令行），使其可以登录；
     * 不覆盖任何已有口令，也不会重建已删除的账号。
     */
    private void repairBlankAccounts() {
        for (User user : userMapper.selectList(new LambdaQueryWrapper<User>())) {
            boolean isAdmin = "ADMIN".equals(user.getRole());
            String initialPassword = isAdmin ? initAdminPassword : initUserPassword;

            boolean needUpdate = false;
            if (!StringUtils.hasText(user.getPassword())) {
                if (!StringUtils.hasText(initialPassword)) {
                    log.warn("账号 {} 口令为空且未配置初始口令，请通过配置项设置后再登录", user.getUsername());
                    continue;
                }
                user.setPassword(passwordEncoder.encode(initialPassword));
                needUpdate = true;
                log.warn("账号 {} 的口令为空，已按配置项设置初始口令，请登录后立即修改", user.getUsername());
            }
            if (user.getStatus() == null) {
                user.setStatus(1);
                needUpdate = true;
            }
            if (needUpdate) {
                userMapper.updateById(user);
            }
        }
    }
}
