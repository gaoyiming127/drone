<template>
  <div class="page-container">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="电池编号">
          <el-input v-model="queryForm.batteryCode" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="queryForm.model" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="健康等级">
          <el-select v-model="queryForm.healthLevel" placeholder="全部" clearable @change="search" style="width: 215px">
            <el-option label="良好" value="GOOD" />
            <el-option label="注意" value="ATTENTION" />
            <el-option label="危险" value="DANGEROUS" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable @change="search" style="width: 215px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="使用中" value="IN_USE" />
            <el-option label="异常" value="ABNORMAL" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-bottom: 15px;">
      <el-button v-if="isAdmin" type="primary" @click="openAddDialog">新增电池</el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="batteryCode" label="电池编号" width="130" />
        <el-table-column prop="batteryName" label="电池名称" width="150" />
        <el-table-column label="品牌型号" width="150">
          <template #default="{ row }">{{ row.brand }} {{ row.model }}</template>
        </el-table-column>
        <el-table-column prop="ratedCapacity" label="标称容量" width="100" align="center">
          <template #default="{ row }">{{ row.ratedCapacity }}mAh</template>
        </el-table-column>
        <el-table-column prop="fullChargeCapacity" label="当前容量" width="100" align="center">
          <template #default="{ row }">{{ row.fullChargeCapacity }}mAh</template>
        </el-table-column>
        <el-table-column label="SOH" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="parseFloat(row.soh || 0)"
              :color="sohColor(row.soh)"
              :text-inside="true"
              :stroke-width="18"
            />
          </template>
        </el-table-column>
        <el-table-column label="健康等级" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="healthLevelMap[row.healthLevel]?.type || 'info'" size="small">
              {{ healthLevelMap[row.healthLevel]?.label || row.healthLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cycleCount" label="循环次数" width="90" align="center" />
        <el-table-column prop="usageCount" label="使用次数" width="90" align="center" />
        <el-table-column label="鼓包" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.swollen === 1" type="danger" size="small">是</el-tag>
            <el-tag v-else type="success" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="batteryStatusMap[row.status]?.type || 'info'" size="small">
              {{ batteryStatusMap[row.status]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="175" align="center" class-name="col-action" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="isAdmin" text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm v-if="isAdmin" title="确定删除该电池？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑电池' : '新增电池'" width="650px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电池编号" prop="batteryCode">
              <el-input v-model="form.batteryCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电池名称" prop="batteryName">
              <el-input v-model="form.batteryName" />
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
            <el-form-item label="标称容量(mAh)" prop="ratedCapacity">
              <el-input-number v-model="form.ratedCapacity" :min="0" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="当前满充容量(mAh)" prop="fullChargeCapacity">
              <el-input-number v-model="form.fullChargeCapacity" :min="0" :step="100" style="width: 100%" />
              <div style="font-size: 12px; color: #909399; line-height: 1.4;">
                该值会随循环次数自动衰减，可在此录入最新检测值进行校准
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="循环次数" prop="cycleCount">
              <el-input-number v-model="form.cycleCount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否鼓包" prop="swollen">
              <el-radio-group v-model="form.swollen">
                <el-radio :value="1">是</el-radio>
                <el-radio :value="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="正常" value="NORMAL" />
                <el-option label="使用中" value="IN_USE" />
                <el-option label="异常" value="ABNORMAL" />
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
    <el-dialog v-model="detailVisible" title="电池详情" width="650px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="电池编号">{{ detail.batteryCode }}</el-descriptions-item>
        <el-descriptions-item label="电池名称">{{ detail.batteryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ detail.model }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ detail.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="batteryStatusMap[detail.status]?.type">{{ batteryStatusMap[detail.status]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="标称容量">{{ detail.ratedCapacity }} mAh</el-descriptions-item>
        <el-descriptions-item label="当前满充容量">{{ detail.fullChargeCapacity }} mAh</el-descriptions-item>
        <el-descriptions-item label="SOH">
          <el-progress :percentage="parseFloat(detail.soh || 0)" :color="sohColor(detail.soh)" :stroke-width="18" style="width: 150px" />
        </el-descriptions-item>
        <el-descriptions-item label="健康等级">
          <el-tag :type="healthLevelMap[detail.healthLevel]?.type">{{ healthLevelMap[detail.healthLevel]?.label }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="循环次数">{{ detail.cycleCount }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ detail.usageCount }}</el-descriptions-item>
        <el-descriptions-item label="累计使用">{{ detail.totalUseMinutes }} 分钟</el-descriptions-item>
        <el-descriptions-item label="鼓包">{{ detail.swollen === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 电池管理页面。
 * 支持按电池编号、型号、健康等级、状态组合查询；管理员可新增、编辑、删除电池，
 * 所有角色均可查看详情（含 SOH 进度与健康等级）。
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import authStore from '../../store/auth'
import { getBatteries, getBatteryById, createBattery, updateBattery, deleteBattery } from '../../api/battery'
import { batteryStatusMap, healthLevelMap } from '../../utils/format'

const isAdmin = computed(() => authStore.isAdmin)

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const queryForm = reactive({
  batteryCode: '',
  model: '',
  healthLevel: '',
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
  batteryCode: '',
  batteryName: '',
  brand: '',
  model: '',
  serialNumber: '',
  ratedCapacity: 5000,
  fullChargeCapacity: 5000,
  cycleCount: 0,
  usageCount: 0,
  totalUseMinutes: 0,
  swollen: 0,
  overheated: 0,
  status: 'NORMAL',
  remark: ''
})

const rules = {
  batteryCode: [{ required: true, message: '请输入电池编号', trigger: 'blur' }],
  batteryName: [{ required: true, message: '请输入电池名称', trigger: 'blur' }],
  ratedCapacity: [{ required: true, message: '请输入标称容量', trigger: 'blur' }],
  fullChargeCapacity: [{ required: true, message: '请输入当前满充容量', trigger: 'blur' }]
}

/**
 * 按 SOH 取值返回进度条颜色，与后端健康等级阈值（85%、70%）保持一致
 * @param {number|string} soh - 健康度百分比
 * @returns {string} 颜色值
 */
function sohColor(soh) {
  const val = parseFloat(soh)
  if (val >= 85) return '#67C23A'
  if (val >= 70) return '#E6A23C'
  return '#F56C6C'
}

onMounted(() => loadData())

/**
 * 按当前查询条件与分页参数加载电池列表
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getBatteries({
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
  queryForm.batteryCode = ''
  queryForm.model = ''
  queryForm.healthLevel = ''
  queryForm.status = ''
  search()
}

/**
 * 打开新增对话框，并把表单恢复为初始值（容量默认 5000mAh、循环与使用次数为 0）
 */
function openAddDialog() {
  isEdit.value = false
  editId.value = null
  Object.assign(form, {
    batteryCode: '', batteryName: '', brand: '', model: '', serialNumber: '',
    ratedCapacity: 5000, fullChargeCapacity: 5000, cycleCount: 0,
    usageCount: 0, totalUseMinutes: 0, swollen: 0, overheated: 0,
    status: 'NORMAL', remark: ''
  })
  dialogVisible.value = true
}

/**
 * 打开编辑对话框：按ID查询最新数据后回填表单；使用次数与累计使用时长由后端累计，仅作展示
 * @param {Object} row - 表格当前行数据
 */
async function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  try {
    const res = await getBatteryById(row.id)
    const d = res.data
    form.batteryCode = d.batteryCode
    form.batteryName = d.batteryName
    form.brand = d.brand
    form.model = d.model
    form.serialNumber = d.serialNumber
    form.ratedCapacity = d.ratedCapacity
    form.fullChargeCapacity = d.fullChargeCapacity
    form.cycleCount = d.cycleCount
    form.usageCount = d.usageCount
    form.totalUseMinutes = d.totalUseMinutes
    form.swollen = d.swollen
    form.overheated = d.overheated
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
      await updateBattery(editId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createBattery(form)
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
 * 删除电池
 * @param {number} id - 电池ID
 */
async function handleDelete(id) {
  try {
    await deleteBattery(id)
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
    const res = await getBatteryById(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}
</script>
