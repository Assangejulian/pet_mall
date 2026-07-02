<script setup lang="ts">
import "../../styles/TopBar.css"
import { useCartStore } from "../../stores/cart"
import { useUserStore } from "../../stores/user"

const cart = useCartStore()
const user = useUserStore()

const ADMIN_URL = "http://localhost:5178"

const pages = [
  { id: "home", path: "/", label: "作品" },
  { id: "market", path: "/market", label: "市集" },
  { id: "community", path: "/community", label: "社区" },
  { id: "notes", path: "/notes", label: "笔记" },
  { id: "ai", path: "/ai", label: "AI助手" },
]

function doLogout() {
  user.logout()
}
</script>

<template>
  <header class="topbar">
    <router-link to="/" class="brand">
      <span class="brand-mark">&#9829;</span>
      <span>Pet<span>Nest</span></span>
    </router-link>

    <nav class="tabs" aria-label="页面导航">
      <router-link v-for="p in pages" :key="p.id" :to="p.path" :class="{ active: $route.name === p.id }">
        {{ p.label }}
      </router-link>
    </nav>

    <div class="nav-actions">
      <a :href="ADMIN_URL" class="nav-admin" target="_blank" rel="noopener">管理后台</a>
      <button class="cart-button" type="button" aria-label="购物车">
        <span class="cart-glyph"></span>
        <span class="cart-badge">{{ cart.count }}</span>
      </button>

      <template v-if="user.isLoggedIn">
        <div class="user-menu">
          <button class="user-trigger" type="button">
            <span class="user-avatar">人</span>
            <span>{{ user.profile?.username }}</span>
            <small>&#9660;</small>
          </button>
          <div class="user-dropdown">
            <button type="button">我的主页</button>
            <button type="button">我的毛孩子</button>
            <button type="button">我的订单</button>
            <button type="button">我的收藏</button>
            <hr />
            <button type="button">账号设置</button>
            <button type="button" @click="doLogout">退出登录</button>
          </div>
        </div>
      </template>
      <template v-else>
        <router-link to="/login" class="btn-login">登录</router-link>
      </template>
    </div>
  </header>
</template>

<style scoped>
.btn-login {
  padding: 8px 20px;
  background: #e8927c;
  color: #fff;
  border-radius: 20px;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s;
}
.btn-login:hover {
  background: #d47a64;
}
.nav-admin {
  padding: 6px 12px;
  color: #8f7366;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid rgba(140,104,83,.2);
  border-radius: 6px;
  transition: all .2s;
}
.nav-admin:hover {
  background: rgba(232,146,124,.15);
  color: #e8927c;
  border-color: rgba(232,146,124,.4);
}
</style>