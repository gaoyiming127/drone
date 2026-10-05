import request from '../utils/request'

/**
 * 分页查询维修记录
 * @param {Object} params - 查询参数：pageNum、pageSize、deviceType、status
 * @returns {Promise} - 分页结果
 */
export function getMaintenanceRecords(params) {
  return request.get('/maintenance-records', { params })
}

/**
 * 查询维修记录详情
 * @param {number|string} id - 维修记录ID
 * @returns {Promise} - 记录详情，含设备编号名称与创建人姓名
 */
export function getMaintenanceRecordById(id) {
  return request.get(`/maintenance-records/${id}`)
}

/**
 * 新增维修工单（仅管理员）
 * @param {Object} data - 工单信息
 * @returns {Promise} - 新增后的工单详情
 */
export function createMaintenanceRecord(data) {
  return request.post('/maintenance-records', data)
}

/**
 * 修改未完成的维修工单（仅管理员）
 * @param {number|string} id - 工单ID
 * @param {Object} data - 待修改的工单信息
 * @returns {Promise} - 修改后的工单详情
 */
export function updateMaintenanceRecord(id, data) {
  return request.put(`/maintenance-records/${id}`, data)
}

/**
 * 完成维修（仅管理员），维修结果以查询参数提交
 * @param {number|string} id - 工单ID
 * @param {Object} params - 维修结果参数：result（RESTORED/SCRAPPED）、maintenanceContent、maintenanceCost
 * @returns {Promise} - 完成后的工单详情
 */
export function completeMaintenance(id, params) {
  return request.put(`/maintenance-records/${id}/complete`, null, { params })
}

/**
 * 删除维修工单（仅管理员）
 * @param {number|string} id - 工单ID
 * @returns {Promise} - 操作结果
 */
export function deleteMaintenanceRecord(id) {
  return request.delete(`/maintenance-records/${id}`)
}
