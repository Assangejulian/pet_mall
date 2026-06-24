<template>
  <div class="admin-page">
    <div class="page-header"><h2>门店管理</h2><p>管理合作宠物门店</p></div>
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索门店名称" @keyup.enter="handleSearch" />
      <select v-model.number="statusFilter">
        <option :value="-1">全部状态</option><option :value="0">待审核</option><option :value="1">营业中</option><option :value="2">已关闭</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>门店名称</th><th>电话</th><th>地址</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td>
            <td><div style="display:flex;align-items:center;gap:8px"><img :src="item.storeLogo" class="avatar" />{{ item.storeName }}</div></td>
            <td>{{ item.storePhone }}</td>
            <td>{{ item.province }}{{ item.city }}{{ item.district }}{{ item.address }}</td>
            <td><span class="badge" :class="statusBadge(item.status)">{{ statusLabel(item.status) }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item)">审核</button>
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

    <!-- Audit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <div class="modal-header"><h3>门店审核</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label>门店名称</label><p>{{ editForm.storeName }}</p></div>
          <div class="form-group"><label>描述</label><p>{{ editForm.storeDesc }}</p></div>
          <div class="form-group"><label>电话</label><p>{{ editForm.storePhone }}</p></div>
          <div class="form-group"><label>地址</label><p>{{ editForm.province }}{{ editForm.city }}{{ editForm.district }}{{ editForm.address }}</p></div>
          <div class="form-group"><label>审核状态</label>
            <select v-model.number="editForm.status"><option :value="0">待审核</option><option :value="1">通过（营业中）</option><option :value="2">拒绝（已关闭）</option></select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="saveAudit">提交审核</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useStoreStore } from "../../stores/store"
import { updateStore } from "../../api/store"
import type { Store } from "../../types/store"

const store = useStoreStore()
const keyword = ref("")
const statusFilter = ref(-1)
const currentPage = ref(1)
const pageSize = 10
const showModal = ref(false)
const editForm = ref<Partial<Store>>({})

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function statusBadge(s: number) { return ["badge-orange","badge-green","badge-red"][s] || "badge-gray" }
function statusLabel(s: number) { return ["待审核","营业中","已关闭"][s] || "未知" }

async function fetchData() { await store.fetch({ current: currentPage.value, size: pageSize, keyword: keyword.value || undefined, status: statusFilter.value >= 0 ? statusFilter.value : undefined }) }
function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; statusFilter.value = -1; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openEdit(item: Store) { editForm.value = { ...item }; showModal.value = true }
async function saveAudit() { if (editForm.value.id) { await updateStore(editForm.value.id, { status: editForm.value.status }); showModal.value = false; fetchData() } }

onMounted(fetchData)
</script>
