<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <svg width="24" height="24" viewBox="0 0 32 32" fill="none">
          <path d="M16 4C10 4 6 8.5 6 14c0 5 4 10 10 14 6-4 10-9 10-14 0-5.5-4-10-10-10z" fill="currentColor" opacity=".35"/>
          <circle cx="12" cy="13" r="1.8" fill="currentColor"/>
          <circle cx="20" cy="13" r="1.8" fill="currentColor"/>
        </svg>
        <span class="logo-text">PetNest</span>
      </div>

      <nav class="sidebar-nav">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path"
          class="nav-item" :class="{ active: route.path === item.path }">
          <span class="nav-icon" v-html="item.icon"></span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-profile">
          <div class="admin-avatar">{{ admin.info?.username?.charAt(0) || "A" }}</div>
          <div class="admin-meta">
            <span class="admin-name">{{ admin.info?.username || "管理员" }}</span>
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
import { ref, computed, onMounted, onUnmounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useAdminStore } from "../stores/admin"
import http from "../api/index"
import { unwrap } from "../api/helper"

const router = useRouter()
const route = useRoute()
const admin = useAdminStore()

const menuItems = [
  { path: "/dashboard", label: "概览", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' },
  { path: "/user", label: "用户管理", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>' },
  { path: "/store", label: "门店管理", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>' },
  { path: "/product", label: "商品管理", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><polyline points="3.54 7.62 12 12.29 20.46 7.62"/><line x1="12" y1="22.04" x2="12" y2="12.29"/></svg>' },
  { path: "/order", label: "订单管理", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>' },
  { path: "/video", label: "视频管理", icon: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>' },
]

const pageTitle = computed(() => {
  const m = menuItems.find(i => i.path === route.path)
  return m ? m.label : "管理后台"
})

interface AdminInfo { userId: string; username: string; avatar: string; role: string }

onMounted(async () => {
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
.admin-layout {
  display: flex;
  height: 100vh;
  background: var(--bg);
}

/* Sidebar */
.sidebar {
  width: 240px;
  background: var(--sidebar-bg, #2d2a24);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-size: 17px;
  font-weight: 700;
}
.sidebar-header svg {
  width: 26px;
  height: 26px;
  color: var(--primary);
}
.logo-text {
  letter-spacing: 1px;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  color: var(--sidebar-text, rgba(255,255,255,.55));
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all .15s;
}
.nav-item:hover {
  background: rgba(255,255,255,.06);
  color: #fff;
}
.nav-item.active {
  background: rgba(232,146,124,.15);
  color: var(--primary);
}
.nav-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-icon :deep(svg) {
  display: block;
}

/* Footer */
.sidebar-footer {
  padding: 16px 16px;
  border-top: 1px solid rgba(255,255,255,.06);
}
.admin-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.admin-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-light, #f5c8b8), var(--primary));
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
}
.admin-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.admin-name {
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.admin-role {
  font-size: 11px;
  color: var(--sidebar-text, rgba(255,255,255,.55));
}
.logout-btn {
  width: 100%;
  padding: 9px;
  border: 1px solid rgba(255,255,255,.1);
  border-radius: 8px;
  background: transparent;
  color: var(--sidebar-text, rgba(255,255,255,.55));
  font-size: 13px;
  cursor: pointer;
  transition: all .15s;
}
.logout-btn:hover {
  background: rgba(232,146,124,.1);
  color: var(--primary);
  border-color: rgba(232,146,124,.3);
}

/* Main area */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.top-header {
  height: 56px;
  background: var(--header-bg, #faf8f5);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 28px;
  flex-shrink: 0;
}
.top-header h2 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text1);
}
.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
}
</style>
