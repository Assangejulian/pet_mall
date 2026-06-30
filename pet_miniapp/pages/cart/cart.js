var app = getApp();
var cartApi = require("../../utils/api/cart");

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
    var that = this;
    if (!this.data.isLogin) return;
    cartApi.list().then(function(res) {
      // res is a list of CartVO objects
      var cartItems = (res || []).map(function(item) {
        return {
          id: item.id,
          productId: item.productId,
          name: item.productInfo ? (item.productInfo.name || item.productInfo.productName) : "未知商品",
          price: item.productInfo ? item.productInfo.price : "0.00",
          image: item.productInfo ? (item.productInfo.image || item.productInfo.mainImage) : "",
          quantity: item.quantity,
          checked: item.checked === 1 || item.checked === true
        };
      });
      that.setData({ cart: cartItems });
      that.calc();
    });
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
    var item = this.data.cart.find(function(i) { return i.id === id; });
    if (!item) return;
    var newChecked = item.checked ? 0 : 1;
    var that = this;
    cartApi.update(id, { checked: newChecked }).then(function() {
      that.load();
    });
  },

  toggleAll: function () {
    var ac = !this.data.allChecked ? 1 : 0;
    var that = this;
    var promises = this.data.cart.map(function(i) {
      if ((i.checked ? 1 : 0) !== ac) {
        return cartApi.update(i.id, { checked: ac });
      }
    }).filter(Boolean);
    Promise.all(promises).then(function() {
      that.load();
    });
  },

  inc: function (e) {
    var id = e.currentTarget.dataset.id;
    var item = this.data.cart.find(function(i) { return i.id === id; });
    if (!item) return;
    var that = this;
    wx.showLoading({ title: '加载中', mask: true });
    cartApi.update(id, { quantity: item.quantity + 1 }).then(function() {
      wx.hideLoading();
      that.load();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || '更新失败', icon: 'none' });
    });
  },

  dec: function (e) {
    var id = e.currentTarget.dataset.id;
    var item = this.data.cart.find(function(i) { return i.id === id; });
    if (!item) return;
    if (item.quantity <= 1) {
      wx.showToast({ title: '数量不能少于1', icon: 'none' });
      return;
    }
    var that = this;
    wx.showLoading({ title: '加载中', mask: true });
    cartApi.update(id, { quantity: item.quantity - 1 }).then(function() {
      wx.hideLoading();
      that.load();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || '更新失败', icon: 'none' });
    });
  },

  del: function (e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "移除",
      content: "确定移除该商品？",
      success: function (r) {
        if (r.confirm) {
          cartApi.remove(id).then(function() {
            that.load();
          });
        }
      }
    });
  },

  goLogin: function () {
    wx.navigateTo({ url: "/subpages/login/login" });
  },

  checkout: function () {
    if (!app.requireAuth()) return;
    if (this.data.checkedCount === 0) {
      wx.showToast({ title: "请选择要结算的商品", icon: "none" });
      return;
    }
    app.globalData.cart = this.data.cart;
    wx.navigateTo({ url: "/subpages/order/submit" });
  },

  goHome: function () {
    wx.switchTab({ url: "/pages/index/index" });
  }
});
