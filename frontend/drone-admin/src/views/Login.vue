<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h2>🛸 无人机设备与电池管理系统</h2>
        <p>请登录您的账号</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
        <div style="text-align: center;">
          <el-button text type="primary" @click="registerDialogVisible = true">注册新账号</el-button>
        </div>
      </el-form>
      <div class="login-footer">
        <p>请使用管理员分配的账号登录</p>
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog v-model="registerDialogVisible" title="注册新账号" width="420px" append-to-body>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="至少6位密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerLoading" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 登录页面。
 * 提供账号登录与自助注册两个入口：登录成功后写入令牌与用户信息并跳转总览页；
 * 注册提交前在本地校验两次密码是否一致，注册成功后关闭对话框。
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login as loginApi, register as registerApi } from '../api/auth'
import { login as setAuth } from '../store/auth'

const router = useRouter()
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const registerLoading = ref(false)
const registerDialogVisible = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  phone: ''
})

/**
 * 注册表单自定义校验：确认密码必须与密码一致
 * @param {Object} rule - 校验规则
 * @param {string} value - 确认密码输入值
 * @param {Function} callback - 校验回调，传入 Error 表示校验不通过
 */
const validateConfirm = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

/**
 * 登录：校验表单后调用登录接口，成功则保存登录状态并跳转总览页
 */
async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await loginApi(loginForm)
    const { token, userId, username, realName, role } = res.data
    setAuth(token, { userId, username, realName, role })
    ElMessage.success('登录成功')
    window.location.href = '/dashboard'
  } catch (e) {
    console.error('登录失败:', e)
  } finally {
    loading.value = false
  }
}

/**
 * 注册：校验表单后调用注册接口，成功后关闭对话框并清空表单（角色与状态由后端设定）
 */
async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  registerLoading.value = true
  try {
    const payload = {
      username: registerForm.username,
      password: registerForm.password,
      realName: registerForm.realName,
      email: registerForm.email || undefined,
      phone: registerForm.phone || undefined
    }
    await registerApi(payload)
    ElMessage.success('注册成功，请登录')
    registerDialogVisible.value = false
    // 清空注册表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.realName = ''
    registerForm.email = ''
    registerForm.phone = ''
  } catch (e) {
    console.error('注册失败:', e)
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: url('/背景1.jpg') center/100% 100% no-repeat;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.45);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 8px;
}
.login-header p {
  font-size: 14px;
  color: #909399;
}
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #909399;
}
.login-footer p {
  margin: 4px 0;
}
</style>
