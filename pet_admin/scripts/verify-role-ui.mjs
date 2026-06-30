import { readFileSync } from "node:fs"
import { resolve } from "node:path"

const root = resolve(import.meta.dirname, "..")
const read = path => readFileSync(resolve(root, path), "utf8")
const checks = []

function assert(name, condition) {
  if (!condition) throw new Error(`FAILED: ${name}`)
  checks.push(name)
}

const config = read("src/config/management.ts")
const router = read("src/router/index.ts")
const store = read("src/stores/admin.ts")
const http = read("src/api/index.ts")
const layout = read("src/layouts/AdminLayout.vue")
const login = read("src/views/Login.vue")

for (const label of ["概览", "我的门店", "我的商品"]) assert(`merchant menu: ${label}`, config.includes(label))
for (const label of ["概览", "门店审核", "商品监管"]) assert(`auditor menu: ${label}`, config.includes(label))
for (const label of ["用户管理", "门店管理", "商品管理", "订单管理", "视频管理", "门店审核", "商品监管"]) assert(`admin menu: ${label}`, config.includes(label))

assert("merchant routes are merchant-only", /merchant\/store[\s\S]*roles: \["merchant"\]/.test(router) && /merchant\/product[\s\S]*roles: \["merchant"\]/.test(router))
assert("auditor routes allow auditor and admin", /auditor\/store[\s\S]*roles: \["auditor", "admin"\]/.test(router) && /auditor\/product[\s\S]*roles: \["auditor", "admin"\]/.test(router))
assert("admin routes are admin-only", /path: "user"[\s\S]*roles: \["admin"\]/.test(router) && /path: "store"[\s\S]*roles: \["admin"\]/.test(router))
assert("guard redirects forbidden roles", router.includes("!roles.includes(admin.role)") && router.includes("return admin.homePath"))
assert("refresh restores validated session", store.includes("restoreSession") && store.includes("decodeTokenInfo") && store.includes("normalizeInfo"))
assert("unknown and user roles are rejected", config.includes('["merchant", "auditor", "admin"]') && store.includes("isManagementRole"))
assert("login persists one validated session", login.includes("admin.setSession") && !login.includes("admin.setToken"))
assert("layout uses computed role menu", layout.includes("ROLE_MENUS") && layout.includes("admin.roleLabel") && !layout.includes('>超级管理员<'))
assert("401 clears session", http.includes("if (code === 401) clearSessionAndRedirect()"))
assert("403 preserves session", http.includes("else if (code === 403) notify") && !/code === 403[^\n]*clearSession/.test(http))
assert("four shared clients use correct bases", ["/api/admin", "/api/merchant", "/api/auditor", "/api"].every(base => http.includes(`createClient("${base}")`)))
assert("merchant modules use merchant client", read("src/api/merchantStore.ts").includes("merchantHttp") && read("src/api/merchantProduct.ts").includes("merchantHttp"))
assert("auditor modules use auditor client", read("src/api/auditorStore.ts").includes("auditorHttp") && read("src/api/auditorProduct.ts").includes("auditorHttp"))
assert("legacy admin modules retain default client", ["user.ts", "store.ts", "product.ts", "order.ts", "video.ts"].every(file => read(`src/api/${file}`).includes('import http from "./index"')))

console.log(`Role UI verification passed: ${checks.length} checks`)
