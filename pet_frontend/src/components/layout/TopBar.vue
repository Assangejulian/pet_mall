<script setup lang="ts">
import "../../styles/TopBar.css"
import { useCartStore } from "../../stores/cart"
import { useUserStore } from "../../stores/user"

const cart = useCartStore()
const user = useUserStore()

const ADMIN_URL = "http://localhost:5178"

const pages = [
  { id: "home", path: "/", label: "\u4f5c\u54c1" },
  { id: "market", path: "/market", label: "\u5e02\u96c6" },
  { id: "community", path: "/community", label: "\u793e\u533a" },
  { id: "notes", path: "/notes", label: "\u7b14\u8bb0" },
  { id: "ai", path: "/ai", label: "AI\u52a9\u624b" },
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

    <nav class="tabs" aria-label="\u9875\u9762\u5bfc\u822a">
      <router-link v-for="p in pages" :key="p.id" :to="p.path" :class="{ active: $route.name === p.id }">
        {{ p.label }}
      </router-link>
      <a :href="ADMIN_URL" class="nav-admin" target="_blank" rel="noopener">\u7ba1\u7406\u540e\u53f0</a>
    </nav>

    <div class="nav-actions">
      <button class="cart-button" type="button" aria-label="\u8d2d\u7269\u8f66">
        <span class="cart-glyph"></span>
        <span class="cart-badge">{{ cart.count }}</span>
      </button>

      <template v-if="user.isLoggedIn">
        <div class="user-menu">
          <button class="user-trigger" type="button">
            <span class="user-avatar">\u4eba</span>
            <span>{{ user.profile?.username }}</span>
            <small>&#9660;</small>
          </button>
          <div class="user-dropdown">
            <button type="button">\u6211\u7684\u4e3b\u9875</button>
            <button type="button">\u6211\u7684\u6bdb\u5b69\u5b50</button>
            <button type="button">\u6211\u7684\u8ba2\u5355</button>
            <button type="button">\u6211\u7684\u6536\u85cf</button>
            <hr />
            <button type="button">\u8d26\u53f7\u8bbe\u7f6e</button>
            <button type="button" @click="doLogout">\u9000\u51fa\u767b\u5f55</button>
          </div>
        </div>
      </template>
      <template v-else>
        <router-link to="/login" class="btn-login">\u767b\u5f55</router-link>
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
  padding: 8px 14px;
  color: rgba(255,255,255,.7);
  text-decoration: none;
  font-size: 13px;
  border: 1px solid rgba(255,255,255,.15);
  border-radius: 6px;
  transition: all .2s;
}
.nav-admin:hover {
  background: rgba(232,146,124,.2);
  color: #e8927c;
  border-color: rgba(232,146,124,.4);
}
</style>
