<template>
  <div class="admin-page">
    <div class="page-header"><h2>订单管理</h2><p>查看和管理用户订单</p></div>
    <div class="search-bar">
      <select v-model="statusFilter">
        <option value="">全部状态</option><option value="pending">待付款</option><option value="paid">已付款</option><option value="shipped">已发货</option><option value="completed">已完成</option><option value="cancelled">已取消</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>订单号</th><th>用户</th><th>金额</th><th>状态</th><th>地址</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.orderNo }}</td><td>{{ item.userName }}</td>
            <td>¥{{ item.totalAmount }}</td>
            <td><span class="badge" :class="statusBadge(item.status)">{{ statusLabel(item.status) }}</span></td>
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.address }}</td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button v-if="item.status === 'pending'" class="btn btn-primary btn-sm" @click="updateStatus(item, 'cancelled')">取消</button>
              <button v-if="item.status === 'paid'" class="btn btn-success btn-sm" @click="updateStatus(item, 'shipped')">发货</button>
              <button v-if="item.status === 'shipped'" class="btn btn-primary btn-sm" @click="updateStatus(item, 'completed')">完成</button>
              <button v-if="item.status === 'pending'" class="btn btn-danger btn-sm" @click="updateStatus(item, 'cancelled')">取消</button>
            </td>
          </tr>
          <tr v-if="!store.list.length"><td colspan="7" style="text-align:center;padding:32px;color:var(--text2)">暂无数据</td></tr>
        </tbody>
      </table>
      <div class="pagination" v-if="store.total > 0">
        <button :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ store.total }} 条</span>
        <button :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useOrderStore } from "../../stores/order"
import { updateOrderStatus } from "../../api/order"
import type { Order, OrderStatus } from "../../types/order"

const store = useOrderStore()
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function statusBadge(s: OrderStatus) { return { pending: "badge-orange", paid: "badge-blue", shipped: "badge-green", completed: "badge-gray", cancelled: "badge-red" }[s] || "badge-gray" }
function statusLabel(s: OrderStatus) { return { pending: "待付款", paid: "已付款", shipped: "已发货", completed: "已完成", cancelled: "已取消" }[s] || s }

async function fetchData() { await store.fetch({ page: currentPage.value, size: pageSize, status: (statusFilter.value as OrderStatus) || undefined }) }
function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }
async function updateStatus(item: Order, status: OrderStatus) { await updateOrderStatus(item.id, status); fetchData() }

onMounted(fetchData)
</script>
