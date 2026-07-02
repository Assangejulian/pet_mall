<template>
  <div class="admin-page">
    <div class="page-header"><h2>视频管理</h2><p>管理平台视频内容</p></div>

    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索视频标题" @keyup.enter="handleSearch" />
      <select v-model.number="statusFilter">
        <option :value="-1">全部状态</option><option :value="1">已发布</option><option :value="0">待审核</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>标题</th><th>封面</th><th>播放量</th><th>点赞</th><th>时长</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td>
            <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ item.title }}</td>
            <td><img :src="item.cover" class="img-preview" @error="handleImgError" /></td>
            <td>{{ item.playCount ?? 0 }}</td>
            <td>{{ item.likes ?? 0 }}</td>
            <td>{{ formatDuration(item.duration) }}</td>
            <td><span class="badge" :class="item.status === 1 ? 'badge-green' : 'badge-gray'">{{ item.status === 1 ? '已发布' : '待审核' }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item)">编辑</button>
              <button class="btn btn-sm" :class="item.status === 1 ? 'btn-warning' : 'btn-success'" @click="toggleStatus(item)">{{ item.status === 1 ? '下架' : '发布' }}</button>
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

    <!-- Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <div class="modal-header"><h3>编辑视频</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label>视频标题</label><input v-model="editForm.title" /></div>
          <div class="form-group"><label>视频描述</label><textarea v-model="editForm.description" rows="3"></textarea></div>
          <div class="form-group"><label>封面链接</label><input v-model="editForm.cover" /></div>
          <div class="form-row">
            <div class="form-group"><label>状态</label>
              <select v-model.number="editForm.status"><option :value="1">已发布</option><option :value="0">下架</option></select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="saveEdit">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from "vue"
import { useVideoStore } from "../../stores/video"
import { deleteVideo, updateVideo, onlineVideo, offlineVideo } from "../../api/video"
import type { Video } from "../../types/video"

const store = useVideoStore()
const keyword = ref("")
const statusFilter = ref(-1)
const currentPage = ref(1)
const pageSize = 10
const showModal = ref(false)
const editForm = reactive<Record<string, any>>({})

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function formatDuration(s: number) {
  if (!s) return "0:00"
  const m = Math.floor(s / 60)
  const sec = s % 60
  return m + ":" + String(sec).padStart(2, "0")
}

async function fetchData() {
  const params = { page: currentPage.value, size: pageSize, keyword: keyword.value || undefined, status: statusFilter.value >= 0 ? statusFilter.value : undefined }
  if (keyword.value) params.keyword = keyword.value
  if (statusFilter.value >= 0) params.status = statusFilter.value
  await store.fetch(params)
}

function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; statusFilter.value = -1; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openEdit(item: Video) {
  Object.assign(editForm, { id: item.id, title: item.title, description: item.description || "", cover: item.cover || "", status: item.status })
  showModal.value = true
}

async function saveEdit() {
  try {
    await updateVideo(editForm.id, { title: editForm.title, description: editForm.description, cover: editForm.cover, status: editForm.status })
    showModal.value = false
    fetchData()
    dispatchToast('保存成功', 'success')
  } catch (e: any) {
    dispatchToast(e?.message || '保存失败', 'error')
  }
}

async function toggleStatus(item: Video) {
  try {
    if (item.status === 1) {
      await offlineVideo(item.id)
    } else {
      await onlineVideo(item.id)
    }
    fetchData()
    dispatchToast(item.status === 1 ? '下架成功' : '发布成功', 'success')
  } catch (e: any) {
    dispatchToast(e?.message || '操作失败', 'error')
  }
}

async function handleDelete(item: Video) {
  if (!confirm('确定删除视频「' + item.title + '」？')) return
  try {
    await deleteVideo(item.id)
    fetchData()
    dispatchToast('删除成功', 'success')
  } catch (e: any) {
    dispatchToast(e?.message || '删除失败', 'error')
  }
}

function handleImgError(e: Event) {
  const target = e.target as HTMLImageElement | null
  if (target) target.style.display = 'none'
}

function dispatchToast(msg: string, type: string) {
  const event = new CustomEvent('toast', { detail: { message: msg, type } })
  document.dispatchEvent(event)
}

onMounted(fetchData)
</script>

<style scoped>
.form-row { display: flex; gap: 16px; }
.form-row .form-group { flex: 1; }
</style>


