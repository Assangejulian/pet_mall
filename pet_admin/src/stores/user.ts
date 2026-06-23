import { defineStore } from "pinia"
import { ref } from "vue"
import type { User } from "../types/user"
import { listUsers } from "../api/user"
export const useUserStore = defineStore("admin_user", () => {
  const list = ref<User[]>([]); const total = ref(0); const loading = ref(false)
  async function fetch(params: { page: number; size: number; keyword?: string }) { loading.value = true; const r = await listUsers(params); list.value = r.records; total.value = r.total; loading.value = false }
  return { list, total, loading, fetch }
})