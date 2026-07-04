<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>我的视频</h2>
      <p>管理自己发布的视频内容</p>
    </div>

    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索视频标题" @keyup.enter="search" />
      <select v-model.number="statusFilter">
        <option :value="-1">全部状态</option>
        <option :value="1">已上架</option>
        <option :value="0">未上架</option>
      </select>
      <button class="btn btn-primary" @click="search">搜索</button>
      <button class="btn btn-outline" @click="reset">重置</button>
      <button class="btn btn-primary push-right" @click="openCreate">+ 新增视频</button>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>标题</th>
            <th>封面</th>
            <th>播放量</th>
            <th>点赞</th>
            <th>评论</th>
            <th>关联商品</th>
            <th>时长</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id">
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.title }}</td>
            <td><img v-if="item.cover" :src="item.cover" style="width:60px;height:40px;object-fit:cover;border-radius:4px" /></td>
            <td>{{ item.playCount ?? 0 }}</td>
            <td>{{ item.likes ?? 0 }}</td>
            <td>{{ item.commentCount ?? 0 }}</td>
            <td>{{ productMap[item.productId || ""] || "-" }}</td>
            <td>{{ formatDuration(item.duration) }}</td>
            <td>
              <span class="badge" :class="item.status === 1 ? 'badge-success' : 'badge-warning'">
                {{ item.status === 1 ? '已上架' : '未上架' }}
              </span>
            </td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item.id)">编辑</button>
              <button class="btn btn-sm" :class="item.status === 1 ? 'btn-warning' : 'btn-success'"
                @click="toggle(item)">{{ item.status === 1 ? '下架' : '上架' }}</button>
              <button class="btn btn-danger btn-sm" @click="remove(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!loading && !records.length">
            <td colspan="10" class="empty-row">暂无视频</td>
          </tr>
          <tr v-if="loading">
            <td colspan="10" class="empty-row">加载中...</td>
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
          <h3>{{ editingId ? '编辑视频' : '新增视频' }}</h3>
          <button class="modal-close" @click="showModal = false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>视频标题 *</label>
            <input v-model="form.title" />
          </div>
          <div class="form-group">
            <label>视频描述</label>
            <textarea v-model="form.description" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>视频URL *</label>
            <div class="input-with-btn">
              <input v-model="form.url" placeholder="上传或输入视频URL" />
              <button type="button" class="btn btn-outline btn-sm" @click="triggerVideoUpload">{{ uploading ? "上传中..." : "上传视频" }}</button>
            </div>
            <input type="file" ref="uploadVideoRef" accept="video/mp4,video/webm" style="display:none" @change="handleVideoUpload" />
          </div>
          <div class="form-group">
            <label>封面图片URL</label>
            <div class="input-with-btn">
              <input v-model="form.cover" placeholder="上传或输入封面URL" />
              <button type="button" class="btn btn-outline btn-sm" @click="triggerCoverUpload">{{ uploading ? "上传中..." : "上传封面" }}</button>
            </div>
            <input type="file" ref="uploadCoverRef" accept="image/*" style="display:none" @change="handleCoverUpload" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>关联商品</label>
              <select v-model="form.productId">
                <option value="">不关联商品</option>
                <option v-for="p in products" :key="p.id" :value="p.id">{{ p.productName || p.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>时长(秒)</label>
              <input v-model.number="form.duration" type="number" min="0" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { listMerchantVideos, getMerchantVideo, createMerchantVideo, updateMerchantVideo, deleteMerchantVideo, onlineMerchantVideo, offlineMerchantVideo } from "../../api/merchantVideo"
import type { Video } from "../../types/video"
import type { Product } from "../../types/product"
import { listMerchantProducts } from "../../api/merchantProduct"
import { publicHttp } from "../../api/index"
import { notify } from "../../utils/notify"

const uploadVideoRef = ref<HTMLInputElement>()
const uploadCoverRef = ref<HTMLInputElement>()
const uploading = ref(false)

const page = ref(1)
const size = 10
const keyword = ref("")
const statusFilter = ref(-1)
const records = ref<Video[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const showModal = ref(false)
const editingId = ref("")

const totalPages = computed(() => Math.ceil(total.value / size))
const products = ref<Product[]>([])
const productMap = computed(() => {
  const m: Record<string, string> = {}
  for (const p of products.value) {
    m[p.id] = p.productName || p.name || ""
  }
  return m
})

const form = ref({ title: "", description: "", url: "", cover: "", productId: "", duration: 0 })

function emptyForm() {
  return { title: "", description: "", url: "", cover: "", productId: "", duration: 0 }
}

function formatDuration(seconds: number) {
  if (!seconds) return "-"
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m + ":" + s.toString().padStart(2, "0")
}

async function fetchData() {
  loading.value = true
  try {
    const r = await listMerchantVideos({
      page: page.value, size,
      keyword: keyword.value.trim() || undefined,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined
    })
    records.value = r.records
    total.value = r.total
  } finally {
    loading.value = false
  }
}

function search() { page.value = 1; fetchData() }
function reset() { keyword.value = ""; statusFilter.value = -1; search() }
function go(n: number) { page.value = n; fetchData() }

function openCreate() {
  editingId.value = ""
  form.value = emptyForm()
  showModal.value = true
}

async function openEdit(id: string) {
  const item = await getMerchantVideo(id)
  editingId.value = id
  form.value = {
    title: item.title || "",
    description: item.description || "",
    url: item.url || "",
    cover: item.cover || "",
    productId: item.productId || "",
    duration: item.duration || 0,
  }
  showModal.value = true
}

async function save() {
  if (!form.value.title || !form.value.url) {
    notify("请填写标题和视频URL", "error")
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateMerchantVideo(editingId.value, form.value)
    } else {
      await createMerchantVideo(form.value)
    }
    showModal.value = false
    notify("保存成功")
    await fetchData()
  } finally {
    saving.value = false
  }
}

async function toggle(item: Video) {
  if (item.status === 1) {
    await offlineMerchantVideo(item.id)
    notify("已下架")
  } else {
    await onlineMerchantVideo(item.id)
    notify("已上架")
  }
  await fetchData()
}

async function remove(item: Video) {
  if (!confirm("确定删除视频「" + item.title + "」？")) return
  await deleteMerchantVideo(item.id)
  notify("删除成功")
  await fetchData()
}

async function loadProducts() {
  try {
    const r = await listMerchantProducts({ page: 1, size: 100 })
    products.value = r.records || []
  } catch {}
}

function triggerVideoUpload() { uploadVideoRef.value?.click() }
function triggerCoverUpload() { uploadCoverRef.value?.click() }

async function uploadFile(file: File): Promise<string> {
  const formData = new FormData()
  formData.append("file", file)
  const res = await publicHttp.post("/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  })
  return res.data || res
}

async function handleVideoUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    form.value.url = await uploadFile(file)
    notify("视频上传成功")
  } catch {
    notify("视频上传失败", "error")
  } finally {
    uploading.value = false
    input.value = ""
  }
}

async function handleCoverUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    form.value.cover = await uploadFile(file)
    notify("封面上传成功")
  } catch {
    notify("封面上传失败", "error")
  } finally {
    uploading.value = false
    input.value = ""
  }
}

onMounted(async () => {
  await loadProducts()
  await fetchData()
})
</script>

<style scoped>
.push-right { margin-left: auto; }
.modal-card { width: min(560px, 100%); }
.form-row .form-group { flex: 1; }
.input-with-btn { display: flex; gap: 8px; }
.input-with-btn input { flex: 1; }
</style>
