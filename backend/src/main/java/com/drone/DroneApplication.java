package com.drone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 无人机设备与电池管理系统启动类。
 *
 * <p>扫描 com.drone 包下的 Spring 组件，并为 com.drone.mapper 下的 MyBatis-Plus Mapper 接口
 * 生成代理实现。</p>
 */
@SpringBootApplication
@MapperScan("com.drone.mapper")
public class DroneApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数，交由 Spring Boot 解析
     */
    public static void main(String[] args) {
        SpringApplication.run(DroneApplication.class, args);
    }
}
