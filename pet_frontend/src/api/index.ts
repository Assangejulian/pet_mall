import axios from "axios"
const http = axios.create({ baseURL: "/api", timeout: 10000 })
http.interceptors.response.use(r => r.data, e => { if (e.response?.status === 401) { localStorage.removeItem("token"); window.location.hash = "#/" } return Promise.reject(e) })
export default http