<template>
  <div class="admin-page">
    <div class="page-header"><h2>商品管理</h2><p>管理平台商品信息</p></div>

    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索商品名称" @keyup.enter="handleSearch" />
      <select v-model.number="typeFilter">
        <option :value="-1">全部类型</option><option :value="1">宠物</option><option :value="2">周边</option>
      </select>
      <select v-model="statusFilter">
        <option value="">全部状态</option><option value="1">上架</option><option value="0">下架</option><option value="2">已售出</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
      <button class="btn btn-success" @click="openAdd" style="margin-left:auto">+ 新增商品</button>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>商品</th><th>类型</th><th>分类</th><th>价格</th><th>库存</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td>
            <td><div style="display:flex;align-items:center;gap:8px"><img :src="item.mainImage || item.image" class="img-preview" />{{ item.productName || item.name }}</div></td>
            <td>{{ productTypeLabel(item.productType) }}</td>
            <td>{{ item.category || '-' }}</td>
            <td>¥{{ item.price }}</td>
            <td>{{ item.stock }}</td>
            <td><span class="badge" :class="statusBadge(item)">{{ statusLabel(item) }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item)">编辑</button>
              <button class="btn btn-sm" :class="item.status === 1 || item.statusCode === 1 ? 'btn-warning' : 'btn-success'" @click="toggleStatus(item)">{{ (item.status === 1 || item.statusCode === 1) ? '下架' : '上架' }}</button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!store.list.length && !store.loading"><td colspan="9" style="text-align:center;padding:32px;color:var(--text2)">暂无数据</td></tr>
          <tr v-if="store.loading"><td colspan="9" style="text-align:center;padding:32px;color:var(--text2)">加载中...</td></tr>
        </tbody>
      </table>
      <div class="pagination" v-if="store.total > 0">
        <button :disabled="currentPage <= 1" @click="goPage(currentPage - 1)">上一页</button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页，共 {{ store.total }} 条</span>
        <button :disabled="currentPage >= totalPages" @click="goPage(currentPage + 1)">下一页</button>
      </div>
    </div>

    <!-- Add / Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card" style="min-width:520px">
        <div class="modal-header"><h3>{{ isAdd ? '新增商品' : '编辑商品' }}</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group"><label>商品名称</label><input v-model="form.productName" placeholder="请输入商品名称" /></div>
            <div class="form-group"><label>价格</label><input v-model.number="form.price" type="number" step="0.01" /></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>商品类型</label>
              <select v-model.number="form.productType"><option :value="1">宠物</option><option :value="2">周边</option></select>
            </div>
            <div class="form-group"><label>分类</label>
              <select v-model="form.category"><option value="cat">猫</option><option value="dog">狗</option><option value="bird">鸟</option><option value="fish">鱼</option><option value="other">其他</option><option value="food">粮食</option><option value="accessory">配件</option></select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group"><label>库存</label><input v-model.number="form.stock" type="number" /></div>
            <div class="form-group"><label>所属门店 ID</label><input v-model.number="form.storeId" type="number" /></div>
          </div>
          <div class="form-group"><label>商品描述</label><textarea v-model="form.productDesc" rows="3"></textarea></div>
          <div class="form-group"><label>主图链接</label><input v-model="form.mainImage" placeholder="https://..." /></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="saveProduct">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from "vue"
import { useProductStore } from "../../stores/product"
import { onlineProduct, offlineProduct, deleteProduct, updateProduct, createProduct } from "../../api/product"
import type { Product } from "../../types/product"

const store = useProductStore()
const keyword = ref("")
const typeFilter = ref(-1)
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10
const showModal = ref(false)
const isAdd = ref(false)
const form = reactive({}) as any

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function productTypeLabel(t: number | undefined) {
  return t === 1 ? '宠物' : t === 2 ? '周边' : '-'
}

function resolveStatusCode(item: Product): number {
  return item.statusCode ?? Number(item.status ?? 0)
}

function statusBadge(item: Product) {
  const s = resolveStatusCode(item)
  return s === 1 ? 'badge-green' : s === 2 ? 'badge-gray' : 'badge-gray'
}

function statusLabel(item: Product) {
  const s = resolveStatusCode(item)
  return s === 1 ? '上架' : s === 2 ? '已售出' : '下架'
}

async function fetchData() {
  const params = { current: currentPage.value, size: pageSize, keyword: keyword.value || undefined, productType: typeFilter.value >= 0 ? typeFilter.value : undefined, status: statusFilter.value || undefined }
  if (keyword.value) params.keyword = keyword.value
  if (typeFilter.value >= 0) params.productType = typeFilter.value
  if (statusFilter.value) params.status = statusFilter.value
  await store.fetch(params)
}

function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; typeFilter.value = -1; statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openAdd() {
  isAdd.value = true
  Object.assign(form, { productName: "", price: 0, stock: 1, productType: 1, category: "cat", storeId: 1, productDesc: "", mainImage: "" })
  showModal.value = true
}

function openEdit(item: Product) {
  isAdd.value = false
  Object.assign(form, {
    id: item.id,
    productName: item.productName || item.name,
    price: item.price,
    stock: item.stock,
    productType: item.productType || 1,
    category: item.category || "",
    storeId: item.storeId,
    productDesc: item.productDesc || item.detail,
    mainImage: item.mainImage || item.image || ""
  })
  showModal.value = true
}

async function saveProduct() {
  try {
    if (isAdd.value) {
      await createProduct(form)
    } else {
      await updateProduct(form.id, form)
    }
    showModal.value = false
    fetchData()
    showToast('保存成功', 'success')
  } catch (e: any) {
    showToast(e?.message || '保存失败', 'error')
  }
}

async function toggleStatus(item: Product) {
  try {
    const id = item.id
    const isOnline = resolveStatusCode(item) === 1
    if (isOnline) {
      await offlineProduct(id)
    } else {
      await onlineProduct(id)
    }
    fetchData()
    showToast(isOnline ? '下架成功' : '上架成功', 'success')
  } catch (e: any) {
    showToast(e?.message || '操作失败', 'error')
  }
}

async function handleDelete(item: Product) {
  if (!confirm('确定删除商品「' + (item.productName || item.name) + '」？')) return
  try {
    await deleteProduct(item.id)
    fetchData()
    showToast('删除成功', 'success')
  } catch (e: any) {
    showToast(e?.message || '删除失败', 'error')
  }
}

function showToast(msg: string, type: string) {
  const el = document.querySelector('.toast-container')
  if (el) {
    const event = new CustomEvent('toast', { detail: { message: msg, type } })
    document.dispatchEvent(event)
  }
}

onMounted(fetchData)
</script>

<style scoped>
.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
</style>

