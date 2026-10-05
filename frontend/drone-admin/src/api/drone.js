import request from '../utils/request'

/**
 * 获取无人机列表
 * @param {Object} params - 查询参数对象
 * @returns {Promise} - 返回一个Promise对象，包含无人机列表数据
 */
export function getDrones(params) {
  // 发送GET请求到'/drones'接口，并传入params参数
  return request.get('/drones', { params })
}

/**
 * 查询无人机详情
 * @param {number|string} id - 无人机ID
 * @returns {Promise} - 无人机详情
 */
export function getDroneById(id) {
  return request.get(`/drones/${id}`)
}

/**
 * 新增无人机（仅管理员）
 * @param {Object} data - 无人机信息
 * @returns {Promise} - 新增后的无人机
 */
export function createDrone(data) {
  return request.post('/drones', data)
}

/**
 * 修改无人机（仅管理员）
 * @param {number|string} id - 无人机ID
 * @param {Object} data - 待修改的无人机信息
 * @returns {Promise} - 修改后的无人机
 */
export function updateDrone(id, data) {
  return request.put(`/drones/${id}`, data)
}

/**
 * 删除无人机（仅管理员）
 * @param {number|string} id - 无人机ID
 * @returns {Promise} - 操作结果
 */
export function deleteDrone(id) {
  return request.delete(`/drones/${id}`)
}

/**
 * 结束使用：把"使用中"的无人机恢复为"空闲"（仅管理员）
 * @param {number|string} id - 无人机ID
 * @returns {Promise} - 状态更新后的无人机
 */
export function releaseDrone(id) {
  return request.put(`/drones/${id}/release`)
}
