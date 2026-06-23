<template>
  <TopBar />
  <main>
    <router-view />
  </main>
</template>

<script setup lang="ts">
import TopBar from "./components/layout/TopBar.vue"
</script>