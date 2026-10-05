<template>
  <div class="page-container">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="设备类型">
          <el-select v-model="queryForm.deviceType" placeholder="全部" clearable @change="search" style="width: 215px">
            <el-option label="无人机" value="DRONE" />
            <el-option label="电池" value="BATTERY" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @change="search" style="width: 215px">
            <el-option label="待维修" value="PENDING" />
            <el-option label="维修中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-bottom: 15px;">
      <el-button v-if="isAdmin" type="primary" @click="openAddDialog">新增维修记录</el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="maintenanceCode" label="维修编号" width="150" />
        <el-table-column label="设备类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.deviceType === 'DRONE' ? 'primary' : 'warning'" size="small">
              {{ row.deviceType === 'DRONE' ? '无人机' : '电池' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceCode" label="设备编号" width="130" />
        <el-table-column prop="deviceName" label="设备名称" width="140" />
        <el-table-column prop="faultDescription" label="故障描述" width="180" show-overflow-tooltip />
        <el-table-column prop="maintenanceDate" label="维修日期" width="110" />
        <el-table-column prop="maintenanceCost" label="费用" width="90" align="right">
          <template #default="{ row }">¥{{ row.maintenanceCost }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="maintenanceStatusMap[row.status]?.type || 'info'" size="small">
              {{ maintenanceStatusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.result" :type="row.result === 'RESTORED' ? 'success' : 'danger'" size="small">
              {{ row.result === 'RESTORED' ? '已恢复' : '已报废' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="215" align="center" class-name="col-action" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="isAdmin && row.status !== 'COMPLETED'" text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="isAdmin && row.status !== 'COMPLETED'" text type="success" @click="openCompleteDialog(row)">完成</el-button>
            <el-popconfirm v-if="isAdmin" title="确定删除该记录？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑维修记录' : '新增维修记录'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="设备类型" prop="deviceType">
              <el-select v-model="form.deviceType" style="width: 100%" :disabled="isEdit" @change="onDeviceTypeChange">
                <el-option label="无人机" value="DRONE" />
                <el-option label="电池" value="BATTERY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备" prop="deviceId">
              <el-select v-model="form.deviceId" filterable style="width: 100%" :disabled="isEdit">
                <el-option v-for="d in deviceOptions" :key="d.id" :label="`${d.code} - ${d.name}`" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="form.faultDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="维修内容" prop="maintenanceContent">
          <el-input v-model="form.maintenanceContent" type="textarea" :rows="2" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="维修日期" prop="maintenanceDate">
              <el-date-picker v-model="form.maintenanceDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维修费用" prop="maintenanceCost">
              <el-input-number v-model="form.maintenanceCost" :min="0" :step="50" style="width: 100%" :precision="2" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">{{ isEdit ? '确认修改' : '确认新增' }}</el-button>
      </template>
    </el-dialog>

    <!-- 完成维修对话框 -->
    <el-dialog v-model="completeVisible" title="完成维修" width="450px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="维修结果" required>
          <el-radio-group v-model="completeForm.result">
            <el-radio value="RESTORED">恢复 - 设备恢复正常使用</el-radio>
            <el-radio value="SCRAPPED">报废 - 设备停用处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维修内容">
          <el-input v-model="completeForm.maintenanceContent" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.maintenanceCost" :min="0" :step="50" style="width: 100%" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="submitComplete">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="维修记录详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="维修编号">{{ detail.maintenanceCode }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">
          <el-tag :type="detail.deviceType === 'DRONE' ? 'primary' : 'warning'">{{ detail.deviceType === 'DRONE' ? '无人机' : '电池' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ detail.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detail.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ detail.faultDescription || '无' }}</el-descriptions-item>
        <el-descriptions-item label="维修内容" :span="2">{{ detail.maintenanceContent || '无' }}</el-descriptions-item>
        <el-descriptions-item label="维修日期">{{ detail.maintenanceDate || '未完成' }}</el-descriptions-item>
        <el-descriptions-item label="维修费用">¥{{ detail.maintenanceCost }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="maintenanceStatusMap[detail.status]?.type">{{ maintenanceStatusMap[detail.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag v-if="detail.result" :type="detail.result === 'RESTORED' ? 'success' : 'danger'">
            {{ detail.result === 'RESTORED' ? '已恢复' : '已报废' }}
          </el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">{{ detail.createdByName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 维修保养页面。
 * 支持按设备类型与维修状态查询；管理员可新增、编辑、删除工单，并通过"完成"登记维修结果
 * （恢复或报废，结果会联动更新设备状态）；所有角色均可查看详情。
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import authStore from '../../store/auth'
import { getMaintenanceRecords, getMaintenanceRecordById, createMaintenanceRecord, updateMaintenanceRecord, completeMaintenance, deleteMaintenanceRecord } from '../../api/maintenanceRecord'
import { getDrones } from '../../api/drone'
import { getBatteries } from '../../api/battery'
import { maintenanceStatusMap } from '../../utils/format'

const isAdmin = computed(() => authStore.isAdmin)

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = reactive({
  deviceType: '',
  status: ''
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const completeVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const submitLoading = ref(false)
const completeLoading = ref(false)
const formRef = ref(null)
const detail = ref({})
const deviceOptions = ref([])

const form = reactive({
  deviceType: 'DRONE',
  deviceId: null,
  faultDescription: '',
  maintenanceContent: '',
  maintenanceDate: '',
  maintenanceCost: 0,
  remark: ''
})

const completeForm = reactive({
  result: 'RESTORED',
  maintenanceContent: '',
  maintenanceCost: 0
})

const rules = {
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }]
}

onMounted(() => loadData())

/**
 * 按当前查询条件与分页参数加载维修记录列表
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getMaintenanceRecords({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...queryForm
    })
    if (res.data) {
      // 兼容 IPage 格式
      tableData.value = res.data.records || res.data || []
      total.value = res.data.total || res.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
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
  queryForm.deviceType = ''
  queryForm.status = ''
  search()
}

/**
 * 设备类型变更：清空已选设备并重新加载对应的设备下拉选项
 */
function onDeviceTypeChange() {
  form.deviceId = null
  loadDeviceOptions()
}

/**
 * 按当前设备类型加载可选设备列表（无人机或电池）
 */
async function loadDeviceOptions() {
  try {
    if (form.deviceType === 'DRONE') {
      const res = await getDrones({ pageNum: 1, pageSize: 999 })
      deviceOptions.value = (res.data || []).map(d => ({ id: d.id, code: d.droneCode, name: d.droneName }))
    } else {
      const res = await getBatteries({ pageNum: 1, pageSize: 999 })
      deviceOptions.value = (res.data || []).map(b => ({ id: b.id, code: b.batteryCode, name: b.batteryName }))
    }
  } catch (e) {
    console.error(e)
  }
}

/**
 * 打开新增对话框，并把表单恢复为初始值（设备类型默认无人机）
 */
function openAddDialog() {
  isEdit.value = false
  editId.value = null
  form.deviceType = 'DRONE'
  form.deviceId = null
  form.faultDescription = ''
  form.maintenanceContent = ''
  form.maintenanceDate = ''
  form.maintenanceCost = 0
  form.remark = ''
  loadDeviceOptions()
  dialogVisible.value = true
}

/**
 * 打开编辑对话框：按ID查询最新数据后回填表单，并加载对应设备类型的下拉选项
 * @param {Object} row - 表格当前行数据
 */
async function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  try {
    const res = await getMaintenanceRecordById(row.id)
    const d = res.data
    form.deviceType = d.deviceType
    form.deviceId = d.deviceId
    form.faultDescription = d.faultDescription
    form.maintenanceContent = d.maintenanceContent
    form.maintenanceDate = d.maintenanceDate
    form.maintenanceCost = d.maintenanceCost
    form.remark = d.remark
    await loadDeviceOptions()
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
      await updateMaintenanceRecord(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createMaintenanceRecord(form)
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
 * 打开完成维修对话框，并记录当前工单ID
 * @param {Object} row - 表格当前行数据
 */
function openCompleteDialog(row) {
  completeForm.result = 'RESTORED'
  completeForm.maintenanceContent = ''
  // 预填已录入的费用，避免完成维修时把之前的费用覆盖为 0
  completeForm.maintenanceCost = row.maintenanceCost ?? 0
  editId.value = row.id
  completeVisible.value = true
}

/**
 * 提交完成维修：登记维修结果，成功后关闭对话框并刷新列表
 */
async function submitComplete() {
  completeLoading.value = true
  try {
    await completeMaintenance(editId.value, {
      result: completeForm.result,
      maintenanceContent: completeForm.maintenanceContent,
      maintenanceCost: completeForm.maintenanceCost
    })
    ElMessage.success('维修完成')
    completeVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    completeLoading.value = false
  }
}

/**
 * 删除维修工单
 * @param {number} id - 工单ID
 */
async function handleDelete(id) {
  try {
    await deleteMaintenanceRecord(id)
    ElMessage.success('删除成功')
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
    const res = await getMaintenanceRecordById(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}
</script>
