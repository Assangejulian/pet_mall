<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>概览</h2>
      <p>{{ description }}</p>
    </div>

    <!-- 日期筛选 -->
    <div class="date-bar" v-if="admin.hasRole('admin') || admin.hasRole('merchant')">
      <label>起止日期：</label>
      <input type="date" v-model="begin" class="date-input" />
      <span class="sep">—</span>
      <input type="date" v-model="end" class="date-input" />
      <button class="btn btn-primary btn-sm" @click="refreshCharts">查询</button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <router-link
        v-for="card in cards" :key="card.label"
        :to="card.path || ''"
        :class="['stat-card', { clickable: !!card.path }]"
      >
        <h3>{{ card.label }}<span class="card-arrow" v-if="card.path"> →</span></h3>
        <div class="num">{{ card.value }} <span class="unit">{{ card.unit }}</span></div>
      </router-link>
    </div>

    <!-- 图表区域 -->
    <template v-if="admin.hasRole('admin') || admin.hasRole('merchant')">
      <div class="charts-grid">
        <div class="chart-card">
          <h3 class="chart-title">营业额统计</h3>
          <div ref="turnoverChart" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <h3 class="chart-title">用户统计</h3>
          <div ref="userChart" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <h3 class="chart-title">订单统计</h3>
          <div ref="orderChart" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <h3 class="chart-title">销量排名 Top10</h3>
          <div ref="salesChart" class="chart-box"></div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, ref, watch, nextTick } from "vue"
import { useAdminStore } from "../stores/admin"
import http, { merchantHttp } from "../api/index"
import { unwrap } from "../api/helper"
import { listMerchantStores } from "../api/merchantStore"
import { listMerchantProducts } from "../api/merchantProduct"
import { listAuditorStores } from "../api/auditorStore"
import { listAuditorProducts } from "../api/auditorProduct"
import * as echarts from "echarts"
import type { TurnoverReportVO, ReportQuery } from "../api/report"
import { getTurnoverReport, getUserReport, getOrderReport, getSalesTop10 } from "../api/report"
import { getMerchantTurnover, getMerchantUsers, getMerchantOrders, getMerchantTop10 } from "../api/report-merchant"

interface AdminStats {
  userCount: number; storeCount: number; productCount: number
  todayOrders: number; totalRevenue: number
}

const admin = useAdminStore()
const stats = ref<AdminStats>({ userCount: 0, storeCount: 0, productCount: 0, todayOrders: 0, totalRevenue: 0 })
const scopedCounts = ref({ stores: 0, products: 0 })

// 日期
const today = new Date().toISOString().slice(0, 10)
const begin = ref(today)
const end = ref(today)

const description = computed(() =>
  admin.hasRole("merchant") ? "管理自己的门店与商品"
    : admin.hasRole("auditor") ? "查看平台审核与监管数据" : "系统数据总览")

const cards = computed(() => admin.hasRole("admin") ? [
  { label: "用户总数", value: stats.value.userCount, unit: "人" },
  { label: "门店总数", value: stats.value.storeCount, unit: "家" },
  { label: "商品总数", value: stats.value.productCount, unit: "件" },
  { label: "今日订单", value: stats.value.todayOrders, unit: "笔" },
  { label: "总营收", value: stats.value.totalRevenue, unit: "元" },
] : admin.hasRole("merchant") ? [
  { label: "我的门店", value: stats.value.storeCount, unit: "家", path: "/merchant/store" },
  { label: "我的商品", value: stats.value.productCount, unit: "件", path: "/merchant/product" },
] : [
  { label: "平台门店", value: scopedCounts.value.stores, unit: "家", path: "/auditor/store" },
  { label: "平台商品", value: scopedCounts.value.products, unit: "件", path: "/auditor/product" },
])

// 图表 ref
const turnoverChart = ref<HTMLDivElement | null>(null)
const userChart = ref<HTMLDivElement | null>(null)
const orderChart = ref<HTMLDivElement | null>(null)
const salesChart = ref<HTMLDivElement | null>(null)

let charts: echarts.ECharts[] = []

function parseCSV(s: string): string[] { return s ? s.split(",") : [] }
function parseNums(s: string): number[] { return parseCSV(s).map(Number) }

const chartBase: echarts.EChartsOption = {
  tooltip: { trigger: "axis" },
  grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
}

async function loadTurnover() {
  const isMerchant = admin.hasRole("merchant")
  const fn = isMerchant ? getMerchantTurnover : getTurnoverReport
  const data = await fn({ begin: begin.value, end: end.value })
  const instance = echarts.init(turnoverChart.value!)
  charts.push(instance)
  instance.setOption({
    ...chartBase,
    xAxis: { type: "category", data: parseCSV(data.dateList) },
    yAxis: { type: "value", axisLabel: { formatter: (v: number) => "¥" + v } },
    series: [{ name: "营业额", type: "line", data: parseNums(data.turnoverList), smooth: true, areaStyle: { opacity: 0.15 }, lineStyle: { color: "#5b7b6a" }, itemStyle: { color: "#5b7b6a" } }],
  } as echarts.EChartsOption)
}

async function loadUsers() {
  const isMerchant = admin.hasRole("merchant")
  const fn = isMerchant ? getMerchantUsers : getUserReport
  const data = await fn({ begin: begin.value, end: end.value })
  const instance = echarts.init(userChart.value!)
  charts.push(instance)
  instance.setOption({
    ...chartBase,
    xAxis: { type: "category", data: parseCSV(data.dateList) },
    yAxis: { type: "value" },
    legend: { data: ["新增用户", "总用户"] },
    series: [
      { name: "新增用户", type: "bar", data: parseNums(data.newUserList), barGap: 0, itemStyle: { color: "#5b7b6a" } },
      { name: "总用户", type: "line", data: parseNums(data.totalUserList), smooth: true, lineStyle: { color: "#c59a6f" }, itemStyle: { color: "#c59a6f" } },
    ],
  } as echarts.EChartsOption)
}

async function loadOrders() {
  const isMerchant = admin.hasRole("merchant")
  const fn = isMerchant ? getMerchantOrders : getOrderReport
  const data = await fn({ begin: begin.value, end: end.value })
  const instance = echarts.init(orderChart.value!)
  charts.push(instance)
  instance.setOption({
    ...chartBase,
    xAxis: { type: "category", data: parseCSV(data.dateList) },
    yAxis: { type: "value" },
    legend: { data: ["订单总数", "有效订单", "已完成"] },
    series: [
      { name: "订单总数", type: "bar", data: parseNums(data.orderCountList), barGap: 0, itemStyle: { color: "#8f9bb3" } },
      { name: "有效订单", type: "bar", data: parseNums(data.validOrderCountList), itemStyle: { color: "#5b7b6a" } },
      { name: "已完成", type: "bar", data: parseNums(data.completedOrderCountList), itemStyle: { color: "#c59a6f" } },
    ],
  } as echarts.EChartsOption)
}

async function loadSales() {
  const isMerchant = admin.hasRole("merchant")
  const fn = isMerchant ? getMerchantTop10 : getSalesTop10
  const data = await fn({ begin: begin.value, end: end.value })
  const names = parseCSV(data.nameList).reverse()
  const nums = parseNums(data.numberList).reverse()
  const instance = echarts.init(salesChart.value!)
  charts.push(instance)
  instance.setOption({
    ...chartBase,
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { left: "3%", right: "8%", bottom: "3%", containLabel: true },
    xAxis: { type: "value", axisLabel: { formatter: (v: number) => v + "件" } },
    yAxis: { type: "category", data: names, axisLabel: { width: 80, overflow: "truncate" } },
    series: [{ name: "销量", type: "bar", data: nums, itemStyle: { color: "#5b7b6a", borderRadius: [0, 4, 4, 0] } }],
  } as echarts.EChartsOption)
}

function disposeCharts() { charts.forEach(c => c.dispose()); charts = [] }

async function refreshCharts() {
  if (!admin.hasRole("admin") && !admin.hasRole("merchant")) return
  disposeCharts()
  await nextTick()
  await Promise.all([loadTurnover(), loadUsers(), loadOrders(), loadSales()])
}

onMounted(async () => {
  if (admin.hasRole("admin") || admin.hasRole("merchant")) {
    stats.value = await unwrap<AdminStats>((admin.hasRole("merchant") ? merchantHttp : http).get("/report/stats"))
    await nextTick()
    await refreshCharts()
    return
  }
  // auditor
  const [stores, products] = await Promise.all([
    listAuditorStores({ current: 1, size: 1 }),
    listAuditorProducts({ page: 1, size: 1 })
  ])
  scopedCounts.value = { stores: stores.total, products: products.total }
})

onBeforeUnmount(disposeCharts)

watch([begin, end], () => { /* 仅通过按钮触发 */ })
</script>

<style scoped>
.date-bar {
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  background: #fff; padding: 12px 16px; border-radius: var(--radius); box-shadow: var(--shadow);
}
.date-bar label { font-size: 13px; color: var(--text2); }
.date-input { border: 1px solid var(--border); border-radius: 6px; padding: 4px 10px; font-size: 13px; }
.sep { color: #ccc; }
.charts-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 20px; }
.chart-card { background: #fff; border-radius: var(--radius); padding: 20px; box-shadow: var(--shadow); }
.chart-title { font-size: 14px; margin-bottom: 12px; color: var(--text1); }
.chart-box { width: 100%; height: 320px; }
.stat-card.clickable { cursor: pointer; transition: transform .15s, box-shadow .15s; }
.stat-card.clickable:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,.10); }
.card-arrow { color: var(--primary); font-size: 14px; margin-left: 6px; opacity: 0; transition: opacity .15s; }
.stat-card.clickable:hover .card-arrow { opacity: 1; }
</style>
