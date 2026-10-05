package com.drone.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.drone.dto.BatteryDto;
import com.drone.entity.Battery;

/**
 * 电池管理业务接口。
 *
 * <p>除常规增删改查外，还负责 SOH 计算与循环次数带来的容量衰减。</p>
 */
public interface BatteryService {

    /**
     * 分页查询电池列表。
     *
     * @param pageNum     页码，从 1 开始
     * @param pageSize    每页条数
     * @param batteryCode 电池编号，模糊匹配，可为空
     * @param model       型号，模糊匹配，可为空
     * @param healthLevel 健康等级，精确匹配，可为空
     * @param status      电池状态，精确匹配，可为空
     * @return 分页结果
     */
    IPage<Battery> pageBatteries(int pageNum, int pageSize, String batteryCode, String model, String healthLevel, String status);

    /**
     * 按ID查询电池详情。
     *
     * @param id 电池ID
     * @return 电池信息
     * @throws com.drone.exception.BusinessException 电池不存在时抛出
     */
    Battery getBatteryById(Long id);

    /**
     * 新增电池：校验编号唯一后写入，并依据容量计算 SOH 与健康等级。
     *
     * @param batteryDto 电池信息
     * @return 新增后的电池（含计算得出的 SOH 与健康等级）
     */
    Battery createBattery(BatteryDto batteryDto);

    /**
     * 修改电池：编号变更时校验唯一性，字段留空表示保持原值，最后重新计算 SOH 与健康等级。
     *
     * @param id         电池ID
     * @param batteryDto 待修改的电池信息
     * @return 修改后的电池
     */
    Battery updateBattery(Long id, BatteryDto batteryDto);

    /**
     * 删除电池：已被飞行记录使用或存在维修记录时不允许删除。
     *
     * @param id 电池ID
     */
    void deleteBattery(Long id);

    /**
     * 依据标称容量与当前满充容量计算健康度 SOH 与健康等级。
     *
     * <p>SOH = 当前满充容量 / 标称容量 × 100，超过 100% 按 100% 计；
     * 出现鼓包时健康等级直接判为 DANGEROUS。</p>
     *
     * @param battery 待计算的电池对象，结果直接写回该对象
     */
    void calculateSoh(Battery battery);

    /**
     * 循环次数增加时对当前满充容量做衰减：按标称容量乘以衰减率的固定幅度扣减，
     * 并保证不低于标称容量的设定比例。调用后需再调用 calculateSoh 刷新健康度与健康等级。
     *
     * @param battery 待衰减的电池对象，结果直接写回该对象
     */
    void applyCycleDecay(Battery battery);
}
