import { defineStore } from "pinia"
import { ref, computed } from "vue"
import http from "../api"

export interface UserProfile {
  userId: number
  username: string
  role: string
}

export const useUserStore = defineStore("user", () => {
  const token = ref(localStorage.getItem("token") || "")
  const profile = ref<UserProfile | null>(loadProfile())

  function loadProfile(): UserProfile | null {
    const raw = localStorage.getItem("profile")
    if (!raw) return null
    try { return JSON.parse(raw) } catch { return null }
  }

  const isLoggedIn = computed(() => !!token.value && !!profile.value)

  async function login(dto: { authType: string; username?: string; password?: string; wxCode?: string }) {
    const res: any = await http.post("/user/login", dto)
    if (res.code !== 200) throw new Error(res.message || "登录失败")
    const data = res.data as { token: string; userId: number; username: string; role: string }
    token.value = data.token
    profile.value = { userId: data.userId, username: data.username, role: data.role }
    localStorage.setItem("token", data.token)
    localStorage.setItem("profile", JSON.stringify(profile.value))
  }

  function logout() {
    token.value = ""
    profile.value = null
    localStorage.removeItem("token")
    localStorage.removeItem("profile")
  }

  return { token, profile, isLoggedIn, login, logout }
})
