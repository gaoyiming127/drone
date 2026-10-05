<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.totalDrones }}</div>
          <div class="stat-label">🛸 无人机总数</div>
        </el-card>
      </el-col>
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.idleDrones }}</div>
          <div class="stat-label">✅ 空闲无人机</div>
        </el-card>
      </el-col>
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.totalBatteries }}</div>
          <div class="stat-label">🔋 电池总数</div>
        </el-card>
      </el-col>
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C">{{ data.dangerousBatteries }}</div>
          <div class="stat-label">⚠️ 危险电池</div>
        </el-card>
      </el-col>
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.monthlyFlightCount }}</div>
          <div class="stat-label">📊 本月飞行</div>
        </el-card>
      </el-col>
      <el-col :span="8" :xs="12" :sm="8" :md="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ data.monthlyFlightMinutes }}</div>
          <div class="stat-label">⏱ 本月飞行(分钟)</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>无人机状态分布</template>
          <div ref="pieChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>电池健康等级</template>
          <div ref="barChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>近6个月飞行次数</template>
          <div ref="lineChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 提醒和最近记录 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span style="color: #E6A23C">⚠️ 系统提醒</span></template>
          <div v-if="data.warnings && data.warnings.length > 0">
            <el-alert
              v-for="(w, i) in data.warnings"
              :key="i"
              :title="w"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 8px;"
            />
          </div>
          <el-empty v-else description="暂无提醒" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>最近飞行记录</template>
          <div v-if="data.recentFlights && data.recentFlights.length > 0">
            <div v-for="f in data.recentFlights" :key="f.id" class="recent-item">
              <div class="recent-title">{{ f.droneName }} - {{ f.flightLocation }}</div>
              <div class="recent-time">{{ f.flightDate }} | {{ f.flightMinutes }}分钟</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>最近维修记录</template>
          <div v-if="data.recentMaintenanceRecords && data.recentMaintenanceRecords.length > 0">
            <div v-for="m in data.recentMaintenanceRecords" :key="m.id" class="recent-item">
              <div class="recent-title">{{ m.deviceName }} - {{ m.faultDescription?.substring(0, 15) }}{{ m.faultDescription?.length > 15 ? '...' : '' }}</div>
              <div class="recent-time">
                <el-tag :type="m.status === 'COMPLETED' ? 'success' : m.status === 'PROCESSING' ? 'warning' : 'info'" size="small">
                  {{ m.status === 'COMPLETED' ? '已完成' : m.status === 'PROCESSING' ? '维修中' : '待维修' }}
                </el-tag>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 总览（首页）页面。
 * 展示无人机/电池等统计卡片、无人机状态饼图、电池健康等级柱状图、近 6 个月飞行次数折线图，
 * 以及系统提醒和最近飞行/维修记录；普通用户的数据范围由后端按本人记录限定。
 */
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStatistics } from '../api/dashboard'

const data = ref({
  totalDrones: 0,
  idleDrones: 0,
  totalBatteries: 0,
  dangerousBatteries: 0,
  monthlyFlightCount: 0,
  monthlyFlightMinutes: 0,
  pendingMaintenanceCount: 0,
  warnings: [],
  recentFlights: [],
  recentMaintenanceRecords: [],
  droneStatusPie: null,
  batteryHealthBar: null,
  monthlyFlightLine: null
})

const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

const statusLabelMap = {
  IDLE: '空闲',
  IN_USE: '使用中',
  MAINTENANCE: '维修中',
  DISABLED: '停用'
}

const healthLabelMap = {
  GOOD: '良好',
  ATTENTION: '注意',
  DANGEROUS: '危险'
}

/**
 * 页面初始化：先取回统计数据，待视图渲染完成后再初始化图表
 */
onMounted(async () => {
  await loadData()
  nextTick(() => {
    initCharts()
  })
})

/**
 * 页面卸载：销毁三个图表实例，释放资源
 */
onUnmounted(() => {
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})

/**
 * 加载首页统计数据
 */
async function loadData() {
  try {
    const res = await getDashboardStatistics()
    data.value = res.data
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  }
}

/**
 * 初始化三个 ECharts 图表：无人机状态饼图、电池健康等级柱状图、近 6 个月飞行次数折线图。
 * 状态与健康等级的英文键在此映射为中文标签，未知键直接展示原值。
 */
function initCharts() {
  // 饼图 - 无人机状态
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const statusData = data.value.droneStatusPie?.data || {}
    const pieData = Object.entries(statusData).map(([k, v]) => ({
      name: statusLabelMap[k] || k,
      value: v
    }))
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: pieData,
        label: { show: true, formatter: '{b}\n{d}%' },
        emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
      }]
    })
  }

  // 柱状图 - 电池健康等级
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    const healthData = data.value.batteryHealthBar?.data || {}
    const barData = Object.entries(healthData).map(([k, v]) => ({
      name: healthLabelMap[k] || k,
      value: v
    }))
    barChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: barData.map(d => d.name) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        type: 'bar',
        data: barData.map(d => d.value),
        itemStyle: {
          color: (params) => {
            const colors = { '良好': '#67C23A', '注意': '#E6A23C', '危险': '#F56C6C' }
            return colors[params.name] || '#409EFF'
          }
        },
        barWidth: '50%'
      }]
    })
  }

  // 折线图 - 近6个月飞行次数
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    const lineData = data.value.monthlyFlightLine?.data || []
    const months = lineData.map(d => d.month)
    const counts = lineData.map(d => d.count)
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: months },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        type: 'line',
        data: counts,
        smooth: true,
        areaStyle: { color: 'rgba(64, 158, 255, 0.15)' },
        lineStyle: { color: '#409EFF', width: 2 },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}
.stat-cards {
  margin-bottom: 0;
}
.stat-card {
  text-align: center;
  margin-bottom: 0;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
.recent-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.recent-item:last-child {
  border-bottom: none;
}
.recent-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}
.recent-time {
  font-size: 12px;
  color: #909399;
}
</style>
