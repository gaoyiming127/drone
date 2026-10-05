import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由表：登录页独立于主布局之外，其余页面均为 MainLayout 的子路由，
 * meta.title 用于顶部面包屑，meta.adminOnly 标记仅管理员可见的页面。
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '总览', icon: 'Odometer' }
      },
      {
        path: 'drones',
        name: 'DroneList',
        component: () => import('../views/drone/DroneList.vue'),
        meta: { title: '无人机管理', icon: 'Airplane' }
      },
      {
        path: 'batteries',
        name: 'BatteryList',
        component: () => import('../views/battery/BatteryList.vue'),
        meta: { title: '电池管理', icon: 'Coin' }
      },
      {
        path: 'flights',
        name: 'FlightRecordList',
        component: () => import('../views/flight/FlightRecordList.vue'),
        meta: { title: '飞行记录', icon: 'List' }
      },
      {
        path: 'maintenance',
        name: 'MaintenanceList',
        component: () => import('../views/maintenance/MaintenanceList.vue'),
        meta: { title: '维修保养', icon: 'Tools' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', adminOnly: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫：未登录时跳转登录页；访问仅管理员可见的页面时非管理员跳回总览
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 检查管理员页面
  if (to.meta.adminOnly) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.role !== 'ADMIN') {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
