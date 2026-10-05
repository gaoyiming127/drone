package com.drone.service;

import com.drone.vo.DashboardVO;

/**
 * 首页看板业务接口。
 */
public interface DashboardService {

    /**
     * 首页统计数据。
     *
     * @param onlyUserId 只返回该使用人员的飞行记录明细，为空表示不限制（管理员）；
     *                   统计类指标（设备总数、本月飞行次数等）始终为全系统口径
     * @return 首页统计、最近记录、图表数据与系统提醒
     */
    DashboardVO getDashboardData(Long onlyUserId);
}
