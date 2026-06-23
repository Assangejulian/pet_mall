import { createRouter, createWebHistory } from "vue-router"
import HomePage from "../views/HomePage.vue"
import MarketPage from "../views/MarketPage.vue"
import CommunityPage from "../views/CommunityPage.vue"
import NotesPage from "../views/NotesPage.vue"

const routes = [
  { path: "/", name: "home", component: HomePage },
  { path: "/market", name: "market", component: MarketPage },
  { path: "/community", name: "community", component: CommunityPage },
  { path: "/notes", name: "notes", component: NotesPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
