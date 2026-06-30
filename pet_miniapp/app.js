App({
  globalData: {
    baseUrl: "http://192.168.123.234:8080",
    // baseUrl: "http://127.0.0.1:8080",
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
