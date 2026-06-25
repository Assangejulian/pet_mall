import axios from "axios"

// Admin API (with auth interceptor)
const http = axios.create({ baseURL: "/api/admin", timeout: 10000 })
http.interceptors.request.use(c => { const t = localStorage.getItem("admin_token"); if (t) c.headers.Authorization = "Bearer " + t; return c })
http.interceptors.response.use(r => r.data, e => { if (e.response?.status === 401) { localStorage.removeItem("admin_token"); location.hash = "#/login" } return Promise.reject(e) })

// Public API (no auth, direct to backend)
export const publicHttp = axios.create({ baseURL: "/api", timeout: 10000 })
publicHttp.interceptors.response.use(r => r.data)

export default http
