import { defineStore } from "pinia"
import { ref } from "vue"

export const useUserStore = defineStore("user", () => {
  const isLoggedIn = ref(false)
  const profile = ref({ name: "\u7231\u5fc3\u4eba\u5c0f\u6696", avatar: "", petCount: 3 })

  function login() { isLoggedIn.value = true }
  function logout() { isLoggedIn.value = false; localStorage.removeItem("token") }

  return { isLoggedIn, profile, login, logout }
})
