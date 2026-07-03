App({
  globalData: {
    baseUrl: "http://127.0.0.1:8080",
    // 真机调试时改为电脑局域网 IP，模拟器用 127.0.0.1
    // baseUrl: "http://10.22.146.238:8080",
    user: null,
    token: ""
  },

  onLaunch() {
    const token = wx.getStorageSync("token") || "";
    this.globalData.token = token;
  },

  /** 检查登录态，未登录则跳转登录页 */
  requireAuth() {
    if (this.globalData.token) return true;
    wx.navigateTo({ url: "/subpages/login/login" });
    return false;
  }
});
