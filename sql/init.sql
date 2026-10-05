-- ============================================
-- 无人机设备与电池管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS drone_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE drone_manager;

-- ============================================
-- 1. 系统用户表
-- ============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ============================================
-- 2. 无人机表
-- ============================================
CREATE TABLE IF NOT EXISTS drone (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    drone_code VARCHAR(50) NOT NULL COMMENT '设备编号',
    drone_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    brand VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    model VARCHAR(50) DEFAULT NULL COMMENT '型号',
    serial_number VARCHAR(100) DEFAULT NULL COMMENT '序列号',
    purchase_date DATE DEFAULT NULL COMMENT '购买日期',
    status VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT '设备状态：IDLE-空闲，IN_USE-使用中，MAINTENANCE-维修中，DISABLED-停用',
    total_flight_count INT NOT NULL DEFAULT 0 COMMENT '累计飞行次数',
    total_flight_minutes INT NOT NULL DEFAULT 0 COMMENT '累计飞行分钟数',
    last_maintenance_date DATE DEFAULT NULL COMMENT '最近保养日期',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_drone_code (drone_code),
    INDEX idx_status (status),
    INDEX idx_brand (brand),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='无人机表';

-- ============================================
-- 3. 电池表
-- ============================================
CREATE TABLE IF NOT EXISTS battery (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    battery_code VARCHAR(50) NOT NULL COMMENT '电池编号',
    battery_name VARCHAR(100) NOT NULL COMMENT '电池名称',
    brand VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    model VARCHAR(50) DEFAULT NULL COMMENT '型号',
    serial_number VARCHAR(100) DEFAULT NULL COMMENT '序列号',
    rated_capacity DECIMAL(10,2) NOT NULL COMMENT '标称容量(mAh)',
    full_charge_capacity DECIMAL(10,2) NOT NULL COMMENT '当前满充容量(mAh)',
    cycle_count INT NOT NULL DEFAULT 0 COMMENT '循环次数',
    usage_count INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    total_use_minutes INT NOT NULL DEFAULT 0 COMMENT '累计使用分钟数',
    soh DECIMAL(5,2) DEFAULT NULL COMMENT '健康度(%)',
    health_level VARCHAR(20) DEFAULT NULL COMMENT '健康等级：GOOD-良好，ATTENTION-注意，DANGEROUS-危险',
    swollen TINYINT NOT NULL DEFAULT 0 COMMENT '是否鼓包：0-否，1-是',
    overheated TINYINT NOT NULL DEFAULT 0 COMMENT '是否过热：0-否，1-是',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '电池状态：NORMAL-正常，IN_USE-使用中，ABNORMAL-异常，DISABLED-停用',
    last_use_time DATETIME DEFAULT NULL COMMENT '最近使用时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_battery_code (battery_code),
    INDEX idx_status (status),
    INDEX idx_health_level (health_level),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电池表';

-- ============================================
-- 4. 飞行记录表
-- ============================================
CREATE TABLE IF NOT EXISTS flight_record (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    record_code VARCHAR(50) NOT NULL COMMENT '记录编号',
    drone_id BIGINT NOT NULL COMMENT '无人机ID',
    user_id BIGINT NOT NULL COMMENT '使用人员ID',
    flight_location VARCHAR(200) DEFAULT NULL COMMENT '飞行地点',
    flight_date DATE NOT NULL COMMENT '飞行日期',
    flight_minutes INT NOT NULL COMMENT '飞行分钟数',
    takeoff_count INT NOT NULL DEFAULT 1 COMMENT '起飞次数',
    has_exception TINYINT NOT NULL DEFAULT 0 COMMENT '是否发生异常：0-否，1-是',
    exception_description VARCHAR(500) DEFAULT NULL COMMENT '异常描述',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_code (record_code),
    INDEX idx_drone_id (drone_id),
    INDEX idx_user_id (user_id),
    INDEX idx_flight_date (flight_date),
    INDEX idx_created_at (created_at),
    CONSTRAINT fk_flight_record_drone FOREIGN KEY (drone_id) REFERENCES drone(id),
    CONSTRAINT fk_flight_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行记录表';

-- ============================================
-- 5. 飞行记录-电池关联表
-- ============================================
CREATE TABLE IF NOT EXISTS flight_record_battery (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    flight_record_id BIGINT NOT NULL COMMENT '飞行记录ID',
    battery_id BIGINT NOT NULL COMMENT '电池ID',
    start_power DECIMAL(5,2) NOT NULL COMMENT '开始电量(%)',
    end_power DECIMAL(5,2) NOT NULL COMMENT '结束电量(%)',
    use_minutes INT NOT NULL COMMENT '使用分钟数',
    PRIMARY KEY (id),
    INDEX idx_flight_record_id (flight_record_id),
    INDEX idx_battery_id (battery_id),
    CONSTRAINT fk_frb_flight_record FOREIGN KEY (flight_record_id) REFERENCES flight_record(id) ON DELETE CASCADE,
    CONSTRAINT fk_frb_battery FOREIGN KEY (battery_id) REFERENCES battery(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='飞行记录电池关联表';

-- ============================================
-- 6. 维修记录表
-- ============================================
CREATE TABLE IF NOT EXISTS maintenance_record (
    id BIGINT AUTO_INCREMENT COMMENT '主键ID',
    maintenance_code VARCHAR(50) NOT NULL COMMENT '维修编号',
    device_type VARCHAR(20) NOT NULL COMMENT '设备类型：DRONE-无人机，BATTERY-电池',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    fault_description VARCHAR(500) DEFAULT NULL COMMENT '故障描述',
    maintenance_content VARCHAR(1000) DEFAULT NULL COMMENT '维修内容',
    maintenance_date DATE DEFAULT NULL COMMENT '维修日期',
    maintenance_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '维修费用',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '维修状态：PENDING-待维修，PROCESSING-维修中，COMPLETED-已完成',
    result VARCHAR(20) DEFAULT NULL COMMENT '维修结果：RESTORED-恢复，SCRAPPED-报废',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_maintenance_code (maintenance_code),
    INDEX idx_device_type_device_id (device_type, device_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表';

-- ============================================
-- 测试数据（密码由系统启动时通过DataInitializer初始化）
-- ============================================
INSERT IGNORE INTO sys_user (username, password, real_name, role, status) VALUES
('admin', '', '系统管理员', 'ADMIN', 1),
('user', '', '普通用户', 'USER', 1);

INSERT IGNORE INTO drone (drone_code, drone_name, brand, model, serial_number, purchase_date, status, total_flight_count, total_flight_minutes, last_maintenance_date) VALUES
('DRN-2024-001', '大疆Mavic Air 3', 'DJI', 'Mavic Air 3', 'MA3-2024-00001', '2024-01-15', 'IDLE', 45, 1280, '2025-06-01'),
('DRN-2024-002', '大疆Phantom 5', 'DJI', 'Phantom 5', 'PH5-2024-00002', '2024-03-20', 'IN_USE', 78, 2450, '2025-05-15'),
('DRN-2024-003', '大疆Mini 4 Pro', 'DJI', 'Mini 4 Pro', 'MI4-2024-00003', '2024-06-10', 'MAINTENANCE', 32, 890, '2025-04-20'),
('DRN-2024-004', 'Autel EVO II', 'Autel', 'EVO II Pro', 'EVO-2024-00004', '2024-08-05', 'IDLE', 56, 1670, '2025-06-10'),
('DRN-2025-001', '大疆Mavic 4', 'DJI', 'Mavic 4', 'MA4-2025-00001', '2025-02-01', 'DISABLED', 12, 340, '2025-01-15');

INSERT IGNORE INTO battery (battery_code, battery_name, brand, model, serial_number, rated_capacity, full_charge_capacity, cycle_count, usage_count, total_use_minutes, soh, health_level, swollen, overheated, status) VALUES
('BAT-2024-001', '大疆智能飞行电池', 'DJI', 'Mavic Air 3 Battery', 'MA3B-2024-001', 5000.00, 4850.00, 45, 80, 1280, 97.00, 'GOOD', 0, 0, 'NORMAL'),
('BAT-2024-002', '大疆智能飞行电池', 'DJI', 'Mavic Air 3 Battery', 'MA3B-2024-002', 5000.00, 4200.00, 120, 200, 3200, 84.00, 'ATTENTION', 0, 0, 'NORMAL'),
('BAT-2024-003', '大疆智能飞行电池', 'DJI', 'Phantom 5 Battery', 'PH5B-2024-001', 6000.00, 3500.00, 200, 350, 5600, 58.33, 'DANGEROUS', 1, 0, 'ABNORMAL'),
('BAT-2024-004', '大疆智能飞行电池', 'DJI', 'Mini 4 Pro Battery', 'MI4B-2024-001', 2500.00, 2300.00, 30, 50, 890, 92.00, 'GOOD', 0, 0, 'IN_USE'),
('BAT-2024-005', 'Autel智能电池', 'Autel', 'EVO II Pro Battery', 'EVOB-2024-001', 7000.00, 6300.00, 80, 150, 2400, 90.00, 'GOOD', 0, 0, 'NORMAL'),
('BAT-2024-006', '大疆智能飞行电池', 'DJI', 'Mavic 4 Battery', 'MA4B-2024-001', 5500.00, 2800.00, 320, 380, 6500, 50.91, 'DANGEROUS', 1, 1, 'DISABLED');

INSERT IGNORE INTO flight_record (record_code, drone_id, user_id, flight_location, flight_date, flight_minutes, takeoff_count, has_exception, exception_description, created_at) VALUES
('FL-2025-06-001', 1, 2, '深圳湾公园', '2025-06-01', 25, 2, 0, NULL, '2025-06-01 10:00:00'),
('FL-2025-06-002', 1, 2, '梧桐山', '2025-06-03', 35, 3, 0, NULL, '2025-06-03 14:00:00'),
('FL-2025-06-003', 2, 2, '大梅沙', '2025-06-05', 40, 2, 0, NULL, '2025-06-05 09:00:00'),
('FL-2025-06-004', 3, 2, '莲花山公园', '2025-06-08', 20, 1, 1, '飞行中遇到强风', '2025-06-08 16:00:00'),
('FL-2025-06-005', 2, 2, '前海石公园', '2025-06-10', 30, 2, 0, NULL, '2025-06-10 11:00:00'),
('FL-2025-07-001', 1, 2, '人才公园', '2025-07-01', 28, 2, 0, NULL, '2025-07-01 10:30:00'),
('FL-2025-07-002', 2, 2, '塘朗山', '2025-07-05', 45, 3, 0, NULL, '2025-07-05 15:00:00');

INSERT IGNORE INTO flight_record_battery (flight_record_id, battery_id, start_power, end_power, use_minutes) VALUES
(1, 1, 100.00, 55.00, 25),
(2, 1, 100.00, 40.00, 35),
(3, 2, 100.00, 35.00, 40),
(4, 4, 100.00, 60.00, 20),
(5, 2, 100.00, 50.00, 30),
(6, 1, 100.00, 45.00, 28),
(7, 2, 100.00, 30.00, 45);

INSERT IGNORE INTO maintenance_record (maintenance_code, device_type, device_id, fault_description, maintenance_content, maintenance_date, maintenance_cost, status, result, created_by, created_at) VALUES
('MT-2025-06-001', 'DRONE', 3, '电机异响，飞行时有异常震动', '更换电机和螺旋桨，重新校准IMU', '2025-06-12', 350.00, 'COMPLETED', 'RESTORED', 1, '2025-06-11 09:00:00'),
('MT-2025-06-002', 'BATTERY', 3, '电池鼓包，无法正常充电', '经检测电池已损坏，建议报废', '2025-06-15', 0.00, 'COMPLETED', 'SCRAPPED', 1, '2025-06-14 10:00:00'),
('MT-2025-07-001', 'DRONE', 3, '图传信号不稳定，经常断连', NULL, NULL, 0.00, 'PENDING', NULL, 1, '2025-07-10 14:00:00');
