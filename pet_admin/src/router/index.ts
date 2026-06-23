import { createRouter, createWebHashHistory } from "vue-router"
import { useAdminStore } from "../stores/admin"
import AdminLayout from "../layouts/AdminLayout.vue"
import Login from "../views/Login.vue"
import Dashboard from "../views/Dashboard.vue"
import UserList from "../views/user/UserList.vue"
import StoreList from "../views/store/StoreList.vue"
import ProductList from "../views/product/ProductList.vue"
import OrderList from "../views/order/OrderList.vue"
import VideoList from "../views/video/VideoList.vue"

const routes = [
  { path: "/login", name: "Login", component: Login, meta: { public: true } },
  {
    path: "/",
    component: AdminLayout,
    redirect: "/dashboard",
    children: [
      { path: "dashboard", name: "Dashboard", component: Dashboard },
      { path: "user", name: "UserList", component: UserList },
      { path: "store", name: "StoreList", component: StoreList },
      { path: "product", name: "ProductList", component: ProductList },
      { path: "order", name: "OrderList", component: OrderList },
      { path: "video", name: "VideoList", component: VideoList },
    ],
  },
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((_to, _from, next) => {
  const admin = useAdminStore()
  if (!admin.token && _to.path !== "/login") return next("/login")
  if (admin.token && _to.path === "/login") return next("/")
  next()
})

export default router
