<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <p v-if="desc">{{ desc }}</p>
    </div>
    <slot />
  </div>
</template>
<script setup lang="ts">
defineProps<{ title: string; desc?: string }>()
</script>
