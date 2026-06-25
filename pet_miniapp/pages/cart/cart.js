var app = getApp();

Page({
  data: {
    cart: [],
    allChecked: true,
    totalPrice: "0.00",
    checkedCount: 0,
    isLogin: false
  },

  onShow: function () {
    this.setData({ isLogin: !!app.globalData.token });
    this.load();
  },

  load: function () {
    this.setData({ cart: app.globalData.cart });
    this.calc();
  },

  calc: function () {
    var cart = this.data.cart;
    var total = 0;
    var cc = 0;
    var ac = cart.length > 0;
    cart.forEach(function (i) {
      if (i.checked) { total += parseFloat(i.price) * i.quantity; cc += i.quantity; }
      else { ac = false; }
    });
    this.setData({ totalPrice: total.toFixed(2), checkedCount: cc, allChecked: ac });
  },

  toggleCheck: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart.map(function (i) {
      if (i.id === id) i.checked = !i.checked;
      return i;
    });
    this.setData({ cart: cart });
    app.updateCart(cart);
    this.calc();
  },

  toggleAll: function () {
    var ac = !this.data.allChecked;
    var cart = this.data.cart.map(function (i) { i.checked = ac; return i; });
    this.setData({ cart: cart, allChecked: ac });
    app.updateCart(cart);
    this.calc();
  },

  inc: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart.map(function (i) {
      if (i.id === id) i.quantity += 1;
      return i;
    });
    this.setData({ cart: cart });
    app.updateCart(cart);
    this.calc();
  },

  dec: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart.map(function (i) {
      if (i.id === id && i.quantity > 1) i.quantity -= 1;
      return i;
    });
    this.setData({ cart: cart });
    app.updateCart(cart);
    this.calc();
  },

  del: function (e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "移除",
      content: "确定移除该商品？",
      success: function (r) {
        if (r.confirm) {
          var cart = that.data.cart.filter(function (i) { return i.id !== id; });
          that.setData({ cart: cart });
          app.updateCart(cart);
          that.calc();
        }
      }
    });
  },

  goLogin: function () {
    wx.navigateTo({ url: "/subpages/login/login" });
  },

  checkout: function () {
    if (!app.requireAuth()) return;
    var checked = this.data.cart.filter(function (i) { return i.checked; });
    if (!checked.length) {
      wx.showToast({ title: "请选择要结算的商品", icon: "none" });
      return;
    }
    wx.navigateTo({ url: "/subpages/order/confirm" });
  },

  goHome: function () {
    wx.switchTab({ url: "/pages/index/index" });
  }
});
