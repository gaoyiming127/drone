import { reactive } from 'vue'

/**
 * 登录状态存储：以 reactive 对象保存当前令牌与用户信息，并与 localStorage 保持同步，
 * 页面刷新后可从 localStorage 恢复登录状态。
 */
const userStr = localStorage.getItem('user')
const user = userStr ? JSON.parse(userStr) : null

const authStore = reactive({
  token: localStorage.getItem('token') || '',
  user: user,
  isLoggedIn: !!localStorage.getItem('token'),
  isAdmin: user?.role === 'ADMIN'
})

/**
 * 登录成功后写入令牌与用户信息（同时写入 localStorage）
 * @param {string} token - 后端签发的 JWT 令牌
 * @param {Object} userInfo - 用户信息：userId、username、realName、role
 */
export function login(token, userInfo) {
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(userInfo))
  authStore.token = token
  authStore.user = userInfo
  authStore.isLoggedIn = true
  authStore.isAdmin = userInfo.role === 'ADMIN'
}

/**
 * 退出登录：清空内存与 localStorage 中的登录状态
 */
export function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  authStore.token = ''
  authStore.user = null
  authStore.isLoggedIn = false
  authStore.isAdmin = false
}

export default authStore
