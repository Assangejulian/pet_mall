<template>
  <div class="report-page">
    <div class="page-header">
      <h2>数据统计报表</h2>
      <p>基于 ECharts 格式的数据统计，适配可视化图表展示</p>
    </div>

    <!-- 时间选择器 -->
    <div class="filter-bar">
      <label>时间范围：</label>
      <button v-for="opt in dateOptions" :key="opt.value"
        :class="['btn-sm', { active: dateRange === opt.value }]"
        @click="dateRange = opt.value; fetchAll()">
        {{ opt.label }}
      </button>
      <span class="sep">|</span>
      <input type="date" v-model="customBegin" @change="onCustomChange" />
      <span class="sep">~</span>
      <input type="date" v-model="customEnd" @change="onCustomChange" />
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-if="!loading">
      <!-- 营业额统计 -->
      <section class="chart-section">
        <h3 class="section-title">📈 营业额统计</h3>
        <div class="summary-row">
          <span class="summary-label">总营业额：</span>
          <span class="summary-value">{{ totalTurnover }} 元</span>
        </div>
        <div class="chart-container">
          <svg class="svg-chart" :viewBox="'0 0 ' + svgWidth + ' 200'" preserveAspectRatio="xMidYMid meet">
            <polyline :points="turnoverPoints" fill="none" stroke="#409eff" stroke-width="2" />
            <circle v-for="(pt, i) in turnoverCoords" :key="'dot-'+i"
              :cx="pt.x" :cy="pt.y" r="3" fill="#409eff" />
            <text v-for="(pt, i) in turnoverCoords" :key="'txt-'+i"
              :x="pt.x" :y="pt.y - 8" text-anchor="middle" font-size="10" fill="#666">
              {{ pt.val }}
            </text>
          </svg>
          <div class="x-labels">
            <span v-for="(label, i) in turnoverLabels" :key="'xl-'+i" class="x-label">{{ label }}</span>
          </div>
        </div>
      </section>

      <!-- 用户统计 -->
      <section class="chart-section">
        <h3 class="section-title">👤 用户统计</h3>
        <div class="stat-cols">
          <div class="stat-col">
            <div class="col-title">每日新增用户</div>
            <div v-for="(v, i) in newUserData" :key="'nu-'+i" class="bar-row">
              <span class="bar-label">{{ newUserLabels[i] }}</span>
              <div class="bar-track">
                <div class="bar-fill bar-green" :style="{ width: (v / newUserMax * 100) + '%' }">
                  <span class="bar-val" v-if="v > 0">{{ v }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="stat-col">
            <div class="col-title">累计总用户</div>
            <svg class="svg-chart-sm" :viewBox="'0 0 ' + svgWidth + ' 100'" preserveAspectRatio="xMidYMid meet">
              <polyline :points="totalUserPoints" fill="#ecf5ff" stroke="#67c23a" stroke-width="2" />
            </svg>
          </div>
        </div>
      </section>

      <!-- 订单统计 -->
      <section class="chart-section">
        <h3 class="section-title">📋 订单统计</h3>
        <div class="chart-container">
          <svg class="svg-chart" :viewBox="'0 0 ' + svgWidth + ' 200'" preserveAspectRatio="xMidYMid meet">
            <polyline :points="orderTotalPoints" fill="none" stroke="#409eff" stroke-width="2" />
            <polyline :points="orderValidPoints" fill="none" stroke="#67c23a" stroke-width="2" stroke-dasharray="4,2" />
            <polyline :points="orderCompletedPoints" fill="none" stroke="#e6a23c" stroke-width="2" stroke-dasharray="2,2" />
          </svg>
          <div class="x-labels">
            <span v-for="(label, i) in orderLabels" :key="'ol-'+i" class="x-label">{{ label }}</span>
          </div>
          <div class="legend">
            <span class="legend-item"><span class="dot dot-blue"></span> 总订单</span>
            <span class="legend-item"><span class="dot dot-green"></span> 有效订单</span>
            <span class="legend-item"><span class="dot dot-orange"></span> 已完成</span>
          </div>
        </div>
      </section>

      <!-- 销量排名 Top10 -->
      <section class="chart-section">
        <h3 class="section-title">🏆 销量排名 Top10</h3>
        <div v-for="(item, i) in top10Data" :key="'top-'+i" class="bar-row">
          <span class="bar-label rank-label">{{ i + 1 }}. {{ item.name }}</span>
          <div class="bar-track">
            <div class="bar-fill bar-orange" :style="{ width: (item.number / top10Max * 100) + '%' }">
              <span class="bar-val" v-if="item.number > 0">{{ item.number }}</span>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue"
import { getTurnoverReport, getUserReport, getOrderReport, getSalesTop10 } from "../../api/report"

const loading = ref(false)
const dateRange = ref("7")
const customBegin = ref("")
const customEnd = ref("")

const dateOptions = [
  { label: "近7天", value: "7" },
  { label: "近30天", value: "30" },
  { label: "自定义", value: "custom" },
]

function getDateRange(): { begin: string; end: string } {
  const end = new Date()
  const endStr = end.toISOString().slice(0, 10)
  if (dateRange.value !== "custom") {
    const begin = new Date(end)
    begin.setDate(begin.getDate() - parseInt(dateRange.value))
    return { begin: begin.toISOString().slice(0, 10), end: endStr }
  }
  return {
    begin: customBegin.value || endStr,
    end: customEnd.value || endStr,
  }
}

function onCustomChange() {
  dateRange.value = "custom"
  fetchAll()
}

// Turnover
const turnoverDates = ref<string[]>([])
const turnoverValues = ref<number[]>([])
const totalTurnover = computed(() => turnoverValues.value.reduce((a, b) => a + b, 0).toFixed(2))
const svgWidth = ref(800)
const turnoverLabels = computed(() => turnoverDates.value.map(d => d.slice(5)))
const turnoverCoords = computed(() => {
  const vals = turnoverValues.value
  const max = Math.max(...vals, 1)
  const w = svgWidth.value
  const h = 190
  const n = vals.length
  return vals.map((v, i) => ({
    x: n > 1 ? 10 + (w - 20) * i / (n - 1) : w / 2,
    y: h - (v / max) * (h - 20) - 10,
    val: v,
  }))
})
const turnoverPoints = computed(() => turnoverCoords.value.map(p => p.x + "," + p.y).join(" "))

// Users
const newUserLabels = ref<string[]>([])
const newUserData = ref<number[]>([])
const newUserMax = computed(() => Math.max(...newUserData.value, 1))
const totalUserData = ref<number[]>([])
const totalUserPoints = computed(() => {
  const vals = totalUserData.value
  const max = Math.max(...vals, 1)
  const w = svgWidth.value
  const h = 90
  const n = vals.length
  return vals.map((v, i) => {
    const x = n > 1 ? 10 + (w - 20) * i / (n - 1) : w / 2
    const y = h - (v / max) * (h - 20) - 5
    return x + "," + y
  }).join(" ")
})

// Orders
const orderLabels = ref<string[]>([])
const orderTotalData = ref<number[]>([])
const orderValidData = ref<number[]>([])
const orderCompletedData = ref<number[]>([])
const orderMax = computed(() => Math.max(...orderTotalData.value, 1))

function makeOrderPoints(vals: number[]) {
  const max = orderMax.value
  const w = svgWidth.value
  const h = 190
  const n = vals.length
  return vals.map((v, i) => {
    const x = n > 1 ? 10 + (w - 20) * i / (n - 1) : w / 2
    const y = h - (v / max) * (h - 20) - 10
    return x + "," + y
  }).join(" ")
}
const orderTotalPoints = computed(() => makeOrderPoints(orderTotalData.value))
const orderValidPoints = computed(() => makeOrderPoints(orderValidData.value))
const orderCompletedPoints = computed(() => makeOrderPoints(orderCompletedData.value))

// Top10
const top10Data = ref<{ name: string; number: number }[]>([])
const top10Max = computed(() => Math.max(...top10Data.value.map(d => d.number), 1))

async function fetchAll() {
  loading.value = true
  try {
    const range = getDateRange()
    const [turnover, users, orders, top10] = await Promise.all([
      getTurnoverReport(range),
      getUserReport(range),
      getOrderReport(range),
      getSalesTop10(range),
    ])
    turnoverDates.value = turnover.dateList ? turnover.dateList.split(",") : []
    turnoverValues.value = turnover.turnoverList ? turnover.turnoverList.split(",").map(Number) : []

    const ud = users.dateList ? users.dateList.split(",") : []
    newUserLabels.value = ud.map(d => d.slice(5))
    newUserData.value = users.newUserList ? users.newUserList.split(",").map(Number) : []
    totalUserData.value = users.totalUserList ? users.totalUserList.split(",").map(Number) : []

    orderLabels.value = orders.dateList ? orders.dateList.split(",").map(d => d.slice(5)) : []
    orderTotalData.value = orders.orderCountList ? orders.orderCountList.split(",").map(Number) : []
    orderValidData.value = orders.validOrderCountList ? orders.validOrderCountList.split(",").map(Number) : []
    orderCompletedData.value = orders.completedOrderCountList ? orders.completedOrderCountList.split(",").map(Number) : []

    if (top10.nameList && top10.numberList) {
      const names = top10.nameList.split(",")
      const nums = top10.numberList.split(",").map(Number)
      top10Data.value = names.map((name, i) => ({ name, number: nums[i] || 0 }))
    } else {
      top10Data.value = []
    }
  } catch (e: any) {
    alert("获取统计数据失败: " + (e.message || "未知错误"))
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchAll())
</script>

<style scoped>
.report-page { max-width: 1100px; }
.filter-bar { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 24px; padding: 12px 16px; background: #fff; border-radius: var(--radius); box-shadow: var(--shadow); }
.filter-bar label { font-weight: 600; color: var(--text1); }
.btn-sm { padding: 4px 12px; border: 1px solid var(--border); border-radius: 4px; background: #fff; cursor: pointer; font-size: 13px; }
.btn-sm.active { background: var(--primary); color: #fff; border-color: var(--primary); }
.sep { color: var(--text3); }
input[type="date"] { padding: 4px 8px; border: 1px solid var(--border); border-radius: 4px; font-size: 13px; }
.loading { text-align: center; padding: 60px 0; color: var(--text2); }
.chart-section { background: #fff; border-radius: var(--radius); box-shadow: var(--shadow); padding: 24px; margin-bottom: 20px; }
.section-title { font-size: 16px; margin-bottom: 16px; color: var(--text1); }
.summary-row { margin-bottom: 16px; font-size: 14px; }
.summary-label { color: var(--text2); }
.summary-value { font-size: 20px; font-weight: 700; color: var(--primary); }
.chart-container { width: 100%; }
.svg-chart { width: 100%; height: auto; display: block; }
.x-labels { display: flex; justify-content: space-between; padding: 4px 10px 0; }
.x-label { font-size: 11px; color: var(--text3); white-space: nowrap; }
.legend { display: flex; gap: 16px; justify-content: center; margin-top: 8px; font-size: 12px; color: var(--text2); }
.legend-item { display: flex; align-items: center; gap: 4px; }
.dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; }
.dot-blue { background: #409eff; }
.dot-green { background: #67c23a; }
.dot-orange { background: #e6a23c; }
.bar-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.bar-label { width: 80px; font-size: 12px; color: var(--text2); text-align: right; flex-shrink: 0; }
.rank-label { width: 140px; }
.bar-track { flex: 1; height: 22px; background: var(--bg); border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; display: flex; align-items: center; justify-content: flex-end; padding-right: 6px; min-width: 0; transition: width .3s; }
.bar-fill.bar-green { background: linear-gradient(90deg, #b3e19d, #67c23a); }
.bar-fill.bar-orange { background: linear-gradient(90deg, #f0c78a, #e6a23c); }
.bar-val { font-size: 11px; color: #fff; font-weight: 600; }
.stat-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 768px) { .stat-cols { grid-template-columns: 1fr; } }
.col-title { font-weight: 600; font-size: 14px; margin-bottom: 12px; color: var(--text1); }
.svg-chart-sm { width: 100%; height: auto; display: block; }
</style>