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
    var token = wx.getStorageSync("token") || "";
    this.setData({ isLogin: !!token });
    this.load();
  },

  load: function () {
    var cart = wx.getStorageSync("cart") || [];
    this.setData({ cart: cart });
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

  save: function () {
    wx.setStorageSync("cart", this.data.cart);
    app.globalData.cart = this.data.cart;
  },

  toggleCheck: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart;
    for (var i = 0; i < cart.length; i++) {
      if (cart[i].id === id) {
        cart[i].checked = !cart[i].checked;
        break;
      }
    }
    this.setData({ cart: cart });
    this.save();
    this.calc();
  },

  toggleAll: function () {
    var ac = !this.data.allChecked;
    var cart = this.data.cart;
    for (var i = 0; i < cart.length; i++) {
      cart[i].checked = ac;
    }
    this.setData({ cart: cart });
    this.save();
    this.calc();
  },

  inc: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart;
    for (var i = 0; i < cart.length; i++) {
      if (cart[i].id === id) {
        cart[i].quantity += 1;
        break;
      }
    }
    this.setData({ cart: cart });
    this.save();
    this.calc();
  },

  dec: function (e) {
    var id = e.currentTarget.dataset.id;
    var cart = this.data.cart;
    for (var i = 0; i < cart.length; i++) {
      if (cart[i].id === id) {
        if (cart[i].quantity <= 1) {
          wx.showToast({ title: "数量不能少于1", icon: "none" });
          return;
        }
        cart[i].quantity -= 1;
        break;
      }
    }
    this.setData({ cart: cart });
    this.save();
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
          var cart = that.data.cart.filter(function(i) { return i.id !== id; });
          that.setData({ cart: cart });
          that.save();
          that.calc();
        }
      }
    });
  },

  goLogin: function () {
    wx.navigateTo({ url: "/subpages/login/login" });
  },

  checkout: function () {
    if (this.data.checkedCount === 0) {
      wx.showToast({ title: "请选择要结算的商品", icon: "none" });
      return;
    }
    wx.navigateTo({ url: "/subpages/order/submit" });
  },

  goHome: function () {
    wx.switchTab({ url: "/pages/index/index" });
  }
});