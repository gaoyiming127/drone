import request from '../utils/request'

/**
 * 分页查询电池列表
 * @param {Object} params - 查询参数：pageNum、pageSize、batteryCode、model、healthLevel、status
 * @returns {Promise} - 分页结果，包含 data（当前页数据）与 total（总条数）
 */
export function getBatteries(params) {
  return request.get('/batteries', { params })
}

/**
 * 查询电池详情
 * @param {number|string} id - 电池ID
 * @returns {Promise} - 电池详情
 */
export function getBatteryById(id) {
  return request.get(`/batteries/${id}`)
}

/**
 * 新增电池（仅管理员）
 * @param {Object} data - 电池信息，SOH 与健康等级由后端计算
 * @returns {Promise} - 新增后的电池
 */
export function createBattery(data) {
  return request.post('/batteries', data)
}

/**
 * 修改电池（仅管理员）
 * @param {number|string} id - 电池ID
 * @param {Object} data - 待修改的电池信息
 * @returns {Promise} - 修改后的电池
 */
export function updateBattery(id, data) {
  return request.put(`/batteries/${id}`, data)
}

/**
 * 删除电池（仅管理员）
 * @param {number|string} id - 电池ID
 * @returns {Promise} - 操作结果
 */
export function deleteBattery(id) {
  return request.delete(`/batteries/${id}`)
}
