import { defineStore } from "pinia"; import { ref } from "vue"; import type { Video } from "../types/video"; import { listVideos } from "../api/video"
export const useVideoStore = defineStore("admin_video", () => {
  const list = ref<Video[]>([]); const total = ref(0); const loading = ref(false)
  async function fetch(params: { page: number; size: number; keyword?: string; status?: number }) { loading.value = true; try { const r = await listVideos(params); list.value = r.records; total.value = r.total } finally { loading.value = false } }
  return { list, total, loading, fetch }
})