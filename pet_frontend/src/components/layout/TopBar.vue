<script setup lang="ts">
import "../../styles/TopBar.css"
import { useCartStore } from "../../stores/cart"
import { useUserStore } from "../../stores/user"
const cart = useCartStore()
const user = useUserStore()

const pages = [
  { id: "home", path: "/", label: "作品" },
  { id: "market", path: "/market", label: "市集" },
  { id: "community", path: "/community", label: "社区" },
  { id: "notes", path: "/notes", label: "笔记" },
  { id: "ai", path: "/ai", label: "AI助手" },
]
</script>

<template>
  <header class="topbar">
    <router-link to="/" class="brand">
      <span class="brand-mark">♡</span>
      <span>Pet<span>Nest</span></span>
    </router-link>

    <nav class="tabs" aria-label="页面导航">
      <router-link v-for="p in pages" :key="p.id" :to="p.path" :class="{ active: $route.name === p.id }">
        {{ p.label }}
      </router-link>
    </nav>

    <div class="nav-actions">
      <button class="cart-button" type="button" aria-label="购物车">
        <span class="cart-glyph"></span>
        <span class="cart-badge">{{ cart.count }}</span>
      </button>
      <div class="user-menu">
        <button class="user-trigger" type="button">
          <span class="user-avatar">人</span>
          <span>{{ user.profile.name }}</span>
          <small>▾</small>
        </button>
        <div class="user-dropdown">
          <button type="button">我的主页</button>
          <button type="button">我的毛孩子</button>
          <button type="button">我的订单</button>
          <button type="button">我的收藏</button>
          <hr />
          <button type="button">账号设置</button>
        </div>
      </div>
    </div>
  </header>
</template>
