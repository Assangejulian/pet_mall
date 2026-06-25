var app = getApp();
var authApi = require("../../utils/api/auth");

Page({
  data: {
    curTab: 0,
    phone: "",
    password: "",
    showPassword: false,
    email: "",
    code: "",
    codeCountdown: 0,
    canSendCode: true,
    agreed: false,
    tabs: [
      { id: 0, label: "密码登录" },
      { id: 1, label: "邮箱登录" },
      { id: 2, label: "微信登录" },
      { id: 3, label: "人脸登录" }
    ]
  },

  swTab: function (e) {
    this.setData({ curTab: e.currentTarget.dataset.id });
  },

  onPhoneInput: function (e) { this.setData({ phone: e.detail.value }); },
  onPasswordInput: function (e) { this.setData({ password: e.detail.value }); },
  togglePassword: function () { this.setData({ showPassword: !this.data.showPassword }); },
  onEmailInput: function (e) { this.setData({ email: e.detail.value }); },
  onCodeInput: function (e) { this.setData({ code: e.detail.value }); },

  onSendCode: function () {
    var email = this.data.email;
    if (!email || email.indexOf("@") === -1) {
      wx.showToast({ title: "请输入正确的邮箱", icon: "none" });
      return;
    }
    this.setData({ codeCountdown: 60, canSendCode: false });
    var t = this;
    var i = setInterval(function () {
      var c = t.data.codeCountdown - 1;
      if (c <= 0) { clearInterval(i); t.setData({ codeCountdown: 0, canSendCode: true }); }
      else { t.setData({ codeCountdown: c }); }
    }, 1000);

    authApi.sendEmailCode(email)
      .then(function () { wx.showToast({ title: "验证码已发送到邮箱", icon: "success" }); })
      .catch(function (err) {
        clearInterval(i);
        t.setData({ codeCountdown: 0, canSendCode: true });
        wx.showToast({ title: err.message || "发送失败", icon: "none" });
      });
  },

  toggleAgreed: function () { this.setData({ agreed: !this.data.agreed }); },

  onPasswordLogin: function () {
    if (!this.checkAgreed()) return;
    var p = this.data.phone;
    if (!p || p.length !== 11) { wx.showToast({ title: "请输入正确的手机号", icon: "none" }); return; }
    if (!this.data.password || this.data.password.length < 6) { wx.showToast({ title: "密码至少6位", icon: "none" }); return; }
    var t = this;
    wx.showLoading({ title: "登录中..." });
    authApi.login({ authType: "password", phone: p, username: p, password: this.data.password })
      .then(function (r) { wx.hideLoading(); t.doLogin(r); })
      .catch(function (err) { wx.hideLoading(); wx.showToast({ title: err.message || "登录失败", icon: "none" }); });
  },

  onEmailLogin: function () {
    if (!this.checkAgreed()) return;
    var email = this.data.email;
    if (!email || email.indexOf("@") === -1) { wx.showToast({ title: "请输入正确的邮箱", icon: "none" }); return; }
    if (!this.data.code || this.data.code.length < 4) { wx.showToast({ title: "请输入验证码", icon: "none" }); return; }
    var t = this;
    wx.showLoading({ title: "登录中..." });
    authApi.login({ authType: "email_code", email: email, code: this.data.code })
      .then(function (r) { wx.hideLoading(); t.doLogin(r); })
      .catch(function (err) { wx.hideLoading(); wx.showToast({ title: err.message || "登录失败", icon: "none" }); });
  },

  onWechatLogin: function () {
    if (!this.checkAgreed()) return;
    var t = this;
    wx.showLoading({ title: "微信授权中..." });
    wx.login({
      success: function (res) {
        wx.hideLoading();
        if (res.code) {
          wx.showLoading({ title: "登录中..." });
          authApi.login({ authType: "wechat", wxCode: res.code })
            .then(function (r) { wx.hideLoading(); t.doLogin(r); })
            .catch(function (err) { wx.hideLoading(); wx.showToast({ title: err.message || "微信登录失败", icon: "none" }); });
        } else {
          wx.showToast({ title: "微信授权失败", icon: "none" });
        }
      },
      fail: function () { wx.hideLoading(); wx.showToast({ title: "微信登录失败", icon: "none" }); }
    });
  },

  onFaceLogin: function () {
    if (!this.checkAgreed()) return;
    var t = this;
    wx.showModal({
      title: "人脸识别",
      content: "请将面部对准屏幕中央，保持光线充足",
      confirmText: "开始识别",
      cancelText: "取消",
      success: function (r) {
        if (r.confirm) {
          wx.showLoading({ title: "识别中..." });
          setTimeout(function () {
            wx.hideLoading();
            wx.showLoading({ title: "登录中..." });
            authApi.login({ authType: "face", faceToken: "face_" + Date.now() })
              .then(function (r) { wx.hideLoading(); t.doLogin(r); })
              .catch(function (err) { wx.hideLoading(); wx.showToast({ title: err.message || "人脸登录失败", icon: "none" }); });
          }, 1500);
        }
      }
    });
  },

  doLogin: function (r) {
    if (!r || !r.token) {
      wx.showToast({ title: "登录异常，请重试", icon: "none" });
      return;
    }
    app.globalData.token = r.token;
    app.globalData.user = { id: r.userId, name: r.username };
    wx.setStorageSync("token", r.token);
    if (r.userId) wx.setStorageSync("userId", r.userId);
    wx.showToast({ title: "登录成功", icon: "success", duration: 1200 });
    setTimeout(function () {
      if (getCurrentPages().length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({ url: "/pages/user/user" });
      }
    }, 1300);
  },

  checkAgreed: function () {
    if (!this.data.agreed) { wx.showToast({ title: "请先阅读并同意用户协议", icon: "none" }); return false; }
    return true;
  },

  onForgotPassword: function () {
    wx.showToast({ title: "请联系客服重置密码", icon: "none" });
  }
});