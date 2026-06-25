<template>
  <TransitionGroup name="toast" tag="div" class="toast-container">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="'toast-' + t.type">
      <svg v-if="t.type === 'success'" width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/>
        <path d="M5 8l2 2 4-4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <svg v-else-if="t.type === 'error'" width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/>
        <path d="M5.5 5.5l5 5M10.5 5.5l-5 5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
      </svg>
      <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.2"/>
        <path d="M5 8l2 2 4-4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
      <span>{{ t.message }}</span>
    </div>
  </TransitionGroup>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue"

type ToastType = "success" | "error" | "info"

interface ToastItem { id: number; message: string; type: ToastType }

const toasts = ref<ToastItem[]>([])
let nextId = 0

function add(message: string, type: ToastType = "info", duration = 3000) {
  const id = nextId++
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    const i = toasts.value.findIndex(t => t.id === id)
    if (i >= 0) toasts.value.splice(i, 1)
  }, duration)
}

// Listen for toast custom events dispatched from views
function onToastEvent(e: Event) {
  const { detail } = e as CustomEvent
  add(detail.message, detail.type || "info")
}

onMounted(() => {
  document.addEventListener("toast", onToastEvent)
})

onUnmounted(() => {
  document.removeEventListener("toast", onToastEvent)
})
</script>

<style scoped>
.toast-container {
  position: fixed; top: 16px; right: 16px; z-index: 9999;
  display: flex; flex-direction: column; gap: 8px; pointer-events: none;
}
.toast {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 20px; border-radius: 8px; font-size: 14px; font-weight: 600;
  box-shadow: 0 4px 20px rgba(0,0,0,.12); pointer-events: auto;
  min-width: 200px; max-width: 380px;
}
.toast-success { background: #e8f5e9; color: #2e7d32; }
.toast-error { background: #ffebee; color: #c62828; }
.toast-info { background: #e3f2fd; color: #1565c0; }

.toast-enter-active { transition: all .25s ease-out; }
.toast-leave-active { transition: all .2s ease-in; }
.toast-enter-from { opacity: 0; transform: translateX(40px); }
.toast-leave-to { opacity: 0; transform: translateX(40px); }
</style>
