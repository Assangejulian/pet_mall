import { computed, ref } from "vue"
import { defineStore } from "pinia"
import { isManagementRole, ROLE_HOME, ROLE_LABEL } from "../config/management"
import type { AdminInfo, LoginResult, ManagementRole } from "../types/user"

const TOKEN_KEY = "admin_token"
const INFO_KEY = "admin_info"

function normalizeInfo(value: unknown): AdminInfo | null {
  if (!value || typeof value !== "object") return null
  const source = value as Record<string, unknown>
  if (!isManagementRole(source.role) || !source.userId || !source.username) return null
  return {
    userId: String(source.userId),
    username: String(source.username),
    avatar: typeof source.avatar === "string" ? source.avatar : "",
    role: source.role,
  }
}

function readStoredInfo(): AdminInfo | null {
  const raw = localStorage.getItem(INFO_KEY)
  if (!raw) return null
  try {
    return normalizeInfo(JSON.parse(raw))
  } catch {
    return null
  }
}

function decodeTokenInfo(token: string): AdminInfo | null {
  try {
    const payload = token.split(".")[1]
    if (!payload) return null
    const json = decodeURIComponent(
      Array.from(atob(payload.replace(/-/g, "+").replace(/_/g, "/")))
        .map(char => `%${char.charCodeAt(0).toString(16).padStart(2, "0")}`)
        .join(""),
    )
    return normalizeInfo(JSON.parse(json))
  } catch {
    return null
  }
}

export const useAdminStore = defineStore("admin", () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || "")
  const info = ref<AdminInfo | null>(readStoredInfo())

  const isAuthenticated = computed(() => Boolean(token.value && info.value))
  const role = computed<ManagementRole | null>(() => info.value?.role ?? null)
  const homePath = computed(() => role.value ? ROLE_HOME[role.value] : "/login")
  const roleLabel = computed(() => role.value ? ROLE_LABEL[role.value] : "")

  function persist() {
    if (token.value) localStorage.setItem(TOKEN_KEY, token.value)
    if (info.value) localStorage.setItem(INFO_KEY, JSON.stringify(info.value))
  }

  function setSession(session: LoginResult) {
    const nextInfo = normalizeInfo(session)
    if (!session.token || !nextInfo) throw new Error("该账号无管理端访问权限")
    token.value = session.token
    info.value = nextInfo
    persist()
  }

  function setToken(value: string) {
    token.value = value
    if (value) localStorage.setItem(TOKEN_KEY, value)
    else localStorage.removeItem(TOKEN_KEY)
  }

  function setInfo(value: AdminInfo) {
    const nextInfo = normalizeInfo(value)
    if (!nextInfo) throw new Error("管理端身份无效")
    info.value = nextInfo
    localStorage.setItem(INFO_KEY, JSON.stringify(nextInfo))
  }

  function restoreSession(): boolean {
    if (!token.value) return false
    const restored = normalizeInfo(info.value) || readStoredInfo() || decodeTokenInfo(token.value)
    if (!restored) {
      logout()
      return false
    }
    info.value = restored
    persist()
    return true
  }

  function hasRole(...roles: ManagementRole[]) {
    return Boolean(role.value && roles.includes(role.value))
  }

  function logout() {
    token.value = ""
    info.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(INFO_KEY)
  }

  if (token.value && !info.value) restoreSession()
  if (!token.value || !info.value) {
    token.value = ""
    info.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(INFO_KEY)
  }

  return {
    token, info, isAuthenticated, role, homePath, roleLabel,
    setSession, setToken, setInfo, restoreSession, hasRole, logout,
  }
})
