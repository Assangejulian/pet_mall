<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <span class="logo-mark">
          <svg width="20" height="20" viewBox="0 0 32 32" fill="none">
            <path d="M16 4C10 4 6 8.5 6 14c0 5 4 10 10 14 6-4 10-9 10-14 0-5.5-4-10-10-10z" fill="currentColor" opacity=".3"/>
            <circle cx="12" cy="13" r="1.5" fill="currentColor"/>
            <circle cx="20" cy="13" r="1.5" fill="currentColor"/>
          </svg>
        </span>
        <span class="logo-text">Pet<span>Nest</span></span>
      </div>

      <nav class="sidebar-nav">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path"
          class="nav-item" :class="{ active: route.path === item.path }">
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-profile">
          <div class="admin-avatar">{{ admin.info?.username?.charAt(0) || '管' }}</div>
          <div class="admin-meta">
            <span class="admin-name">{{ admin.info?.username || '管理员' }}</span>
            <span class="admin-role">超级管理员</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </div>
    </aside>

    <div class="main-area">
      <header class="top-header">
        <h2>{{ pageTitle }}</h2>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useAdminStore } from "../stores/admin"
import http from "../api/index"
import { unwrap } from "../api/helper"

const router = useRouter()
const route = useRoute()
const admin = useAdminStore()

const menuItems = [
  { path: "/dashboard", label: "概览", icon: "◆" },
  { path: "/user", label: "用户管理", icon: "★" },
  { path: "/store", label: "门店管理", icon: "●" },
  { path: "/product", label: "商品管理", icon: "♥" },
  { path: "/order", label: "订单管理", icon: "☆" },
  { path: "/video", label: "视频管理", icon: "▶" },
]

const pageTitle = computed(() => {
  const m = menuItems.find(i => i.path === route.path)
  return m ? m.label : "管理后台"
})

interface AdminInfo { userId: string; username: string; avatar: string; role: string }

onMounted(async () => {
  // 页面刷新后 token 还在但 info 为空，从后端恢复
  if (admin.token && !admin.info) {
    try {
      const data = await unwrap<AdminInfo>(http.get("/info"))
      admin.setInfo({ id: data.userId, username: data.username, avatar: data.avatar || "", role: data.role })
    } catch {
      admin.logout()
      router.push("/login")
    }
  }
})

function handleLogout() {
  admin.logout()
  router.push("/login")
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }

/* Sidebar */
.sidebar { width: 240px; background: var(--sidebar-bg); display: flex; flex-direction: column; flex-shrink: 0; }
.sidebar-header { padding: 20px 24px; display: flex; align-items: center; gap: 10px; }
.logo-mark { width: 32px; height: 32px; display: grid; place-items: center; border-radius: 10px; background: linear-gradient(135deg, #f5c8b8, var(--primary)); color: #fff; }
.logo-text { font-family: "Noto Serif SC", serif; font-size: 16px; font-weight: 700; color: #fff; letter-spacing: 2px; }
.logo-text span { color: var(--primary); }

.sidebar-nav { flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 2px; }
.nav-item { display: flex; align-items: center; gap: 10px; padding: 10px 14px; border-radius: 8px; color: var(--sidebar-text); font-size: 14px; font-weight: 600; text-decoration: none; transition: all .15s; }
.nav-item:hover { background: rgba(255,255,255,.06); color: #fff; }
.nav-item.active { background: rgba(232,146,124,.15); color: var(--sidebar-active); }
.nav-icon { font-size: 16px; width: 20px; text-align: center; }

.sidebar-footer { padding: 16px 12px; border-top: 1px solid rgba(255,255,255,.06); }
.admin-profile { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.admin-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, var(--primary-light), var(--primary)); color: #fff; display: grid; place-items: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.admin-meta { display: flex; flex-direction: column; min-width: 0; }
.admin-name { font-size: 13px; font-weight: 700; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.admin-role { font-size: 11px; color: var(--sidebar-text); }

.logout-btn { width: 100%; padding: 10px; border: 1px solid rgba(255,255,255,.1); border-radius: 8px; background: transparent; color: var(--sidebar-text); font-size: 13px; cursor: pointer; transition: all .15s; }
.logout-btn:hover { background: rgba(232,146,124,.1); color: var(--sidebar-active); border-color: rgba(232,146,124,.3); }

/* Main */
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.top-header { height: 60px; background: var(--header-bg); border-bottom: 1px solid var(--border); display: flex; align-items: center; padding: 0 28px; flex-shrink: 0; }
.top-header h2 { font-size: 17px; font-weight: 700; }
.content { flex: 1; overflow-y: auto; }
</style>