<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>我的订单</h2>
      <p>查看和管理自己店铺的订单</p>
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
            <th>商品</th>
            <th>订单号</th>
            <th>金额</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.id">
            <td class="product-cell">
              <div class="product-list" v-if="item.items?.length">
                <div class="product-item" v-for="it in item.items" :key="it.id">
                  <img class="product-thumb" :src="it.productImage || '/placeholder.png'" :alt="it.productName" />
                  <div class="product-meta">
                    <span class="product-name">{{ it.productName }}</span>
                    <span class="product-qty">×{{ it.quantity }}</span>
                  </div>
                </div>
              </div>
              <span v-else class="text-muted">暂无商品</span>
            </td>
            <td class="order-no" @click="showDetail(item)">{{ item.orderNo }}</td>
            <td class="amount">¥{{ item.totalAmount?.toFixed(2) }}</td>
            <td>
              <span class="badge" :class="statusBadge(item.status)">
                {{ statusLabel(item.status) }}
                <template v-if="item.status === '-1' && item.cancelType">
                  ({{ String(item.cancelType) === 'timeout' ? '超时' : '用户' }})
                </template>
              </span>
            </td>
            <td class="time-cell">{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-sm btn-outline" @click="showDetail(item)">详情</button>
              <button v-if="String(item.status) === '1'" class="btn btn-sm btn-primary" @click="openShipModal(item)">发货</button>
            </td>
          </tr>
          <tr v-if="!list.length">
            <td colspan="6" class="empty-row">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination" v-if="total > 0">
        <button :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ total }} 条</span>
        <button :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div class="modal-overlay" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="modal modal-lg">
        <div class="modal-header">
          <h3>订单详情 — {{ detail?.orderNo }}</h3>
          <button class="modal-close" @click="detailVisible = false">✕</button>
        </div>
        <div class="modal-body" v-if="detail">
          <div class="detail-grid">
            <div class="detail-section">
              <h4>订单信息</h4>
              <div class="detail-row"><label>订单号</label><span>{{ detail.orderNo }}</span></div>
              <div class="detail-row"><label>总金额</label><span class="amount">¥{{ detail.totalAmount?.toFixed(2) }}</span></div>
              <div class="detail-row"><label>状　态</label><span class="badge" :class="statusBadge(detail.status)">{{ statusLabel(detail.status) }}</span></div>
              <div class="detail-row"><label>创建时间</label><span>{{ detail.createTime }}</span></div>
              <div class="detail-row" v-if="detail.payTime"><label>支付时间</label><span>{{ detail.payTime }}</span></div>
              <div class="detail-row" v-if="detail.shipTime"><label>发货时间</label><span>{{ detail.shipTime }}</span></div>
              <div class="detail-row" v-if="detail.logisticsCarrier"><label>物流公司</label><span>{{ detail.logisticsCarrier }}</span></div>
              <div class="detail-row" v-if="detail.logisticsNo"><label>物流单号</label><span>{{ detail.logisticsNo }}</span></div>
              <div class="detail-row" v-if="detail.receiveTime"><label>收货时间</label><span>{{ detail.receiveTime }}</span></div>
              <div class="detail-row" v-if="detail.cancelReason"><label>取消原因</label><span class="text-danger">{{ detail.cancelReason }}</span></div>
              <div class="detail-row" v-if="detail.returnReason"><label>退单理由</label><span class="text-danger">{{ detail.returnReason }}</span></div>
            </div>
            <div class="detail-section">
              <h4>收货信息</h4>
              <div class="detail-row"><label>收件人</label><span>{{ detail.userName || '—' }}</span></div>
              <div class="detail-row"><label>地址</label><span>{{ detail.address || '—' }}</span></div>
            </div>
          </div>
          <div class="detail-items-section">
            <h4>商品明细</h4>
            <table class="detail-items-table">
              <thead>
                <tr>
                  <th></th>
                  <th>商品</th>
                  <th>单价</th>
                  <th>数量</th>
                  <th>小计</th>
                  <th>评价</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="it in detail.items" :key="it.id">
                  <td><img class="detail-thumb" :src="it.productImage || '/placeholder.png'" :alt="it.productName" /></td>
                  <td>{{ it.productName }}</td>
                  <td>¥{{ it.price?.toFixed(2) }}</td>
                  <td>×{{ it.quantity }}</td>
                  <td>¥{{ (it.price * it.quantity)?.toFixed(2) }}</td>
                  <td>
                    <span v-if="it.evaluateStar" class="stars">{{ '★'.repeat(it.evaluateStar) }}{{ '☆'.repeat(5 - it.evaluateStar) }}</span>
                    <span v-else class="text-muted">—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="shipVisible" @click.self="closeShipModal">
      <div class="modal">
        <div class="modal-header">
          <h3>订单发货 — {{ shippingTarget?.orderNo }}</h3>
          <button class="modal-close" @click="closeShipModal">✕</button>
        </div>
        <form class="modal-body ship-form" @submit.prevent="submitShip">
          <label class="form-row">
            <span>物流公司</span>
            <input v-model.trim="shipForm.carrier" maxlength="100" placeholder="例如 顺丰速运" />
          </label>
          <label class="form-row">
            <span>物流单号</span>
            <input v-model.trim="shipForm.logisticsNo" maxlength="100" placeholder="请输入运单号" />
          </label>
          <p v-if="shipError" class="form-error">{{ shipError }}</p>
          <div class="form-actions">
            <button type="button" class="btn btn-outline" @click="closeShipModal">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="shipping">
              {{ shipping ? '提交中...' : '确认发货' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { listMerchantOrders, shipMerchantOrder } from "../../api/merchant-order"
import type { Order, OrderStatus } from "../../types/order"

const list = ref<Order[]>([])
const total = ref(0)
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10
const detail = ref<Order | null>(null)
const detailVisible = ref(false)
const shipVisible = ref(false)
const shipping = ref(false)
const shippingTarget = ref<Order | null>(null)
const shipForm = ref({ carrier: "", logisticsNo: "" })
const shipError = ref("")

const totalPages = computed(() => Math.ceil(total.value / pageSize) || 1)

const statusMap: Record<string, string> = {
  '0': '待支付', '1': '已支付', '2': '已发货', '3': '已收货', '4': '已评价',
  '-1': '已取消', '-2': '申请退单', '-3': '退单通过', '-4': '管理员退单'
}
const badgeMap: Record<string, string> = {
  '0': 'badge-orange', '1': 'badge-blue', '2': 'badge-green',
  '3': 'badge-teal', '4': 'badge-gray',
  '-1': 'badge-gray', '-2': 'badge-red', '-3': 'badge-green', '-4': 'badge-red'
}

function statusLabel(s: OrderStatus) { return statusMap[s] || s }
function statusBadge(s: OrderStatus) { return badgeMap[s] || 'badge-gray' }

async function fetchData() {
  const r = await listMerchantOrders({
    current: currentPage.value, size: pageSize,
    orderStatus: statusFilter.value || undefined
  })
  list.value = r.records
  total.value = r.total
}

function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openShipModal(item: Order) {
  shippingTarget.value = item
  shipForm.value = {
    carrier: item.logisticsCarrier || "",
    logisticsNo: item.logisticsNo || ""
  }
  shipError.value = ""
  shipVisible.value = true
}

function closeShipModal() {
  if (shipping.value) return
  shipVisible.value = false
  shippingTarget.value = null
  shipError.value = ""
}

async function submitShip() {
  const carrier = shipForm.value.carrier.trim()
  const logisticsNo = shipForm.value.logisticsNo.trim()
  if (!carrier) { shipError.value = "请输入物流公司"; return }
  if (!logisticsNo) { shipError.value = "请输入物流单号"; return }
  if (carrier.length > 100 || logisticsNo.length > 100) {
    shipError.value = "物流公司和物流单号长度不能超过100"
    return
  }
  if (!shippingTarget.value) return
  shipping.value = true
  shipError.value = ""
  try {
    await shipMerchantOrder({ orderId: shippingTarget.value.id, carrier, logisticsNo })
    shippingTarget.value.status = '2' as OrderStatus
    shippingTarget.value.logisticsCarrier = carrier
    shippingTarget.value.logisticsNo = logisticsNo
    shipVisible.value = false
    await fetchData()
  } finally {
    shipping.value = false
  }
}

function showDetail(item: Order) {
  detail.value = item
  detailVisible.value = true
}

onMounted(fetchData)
</script>

<style scoped>
/* 商品列 */
.product-cell { min-width: 200px; }
.product-list { display: flex; flex-direction: column; gap: 6px; }
.product-item { display: flex; align-items: center; gap: 8px; }
.product-thumb { width: 40px; height: 40px; border-radius: 6px; object-fit: cover; flex-shrink: 0; background: #f5f5f5; }
.product-meta { display: flex; flex-direction: column; gap: 1px; }
.product-name { font-size: 13px; font-weight: 500; line-height: 1.3; }
.product-qty { color: #999; font-size: 12px; }

.order-no { color: var(--primary); cursor: pointer; font-weight: 600; white-space: nowrap; }
.order-no:hover { text-decoration: underline; }
.amount { font-weight: 600; color: #e74c3c; white-space: nowrap; }
.time-cell { white-space: nowrap; font-size: 12px; color: #888; }
.text-muted { color: #bbb; font-size: 12px; }
.text-danger { color: #e74c3c; }

/* 详情弹窗 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal { background: #fff; border-radius: 12px; width: 580px; max-height: 80vh; display: flex; flex-direction: column; box-shadow: 0 8px 32px rgba(0,0,0,.15); }
.modal-lg { width: 720px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid var(--border); }
.modal-header h3 { font-size: 16px; }
.modal-close { background: none; border: none; font-size: 18px; cursor: pointer; color: #999; }
.modal-body { padding: 24px; overflow-y: auto; }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }
.detail-section h4 { font-size: 14px; margin-bottom: 12px; color: #333; border-bottom: 1px solid #f0f0f0; padding-bottom: 8px; }
.detail-row { display: flex; padding: 5px 0; font-size: 13px; }
.detail-row label { width: 72px; color: #999; flex-shrink: 0; }
.detail-row span { word-break: break-all; }

.ship-form { display: flex; flex-direction: column; gap: 14px; }
.form-row { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #555; }
.form-row input { height: 36px; border: 1px solid var(--border); border-radius: 6px; padding: 0 10px; font: inherit; }
.form-row input:focus { outline: none; border-color: var(--primary); }
.form-error { color: #e74c3c; font-size: 13px; margin: 0; }
.form-actions { display: flex; justify-content: flex-end; gap: 10px; padding-top: 4px; }

.detail-items-section { margin-top: 8px; }
.detail-items-section h4 { font-size: 14px; margin-bottom: 12px; color: #333; border-bottom: 1px solid #f0f0f0; padding-bottom: 8px; }
.detail-items-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.detail-items-table th { text-align: left; padding: 8px 6px; border-bottom: 2px solid #f0f0f0; color: #888; font-weight: 500; font-size: 12px; }
.detail-items-table td { padding: 10px 6px; border-bottom: 1px solid #f5f5f5; vertical-align: middle; }
.detail-thumb { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; background: #f5f5f5; }

.stars { color: #f39c12; letter-spacing: 1px; }

.badge-teal { background: #e0f2f1; color: #00695c; }
</style>
