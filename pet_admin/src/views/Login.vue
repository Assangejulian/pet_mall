<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Logo -->
      <div class="login-brand">
        <div class="brand-icon">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <path d="M16 4C10 4 6 8.5 6 14c0 5 4 10 10 14 6-4 10-9 10-14 0-5.5-4-10-10-10z" fill="currentColor" opacity=".2"/>
            <circle cx="12" cy="13" r="1.5" fill="currentColor"/>
            <circle cx="20" cy="13" r="1.5" fill="currentColor"/>
            <path d="M12 18c1.5 2 4.5 2 6 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-title">Pet<span>Nest</span></h1>
        <p class="brand-sub">管理后台</p>
      </div>

      <!-- Form -->
      <div class="login-form">
        <div class="field">
          <label>账号</label>
          <div class="input-wrap" :class="{ focus: focusField === 'username' }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <circle cx="8" cy="5" r="3" stroke="currentColor" stroke-width="1.2"/>
              <path d="M2 14c0-3.3 2.7-6 6-6s6 2.7 6 6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入管理员账号"
              @focus="focusField = 'username'"
              @blur="focusField = ''"
              @keyup.enter="handleLogin"
            />
          </div>
        </div>

        <div class="field">
          <label>密码</label>
          <div class="input-wrap" :class="{ focus: focusField === 'password' }">
            <svg class="input-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" stroke-width="1.2"/>
              <path d="M5 7V4.5a3 3 0 0 1 6 0V7" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
            </svg>
            <input
              v-model="form.password"
              :type="showPwd ? 'text' : 'password'"
              placeholder="请输入密码"
              @focus="focusField = 'password'"
              @blur="focusField = ''"
              @keyup.enter="handleLogin"
            />
            <button class="eye-btn" type="button" @click="showPwd = !showPwd" tabindex="-1">
              <svg v-if="showPwd" width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M1 8s2.5-5 7-5 7 5 7 5-2.5 5-7 5-7-5-7-5z" stroke="currentColor" stroke-width="1.2"/>
                <circle cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.2"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M1 8s2.5-5 7-5 7 5 7 5-2.5 5-7 5-7-5-7-5z" stroke="currentColor" stroke-width="1.2"/>
                <circle cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.2"/>
                <path d="M3 3l10 10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Options -->
        <div class="field-options">
          <label class="remember">
            <input type="checkbox" v-model="remember" />
            <span>记住账号</span>
          </label>
        </div>

        <!-- Error -->
        <transition name="fade">
          <div v-if="error" class="login-error">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <circle cx="7" cy="7" r="6" stroke="currentColor" stroke-width="1.2"/>
              <path d="M7 4.5v3" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
              <circle cx="7" cy="10" r=".6" fill="currentColor"/>
            </svg>
            <span>{{ error }}</span>
          </div>
        </transition>

        <!-- Submit -->
        <button class="login-btn" @click="handleLogin" :disabled="loading">
          <span v-if="loading" class="spinner"></span>
          <span v-else>登 录</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { useRouter } from "vue-router"
import { useAdminStore } from "../stores/admin"
import { publicHttp } from "../api/index"
import { unwrap } from "../api/helper"
import type { LoginResult } from "../types/user"

const router = useRouter()
const admin = useAdminStore()
const loading = ref(false)
const error = ref("")
const focusField = ref("")
const showPwd = ref(false)
const rememberedUsername = localStorage.getItem("admin_remember_username") || ""
const remember = ref(Boolean(rememberedUsername))

const form = reactive({
  username: rememberedUsername,
  password: ""
})

onMounted(() => {
  if (form.username) form.password = "" // 仅回填账号
})

async function handleLogin() {
  if (!form.username) { error.value = "请输入管理员账号"; return }
  if (!form.password) { error.value = "请输入密码"; return }
  loading.value = true; error.value = ""

  try {
    const data = await unwrap<LoginResult>(
      publicHttp.post("/admin/login", {
        username: form.username,
        password: form.password,
        authType: "password"
      })
    )
    admin.setSession({ ...data, avatar: data.avatar || "" })

    if (remember.value) {
      localStorage.setItem("admin_remember_username", form.username)
    } else {
      localStorage.removeItem("admin_remember_username")
    }

    await router.replace(admin.homePath)
  } catch (cause: unknown) {
    admin.logout()
    error.value = cause instanceof Error ? cause.message : "登录失败，请检查账号密码"
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #faf7ef 0%, #f5ede6 50%, #efe6dc 100%);
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 20px;
  padding: 48px 40px 40px;
  width: 400px;
  box-shadow: 0 24px 80px rgba(64, 43, 28, 0.1), 0 4px 16px rgba(64, 43, 28, 0.04);
  animation: cardIn .4s ease-out;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(16px) scale(.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* Brand */
.login-brand { text-align: center; margin-bottom: 36px; }
.brand-icon { display: inline-flex; width: 56px; height: 56px; border-radius: 16px; background: linear-gradient(135deg, #f5c8b8, var(--primary)); color: #fff; align-items: center; justify-content: center; margin-bottom: 16px; }
.brand-title { font-family: "Noto Serif SC", serif; font-size: 22px; letter-spacing: 3px; }
.brand-title span { color: var(--primary); }
.brand-sub { color: var(--text2); font-size: 13px; margin-top: 4px; }

/* Fields */
.login-form { }
.field { margin-bottom: 20px; }
.field label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 6px; color: var(--text2); }

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  height: 46px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius);
  background: #fafaf9;
  transition: all .2s;
}
.input-wrap.focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 3px rgba(232, 146, 124, .1); }

.input-icon { flex-shrink: 0; color: var(--text2); }
.input-wrap.focus .input-icon { color: var(--primary); }

.input-wrap input {
  flex: 1;
  border: 0;
  background: transparent;
  font-size: 14px;
  outline: none;
  color: var(--text1);
}
.input-wrap input::placeholder { color: #c0b8b0; }

.eye-btn {
  flex-shrink: 0;
  border: 0;
  background: transparent;
  color: var(--text2);
  cursor: pointer;
  padding: 4px;
  display: flex;
  transition: color .15s;
}
.eye-btn:hover { color: var(--primary); }

/* Options */
.field-options { display: flex; align-items: center; justify-content: space-between; margin: 4px 0 20px; }
.remember { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text2); cursor: pointer; }
.remember input { accent-color: var(--primary); }

/* Error */
.login-error {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: var(--radius);
  background: #fef2f0;
  color: #c62828;
  font-size: 13px;
  margin-bottom: 16px;
}
.fade-enter-active, .fade-leave-active { transition: all .2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(-4px); }

/* Submit */
.login-btn {
  position: relative;
  width: 100%;
  height: 46px;
  border: 0;
  border-radius: var(--radius);
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 4px;
  cursor: pointer;
  transition: all .2s;
  overflow: hidden;
}
.login-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(232, 146, 124, .3); }
.login-btn:active { transform: translateY(0); }
.login-btn:disabled { opacity: .6; cursor: default; transform: none; box-shadow: none; }

.spinner { display: inline-block; width: 18px; height: 18px; border: 2px solid rgba(255,255,255,.3); border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
