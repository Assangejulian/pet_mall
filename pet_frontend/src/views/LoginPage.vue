<script setup lang="ts">
import { ref, onMounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "../stores/user"
import http from "../api"

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const curTab = ref(0)
const loading = ref(false)
const errorMsg = ref("")

const username = ref("")
const password = ref("")

const email = ref("")
const emailCode = ref("")
const emailCountdown = ref(0)
const canSendEmailCode = ref(true)

const smsPhone = ref("")
const smsCode = ref("")
const smsCountdown = ref(0)
const canSendSmsCode = ref(true)

const tabs = [
  { id: 0, label: "密码登录" },
  { id: 1, label: "邮箱验证码" },
  { id: 2, label: "手机验证码" },
  { id: 3, label: "微信扫码" }
]

onMounted(async () => {
  // 校验微信回调 state (防CSRF)
  const state = route.query.state as string | undefined
  const savedState = localStorage.getItem("wx_login_state")
  if (state && savedState && state !== savedState) {
    errorMsg.value = "微信登录安全校验失败，请重试"
    return
  }
  localStorage.removeItem("wx_login_state")

  const code = route.query.code as string | undefined
  if (code) {
    loading.value = true
    try {
      await userStore.login({ authType: "wechat_pc", wxCode: code } as any)
      router.push((route.query.redirect as string) || "/")
    } catch (e: any) {
      errorMsg.value = e.message || "微信登录失败"
    } finally {
      loading.value = false
    }
  }
})

function swTab(idx: number) {
  curTab.value = idx
  errorMsg.value = ""
}

async function handlePasswordLogin() {
  if (!username.value || !password.value) {
    errorMsg.value = "请输入用户名和密码"
    return
  }
  loading.value = true
  errorMsg.value = ""
  try {
    await userStore.login({ authType: "password", username: username.value, password: password.value } as any)
    const redirect = (route.query.redirect as string) || "/"
    router.push(redirect)
  } catch (e: any) {
    errorMsg.value = e.message || "登录失败"
  } finally {
    loading.value = false
  }
}

async function sendEmailCode() {
  if (!email.value || !email.value.includes("@")) {
    errorMsg.value = "请输入正确的邮箱地址"
    return
  }
  emailCountdown.value = 60
  canSendEmailCode.value = false
  const timer = setInterval(() => {
    emailCountdown.value--
    if (emailCountdown.value <= 0) {
      clearInterval(timer)
      canSendEmailCode.value = true
    }
  }, 1000)
  try {
    await http.post("/user/send-email-code", { email: email.value })
    errorMsg.value = ""
  } catch {
    clearInterval(timer)
    emailCountdown.value = 0
    canSendEmailCode.value = true
    errorMsg.value = "发送失败，请稍后重试"
  }
}

async function handleEmailLogin() {
  if (!email.value || !email.value.includes("@")) {
    errorMsg.value = "请输入正确的邮箱地址"
    return
  }
  if (!emailCode.value || emailCode.value.length < 4) {
    errorMsg.value = "请输入验证码"
    return
  }
  loading.value = true
  errorMsg.value = ""
  try {
    await userStore.login({ authType: "email_code", email: email.value, code: emailCode.value } as any)
    router.push((route.query.redirect as string) || "/")
  } catch (e: any) {
    errorMsg.value = e.message || "登录失败"
  } finally {
    loading.value = false
  }
}

async function sendSmsCode() {
  if (!smsPhone.value || smsPhone.value.length < 11) {
    errorMsg.value = "请输入正确的手机号"
    return
  }
  smsCountdown.value = 60
  canSendSmsCode.value = false
  const timer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      clearInterval(timer)
      canSendSmsCode.value = true
    }
  }, 1000)
  try {
    await http.post("/user/send-sms-code", { phone: smsPhone.value })
    errorMsg.value = ""
  } catch {
    clearInterval(timer)
    smsCountdown.value = 0
    canSendSmsCode.value = true
    errorMsg.value = "发送失败，请稍后重试"
  }
}

async function handleSmsLogin() {
  if (!smsPhone.value || smsPhone.value.length < 11) {
    errorMsg.value = "请输入正确的手机号"
    return
  }
  if (!smsCode.value || smsCode.value.length < 4) {
    errorMsg.value = "请输入验证码"
    return
  }
  loading.value = true
  errorMsg.value = ""
  try {
    await userStore.login({ authType: "sms", phone: smsPhone.value, code: smsCode.value } as any)
    router.push((route.query.redirect as string) || "/")
  } catch (e: any) {
    errorMsg.value = e.message || "登录失败"
  } finally {
    loading.value = false
  }
}

function handleWechatLogin() {
  const appid = import.meta.env.VITE_WECHAT_PC_APPID || ""
  if (!appid) {
    errorMsg.value = "微信登录未配置"
    return
  }
  // 生成随机 state 防 CSRF
  const state = Math.random().toString(36).substring(2, 15)
  localStorage.setItem("wx_login_state", state)
  const redirectUri = encodeURIComponent(window.location.origin + "/login")
  window.location.href = `https://open.weixin.qq.com/connect/qrconnect?appid=${appid}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_login&state=${state}`
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1>登录</h1>
      <p class="subtitle">欢迎来到 PetNest</p>

      <div class="login-tabs">
        <button v-for="t in tabs" :key="t.id" class="tab-btn" :class="{ active: curTab === t.id }" @click="swTab(t.id)">{{ t.label }}</button>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <form v-show="curTab === 0" @submit.prevent="handlePasswordLogin">
        <label>用户名<input v-model="username" type="text" placeholder="请输入用户名" /></label>
        <label>密码<input v-model="password" type="password" placeholder="请输入密码" /></label>
        <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? "登录中..." : "登录" }}</button>
      </form>

      <form v-show="curTab === 1" @submit.prevent="handleEmailLogin">
        <label>邮箱<input v-model="email" type="email" placeholder="请输入邮箱地址" /></label>
        <label>
          验证码
          <div class="code-row">
            <input v-model="emailCode" type="text" placeholder="请输入验证码" maxlength="6" />
            <button type="button" class="btn-code" :disabled="!canSendEmailCode" @click="sendEmailCode">{{ canSendEmailCode ? "发送验证码" : emailCountdown + "s" }}</button>
          </div>
        </label>
        <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? "登录中..." : "登录" }}</button>
      </form>

      <form v-show="curTab === 2" @submit.prevent="handleSmsLogin">
        <label>手机号<input v-model="smsPhone" type="tel" placeholder="请输入手机号" maxlength="11" /></label>
        <label>
          验证码
          <div class="code-row">
            <input v-model="smsCode" type="text" placeholder="请输入验证码" maxlength="6" />
            <button type="button" class="btn-code" :disabled="!canSendSmsCode" @click="sendSmsCode">{{ canSendSmsCode ? "发送验证码" : smsCountdown + "s" }}</button>
          </div>
        </label>
        <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? "登录中..." : "登录" }}</button>
      </form>

      <div v-show="curTab === 3">
        <div class="wechat-qr-placeholder">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#07c160" stroke-width="1.5">
            <path d="M8 7h2v2H8V7zm6 0h2v2h-2V7zM4 11h2v2H4v-2zm14 0h2v2h-2v-2z"/>
            <rect x="2" y="2" width="20" height="20" rx="3" stroke="currentColor" fill="none"/>
          </svg>
          <p>使用微信扫码登录</p>
        </div>
        <button type="button" class="btn-wechat" @click="handleWechatLogin" :disabled="loading">微信扫码登录</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page { min-height: calc(100vh - 120px); display: flex; align-items: center; justify-content: center; background: #f5f5f5; }
.login-card { background: #fff; padding: 40px; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,.08); width: 420px; max-width: 90vw; }
.login-card h1 { margin: 0 0 4px; font-size: 24px; text-align: center; }
.subtitle { color: #888; margin: 0 0 20px; font-size: 14px; text-align: center; }
.login-tabs { display: flex; margin-bottom: 20px; border-bottom: 2px solid #eee; }
.tab-btn { flex: 1; padding: 10px 0; background: none; border: none; font-size: 13px; color: #888; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all .2s; }
.tab-btn.active { color: #e8927c; border-bottom-color: #e8927c; font-weight: 600; }
.tab-btn:hover { color: #e8927c; }
.error-msg { background: #fdecea; color: #d32f2f; padding: 8px 12px; border-radius: 6px; font-size: 13px; margin-bottom: 16px; }
label { display: block; font-size: 14px; color: #333; margin-bottom: 16px; }
label input, .code-row input { display: block; width: 100%; margin-top: 4px; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; box-sizing: border-box; }
label input:focus, .code-row input:focus { outline: none; border-color: #e8927c; }
.code-row { display: flex; gap: 8px; margin-top: 4px; }
.code-row input { flex: 1; margin-top: 0; }
.btn-code { flex-shrink: 0; padding: 10px 14px; background: #f5ede6; color: #e8927c; border: 1px solid #e8927c; border-radius: 8px; font-size: 13px; cursor: pointer; white-space: nowrap; transition: all .2s; }
.btn-code:hover:not(:disabled) { background: #e8927c; color: #fff; }
.btn-code:disabled { opacity: .5; cursor: not-allowed; }
.btn-primary { width: 100%; padding: 12px; background: #e8927c; color: #fff; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; }
.btn-primary:disabled { opacity: .6; cursor: not-allowed; }
.wechat-qr-placeholder { text-align: center; padding: 30px 0; color: #888; }
.wechat-qr-placeholder svg { display: block; margin: 0 auto 12px; }
.btn-wechat { width: 100%; padding: 12px; background: #07c160; color: #fff; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; }
.btn-wechat:disabled { opacity: .6; }
</style>


