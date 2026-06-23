<template>
  <div ref="cursorDot" class="cursor-dot" aria-hidden="true"></div>
  <div ref="cursorRing" class="cursor-ring" aria-hidden="true"></div>
  <TopBar />
  <main>
    <router-view />
  </main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from "vue"
import TopBar from "./components/layout/TopBar.vue"

const cursorDot = ref<HTMLDivElement | null>(null)
const cursorRing = ref<HTMLDivElement | null>(null)

const moveCursor = (event: MouseEvent) => {
  if (!cursorDot.value || !cursorRing.value) return

  setCursorVisible(true)
  cursorDot.value.style.left = `${event.clientX}px`
  cursorDot.value.style.top = `${event.clientY}px`
  cursorRing.value.style.left = `${event.clientX}px`
  cursorRing.value.style.top = `${event.clientY}px`
}

const setCursorVisible = (visible: boolean) => {
  if (!cursorDot.value || !cursorRing.value) return

  cursorDot.value.style.opacity = visible ? "1" : "0"
  cursorRing.value.style.opacity = visible ? "1" : "0"
}

const showCursor = () => setCursorVisible(true)
const hideCursor = () => setCursorVisible(false)

onMounted(() => {
  document.addEventListener("mousemove", moveCursor)
  document.addEventListener("mouseenter", showCursor)
  document.addEventListener("mouseleave", hideCursor)
})

onUnmounted(() => {
  document.removeEventListener("mousemove", moveCursor)
  document.removeEventListener("mouseenter", showCursor)
  document.removeEventListener("mouseleave", hideCursor)
})
</script>
