<template>
  <div class="page-container">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-form :model="queryForm" inline>
        <el-form-item label="无人机编号">
          <el-input v-model="queryForm.droneCode" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <!-- 普通用户列表仅包含本人记录，无需按人员检索 -->
        <el-form-item v-if="isAdmin" label="使用人员">
          <el-input v-model="queryForm.userName" placeholder="请输入" clearable @clear="search" />
        </el-form-item>
        <el-form-item label="飞行日期">
          <el-input v-model="queryForm.flightDate" placeholder="2025-06" clearable @clear="search" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div style="margin-bottom: 15px;">
      <el-button type="primary" @click="openAddDialog">新增飞行记录</el-button>
    </div>

    <el-card shadow="hover">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="recordCode" label="记录编号" width="150" />
        <el-table-column prop="droneCode" label="无人机编号" width="120" />
        <el-table-column prop="droneName" label="无人机名称" width="130" />
        <el-table-column label="使用人员" width="100">
          <template #default="{ row }">{{ row.userRealName || row.userName }}</template>
        </el-table-column>
        <el-table-column prop="flightLocation" label="飞行地点" width="130" />
        <el-table-column prop="flightDate" label="飞行日期" width="110" />
        <el-table-column prop="flightMinutes" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="takeoffCount" label="起飞次数" width="90" align="center" />
        <el-table-column label="异常" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.hasException === 1" type="danger" size="small">是</el-tag>
            <el-tag v-else type="success" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" class-name="col-action" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="viewDetail(row)">详情</el-button>
            <el-popconfirm v-if="canManage(row)" title="确定删除该记录？" @confirm="handleDelete(row.id)">
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

    <!-- 新增飞行记录对话框 -->
    <el-dialog v-model="dialogVisible" title="新增飞行记录" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="无人机" prop="droneId">
              <el-select v-model="form.droneId" filterable style="width: 100%" placeholder="选择无人机">
                <el-option v-for="d in droneList" :key="d.id" :label="`${d.droneCode} - ${d.droneName}`" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
              <el-form-item label="使用人员" prop="userId">
              <el-select v-model="form.userId" filterable :disabled="!isAdmin" style="width: 100%" placeholder="选择人员">
                <el-option v-for="u in userList" :key="u.id" :label="`${u.realName || u.username}`" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="飞行地点" prop="flightLocation">
              <el-input v-model="form.flightLocation" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="飞行日期" prop="flightDate">
              <el-date-picker v-model="form.flightDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="飞行分钟数" prop="flightMinutes">
              <el-input-number v-model="form.flightMinutes" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起飞次数" prop="takeoffCount">
              <el-input-number v-model="form.takeoffCount" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否异常">
              <el-radio-group v-model="form.hasException">
                <el-radio :value="0">否</el-radio>
                <el-radio :value="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.hasException === 1">
            <el-form-item label="异常描述">
              <el-input v-model="form.exceptionDescription" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 电池使用列表 -->
        <el-divider>电池使用记录</el-divider>
        <div v-for="(item, index) in form.batteries" :key="index" style="border: 1px solid #ebeef5; border-radius: 4px; padding: 15px; margin-bottom: 10px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <span style="font-weight: bold;">电池 {{ index + 1 }}</span>
            <el-button v-if="form.batteries.length > 1" type="danger" text @click="removeBattery(index)">移除</el-button>
          </div>
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item :label="'选择电池'" :prop="'batteries.' + index + '.batteryId'" :rules="{ required: true, message: '请选择电池' }">
                <el-select v-model="item.batteryId" filterable style="width: 100%" placeholder="选择电池">
                  <el-option v-for="b in batteryList" :key="b.id" :label="`${b.batteryCode} - ${b.batteryName} (SOH:${b.soh}%)`" :value="b.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="使用分钟" :prop="'batteries.' + index + '.useMinutes'" :rules="{ required: true, message: '必填' }">
                <el-input-number v-model="item.useMinutes" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="10">
            <el-col :span="12">
              <el-form-item :label="'开始电量'" :prop="'batteries.' + index + '.startPower'" :rules="{ required: true, message: '必填' }">
                <el-input-number v-model="item.startPower" :min="0" :max="100" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="'结束电量'" :prop="'batteries.' + index + '.endPower'" :rules="{ required: true, message: '必填' }">
                <el-input-number v-model="item.endPower" :min="0" :max="100" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
        <el-button type="primary" plain @click="addBattery">+ 添加电池</el-button>

        <el-form-item label="备注" style="margin-top: 15px;">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存飞行记录</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="飞行记录详情" width="700px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="记录编号">{{ detail.recordCode }}</el-descriptions-item>
        <el-descriptions-item label="无人机">{{ detail.droneCode }} - {{ detail.droneName }}</el-descriptions-item>
        <el-descriptions-item label="使用人员">{{ detail.userRealName || detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="飞行地点">{{ detail.flightLocation }}</el-descriptions-item>
        <el-descriptions-item label="飞行日期">{{ detail.flightDate }}</el-descriptions-item>
        <el-descriptions-item label="飞行时长">{{ detail.flightMinutes }} 分钟</el-descriptions-item>
        <el-descriptions-item label="起飞次数">{{ detail.takeoffCount }} 次</el-descriptions-item>
        <el-descriptions-item label="是否异常">
          <el-tag v-if="detail.hasException === 1" type="danger">是 - {{ detail.exceptionDescription }}</el-tag>
          <el-tag v-else type="success">否</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.batteries" border size="small" style="width: 100%">
        <el-table-column label="电池编号" prop="batteryCode" />
        <el-table-column label="电池名称" prop="batteryName" />
        <el-table-column label="开始电量" prop="startPower">
          <template #default="{ row }">{{ row.startPower }}%</template>
        </el-table-column>
        <el-table-column label="结束电量" prop="endPower">
          <template #default="{ row }">{{ row.endPower }}%</template>
        </el-table-column>
        <el-table-column label="耗电量">
          <template #default="{ row }">{{ (row.startPower - row.endPower).toFixed(1) }}%</template>
        </el-table-column>
        <el-table-column label="使用分钟" prop="useMinutes" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 飞行记录页面。
 * 登记记录时需选择无人机、使用人员并逐块录入电池使用信息（开始/结束电量、使用分钟）；
 * 管理员可查看全部记录并按人员检索，普通用户只能查看、登记与删除本人的记录。
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import authStore from '../../store/auth'
import { getFlightRecords, getFlightRecordById, createFlightRecord, deleteFlightRecord } from '../../api/flightRecord'
import { getDrones } from '../../api/drone'
import { getBatteries } from '../../api/battery'
import { getUserOptions } from '../../api/user'

// 普通用户只能登记和删除本人的飞行记录
const isAdmin = computed(() => authStore.isAdmin)
const currentUserId = computed(() => authStore.user?.userId)

/**
 * 判断当前用户是否可以删除该记录：管理员或记录登记人本人
 * @param {Object} row - 表格当前行数据
 * @returns {boolean} 可删除返回 true
 */
function canManage(row) {
  return isAdmin.value || row.userId === currentUserId.value
}

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const droneList = ref([])
const batteryList = ref([])
const userList = ref([])

const queryForm = reactive({
  droneCode: '',
  userName: '',
  flightDate: ''
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const detail = ref({})

const form = reactive({
  droneId: null,
  userId: null,
  flightLocation: '',
  flightDate: '',
  flightMinutes: 30,
  takeoffCount: 1,
  hasException: 0,
  exceptionDescription: '',
  remark: '',
  batteries: [{ batteryId: null, startPower: 100, endPower: 50, useMinutes: 30 }]
})

const rules = {
  droneId: [{ required: true, message: '请选择无人机', trigger: 'change' }],
  userId: [{ required: true, message: '请选择使用人员', trigger: 'change' }],
  flightLocation: [{ required: true, message: '请输入飞行地点', trigger: 'blur' }],
  flightDate: [{ required: true, message: '请选择飞行日期', trigger: 'change' }],
  flightMinutes: [{ required: true, message: '请输入飞行分钟数', trigger: 'blur' }],
  takeoffCount: [{ required: true, message: '请输入起飞次数', trigger: 'blur' }]
}

/**
 * 页面初始化：先加载列表，再加载下拉选项
 */
onMounted(async () => {
  await loadData()
  loadSelectOptions()
})

/**
 * 加载无人机、电池与使用人员三个下拉选项
 */
async function loadSelectOptions() {
  // 三个下拉各自独立加载，避免某个接口失败导致所有下拉都为空
  const [dronesRes, batteriesRes, usersRes] = await Promise.allSettled([
    getDrones({ pageNum: 1, pageSize: 999 }),
    getBatteries({ pageNum: 1, pageSize: 999 }),
    getUserOptions()
  ])
  if (dronesRes.status === 'fulfilled') droneList.value = dronesRes.value.data || []
  if (batteriesRes.status === 'fulfilled') batteryList.value = batteriesRes.value.data || []
  if (usersRes.status === 'fulfilled') userList.value = usersRes.value.data || []
}

/**
 * 按当前查询条件与分页参数加载飞行记录列表
 */
async function loadData() {
  loading.value = true
  try {
    const res = await getFlightRecords({
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
  queryForm.userName = ''
  queryForm.flightDate = ''
  search()
}

/**
 * 打开新增对话框，并把表单恢复为初始值（默认一条电池使用明细）
 */
function openAddDialog() {
  form.droneId = null
  // 普通用户默认且只能登记本人执行的飞行
  form.userId = isAdmin.value ? null : currentUserId.value
  form.flightLocation = ''
  form.flightDate = ''
  form.flightMinutes = 30
  form.takeoffCount = 1
  form.hasException = 0
  form.exceptionDescription = ''
  form.remark = ''
  form.batteries = [{ batteryId: null, startPower: 100, endPower: 50, useMinutes: 30 }]
  dialogVisible.value = true
}

/**
 * 增加一条电池使用明细
 */
function addBattery() {
  form.batteries.push({ batteryId: null, startPower: 100, endPower: 50, useMinutes: 30 })
}

/**
 * 移除指定序号的电池使用明细
 * @param {number} index - 明细序号
 */
function removeBattery(index) {
  form.batteries.splice(index, 1)
}

/**
 * 提交表单：校验通过后登记飞行记录，成功后关闭对话框并刷新列表
 */
async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    await createFlightRecord(form)
    ElMessage.success('新增飞行记录成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

/**
 * 删除飞行记录
 * @param {number} id - 飞行记录ID
 */
async function handleDelete(id) {
  try {
    await deleteFlightRecord(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

/**
 * 查看详情：按ID查询最新数据并弹出详情对话框（含电池使用明细）
 * @param {Object} row - 表格当前行数据
 */
async function viewDetail(row) {
  try {
    const res = await getFlightRecordById(row.id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}
</script>
