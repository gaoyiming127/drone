// 全局通用的字典映射与格式化方法，供各列表页与详情弹窗复用。

/**
 * 设备状态映射：键与后端 drone.status 的取值一致
 * @type {Object<string, {label: string, type: string}>}
 */
export const droneStatusMap = {
  IDLE: { label: '空闲', type: 'success' },
  IN_USE: { label: '使用中', type: 'primary' },
  MAINTENANCE: { label: '维修中', type: 'warning' },
  DISABLED: { label: '停用', type: 'danger' }
}

/**
 * 电池状态映射：键与后端 battery.status 的取值一致
 * @type {Object<string, {label: string, type: string}>}
 */
export const batteryStatusMap = {
  NORMAL: { label: '正常', type: 'success' },
  IN_USE: { label: '使用中', type: 'primary' },
  ABNORMAL: { label: '异常', type: 'danger' },
  DISABLED: { label: '停用', type: 'info' }
}

/**
 * 健康等级映射：键与后端 battery.health_level 的取值一致
 * @type {Object<string, {label: string, type: string}>}
 */
export const healthLevelMap = {
  GOOD: { label: '良好', type: 'success' },
  ATTENTION: { label: '注意', type: 'warning' },
  DANGEROUS: { label: '危险', type: 'danger' }
}

/**
 * 维修状态映射：键与后端 maintenance_record.status 的取值一致
 * @type {Object<string, {label: string, type: string}>}
 */
export const maintenanceStatusMap = {
  PENDING: { label: '待维修', type: 'info' },
  PROCESSING: { label: '维修中', type: 'warning' },
  COMPLETED: { label: '已完成', type: 'success' }
}

/**
 * 维修结果映射：键与后端 maintenance_record.result 的取值一致
 *
 * 【待人工确认】当前前端未引用该映射（维修列表与详情中的结果标签使用三元表达式直接判断），
 * 因无法确认是否存在外部依赖，暂予保留，请人工确认后再决定是否删除。
 * @type {Object<string, {label: string, type: string}>}
 */
export const maintenanceResultMap = {
  RESTORED: { label: '已恢复', type: 'success' },
  SCRAPPED: { label: '已报废', type: 'danger' }
}

/**
 * 设备类型映射：键与后端 device_type 的取值一致
 *
 * 【待人工确认】当前前端未引用该映射（维修列表与详情中的设备类型标签使用三元表达式直接判断），
 * 因无法确认是否存在外部依赖，暂予保留，请人工确认后再决定是否删除。
 * @type {Object<string, {label: string, type: string}>}
 */
export const deviceTypeMap = {
  DRONE: { label: '无人机', type: 'primary' },
  BATTERY: { label: '电池', type: 'warning' }
}

/**
 * 格式化日期为本地日期字符串
 *
 * 【待人工确认】当前前端未引用该方法，因无法确认是否存在外部依赖，暂予保留，请人工确认。
 * @param {string|Date} date - 日期值
 * @returns {string} 形如 2025/6/18 的本地日期；空值返回空字符串
 */
export function formatDate(date) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

/**
 * 格式化日期与时间为本地日期时间字符串
 *
 * 【待人工确认】当前前端未引用该方法，因无法确认是否存在外部依赖，暂予保留，请人工确认。
 * @param {string|Date} date - 日期时间值
 * @returns {string} 形如 2025/6/18 10:30:00 的本地日期时间；空值返回空字符串
 */
export function formatDateTime(date) {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

/**
 * 格式化金额
 *
 * 【待人工确认】当前前端未引用该方法（列表与详情中的费用直接拼接 ¥ 符号），
 * 因无法确认是否存在外部依赖，暂予保留，请人工确认。
 * @param {number|string} amount - 金额
 * @returns {string} 形如 ¥100.00 的字符串；空值返回 ¥0.00
 */
export function formatMoney(amount) {
  if (!amount) return '¥0.00'
  return '¥' + parseFloat(amount).toFixed(2)
}

/**
 * 格式化百分比
 *
 * 【待人工确认】当前前端未引用该方法，因无法确认是否存在外部依赖，暂予保留，请人工确认。
 * @param {number|string} value - 百分比数值
 * @returns {string} 保留一位小数的百分比；空值返回 --
 */
export function formatPercent(value) {
  if (value === null || value === undefined) return '--'
  return parseFloat(value).toFixed(1) + '%'
}
