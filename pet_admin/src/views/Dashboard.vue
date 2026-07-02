<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>概览</h2>
      <p>{{ description }}</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card" v-for="card in cards" :key="card.label">
        <h3>{{ card.label }}</h3>
        <div class="num">{{ card.value }} <span class="unit">{{ card.unit }}</span></div>
      </div>
    </div>

    <div class="quick-grid" v-if="quickLinks.length">
      <router-link v-for="link in quickLinks" :key="link.path" :to="link.path" class="quick-card">
        <strong>{{ link.label }}</strong>
        <span>{{ link.hint }}</span>
      </router-link>
    </div>

    <div class="card-section" v-if="admin.hasRole('admin') && orderStatusList.length">
      <h3 class="section-title">订单状态分布</h3>
      <div class="status-grid">
        <div class="status-item" v-for="item in orderStatusList" :key="item.label">
          <span class="badge" :class="item.badge">{{ item.label }}</span>
          <span class="count">{{ item.count }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue"
import { useAdminStore } from "../stores/admin"
import http from "../api/index"
import { unwrap } from "../api/helper"
import { listMerchantStores } from "../api/merchantStore"
import { listMerchantProducts } from "../api/merchantProduct"
import { listAuditorStores } from "../api/auditorStore"
import { listAuditorProducts } from "../api/auditorProduct"

interface AdminStats {
  userCount: number
  storeCount: number
  productCount: number
  todayOrders: number
  totalRevenue: number
  orderStatusCount: Record<string, number>
}

const admin = useAdminStore()
const stats = ref<AdminStats>({ userCount: 0, storeCount: 0, productCount: 0, todayOrders: 0, totalRevenue: 0, orderStatusCount: {} })
const scopedCounts = ref({ stores: 0, products: 0 })

const description = computed(() => admin.hasRole("merchant")
  ? "管理自己的门店与商品"
  : admin.hasRole("auditor") ? "查看平台审核与监管数据" : "系统数据总览")

const cards = computed(() => admin.hasRole("admin") ? [
  { label: "用户总数", value: stats.value.userCount, unit: "人" },
  { label: "门店总数", value: stats.value.storeCount, unit: "家" },
  { label: "商品总数", value: stats.value.productCount, unit: "件" },
  { label: "今日订单", value: stats.value.todayOrders, unit: "笔" },
  { label: "总营收", value: stats.value.totalRevenue, unit: "元" },
] : [
  { label: admin.hasRole("merchant") ? "我的门店" : "平台门店", value: scopedCounts.value.stores, unit: "家" },
  { label: admin.hasRole("merchant") ? "我的商品" : "平台商品", value: scopedCounts.value.products, unit: "件" },
])

const quickLinks = computed(() => admin.hasRole("merchant") ? [
  { path: "/merchant/store", label: "我的门店", hint: "维护经营资料与审核状态" },
  { path: "/merchant/product", label: "我的商品", hint: "维护商品与上下架状态" },
] : admin.hasRole("auditor") ? [
  { path: "/auditor/store", label: "门店审核", hint: "处理待审核门店" },
  { path: "/auditor/product", label: "商品监管", hint: "查看并处理违规商品" },
] : [])

const statusLabels: Record<string, string> = { "0": "待支付", "1": "已支付", "2": "已发货", "3": "已收货", "4": "已评价", "-1": "已取消", "-2": "待审核退款", "-3": "退单通过", "-4": "已退款" }
const orderStatusList = computed(() => Object.entries(stats.value.orderStatusCount).map(([key, count]) => ({ label: statusLabels[key] || key, count, badge: Number(key) < 0 ? "badge-red" : "badge-green" })))

onMounted(async () => {
  if (admin.hasRole("admin")) {
    stats.value = await unwrap<AdminStats>(http.get("/dashboard/stats"))
    return
  }
  if (admin.hasRole("merchant")) {
    const [stores, products] = await Promise.all([listMerchantStores({ current: 1, size: 1 }), listMerchantProducts({ page: 1, size: 1 })])
    scopedCounts.value = { stores: stores.total, products: products.total }
    return
  }
  const [stores, products] = await Promise.all([listAuditorStores({ current: 1, size: 1 }), listAuditorProducts({ page: 1, size: 1 })])
  scopedCounts.value = { stores: stores.total, products: products.total }
})
</script>

<style scoped>
.quick-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:16px; }
.quick-card { background:#fff; border:1px solid var(--border); border-radius:var(--radius); padding:20px; display:flex; flex-direction:column; gap:6px; transition:.15s; }
.quick-card:hover { border-color:var(--primary); transform:translateY(-1px); }
.quick-card strong { color:var(--text1); font-size:16px; }
.quick-card span { color:var(--text2); font-size:13px; }
.card-section { background:#fff; border-radius:var(--radius); padding:24px; box-shadow:var(--shadow); margin-top:24px; }
.section-title { font-size:15px; margin-bottom:16px; }
.status-grid { display:flex; flex-wrap:wrap; gap:12px; }
.status-item { display:flex; align-items:center; gap:10px; padding:8px 16px; background:var(--bg); border-radius:8px; }
.count { font-size:18px; font-weight:700; }
</style>
