<template>
  <div class="page-container">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="用户名/姓名" clearable @clear="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-bottom: 15px;">
      <el-button type="primary" @click="openAddDialog">新增用户</el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="175" align="center" class-name="col-action" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button
              v-if="row.username !== 'admin'"
              :type="row.status === 1 ? 'warning' : 'success'"
              text
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm v-if="row.username !== 'admin'" title="确定删除该用户？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ isEdit ? '确认修改' : '确认新增' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 用户管理页面（仅管理员可见）。
 * 支持按用户名/姓名检索；可新增、编辑用户，切换启用/禁用状态以及删除用户；
 * 内置 admin 账号不允许被禁用或删除（与后端"至少保留一个启用管理员"的约束一致）。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, getUserById, createUser, updateUser, updateUserStatus, deleteUser } from '../../api/user'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = reactive({
  keyword: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => loadData())

/**
 * 按当前查询条件与分页参数加载用户列表
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getUsers({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...queryForm
    })
    tableData.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/**
 * 点击查询：回到第一页后重新加载
 */
function search() {
  currentPage.value = 1
  loadData()
}

/**
 * 重置查询条件后重新查询
 */
function resetSearch() {
  queryForm.keyword = ''
  search()
}

/**
 * 打开新增对话框，并把表单恢复为初始值（角色默认普通用户、状态默认启用）
 */
function openAddDialog() {
  isEdit.value = false
  editId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.role = 'USER'
  form.status = 1
  dialogVisible.value = true
  // 密码为必填
  rules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 打开编辑对话框：按ID查询最新数据后回填表单，密码留空表示不修改
 * @param {Object} row - 表格当前行数据
 */
async function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  try {
    const res = await getUserById(row.id)
    const d = res.data
    form.username = d.username
    form.password = ''
    form.realName = d.realName
    form.email = d.email
    form.phone = d.phone
    form.role = d.role
    form.status = d.status
    dialogVisible.value = true
    // 编辑时密码非必填
    rules.password = []
  } catch (e) {
    console.error(e)
  }
}

/**
 * 提交表单：校验通过后按当前模式调用新增或修改接口，成功后关闭对话框并刷新列表
 */
async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUser(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 切换用户的启用/禁用状态
 * @param {Object} row - 表格当前行数据
 */
async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '用户已启用' : '用户已禁用')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

/**
 * 删除用户
 * @param {number} id - 用户ID
 */
async function handleDelete(id) {
  try {
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}
</script>
