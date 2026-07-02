<template>
  <div class="admin-page">
    <div class="page-header"><h2>用户管理</h2><p>管理系统注册用户</p></div>
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索用户名/手机号" @keyup.enter="handleSearch" />
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>用户名</th><th>手机号</th><th>邮箱</th><th>会员等级</th><th>状态</th><th>注册时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td><td><div style="display:flex;align-items:center;gap:8px"><img :src="item.avatar" class="avatar" />{{ item.username }}</div></td>
            <td>{{ item.phone }}</td><td>{{ item.email }}</td>
            <td><span class="badge" :class="badgeClass(item.memberLevel)">{{ memberLabel(item.memberLevel) }}</span></td>
            <td><span class="badge" :class="item.status === 1 ? 'badge-green' : 'badge-gray'">{{ item.status === 1 ? '正常' : '禁用' }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-outline btn-sm" @click="openEdit(item)">编辑</button>
              <button class="btn btn-sm" :class="item.status === 1 ? 'btn-danger' : 'btn-success'" @click="toggleStatus(item)">{{ item.status === 1 ? '禁用' : '启用' }}</button>
            </td>
          </tr>
          <tr v-if="!store.list.length"><td colspan="8" style="text-align:center;padding:32px;color:var(--text2)">暂无数据</td></tr>
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
        <div class="modal-header"><h3>编辑用户</h3><button class="modal-close" @click="showModal = false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label>用户名</label><input v-model="editForm.username" /></div>
          <div class="form-group"><label>手机号</label><input v-model="editForm.phone" /></div>
          <div class="form-group"><label>邮箱</label><input v-model="editForm.email" /></div>
          <div class="form-group"><label>会员等级</label>
            <select v-model.number="editForm.memberLevel"><option :value="0">普通</option><option :value="1">白银</option><option :value="2">黄金</option><option :value="3">钻石</option></select>
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
import { ref, onMounted, computed } from "vue"
import { useUserStore } from "../../stores/user"
import { updateUser } from "../../api/user"
import type { User } from "../../types/user"

const store = useUserStore()
const keyword = ref("")
const currentPage = ref(1)
const pageSize = 10
const showModal = ref(false)
const editForm = ref<Partial<User>>({})

const totalPages = computed(() => Math.ceil(store.total / pageSize))

function badgeClass(level: number) { return ["badge-gray","badge-blue","badge-orange","badge-green"][level] || "badge-gray" }
function memberLabel(level: number) { return ["普通","白银","黄金","钻石"][level] || "普通" }

async function fetchData() { await store.fetch({ page: currentPage.value, size: pageSize, keyword: keyword.value || undefined }) }
function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }

function openEdit(item: User) { editForm.value = { ...item }; showModal.value = true }
async function saveEdit() { if (editForm.value.id) { await updateUser(editForm.value.id, editForm.value); showModal.value = false; fetchData() } }
async function toggleStatus(item: User) { await updateUser(item.id, { status: item.status === 1 ? 0 : 1 }); fetchData() }

onMounted(fetchData)
</script>

