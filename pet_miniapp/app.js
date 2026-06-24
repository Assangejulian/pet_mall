App({
  globalData: { baseUrl: "http://localhost:8080", cart: [], user: null, token: "" },
  onLaunch() {
    var cart = wx.getStorageSync("cart") || [];
    this.globalData.cart = cart;
    var token = wx.getStorageSync("token") || "";
    this.globalData.token = token;
  },
  getCartCount() { return this.globalData.cart.reduce(function(s,i){ return s+i.quantity; }, 0); },
  addToCart(p) {
    var cart = this.globalData.cart;
    var idx = cart.findIndex(function(i){ return i.id === p.id; });
    if (idx > -1) cart[idx].quantity += 1;
    else cart.push({ id: p.id, name: p.name, price: p.price, image: p.image, quantity: 1, checked: true });
    this.globalData.cart = cart;
    wx.setStorageSync("cart", cart);
  },
  updateCart(cart) { this.globalData.cart = cart; wx.setStorageSync("cart", cart); }
});
