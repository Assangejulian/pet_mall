import axios from "axios"
import router from "../router"

const http = axios.create({ baseURL: "/api", timeout: 10000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem("token")
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (r) => r.data,
  (e) => {
    if (e.response?.status === 401) {
      localStorage.removeItem("token")
      localStorage.removeItem("profile")
      router.push("/login")
    }
    return Promise.reject(e)
  }
)

export default http
