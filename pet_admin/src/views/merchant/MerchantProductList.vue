<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>我的商品</h2>
      <p>维护自己门店的商品、库存与上下架状态</p>
    </div>

    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索商品名称" @keyup.enter="search" />
      <select v-model="storeFilter">
        <option value="">全部门店</option>
        <option v-for="item in stores" :key="item.id" :value="item.id">{{ item.storeName }}</option>
      </select>
      <select v-model.number="statusFilter">
        <option :value="-1">全部状态</option>
        <option :value="1">上架</option>
        <option :value="0">下架</option>
        <option :value="2">已售出</option>
      </select>
      <button class="btn btn-primary" @click="search">搜索</button>
      <button class="btn btn-outline" @click="reset">重置</button>
      <button class="btn btn-primary push-right" @click="openCreate">+ 新增商品</button>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>商品</th>
            <th>类型</th>
            <th>门店</th>
            <th>价格</th>
            <th>库存</th>
            <th>状态</th>
            <th>平台限制</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id">
            <td>{{ item.productName || item.name }}</td>
            <td>{{ productTypeLabel(item.productType) }}</td>
            <td>{{ item.store?.storeName || storeName(item.storeId) }}</td>
            <td>¥{{ item.price }}</td>
            <td>
              <span :class="{ 'stock-invalid': isInvalidLivePetStock(item) }">{{ item.stock }}</span>
            </td>
            <td>
              <span class="badge" :class="statusBadge(productStatusCode(item.status, item.statusCode))">
                {{ productStatusLabel(item.status, item.statusCode) }}
              </span>
            </td>
            <td><div v-if="item.platformRestricted" class="restriction"><b>平台强制下架</b><span>{{ item.offlineReason || '未填写原因' }}</span><small>{{ item.offlineTime || '-' }}</small></div><span v-else>-</span></td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item.id)">编辑</button>
              <button
                class="btn btn-sm"
                :class="productStatusCode(item.status, item.statusCode) === 1 ? 'btn-warning' : 'btn-success'"
                :disabled="productStatusCode(item.status, item.statusCode) !== 1 && item.platformRestricted"
                :title="item.platformRestricted ? '平台限制未解除，暂不能上架' : ''"
                @click="toggle(item)"
              >
                {{ productStatusCode(item.status, item.statusCode) === 1 ? '下架' : '上架' }}
              </button>
              <button class="btn btn-danger btn-sm" @click="remove(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!loading && !records.length">
            <td colspan="8" class="empty-row">暂无商品</td>
          </tr>
          <tr v-if="loading">
            <td colspan="8" class="empty-row">加载中...</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination" v-if="total">
        <button :disabled="page <= 1" @click="go(page - 1)">上一页</button>
        <span>第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span>
        <button :disabled="page >= totalPages" @click="go(page + 1)">下一页</button>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ editingId ? '编辑商品' : '新增商品' }}</h3>
          <button class="modal-close" @click="showModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="illegalRecordMessage" class="form-alert">{{ illegalRecordMessage }}</div>

          <div class="form-group">
            <label>所属门店 *</label>
            <select v-model="form.storeId">
              <option value="" disabled>请选择自己的门店</option>
              <option v-for="item in stores" :key="item.id" :value="item.id">{{ item.storeName }}</option>
            </select>
          </div>

          <div class="form-group">
            <label>商品名称 *</label>
            <input v-model.trim="form.productName" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>价格 *</label>
              <input v-model.number="form.price" type="number" min="0" step="0.01" />
            </div>
            <div class="form-group">
              <label>库存 *</label>
              <input
                v-model.number="form.stock"
                type="number"
                min="0"
                :max="stockMax"
                step="1"
              />
              <p class="field-hint">{{ stockHint }}</p>
              <p v-if="stockError" class="field-error">{{ stockError }}</p>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>商品类型</label>
              <select v-model.number="form.productType">
                <option :value="TYPE_LIVE_PET">活体宠物（每条代表1只）</option>
                <option :value="TYPE_SUPPLY">宠物用品/周边</option>
              </select>
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option value="1">上架</option>
                <option value="0">下架</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label>分类</label>
            <input v-model.trim="form.category" />
          </div>
          <div class="form-group">
            <label>主图 URL</label>
            <input v-model.trim="form.mainImage" />
          </div>
          <div class="form-group">
            <label>商品描述</label>
            <textarea v-model.trim="form.productDesc" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="save">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue"
import {
  createMerchantProduct,
  deleteMerchantProduct,
  getMerchantProduct,
  listMerchantProducts,
  offlineMerchantProduct,
  onlineMerchantProduct,
  updateMerchantProduct,
  type MerchantProductPayload
} from "../../api/merchantProduct"
import { listMerchantStores } from "../../api/merchantStore"
import type { Product } from "../../types/product"
import type { Store } from "../../types/store"
import { productStatusCode, productStatusLabel, statusBadge } from "../../utils/status"
import { notify } from "../../utils/notify"

const TYPE_LIVE_PET = 1
const TYPE_SUPPLY = 2

const emptyForm = (): MerchantProductPayload => ({
  storeId: "",
  productName: "",
  productType: TYPE_LIVE_PET,
  category: "",
  productDesc: "",
  price: 0,
  stock: 1,
  mainImage: "",
  status: "1"
})

const records = ref<Product[]>([])
const stores = ref<Store[]>([])
const total = ref(0)
const page = ref(1)
const keyword = ref("")
const storeFilter = ref("")
const statusFilter = ref(-1)
const loading = ref(false)
const showModal = ref(false)
const editingId = ref("")
const saving = ref(false)
const editingRestricted = ref(false)
const size = 10
const form = ref<MerchantProductPayload>(emptyForm())

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size)))
const stockMax = computed(() => form.value.productType === TYPE_LIVE_PET ? 1 : undefined)
const stockHint = computed(() => form.value.productType === TYPE_LIVE_PET
  ? "活体宠物每条商品代表一只，库存只能为0或1"
  : "宠物用品/周边库存可填写任意非负整数")
const stockError = computed(() => stockValidationMessage())
const illegalRecordMessage = computed(() => {
  if (!editingId.value || form.value.productType !== TYPE_LIVE_PET || Number(form.value.stock) <= 1) return ""
  return "当前记录违反活体宠物库存规则，请先把库存修正为0或1后再保存。"
})

const storeName = (id: string | number) => stores.value.find(item => String(item.id) === String(id))?.storeName || "-"

function productTypeLabel(productType?: number) {
  if (productType === TYPE_LIVE_PET) return "活体宠物（每条代表1只）"
  if (productType === TYPE_SUPPLY) return "宠物用品/周边"
  return "-"
}

function isInvalidLivePetStock(item: Product) {
  return item.productType === TYPE_LIVE_PET && Number(item.stock) > 1
}

function stockValidationMessage() {
  const stock = Number(form.value.stock)
  if (!Number.isInteger(stock)) return "库存必须是整数"
  if (stock < 0) return "库存不能小于0"
  if (form.value.productType === TYPE_LIVE_PET && stock > 1) {
    return "活体宠物每条商品代表一只，库存只能为0或1"
  }
  if (form.value.status === "1" && stock === 0) {
    return "库存为0的商品不能上架"
  }
  return ""
}

function validationMessage() {
  if (!form.value.storeId) return "请选择所属门店"
  if (!form.value.productName) return "请填写商品名称"
  if (!Number.isFinite(Number(form.value.price)) || Number(form.value.price) < 0) return "价格不能小于0"
  if (![TYPE_LIVE_PET, TYPE_SUPPLY].includes(Number(form.value.productType))) return "请选择正确的商品类型"
  if (!["0", "1"].includes(form.value.status || "")) return "请选择正确的商品状态"
  if (editingRestricted.value && form.value.status === "1") return "商品仍受平台强制下架限制，请等待监管人员解除限制"
  return stockValidationMessage()
}

async function loadStores() {
  const result = await listMerchantStores({ current: 1, size: 100 })
  stores.value = result.records
}

async function fetchData() {
  loading.value = true
  try {
    const trimmedKeyword = keyword.value.trim()
    const result = await listMerchantProducts({
      page: page.value,
      size,
      keyword: trimmedKeyword || undefined,
      storeId: storeFilter.value || undefined,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined
    })
    records.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  fetchData()
}

function reset() {
  keyword.value = ""
  storeFilter.value = ""
  statusFilter.value = -1
  search()
}

function go(next: number) {
  page.value = next
  fetchData()
}

function openCreate() {
  editingId.value = ""
  editingRestricted.value = false
  form.value = emptyForm()
  showModal.value = true
}

async function openEdit(id: string) {
  const item = await getMerchantProduct(id)
  editingId.value = id
  editingRestricted.value = Boolean(item.platformRestricted)
  form.value = {
    storeId: item.storeId,
    productName: item.productName || item.name || "",
    productType: item.productType || TYPE_LIVE_PET,
    category: item.category || "",
    productDesc: item.productDesc || item.detail || "",
    price: Number(item.price),
    stock: item.stock,
    mainImage: item.mainImage || item.image || "",
    images: item.images,
    status: productStatusCode(item.status, item.statusCode) === 1 ? "1" : "0"
  }
  showModal.value = true
}

async function save() {
  const message = validationMessage()
  if (message) {
    notify(message, "error")
    return
  }
  saving.value = true
  try {
    if (editingId.value) await updateMerchantProduct(editingId.value, form.value)
    else await createMerchantProduct(form.value)
    showModal.value = false
    notify("保存成功")
    await fetchData()
  } finally {
    saving.value = false
  }
}

async function toggle(item: Product) {
  const online = productStatusCode(item.status, item.statusCode) === 1
  if (!online && item.platformRestricted) {
    notify("商品仍受平台强制下架限制，请等待监管人员解除限制", "error")
    return
  }
  if (online) await offlineMerchantProduct(item.id)
  else await onlineMerchantProduct(item.id)
  notify(online ? "下架成功" : "上架成功")
  await fetchData()
}

async function remove(item: Product) {
  if (!confirm(`确定删除商品「${item.productName || item.name}」？`)) return
  await deleteMerchantProduct(item.id)
  notify("删除成功")
  await fetchData()
}

watch(
  () => form.value.productType,
  (next, previous) => {
    if (!showModal.value || next !== TYPE_LIVE_PET || previous === TYPE_LIVE_PET) return
    if (Number(form.value.stock) > 1) {
      form.value.stock = 1
      notify("已将活体宠物库存调整为1", "error")
    }
  }
)

onMounted(async () => {
  await loadStores()
  await fetchData()
})
</script>

<style scoped>
.push-right { margin-left: auto; }
.modal-card { width: min(640px, 100%); }
.form-row .form-group { flex: 1; }
.field-hint { margin-top: 4px; color: var(--text3); font-size: 12px; }
.field-error { margin-top: 4px; color: #c62828; font-size: 12px; }
.form-alert {
  margin-bottom: 16px;
  padding: 10px 12px;
  border: 1px solid #f4b4aa;
  border-radius: var(--radius);
  background: #fff5f3;
  color: #a33a2a;
  font-size: 13px;
}
.stock-invalid { color: #c62828; font-weight: 700; }
.restriction{display:grid;gap:3px;color:#a33a2a;max-width:220px}.restriction span{font-size:12px}.restriction small{color:var(--text3)}
</style>
