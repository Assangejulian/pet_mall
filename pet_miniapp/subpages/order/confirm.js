var app = getApp();
var orderApi = require("../../utils/api/order");
var userApi = require("../../utils/api/user");

Page({
  data: {
    items: [],
    address: null,
    total: "0.00",
    remark: ""
  },

  onLoad: function () {
    // 从购物车取已勾选商品
    var cart = app.globalData.cart || [];
    var checked = cart.filter(function (i) { return i.checked; });
    if (!checked.length) {
      wx.showToast({ title: "没有要结算的商品", icon: "none" });
      wx.navigateBack();
      return;
    }
    this.setData({ items: checked });
    this.calcTotal(checked);
    this.loadDefaultAddress();
  },

  calcTotal: function (items) {
    var total = 0;
    items.forEach(function (i) { total += parseFloat(i.price) * i.quantity; });
    this.setData({ total: total.toFixed(2) });
  },

  loadDefaultAddress: function () {
    var that = this;
    userApi.defaultAddress().then(function (res) {
      if (res) that.setData({ address: res });
    }).catch(function () {});
  },

  selectAddress: function () {
    var that = this;
    wx.navigateTo({ url: "/subpages/address/list?select=1" });
  },

  // 地址选择页返回时带回选中地址
  onAddressSelected: function (addr) {
    this.setData({ address: addr });
  },

  onRemarkInput: function (e) {
    this.setData({ remark: e.detail.value });
  },

  submitOrder: function () {
    var that = this;
    if (!this.data.address) {
      wx.showToast({ title: "请选择收货地址", icon: "none" });
      return;
    }

    wx.showLoading({ title: "提交中..." });
    var items = this.data.items.map(function (i) {
      return { productId: i.id, quantity: i.quantity };
    });

    orderApi.create({
      addressId: that.data.address.id,
      items: items,
      remark: that.data.remark
    }).then(function (res) {
      wx.hideLoading();
      // 清购物车中已购商品
      var cart = app.globalData.cart.filter(function (c) { return !c.checked; });
      app.globalData.cart = cart;
      wx.setStorageSync("cart", cart);

      wx.showToast({ title: "下单成功", icon: "success" });
      setTimeout(function () {
        wx.redirectTo({ url: "/subpages/order/detail/detail?id=" + res });
      }, 1500);
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: err && err.message || "下单失败", icon: "none" });
    });
  }
});
