<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>订单管理</h2>
      <p>查看和管理用户订单 · 退单审核
        <span v-if="store.pendingReturnCount > 0" class="alert-badge">
          待审核 {{ store.pendingReturnCount }}
        </span>
      </p>
    </div>

    <div class="search-bar">
      <select v-model="statusFilter">
        <option value="">全部状态</option>
        <option value="0">待支付</option>
        <option value="1">已支付</option>
        <option value="2">已发货</option>
        <option value="3">已收货</option>
        <option value="4">已评价</option>
        <option value="-1">已取消</option>
        <option value="-2">申请退单</option>
        <option value="-3">退单通过</option>
        <option value="-4">已退单</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户</th>
            <th>金额</th>
            <th>状态</th>
            <th>地址</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td class="order-no" @click="showDetail(item)">{{ item.orderNo }}</td>
            <td>{{ item.userName }}</td>
            <td>¥{{ item.totalAmount }}</td>
            <td>
              <span class="badge" :class="statusBadge(item.status)">
                {{ statusLabel(item.status) }}
                <template v-if="item.status === '-1' && item.cancelType">
                  ({{ item.cancelType === 'timeout' ? '超时' : '用户' }})
                </template>
              </span>
            </td>
            <td class="ellipsis-cell">{{ item.address }}</td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button v-if="canCancel(item)" class="btn btn-warning btn-sm" @click="cancelOrder(item)">取消</button>
              <button v-if="item.status === '1'" class="btn btn-primary btn-sm" @click="shipOrder(item)">发货</button>
              <button v-if="item.status === '2'" class="btn btn-success btn-sm" @click="completeOrder(item)">收货完成</button>
              <button v-if="canDirectReturn(item)" class="btn btn-danger btn-sm" @click="directReturn(item)">退单</button>
              <template v-if="item.status === '-2'">
                <button class="btn btn-success btn-sm" @click="approveReturn(item)">通过</button>
                <button class="btn btn-danger btn-sm" @click="rejectReturn(item)">拒绝</button>
              </template>
            </td>
          </tr>
          <tr v-if="!store.list.length">
            <td colspan="7" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="store.total > 0">
        <button :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ store.total }} 条</span>
        <button :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
      </div>
    </div>

    <div class="modal-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="modal">
        <div class="modal-header">
          <h3>订单详情 — {{ detail?.orderNo }}</h3>
          <button class="modal-close" @click="detailVisible = false">✕</button>
        </div>
        <div class="modal-body" v-if="detail">
          <div class="detail-info">
            <div><label>用户</label><span>{{ detail.userName }}</span></div>
            <div><label>金额</label><span>¥{{ detail.totalAmount }}</span></div>
            <div><label>状态</label><span class="badge" :class="statusBadge(detail.status)">{{ statusLabel(detail.status) }}</span></div>
            <div><label>地址</label><span>{{ detail.address }}</span></div>
            <div><label>创建时间</label><span>{{ detail.createTime }}</span></div>
            <div v-if="detail.payTime"><label>支付时间</label><span>{{ detail.payTime }}</span></div>
            <div v-if="detail.returnReason"><label>退单理由</label><span class="text-danger">{{ detail.returnReason }}</span></div>
            <div v-if="detail.cancelReason"><label>取消原因</label><span>{{ detail.cancelReason }}</span></div>
          </div>
          <div class="detail-items">
            <h4>商品明细</h4>
            <div class="item-row" v-for="it in detail.items" :key="it.id">
              <span class="item-name">{{ it.productName }}</span>
              <span>×{{ it.quantity }}</span>
              <span>¥{{ it.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useOrderStore } from "../../stores/order"
import { updateOrderStatus, reviewReturn } from "../../api/order"
import type { Order, OrderStatus } from "../../types/order"

const store = useOrderStore()
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10
const detail = ref<Order | null>(null)
const detailVisible = ref(false)

const totalPages = computed(() => Math.ceil(store.total / pageSize) || 1)

const statusMap: Record<string, string> = {
  '0': '待支付', '1': '已支付', '2': '已发货', '3': '已收货', '4': '已评价',
  '-1': '已取消', '-2': '申请退单', '-3': '退单通过', '-4': '已退单'
}
const badgeMap: Record<string, string> = {
  '0': 'badge-orange', '1': 'badge-blue', '2': 'badge-green',
  '3': 'badge-teal', '4': 'badge-gray',
  '-1': 'badge-gray', '-2': 'badge-red', '-3': 'badge-green', '-4': 'badge-red'
}

function statusLabel(s: OrderStatus) { return statusMap[s] || s }
function statusBadge(s: OrderStatus) { return badgeMap[s] || 'badge-gray' }
function canCancel(o: Order) { return o.status === '0' || o.status === '1' }
function canDirectReturn(o: Order) { return o.status === '3' }

async function fetchData() {
  await store.fetch({
    page: currentPage.value, size: pageSize,
    status: (statusFilter.value as OrderStatus) || undefined
  })
}

function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

async function cancelOrder(item: Order) {
  const reason = prompt("请输入取消原因（选填）：") || ""
  await updateOrderStatus(item.id, '-1', reason)
  store.updateLocalStatus(item.id, '-1', { cancelReason: reason, cancelType: 'user' })
}

async function shipOrder(item: Order) {
  await updateOrderStatus(item.id, '2')
  store.updateLocalStatus(item.id, '2')
}

async function completeOrder(item: Order) {
  await updateOrderStatus(item.id, '3')
  store.updateLocalStatus(item.id, '3')
}

async function directReturn(item: Order) {
  const reason = prompt("请输入退单理由：")
  if (!reason) return
  await updateOrderStatus(item.id, '-4', reason)
  store.updateLocalStatus(item.id, '-4', { returnReason: reason })
}

async function approveReturn(item: Order) {
  await reviewReturn(item.id, true, "审核通过")
  store.updateLocalStatus(item.id, '-3')
}

async function rejectReturn(item: Order) {
  const reason = prompt("请输入拒绝理由：") || "审核不通过"
  await reviewReturn(item.id, false, reason)
  store.updateLocalStatus(item.id, '3' as OrderStatus)
}

function showDetail(item: Order) {
  detail.value = item
  detailVisible.value = true
}

onMounted(fetchData)
</script>
<style scoped>
.order-no { color: var(--primary); cursor: pointer; font-weight: 600; }
.order-no:hover { text-decoration: underline; }
.ellipsis-cell { max-width: 180px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.text-danger { color: #e74c3c; }
.alert-badge {
  display: inline-flex; align-items: center; gap: 4px;
  background: #e74c3c; color: #fff; font-size: 12px;
  padding: 2px 10px; border-radius: 10px; margin-left: 8px;
}
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: #fff; border-radius: 12px; width: 580px; max-height: 80vh;
  display: flex; flex-direction: column; box-shadow: 0 8px 32px rgba(0,0,0,.15);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid var(--border);
}
.modal-header h3 { font-size: 16px; }
.modal-close { background: none; border: none; font-size: 18px; cursor: pointer; color: #999; }
.modal-body { padding: 24px; overflow-y: auto; }
.detail-info div { display: flex; padding: 6px 0; font-size: 14px; }
.detail-info label { width: 80px; color: #999; flex-shrink: 0; }
.detail-items { margin-top: 20px; }
.detail-items h4 { font-size: 14px; margin-bottom: 12px; }
.item-row {
  display: flex; justify-content: space-between; padding: 8px 0;
  border-bottom: 1px solid #f5f5f5; font-size: 13px;
}
.item-name { flex: 1; }
.badge-teal { background: #e0f2f1; color: #00695c; }
</style>