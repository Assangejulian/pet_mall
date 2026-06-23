import { defineStore } from "pinia"
import { ref } from "vue"
import type { AdminInfo } from "../types/user"
export const useAdminStore = defineStore("admin", () => {
  const token = ref(localStorage.getItem("admin_token") || "")
  const info = ref<AdminInfo | null>(null)
  function setToken(t: string) { token.value = t; localStorage.setItem("admin_token", t) }
  function setInfo(i: AdminInfo) { info.value = i }
  function logout() { token.value = ""; info.value = null; localStorage.removeItem("admin_token") }
  return { token, info, setToken, setInfo, logout }
})