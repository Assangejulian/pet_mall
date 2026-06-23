<template>
  <div class="admin-page">
    <div class="page-header"><h2>视频管理</h2><p>管理平台视频内容</p></div>
    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>标题</th><th>封面</th><th>时长</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.title }}</td>
            <td><img :src="item.cover" class="img-preview" /></td>
            <td>{{ formatDuration(item.duration) }}</td>
            <td><span class="badge" :class="item.status === 1 ? 'badge-green' : 'badge-gray'">{{ item.status === 1 ? '已发布' : '审核中' }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-danger btn-sm" @click="handleDelete(item)">删除</button>
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
import { useVideoStore } from "../../stores/video"
import { deleteVideo } from "../../api/video"
import type { Video } from "../../types/video"

const store = useVideoStore()
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function formatDuration(s: number) { const m = Math.floor(s / 60); const sec = s % 60; return m + ":" + String(sec).padStart(2, "0") }
async function fetchData() { await store.fetch({ page: currentPage.value, size: pageSize }) }
function goPage(p: number) { currentPage.value = p; fetchData() }
async function handleDelete(item: Video) { if (confirm("确定删除视频「" + item.title + "」？")) { await deleteVideo(item.id); fetchData() } }

onMounted(fetchData)
</script>
