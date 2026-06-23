import { defineStore } from "pinia"; import { ref } from "vue"; import type { Store } from "../types/store"; import { listStores } from "../api/store"
export const useStoreStore = defineStore("admin_store", () => {
  const list = ref<Store[]>([]); const total = ref(0); const loading = ref(false)
  async function fetch(params: { page: number; size: number; keyword?: string; status?: number }) { loading.value = true; const r = await listStores(params); list.value = r.records; total.value = r.total; loading.value = false }
  return { list, total, loading, fetch }
})