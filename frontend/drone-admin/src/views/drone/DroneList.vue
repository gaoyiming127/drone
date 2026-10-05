<template>
  <div class="page-container">
    <!-- 搜索栏 -->
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="设备编号">
          <el-input v-model="queryForm.droneCode" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="queryForm.droneName" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="queryForm.brand" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @change="search" style="width: 215px">
            <el-option label="空闲" value="IDLE" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="维修中" value="MAINTENANCE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div style="margin-bottom: 15px;">
      <el-button v-if="isAdmin" type="primary" @click="openAddDialog">新增无人机</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="droneCode" label="设备编号" width="140" />
        <el-table-column prop="droneName" label="设备名称" width="160" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalFlightCount" label="飞行次数" width="90" align="center" />
        <el-table-column prop="totalFlightMinutes" label="飞行分钟" width="90" align="center" />
        <el-table-column prop="purchaseDate" label="购买日期" width="110" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="245" align="center" class-name="col-action" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-popconfirm
              v-if="isAdmin && row.status === 'IN_USE'"
              title="确认结束使用并回库？设备状态将变为空闲。"
              @confirm="handleRelease(row.id)"
            >
              <template #reference>
                <el-button text type="success">结束使用</el-button>
              </template>
            </el-popconfirm>
            <el-button v-if="isAdmin" text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm v-if="isAdmin" title="确定删除该无人机？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑无人机' : '新增无人机'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备编号" prop="droneCode">
              <el-input v-model="form.droneCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="droneName">
              <el-input v-model="form.droneName" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="form.model" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input v-model="form.serialNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购买日期" prop="purchaseDate">
              <el-date-picker v-model="form.purchaseDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="空闲" value="IDLE" />
                <el-option label="使用中" value="IN_USE" />
                <el-option label="维修中" value="MAINTENANCE" />
                <el-option label="停用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="无人机详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="设备编号">{{ detail.droneCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detail.droneName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ detail.model }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ detail.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="购买日期">{{ detail.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计飞行">{{ detail.totalFlightCount }}次 / {{ detail.totalFlightMinutes }}分钟</el-descriptions-item>
        <el-descriptions-item label="最近保养">{{ detail.lastMaintenanceDate || '未保养' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 无人机管理页面。
 * 支持按设备编号、名称、品牌、状态组合查询；管理员可新增、编辑、删除无人机，
 * 并对使用中的无人机执行"结束使用"（恢复为空闲）；所有角色均可查看详情。
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import authStore from '../../store/auth'
import { getDrones, getDroneById, createDrone, updateDrone, deleteDrone, releaseDrone } from '../../api/drone'
import { droneStatusMap as statusMap } from '../../utils/format'

const isAdmin = computed(() => authStore.isAdmin)

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = reactive({
  droneCode: '',
  droneName: '',
  brand: '',
  status: ''
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)
const detail = ref({})

const form = reactive({
  droneCode: '',
  droneName: '',
  brand: '',
  model: '',
  serialNumber: '',
  purchaseDate: '',
  status: 'IDLE',
  remark: ''
})

const rules = {
  droneCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  droneName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }]
}

onMounted(() => loadData())

/**
 * 按当前查询条件与分页参数加载无人机列表
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getDrones({
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
  queryForm.droneCode = ''
  queryForm.droneName = ''
  queryForm.brand = ''
  queryForm.status = ''
  search()
}

/**
 * 打开新增对话框，并把表单恢复为初始值
 */
function openAddDialog() {
  isEdit.value = false
  editId.value = null
  form.droneCode = ''
  form.droneName = ''
  form.brand = ''
  form.model = ''
  form.serialNumber = ''
  form.purchaseDate = ''
  form.status = 'IDLE'
  form.remark = ''
  dialogVisible.value = true
}

/**
 * 打开编辑对话框：按ID查询最新数据后回填表单
 * @param {Object} row - 表格当前行数据
 */
async function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  try {
    const res = await getDroneById(row.id)
    const d = res.data
    form.droneCode = d.droneCode
    form.droneName = d.droneName
    form.brand = d.brand
    form.model = d.model
    form.serialNumber = d.serialNumber
    form.purchaseDate = d.purchaseDate
    form.status = d.status
    form.remark = d.remark
    dialogVisible.value = true
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
      await updateDrone(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createDrone(form)
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
 * 删除无人机
 * @param {number} id - 无人机ID
 */
async function handleDelete(id) {
  try {
    await deleteDrone(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

/**
 * 结束使用：把使用中的无人机恢复为空闲
 * @param {number} id - 无人机ID
 */
async function handleRelease(id) {
  try {
    await releaseDrone(id)
    ElMessage.success('已结束使用，设备状态变为空闲')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

/**
 * 查看详情：按ID查询最新数据并弹出详情对话框
 * @param {Object} row - 表格当前行数据
 */
async function viewDetail(row) {
  try {
    const res = await getDroneById(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}
</script>
