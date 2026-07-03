<template>
  <view class="page">
    <!-- Tab Bar -->
    <view class="tab-bar">
      <view
        v-for="(tab, idx) in tabs"
        :key="idx"
        class="tab-item"
        :class="{ active: activeTab === idx }"
        @tap="switchTab(idx)"
      >
        <text class="tab-label">{{ tab }}</text>
        <view class="tab-bar-active" v-if="activeTab === idx"></view>
      </view>
    </view>

    <!-- Tab 0: 密码登录 -->
    <view class="tab-body" v-if="activeTab === 0">
      <view class="input-group">
        <view class="input-row">
          <text class="input-icon">👤</text>
          <input class="input-field" v-model="pwdForm.username" placeholder="请输入用户名" placeholder-style="color:#c4b8ad" maxlength="30" />
        </view>
        <view class="input-row">
          <text class="input-icon">🔒</text>
          <input class="input-field" v-model="pwdForm.password" placeholder="请输入密码" placeholder-style="color:#c4b8ad" :password="!showPwd" maxlength="30" />
          <text class="input-suffix" @tap="showPwd = !showPwd">{{ showPwd ? "🙈" : "👁" }}</text>
        </view>
      </view>
    </view>

    <!-- Tab 1: 邮箱验证码登录 -->
    <view class="tab-body" v-if="activeTab === 1">
      <view class="input-group">
        <view class="input-row">
          <text class="input-icon">📧</text>
          <input class="input-field" v-model="emailForm.email" placeholder="请输入邮箱地址" placeholder-style="color:#c4b8ad" type="text" maxlength="60" />
        </view>
        <view class="input-row">
          <text class="input-icon">✉️</text>
          <input class="input-field code-field" v-model="emailForm.code" placeholder="请输入验证码" placeholder-style="color:#c4b8ad" maxlength="6" />
          <text class="input-suffix send-code" :class="{ disabled: emailCountdown > 0 }" @tap="sendEmailCode">{{ emailCountdown > 0 ? emailCountdown + 's' : '获取验证码' }}</text>
        </view>
      </view>
    </view>

    <!-- Tab 2: 短信登录 -->
    <view class="tab-body" v-if="activeTab === 2">
      <view class="input-group">
        <view class="input-row">
          <text class="input-icon">📱</text>
          <input class="input-field" v-model="smsForm.phone" placeholder="请输入手机号" placeholder-style="color:#c4b8ad" type="number" maxlength="11" />
        </view>
        <view class="input-row">
          <text class="input-icon">💬</text>
          <input class="input-field code-field" v-model="smsForm.code" placeholder="请输入短信验证码" placeholder-style="color:#c4b8ad" maxlength="6" />
          <text class="input-suffix send-code" :class="{ disabled: smsCountdown > 0 }" @tap="sendSmsCode">{{ smsCountdown > 0 ? smsCountdown + 's' : '获取验证码' }}</text>
        </view>
      </view>
    </view>

    <!-- Tab 3: 微信登录 -->
    <view class="tab-body" v-if="activeTab === 3">
      <view class="wechat-login-area">
        <view class="wechat-icon-wrap"><text class="wechat-icon">💚</text></view>
        <text class="wechat-title">微信一键登录</text>
        <text class="wechat-desc">授权微信账号快速登录暖窝</text>
        <button class="wechat-btn" @tap="wxLogin" :loading="wxLoggingIn">
          <text v-if="!wxLoggingIn">微信授权登录</text>
          <text v-else>授权中...</text>
        </button>
      </view>
    </view>

    <!-- Tab 4: 人脸登录 -->
    <view class="tab-body" v-if="activeTab === 4">
      <view class="face-login-area">
        <view class="face-icon-wrap"><text class="face-icon">🤳</text></view>
        <text class="face-title">人脸识别登录</text>
        <text class="face-desc">使用摄像头快速验证身份</text>
        <button class="face-btn" @tap="goFaceLogin">开始人脸识别</button>
      </view>
    </view>

    <!-- Agreement -->
    <view class="agreement-row">
      <view class="agreement-check" @tap="agreed = !agreed">
        <view class="check-dot" :class="{ on: agreed }"></view>
      </view>
      <text class="agreement-text">已阅读并同意<text class="agreement-link" @tap.stop="showAgreement">《用户服务协议》</text>和<text class="agreement-link" @tap.stop="showAgreement">《隐私政策》</text></text>
    </view>

    <!-- Submit Button (for tabs 0-2) -->
    <view class="submit-area" v-if="activeTab <= 2">
      <button class="submit-btn" :class="{ disabled: submitting }" :loading="submitting" :disabled="submitting" @tap="handleLogin">{{ submitting ? '登录中...' : '登录' }}</button>
      <view class="extra-links">
        <text class="extra-link" @tap="switchTab(2)">短信验证码登录</text>
        <text class="extra-link" v-if="activeTab !== 0" @tap="switchTab(0)">账号密码登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import authApi from "@/utils/api/auth";

const app = getApp();

export default {
  data() {
    return {
      activeTab: 0,
      tabs: ["密码登录", "邮箱验证码", "短信登录", "微信登录", "人脸登录"],
      pwdForm: { username: "", password: "" },
      emailForm: { email: "", code: "" },
      smsForm: { phone: "", code: "" },
      showPwd: false,
      agreed: false,
      submitting: false,
      wxLoggingIn: false,
      emailCountdown: 0,
      smsCountdown: 0,
      emailTimer: null,
      smsTimer: null,
      _pendingFaceLogin: false
    };
  },
  onShow() {
    if (this._pendingFaceLogin) { this._pendingFaceLogin = false; }
    if (app.globalData.token) { this.navigateHome(); }
  },
  onUnload() { this.clearTimers(); },
  methods: {
    switchTab(idx) {
      if (idx === this.activeTab) return;
      this.activeTab = idx;
      this.showPwd = false;
    },
    validatePasswordLogin() {
      const { username, password } = this.pwdForm;
      if (!username.trim()) { uni.showToast({ title: "请输入用户名", icon: "none" }); return false; }
      if (!password) { uni.showToast({ title: "请输入密码", icon: "none" }); return false; }
      if (password.length < 6) { uni.showToast({ title: "密码至少6位", icon: "none" }); return false; }
      return true;
    },
    validateEmailLogin() {
      const { email, code } = this.emailForm;
      if (!email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) { uni.showToast({ title: "请输入正确的邮箱地址", icon: "none" }); return false; }
      if (!code || code.length < 4) { uni.showToast({ title: "请输入验证码", icon: "none" }); return false; }
      return true;
    },
    validateSmsLogin() {
      const { phone, code } = this.smsForm;
      if (!phone.trim() || !/^1[3-9]\d{9}$/.test(phone.trim())) { uni.showToast({ title: "请输入正确的手机号", icon: "none" }); return false; }
      if (!code || code.length < 4) { uni.showToast({ title: "请输入短信验证码", icon: "none" }); return false; }
      return true;
    },
    showAgreement() { uni.showToast({ title: "用户协议与隐私政策", icon: "none" }); },
    handleLogin() {
      if (!this.agreed) { uni.showToast({ title: "请先同意用户协议", icon: "none" }); return; }
      if (this.submitting) return;
      let dto = null;
      if (this.activeTab === 0) {
        if (!this.validatePasswordLogin()) return;
        dto = { authType: "password", username: this.pwdForm.username.trim(), password: this.pwdForm.password };
      } else if (this.activeTab === 1) {
        if (!this.validateEmailLogin()) return;
        dto = { authType: "email", email: this.emailForm.email.trim(), code: this.emailForm.code.trim() };
      } else if (this.activeTab === 2) {
        if (!this.validateSmsLogin()) return;
        dto = { authType: "sms", phone: this.smsForm.phone.trim(), code: this.smsForm.code.trim() };
      } else { return; }
      this.submitting = true;
      authApi.login(dto).then((res) => { this.doLogin(res); }).catch((err) => { uni.showToast({ title: (err && err.message) || "登录失败", icon: "none" }); }).finally(() => { this.submitting = false; });
    },
    doLogin(res) {
      const data = res || {};
      const token = data.token || data.accessToken || "";
      const user = data.user || data.userInfo || null;
      const userId = user ? (user.id || user.userId) : (data.userId || "");
      if (!token) { uni.showToast({ title: "登录凭证异常", icon: "none" }); return; }
      app.globalData.token = token;
      app.globalData.user = user;
      uni.setStorageSync("token", token);
      if (userId) uni.setStorageSync("userId", userId);
      uni.showToast({ title: "登录成功", icon: "success" });
      setTimeout(() => { this.navigateHome(); }, 800);
    },
    navigateHome() {
      const pages = getCurrentPages();
      if (pages.length > 1) { uni.navigateBack(); } else { uni.switchTab({ url: "/pages/index/index" }); }
    },
    clearTimers() {
      if (this.emailTimer) { clearInterval(this.emailTimer); this.emailTimer = null; }
      if (this.smsTimer) { clearInterval(this.smsTimer); this.smsTimer = null; }
    },
    sendEmailCode() {
      if (this.emailCountdown > 0) return;
      const email = this.emailForm.email.trim();
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { uni.showToast({ title: "请输入正确的邮箱地址", icon: "none" }); return; }
      authApi.sendEmailCode(email).then(() => { uni.showToast({ title: "验证码已发送", icon: "none" }); this.startEmailCountdown(); }).catch((err) => { uni.showToast({ title: (err && err.message) || "发送失败", icon: "none" }); });
    },
    startEmailCountdown() {
      this.clearTimers();
      this.emailCountdown = 60;
      this.emailTimer = setInterval(() => { this.emailCountdown--; if (this.emailCountdown <= 0) { clearInterval(this.emailTimer); this.emailTimer = null; } }, 1000);
    },
    sendSmsCode() {
      if (this.smsCountdown > 0) return;
      const phone = this.smsForm.phone.trim();
      if (!phone || !/^1[3-9]\d{9}$/.test(phone)) { uni.showToast({ title: "请输入正确的手机号", icon: "none" }); return; }
      authApi.sendSmsCode(phone).then(() => { uni.showToast({ title: "验证码已发送", icon: "none" }); this.startSmsCountdown(); }).catch((err) => { uni.showToast({ title: (err && err.message) || "发送失败", icon: "none" }); });
    },
    startSmsCountdown() {
      this.clearTimers();
      this.smsCountdown = 60;
      this.smsTimer = setInterval(() => { this.smsCountdown--; if (this.smsCountdown <= 0) { clearInterval(this.smsTimer); this.smsTimer = null; } }, 1000);
    },
    wxLogin() {
      if (this.wxLoggingIn) return;
      if (!this.agreed) { uni.showToast({ title: "请先同意用户协议", icon: "none" }); return; }
      this.wxLoggingIn = true;
      // #ifdef MP-WEIXIN
      uni.login({ provider: "weixin", success: (loginRes) => {
        const code = loginRes.code;
        if (!code) { uni.showToast({ title: "微信授权失败", icon: "none" }); this.wxLoggingIn = false; return; }
        authApi.login({ authType: "wechat", code }).then((res) => { this.doLogin(res); }).catch((err) => { uni.showToast({ title: (err && err.message) || "微信登录失败", icon: "none" }); }).finally(() => { this.wxLoggingIn = false; });
      }, fail: () => { uni.showToast({ title: "微信登录失败", icon: "none" }); this.wxLoggingIn = false; } });
      // #endif
      // #ifndef MP-WEIXIN
      uni.showToast({ title: "请在微信小程序中使用", icon: "none" }); this.wxLoggingIn = false;
      // #endif
    },
    goFaceLogin() {
      if (!this.agreed) { uni.showToast({ title: "请先同意用户协议", icon: "none" }); return; }
      this._pendingFaceLogin = true;
      uni.navigateTo({ url: "/subpages/login/face_capture" });
    }
  }
};
</script>

<style scoped>
.page{min-height:100vh;padding:0 40rpx 60rpx;background:#faf7ef;box-sizing:border-box}
.tab-bar{display:flex;align-items:center;justify-content:space-around;padding:30rpx 0 0;overflow-x:auto;white-space:nowrap}
.tab-item{position:relative;padding:12rpx 16rpx 16rpx;display:flex;flex-direction:column;align-items:center}
.tab-label{font-size:28rpx;color:#8f7366;font-weight:600;transition:color 180ms}
.tab-item.active .tab-label{color:#e8927c;font-weight:800}
.tab-bar-active{position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:32rpx;height:6rpx;border-radius:3rpx;background:#e8927c}
.tab-body{padding-top:48rpx}
.input-group{display:flex;flex-direction:column;gap:20rpx}
.input-row{display:flex;align-items:center;background:#fff;border-radius:16rpx;padding:0 24rpx;height:96rpx;box-shadow:0 2rpx 12rpx rgba(140,104,83,.06)}
.input-icon{font-size:34rpx;width:56rpx;text-align:center;flex-shrink:0}
.input-field{flex:1;font-size:28rpx;color:#2d1f18;height:100%}
.code-field{max-width:220rpx}
.input-suffix{flex-shrink:0;font-size:24rpx;color:#8f7366;padding:8rpx 12rpx}
.send-code{color:#e8927c;font-weight:700;padding:8rpx 20rpx;background:rgba(232,146,124,.1);border-radius:12rpx}
.send-code.disabled{color:#c4b8ad;background:#f0ebe5}
.wechat-login-area,.face-login-area{display:flex;flex-direction:column;align-items:center;padding:60rpx 0}
.wechat-icon-wrap,.face-icon-wrap{width:120rpx;height:120rpx;border-radius:50%;display:flex;align-items:center;justify-content:center;margin-bottom:28rpx}
.wechat-icon-wrap{background:rgba(7,193,96,.12)}.face-icon-wrap{background:rgba(232,146,124,.12)}
.wechat-icon,.face-icon{font-size:56rpx}
.wechat-title,.face-title{font-size:32rpx;font-weight:700;color:#2d1f18;margin-bottom:12rpx}
.wechat-desc,.face-desc{font-size:24rpx;color:#8f7366;margin-bottom:40rpx}
.wechat-btn{width:480rpx;height:88rpx;display:flex;align-items:center;justify-content:center;padding:0;border-radius:44rpx;background:#07c160;color:#fff;font-size:30rpx;font-weight:700;border:none;box-shadow:0 4rpx 16rpx rgba(7,193,96,.25)}
.face-btn{width:480rpx;height:88rpx;display:flex;align-items:center;justify-content:center;padding:0;border-radius:44rpx;background:#e8927c;color:#fff;font-size:30rpx;font-weight:700;border:none;box-shadow:0 4rpx 16rpx rgba(232,146,124,.25)}
.wechat-btn::after,.face-btn::after{border:none}
.agreement-row{display:flex;align-items:flex-start;padding:36rpx 0 8rpx;gap:12rpx}
.agreement-check{flex-shrink:0;margin-top:4rpx}
.check-dot{width:32rpx;height:32rpx;border-radius:50%;border:3rpx solid #c4b8ad;transition:all 180ms}
.check-dot.on{border-color:#e8927c;background:#e8927c;position:relative}
.check-dot.on::after{content:"";position:absolute;left:8rpx;top:4rpx;width:10rpx;height:16rpx;border:3rpx solid #fff;border-top:none;border-left:none;transform:rotate(45deg)}
.agreement-text{font-size:23rpx;color:#8f7366;line-height:1.5}
.agreement-link{color:#e8927c;font-weight:600}
.submit-area{padding-top:48rpx}
.submit-btn{width:100%;height:96rpx;display:flex;align-items:center;justify-content:center;padding:0;border-radius:48rpx;background:#e8927c;color:#fff;font-size:32rpx;font-weight:700;border:none;box-shadow:0 6rpx 20rpx rgba(232,146,124,.3)}
.submit-btn::after{border:none}
.submit-btn.disabled{opacity:.6}
.extra-links{display:flex;justify-content:center;gap:36rpx;padding-top:32rpx}
.extra-link{font-size:24rpx;color:#8f7366;text-decoration:underline}
</style>
