<template>
  <div class="admin-page">
    <div class="page-header"><h2>商品管理</h2><p>管理平台商品信息</p></div>
    <div class="search-bar">
      <input v-model="keyword" placeholder="搜索商品名称" @keyup.enter="handleSearch" />
      <select v-model="statusFilter">
        <option value="">全部状态</option><option value="1">上架</option><option value="0">下架</option><option value="2">已售出</option>
      </select>
      <button class="btn btn-primary" @click="handleSearch">搜索</button>
      <button class="btn btn-outline" @click="resetSearch">重置</button>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead><tr><th>ID</th><th>商品</th><th>类型</th><th>价格</th><th>库存</th><th>状态</th><th>创建时间</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in store.list" :key="item.id">
            <td>{{ item.id }}</td>
            <td><div style="display:flex;align-items:center;gap:8px"><img :src="item.image" class="img-preview" />{{ item.name }}</div></td>
            <td>{{ item.type }}</td>
            <td>¥{{ item.price }}</td>
            <td>{{ item.stock }}</td>
            <td><span class="badge" :class="item.status === '上架' ? 'badge-green' : 'badge-gray'">{{ item.status }}</span></td>
            <td>{{ item.createTime }}</td>
            <td class="actions">
              <button class="btn btn-sm" :class="item.status === '上架' ? 'btn-outline' : 'btn-success'" @click="toggleStatus(item)">{{ item.status === '上架' ? '下架' : '上架' }}</button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(item)">删除</button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useProductStore } from "../../stores/product"
import { onlineProduct, offlineProduct, deleteProduct } from "../../api/product"
import type { Product } from "../../types/product"

const store = useProductStore()
const keyword = ref("")
const statusFilter = ref("")
const currentPage = ref(1)
const pageSize = 10

const totalPages = computed(() => Math.ceil(store.total / pageSize))

async function fetchData() { await store.fetch({ current: currentPage.value, size: pageSize, keyword: keyword.value || undefined, status: statusFilter.value || undefined }) }
function handleSearch() { currentPage.value = 1; fetchData() }
function resetSearch() { keyword.value = ""; statusFilter.value = ""; currentPage.value = 1; fetchData() }
function goPage(p: number) { currentPage.value = p; fetchData() }
async function toggleStatus(item: Product) { item.status === "上架" ? await offlineProduct(item.id) : await onlineProduct(item.id); fetchData() }
async function handleDelete(item: Product) { if (confirm("确定删除商品「" + item.name + "」？")) { await deleteProduct(item.id); fetchData() } }

onMounted(fetchData)
</script>
