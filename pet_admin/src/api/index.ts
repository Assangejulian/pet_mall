import axios, { type AxiosError, type AxiosInstance } from "axios"

export class ApiError extends Error {
  constructor(message: string, public readonly code?: number) {
    super(message)
    this.name = "ApiError"
  }
}

let redirectingToLogin = false

function notify(message: string, type: "error" | "success" = "error") {
  document.dispatchEvent(new CustomEvent("toast", { detail: { message, type } }))
}

function clearSessionAndRedirect() {
  localStorage.removeItem("admin_token")
  localStorage.removeItem("admin_info")
  window.dispatchEvent(new CustomEvent("management-session-cleared"))
  if (!redirectingToLogin && location.hash !== "#/login") {
    redirectingToLogin = true
    location.hash = "#/login"
    window.setTimeout(() => { redirectingToLogin = false }, 300)
  }
}

function rejectForCode(code: number, message?: string): never {
  const text = message || (code === 401 ? "登录已过期" : code === 403 ? "无权限" : code === 404 ? "资源不存在" : "请求失败")
  if (code === 401) clearSessionAndRedirect()
  else if (code === 403) notify(text || "无权限")
  else if (code === 404) notify("资源不存在")
  else notify(text)
  throw new ApiError(text, code)
}

function installInterceptors(client: AxiosInstance) {
  client.interceptors.request.use(config => {
    const token = localStorage.getItem("admin_token")
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  })

  client.interceptors.response.use(response => {
    const body = response.data
    if (body && typeof body.code === "number" && body.code !== 200) {
      return rejectForCode(body.code, body.message)
    }
    return body
  }, (error: AxiosError<{ code?: number; message?: string }>) => {
    const status = error.response?.status
    const code = error.response?.data?.code ?? status
    const message = error.response?.data?.message || error.message
    if (code) return Promise.reject((() => {
      try { rejectForCode(code, message) } catch (reason) { return reason }
    })())
    notify("网络异常，请稍后重试")
    return Promise.reject(new ApiError("网络异常，请稍后重试"))
  })
}

function createClient(baseURL: string) {
  const client = axios.create({ baseURL, timeout: 10000 })
  installInterceptors(client)
  return client
}

export const adminHttp = createClient("/api/admin")
export const merchantHttp = createClient("/api/merchant")
export const auditorHttp = createClient("/api/auditor")
export const publicHttp = createClient("/api")

export default adminHttp
