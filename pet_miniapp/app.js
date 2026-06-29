App({
  globalData: {
    baseUrl: "http://127.0.0.1:8080",
    cart: [],
    user: null,
    token: ""
  },

  onLaunch() {
    const cart = wx.getStorageSync("cart") || [];
    const token = wx.getStorageSync("token") || "";
    this.globalData.cart = cart;
    this.globalData.token = token;
  },

  /** 检查登录态，未登录则跳转登录页 */
  requireAuth() {
    if (this.globalData.token) return true;
    wx.navigateTo({ url: "/subpages/login/login" });
    return false;
  },

  getCartCount() {
    // This could optionally fetch from backend or be removed
    return 0;
  },

  addToCart(product) {
    // Legacy local cache method - removed in favor of backend API
    console.log("addToCart deprecated, use cartApi.add directly");
  },

  updateCart(cart) {
    // Legacy local cache method - removed in favor of backend API
    console.log("updateCart deprecated, use cartApi.update directly");
  }
});
