import request from '../utils/request'

/**
 * 登录功能函数
 * @param {Object} data - 登录所需的数据对象，通常包含用户名和密码等信息
 * @returns {Promise} - 返回一个Promise对象，包含登录请求的结果
 */
export function login(data) {
  // 发送POST请求到登录接口，并传入登录数据
  return request.post('/auth/login', data)
}

/**
 * 用户注册函数
 * @param {Object} data - 注册所需的数据对象
 * @returns {Promise} - 返回一个Promise对象，包含注册请求的响应结果
 */
export function register(data) {
  // 使用request发送POST请求到'/auth/register'接口，并传入data参数
  return request.post('/auth/register', data)
}

/**
 * 更新用户密码的函数
 * @param {Object} data - 包含更新密码所需的数据对象
 * @returns {Promise} - 返回一个Promise对象，包含服务器响应结果
 */
export function updatePassword(data) {
  // 使用request发送PUT请求到'/auth/password'接口，并传入data参数
  return request.put('/auth/password', data)
}
