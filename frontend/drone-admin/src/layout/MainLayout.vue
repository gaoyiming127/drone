<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" style="background-color: #304156; transition: width 0.3s">
      <div class="logo" :style="{ width: isCollapse ? '64px' : '220px' }">
        <span v-if="!isCollapse">🛸 无人机管理系统</span>
        <span v-else>🛸</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>总览</span>
        </el-menu-item>
        <el-menu-item index="/drones">
          <el-icon><Monitor /></el-icon>
          <span>无人机管理</span>
        </el-menu-item>
        <el-menu-item index="/batteries">
          <el-icon><Coin /></el-icon>
          <span>电池管理</span>
        </el-menu-item>
        <el-menu-item index="/flights">
          <el-icon><List /></el-icon>
          <span>飞行记录</span>
        </el-menu-item>
        <el-menu-item index="/maintenance">
          <el-icon><Tools /></el-icon>
          <span>维修保养</span>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
        <div style="display: flex; align-items: center;">
          <el-button @click="toggleCollapse" text>
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/" style="margin-left: 20px;">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">总览</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle && route.path !== '/dashboard'">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div style="display: flex; align-items: center; gap: 15px;">
          <el-tag type="info" size="small">{{ isAdmin ? '管理员' : '普通用户' }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span style="cursor: pointer; display: flex; align-items: center; gap: 5px;">
              <el-avatar :size="28">{{ username?.charAt(0)?.toUpperCase() }}</el-avatar>
              <span>{{ realName || username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main style="background-color: #f0f2f5; padding: 20px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改密码对话框 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPassword">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 主布局：左侧菜单（按角色隐藏用户管理）、顶部面包屑与用户下拉菜单、
 * 右侧内容区渲染子路由页面，并提供修改密码对话框与退出登录。
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Odometer, Monitor, Coin, List, Tools, User, Fold, Expand } from '@element-plus/icons-vue'
import authStore, { logout } from '../store/auth'
import { updatePassword } from '../api/auth'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: ''
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度至少6位', trigger: 'blur' }]
}

const username = computed(() => authStore.user?.username)
const realName = computed(() => authStore.user?.realName)
const isAdmin = computed(() => authStore.isAdmin)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title)

/**
 * 折叠/展开左侧菜单
 */
function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

/**
 * 顶部用户下拉菜单命令处理：打开修改密码对话框或退出登录
 * @param {string} command - 菜单命令：password-修改密码，logout-退出登录
 */
function handleCommand(command) {
  if (command === 'password') {
    passwordForm.value = { oldPassword: '', newPassword: '' }
    passwordDialogVisible.value = true
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示').then(() => {
      logout()
      router.push('/login')
    }).catch(() => {})
  }
}

/**
 * 提交修改密码：校验表单通过后调用接口，成功后关闭对话框
 */
async function submitPassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await updatePassword(passwordForm.value)
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (e) {
    // 错误已在拦截器中处理
  }
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #263445;
  overflow: hidden;
  white-space: nowrap;
}
.el-aside {
  overflow: hidden;
}
.el-menu {
  border-right: none;
}
</style>
