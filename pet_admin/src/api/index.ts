import axios from "axios"
const http = axios.create({ baseURL: "/api/admin", timeout: 10000 })
http.interceptors.request.use(c => { const t = localStorage.getItem("admin_token"); if (t) c.headers.Authorization = "Bearer " + t; return c })
http.interceptors.response.use(r => r.data, e => { if (e.response?.status === 401) { localStorage.removeItem("admin_token"); location.hash = "#/login" } return Promise.reject(e) })
export default http