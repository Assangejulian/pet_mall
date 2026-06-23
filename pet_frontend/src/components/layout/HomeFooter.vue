<script setup lang="ts">
import "../../styles/global.css"
</script>

<template>
  <footer class="home-footer">
    <div class="footer-brand">
      <h3><span>♥</span> Pet<em>Nest</em></h3>
      <p>让每一次心动，都能慢慢变成陪伴。</p>
    </div>
    <div class="footer-dot"></div>
    <nav>
      <strong>浏览</strong>
      <router-link to="/">首页</router-link>
      <router-link to="/market">去见它</router-link>
      <router-link to="/community">社区</router-link>
      <router-link to="/notes">百科</router-link>
    </nav>
    <nav>
      <strong>帮助</strong>
      <button type="button">陪伴指南</button>
      <button type="button">健康照护</button>
      <button type="button">常见问题</button>
      <button type="button">联系我们</button>
    </nav>
    <nav>
      <strong>关于</strong>
      <button type="button">我们的初心</button>
      <button type="button">爱心合作</button>
      <button type="button">加入我们</button>
      <button type="button">隐私政策</button>
    </nav>
  </footer>
</template>
