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
                  ({{ String(item.cancelType) === 'timeout' ? '超时' : '用户' }})
                </template>
              </span>
            </td>
            <td class="ellipsis-cell">{{ item.address }}</td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button v-if="canCancel(item)" class="btn btn-warning btn-sm" @click="cancelOrder(item)">取消</button>
              <button v-if="String(item.status) === '1'" class="btn btn-primary btn-sm" @click="shipOrder(item)">发货</button>

              <button v-if="String(item.status) === '4'" class="btn btn-info btn-sm" @click="showReview(item)">查看评论</button>
              <button v-if="item.cancelReason" class="btn btn-info btn-sm" @click="showReason(item)">查看退款原因</button>
              <template v-if="String(item.status) === '-2'">
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
              <img class="detail-thumb" :src="it.productImage || '/placeholder.png'" :alt="it.productName" />
              <span class="item-name">{{ it.productName }}</span>
              <span class="item-quantity">×{{ it.quantity }}</span>
              <span class="item-price">¥{{ it.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-overlay" v-if="reviewVisible" @click.self="reviewVisible = false">
      <div class="modal review-modal">
        <div class="modal-header">
          <h3>用户评论 — {{ detail?.orderNo }}</h3>
          <button class="modal-close" @click="reviewVisible = false">✕</button>
        </div>
        <div class="modal-body" v-if="detail">
          <div class="review-list">
            <div class="review-item" v-for="it in detail.items" :key="it.id">
              <div class="review-product-info">
                <img class="review-thumb" :src="it.productImage || '/placeholder.png'" :alt="it.productName" />
                <span class="review-name">{{ it.productName }}</span>
              </div>
              <div class="review-content-box">
                <p class="review-text" v-if="it.evaluateContent">{{ it.evaluateContent }}</p>
                <p class="review-text text-gray" v-else>用户未评论</p>
              </div>
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
import { shipOrderAdmin, cancelOrderAdmin, reviewReturn } from "../../api/order"
import type { Order, OrderStatus } from "../../types/order"

const store = useOrderStore()
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10
const detailVisible = ref(false)
const reviewVisible = ref(false)
const detail = ref<Order | null>(null)

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
function canCancel(o: Order) { const s = String(o.status); return s === '0' || s === '1' }

async function fetchData() {
  await store.fetch({
    page: currentPage.value, size: pageSize,
    orderStatus: statusFilter.value || undefined
  })
}

function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

async function cancelOrder(item: Order) {
  const reason = prompt("请输入取消原因（选填）：") || ""
  await cancelOrderAdmin(item.id, reason)
  store.updateLocalStatus(item.id, '-1', { cancelReason: reason, cancelType: 'user' })
}

function showReason(item: Order) {
  alert(item.cancelReason || "未填写退款原因");
}

async function shipOrder(item: Order) {
  await shipOrderAdmin(item.id)
  store.updateLocalStatus(item.id, '2')
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

function showReview(item: Order) {
  detail.value = item
  reviewVisible.value = true
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
  display: flex; align-items: center; justify-content: space-between; padding: 8px 0;
  border-bottom: 1px solid #f5f5f5; font-size: 13px; gap: 12px;
}
.detail-thumb {
  width: 40px; height: 40px; border-radius: 4px; object-fit: cover;
}
.item-name { flex: 1; font-weight: 500; }
.item-quantity { color: #888; }
.item-price { color: var(--primary); font-weight: 600; min-width: 60px; text-align: right; }
.badge-teal { background: #e0f2f1; color: #00695c; }
.btn-info { background-color: #3498db; color: white; border: none; }
.btn-info:hover { background-color: #2980b9; }
.review-modal { width: 500px; }
.review-item { margin-bottom: 20px; border-bottom: 1px dashed #eee; padding-bottom: 15px; }
.review-item:last-child { border-bottom: none; }
.review-product-info { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.review-thumb { width: 40px; height: 40px; border-radius: 4px; object-fit: cover; }
.review-name { font-weight: 500; font-size: 14px; }
.review-content-box { background: #f8f9fa; padding: 12px; border-radius: 6px; }
.review-text { margin: 0; font-size: 14px; line-height: 1.5; color: #333; }
.text-gray { color: #999; font-style: italic; }
</style>
