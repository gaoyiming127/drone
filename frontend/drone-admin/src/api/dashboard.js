import request from '../utils/request'

/**
 * 查询首页统计数据
 * @returns {Promise} - 首页统计指标、最近记录、图表数据与系统提醒；
 *                      普通用户仅返回本人的飞行记录明细
 */
export function getDashboardStatistics() {
  return request.get('/dashboard/statistics')
}
