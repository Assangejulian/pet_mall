import { createRouter, createWebHashHistory, type RouteRecordRaw } from "vue-router"
import { useAdminStore } from "../stores/admin"
import AdminLayout from "../layouts/AdminLayout.vue"
import Login from "../views/Login.vue"
import Dashboard from "../views/Dashboard.vue"
import UserList from "../views/user/UserList.vue"
import StoreList from "../views/store/StoreList.vue"
import ProductList from "../views/product/ProductList.vue"
import OrderList from "../views/order/OrderList.vue"
import VideoList from "../views/video/VideoList.vue"
import MerchantStoreList from "../views/merchant/MerchantStoreList.vue"
import MerchantProductList from "../views/merchant/MerchantProductList.vue"
import MerchantOrderList from "../views/merchant/MerchantOrderList.vue"
import AuditorStoreList from "../views/auditor/AuditorStoreList.vue"
import AuditorProductList from "../views/auditor/AuditorProductList.vue"

const routes: RouteRecordRaw[] = [
  { path: "/login", name: "Login", component: Login, meta: { public: true, title: "登录" } },
  {
    path: "/",
    component: AdminLayout,
    redirect: () => useAdminStore().homePath,
    children: [
      { path: "dashboard", name: "Dashboard", component: Dashboard, meta: { title: "概览", roles: ["merchant", "auditor", "admin"] } },
      { path: "merchant/store", name: "MerchantStoreList", component: MerchantStoreList, meta: { title: "我的门店", roles: ["merchant"] } },
      { path: "merchant/product", name: "MerchantProductList", component: MerchantProductList, meta: { title: "我的商品", roles: ["merchant"] } },
      { path: "merchant/order", name: "MerchantOrderList", component: MerchantOrderList, meta: { title: "我的订单", roles: ["merchant"] } },
      { path: "auditor/store", name: "AuditorStoreList", component: AuditorStoreList, meta: { title: "门店审核", roles: ["auditor", "admin"] } },
      { path: "auditor/product", name: "AuditorProductList", component: AuditorProductList, meta: { title: "商品监管", roles: ["auditor", "admin"] } },
      { path: "user", name: "UserList", component: UserList, meta: { title: "用户管理", roles: ["admin"] } },
      { path: "store", name: "StoreList", component: StoreList, meta: { title: "门店管理", roles: ["admin"] } },
      { path: "product", name: "ProductList", component: ProductList, meta: { title: "商品管理", roles: ["admin"] } },
      { path: "order", name: "OrderList", component: OrderList, meta: { title: "订单管理", roles: ["admin"] } },
      { path: "video", name: "VideoList", component: VideoList, meta: { title: "视频管理", roles: ["admin"] } },
    ],
  },
  { path: "/:pathMatch(.*)*", redirect: "/" },
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach(to => {
  const admin = useAdminStore()
  if (to.meta.public) {
    if (to.path === "/login" && admin.token && admin.restoreSession()) return admin.homePath
    return true
  }
  if (!admin.token) return "/login"
  if (!admin.restoreSession() || !admin.role) return "/login"
  const roles = to.meta.roles
  if (roles?.length && !roles.includes(admin.role)) return admin.homePath
  return true
})

export default router

