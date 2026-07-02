<template>
  <div class="admin-page">
    <div class="page-header"><h2>门店管理</h2><p>管理合作宠物门店</p></div>
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索门店名称" @keyup.enter="handleSearch" />
      <select v-model.number="statusFilter">
        <option :value="-1">全部状态</option><option :value="0">待审核</option><option :value="1">营业中</option><option :value="2">已关闭</option><option :value="3">审核驳回</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
      <button class="btn btn-primary" @click="openCreate" style="margin-left:auto">+ 新增门店</button>
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
              <button class="btn btn-outline btn-sm" @click="openEdit(item)">编辑</button>
              <button v-if="item.status === 0" class="btn btn-outline btn-sm" @click="openAudit(item)">审核</button>
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

    <!-- Edit / Create Modal -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal-card" style="max-width:560px">
        <div class="modal-header"><h3>{{ isCreate ? '新增门店' : '编辑门店' }}</h3><button class="modal-close" @click="showEditModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label>门店名称 <span class="red">*</span></label><input v-model="editForm.storeName" class="form-input" /></div>
          <div class="form-group"><label>店主用户ID <span class="red">*</span></label><input v-model.number="editForm.userId" class="form-input" type="number" /></div>
          <div class="form-group"><label>门店Logo URL</label><input v-model="editForm.storeLogo" class="form-input" placeholder="https://..." /></div>
          <div class="form-group"><label>联系电话</label><input v-model="editForm.storePhone" class="form-input" /></div>
          <div class="form-group"><label>描述</label><textarea v-model="editForm.storeDesc" class="form-input" rows="3"></textarea></div>
          <div class="form-row">
            <div class="form-group" style="flex:1"><label>省份</label><input v-model="editForm.province" class="form-input" placeholder="福建省" /></div>
            <div class="form-group" style="flex:1"><label>城市</label><input v-model="editForm.city" class="form-input" placeholder="厦门市" /></div>
            <div class="form-group" style="flex:1"><label>区县</label><input v-model="editForm.district" class="form-input" placeholder="思明区" /></div>
          </div>
          <div class="form-group"><label>详细地址 <span class="red">*</span></label><input v-model="editForm.address" class="form-input" placeholder="填写地址后自动解析坐标" /></div>
          <div class="form-row">
            <div class="form-group" style="flex:1"><label>经度（选填）</label><input v-model.number="editForm.longitude" class="form-input" type="number" step="0.0001" /></div>
            <div class="form-group" style="flex:1"><label>纬度（选填）</label><input v-model.number="editForm.latitude" class="form-input" type="number" step="0.0001" /></div>
          </div>
          <p class="hint">不填经纬度时，系统将通过高德地图自动从地址解析坐标</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showEditModal = false">取消</button>
          <button class="btn btn-primary" @click="saveEdit" :disabled="saving">{{ saving ? '保存中…' : '保存' }}</button>
        </div>
      </div>
    </div>

    <!-- Audit Modal -->
    <div v-if="showAuditModal" class="modal-overlay" @click.self="showAuditModal = false">
      <div class="modal-card">
        <div class="modal-header"><h3>门店审核</h3><button class="modal-close" @click="showAuditModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label>门店名称</label><p>{{ auditForm.storeName }}</p></div>
          <div class="form-group"><label>描述</label><p>{{ auditForm.storeDesc }}</p></div>
          <div class="form-group"><label>电话</label><p>{{ auditForm.storePhone }}</p></div>
          <div class="form-group"><label>地址</label><p>{{ auditForm.province }}{{ auditForm.city }}{{ auditForm.district }}{{ auditForm.address }}</p></div>
          <div class="form-group"><label>审核状态</label>
            <select v-model.number="auditForm.status"><option :value="1">通过（营业中）</option><option :value="3">驳回</option></select>
          </div>
          <div class="form-group"><label>{{ auditForm.status === 3 ? '驳回原因（必填）' : '审核意见（选填）' }}</label><textarea v-model.trim="auditForm.auditRemark" rows="3"></textarea></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showAuditModal = false">取消</button>
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
import { approveStore, rejectStore } from "../../api/auditorStore"
import http from "../../api/index"
import { unwrap } from "../../api/helper"
import type { Store } from "../../types/store"
import { notify } from "../../utils/notify"

const store = useStoreStore()
const keyword = ref("")
const statusFilter = ref(-1)
const currentPage = ref(1)
const pageSize = 10

const showEditModal = ref(false)
const showAuditModal = ref(false)
const isCreate = ref(false)
const saving = ref(false)

const defaultForm = () => ({
  storeName: "", userId: undefined, storeLogo: "", storePhone: "", storeDesc: "",
  province: "", city: "", district: "", address: "",
  longitude: undefined, latitude: undefined, status: 0
})
const editForm = ref<any>(defaultForm())
const auditForm = ref<any>({})

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function statusBadge(s: number) { return s === 1 ? "badge-green" : s === 0 ? "badge-orange" : "badge-gray" }
function statusLabel(s: number) { return ["待审核","营业中","已关闭","审核驳回"][s] || "未知" }

async function fetchData() { await store.fetch({ current: currentPage.value, size: pageSize, keyword: keyword.value || undefined, status: statusFilter.value >= 0 ? statusFilter.value : undefined }) }
function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; statusFilter.value = -1; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openCreate() {
  isCreate.value = true
  editForm.value = defaultForm()
  showEditModal.value = true
}

function openEdit(item: Store) {
  isCreate.value = false
  editForm.value = { ...item }
  showEditModal.value = true
}

function openAudit(item: Store) {
  auditForm.value = { ...item, status: 1, auditRemark: "" }
  showAuditModal.value = true
}

async function saveEdit() {
  if (!editForm.value.storeName) return
  saving.value = true
  try {
    const data: any = {
      storeName: editForm.value.storeName,
      storeLogo: editForm.value.storeLogo,
      storePhone: editForm.value.storePhone,
      storeDesc: editForm.value.storeDesc,
      province: editForm.value.province,
      city: editForm.value.city,
      district: editForm.value.district,
      address: editForm.value.address,
      longitude: editForm.value.longitude || undefined,
      latitude: editForm.value.latitude || undefined
    }
    if (isCreate.value) {
      data.userId = editForm.value.userId
      await unwrap(http.post("/store", data))
    } else {
      await updateStore(editForm.value.id, data)
    }
    showEditModal.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function saveAudit() {
  if (auditForm.value.id) {
    const remark = auditForm.value.auditRemark?.trim() || undefined
    if (auditForm.value.status === 3 && !remark) {
      notify("驳回原因不能为空", "error")
      return
    }
    if (auditForm.value.status === 1) await approveStore(auditForm.value.id, remark)
    else await rejectStore(auditForm.value.id, remark)
    showAuditModal.value = false
    notify(auditForm.value.status === 1 ? "审核通过" : "审核驳回")
    fetchData()
  }
}

onMounted(fetchData)
</script>
