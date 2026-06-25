App({
  globalData: {
    baseUrl: "http://192.168.123.234:8080",
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
    return this.globalData.cart.reduce((sum, item) => sum + item.quantity, 0);
  },

  addToCart(product) {
    const cart = this.globalData.cart;
    const index = cart.findIndex((item) => item.id === product.id);
    if (index > -1) {
      cart[index].quantity += 1;
    } else {
      cart.push({
        id: product.id,
        name: product.name,
        price: product.price,
        image: product.image,
        quantity: 1,
        checked: true
      });
    }
    this.globalData.cart = cart;
    wx.setStorageSync("cart", cart);
  },

  updateCart(cart) {
    this.globalData.cart = cart;
    wx.setStorageSync("cart", cart);
  }
});
