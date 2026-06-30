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
          <span class="nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-profile">
          <div class="admin-avatar">{{ admin.info?.username?.charAt(0) || "A" }}</div>
          <div class="admin-meta">
            <span class="admin-name">{{ admin.info?.username || "管理员" }}</span>
            <span class="admin-role">{{ admin.roleLabel }}</span>
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
import { computed, onMounted, onUnmounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useAdminStore } from "../stores/admin"
import { ROLE_MENUS } from "../config/management"

const router = useRouter()
const route = useRoute()
const admin = useAdminStore()

const menuItems = computed(() => admin.role ? ROLE_MENUS[admin.role] : [])

const pageTitle = computed(() => {
  return typeof route.meta.title === "string" ? route.meta.title : "管理后台"
})

function handleSessionCleared() {
  admin.logout()
  if (route.path !== "/login") router.replace("/login")
}

onMounted(() => window.addEventListener("management-session-cleared", handleSessionCleared))
onUnmounted(() => window.removeEventListener("management-session-cleared", handleSessionCleared))

function handleLogout() {
  admin.logout()
  router.replace("/login")
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
