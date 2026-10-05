import request from '../utils/request'

/**
 * 分页查询用户列表（仅管理员）
 * @param {Object} params - 查询参数：pageNum、pageSize、keyword（用户名/姓名）
 * @returns {Promise} - 分页结果
 */
export function getUsers(params) {
  return request.get('/users', { params })
}

/**
 * 查询用户下拉选项，供业务表单选择使用人员，普通用户也可访问
 * @returns {Promise} - 仅含ID、登录名与真实姓名的用户列表
 */
export function getUserOptions() {
  return request.get('/user-options')
}

/**
 * 查询用户详情（仅管理员）
 * @param {number|string} id - 用户ID
 * @returns {Promise} - 用户信息（不含密码）
 */
export function getUserById(id) {
  return request.get(`/users/${id}`)
}

/**
 * 新增用户（仅管理员）
 * @param {Object} data - 用户信息，密码必填
 * @returns {Promise} - 新增后的用户
 */
export function createUser(data) {
  return request.post('/users', data)
}

/**
 * 修改用户（仅管理员）
 * @param {number|string} id - 用户ID
 * @param {Object} data - 待修改的用户信息，密码留空表示不修改
 * @returns {Promise} - 修改后的用户
 */
export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

/**
 * 启用/禁用用户（仅管理员），状态以查询参数提交
 * @param {number|string} id - 用户ID
 * @param {number} status - 目标状态：0-禁用，1-启用
 * @returns {Promise} - 操作结果
 */
export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}

/**
 * 删除用户（仅管理员）
 * @param {number|string} id - 用户ID
 * @returns {Promise} - 操作结果
 */
export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}
