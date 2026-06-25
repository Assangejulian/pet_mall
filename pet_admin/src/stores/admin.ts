import { defineStore } from "pinia"
import { ref } from "vue"
import type { AdminInfo } from "../types/user"
export const useAdminStore = defineStore("admin", () => {
  const token = ref(localStorage.getItem("admin_token") || "")
  const raw = localStorage.getItem("admin_info")
  const info = ref<AdminInfo | null>(raw ? JSON.parse(raw) : null)
  function setToken(t: string) { token.value = t; localStorage.setItem("admin_token", t) }
  function setInfo(i: AdminInfo) { info.value = i; localStorage.setItem("admin_info", JSON.stringify(i)) }
  function logout() { token.value = ""; info.value = null; localStorage.removeItem("admin_token"); localStorage.removeItem("admin_info") }
  return { token, info, setToken, setInfo, logout }
})