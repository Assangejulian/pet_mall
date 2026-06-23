<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="login-logo">♡</span>
        <h1>Pet<span>Nest</span></h1>
        <p>管理后台</p>
      </div>
      <div class="login-form">
        <div class="form-group">
          <label>账号</label>
          <input v-model="form.username" type="text" placeholder="请输入管理员账号" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
        </div>
        <div v-if="error" class="login-error">{{ error }}</div>
        <button class="login-btn" @click="handleLogin" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue"
import { useRouter } from "vue-router"
import { useAdminStore } from "../stores/admin"

const router = useRouter()
const admin = useAdminStore()
const loading = ref(false)
const error = ref("")
const form = reactive({ username: "", password: "" })

async function handleLogin() {
  if (!form.username || !form.password) { error.value = "请输入账号和密码"; return }
  loading.value = true; error.value = ""
  try {
    // Mock login - will be replaced with real API
    admin.setToken("mock_token_" + Date.now())
    admin.setInfo({ id: "1", username: form.username, avatar: "", role: "admin" })
    router.push("/")
  } catch (e: any) {
    error.value = e?.response?.data?.msg || "登录失败，请重试"
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #faf7ef 0%, #f5ede6 100%); }
.login-card { background: #fff; border-radius: 16px; padding: 48px 40px; width: 400px; box-shadow: 0 24px 80px rgba(64,43,28,.1); }
.login-header { text-align: center; margin-bottom: 36px; }
.login-logo { display: inline-flex; width: 48px; height: 48px; border-radius: 999px; background: linear-gradient(135deg, #f5c8b8, var(--primary)); color: #fff; font-size: 24px; align-items: center; justify-content: center; margin-bottom: 16px; }
.login-header h1 { font-family: "Noto Serif SC", serif; font-size: 24px; letter-spacing: 2px; }
.login-header h1 span { color: var(--primary); }
.login-header p { color: var(--text2); font-size: 14px; margin-top: 4px; }
.login-form .form-group { margin-bottom: 20px; }
.login-form input { width: 100%; padding: 12px 16px; border: 1px solid var(--border); border-radius: var(--radius); font-size: 14px; outline: none; transition: border-color .2s; }
.login-form input:focus { border-color: var(--primary); }
.login-error { color: #c62828; font-size: 13px; margin-bottom: 12px; }
.login-btn { width: 100%; padding: 12px; border: 0; border-radius: var(--radius); background: var(--primary); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; transition: background .15s; }
.login-btn:hover { background: var(--primary-dark); }
.login-btn:disabled { opacity: .6; cursor: default; }
</style>