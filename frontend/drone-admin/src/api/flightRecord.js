import request from '../utils/request'

/**
 * 分页查询飞行记录
 * @param {Object} params - 查询参数：pageNum、pageSize、droneCode、userName、flightDate
 * @returns {Promise} - 分页结果，普通用户仅返回本人登记的记录
 */
export function getFlightRecords(params) {
  return request.get('/flight-records', { params })
}

/**
 * 查询飞行记录详情
 * @param {number|string} id - 飞行记录ID
 * @returns {Promise} - 记录详情，含电池使用明细
 */
export function getFlightRecordById(id) {
  return request.get(`/flight-records/${id}`)
}

/**
 * 登记飞行记录
 * @param {Object} data - 飞行记录信息，含 batteries 电池使用明细
 * @returns {Promise} - 新增后的飞行记录详情
 */
export function createFlightRecord(data) {
  return request.post('/flight-records', data)
}

/**
 * 删除飞行记录
 * @param {number|string} id - 飞行记录ID
 * @returns {Promise} - 操作结果，普通用户只能删除本人登记的记录
 */
export function deleteFlightRecord(id) {
  return request.delete(`/flight-records/${id}`)
}
