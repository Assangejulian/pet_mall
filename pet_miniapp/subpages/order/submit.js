var orderApi = require("../../utils/api/order");
var cartApi = require("../../utils/api/cart");
var userApi = require("../../utils/api/user");

Page({
  data: {
    items: [],
    totalAmount: "0.00",
    address: null,
    remark: ""
  },

  onShow: function() {
    this.loadAddress();
    this.loadItems();
  },

  loadAddress: function() {
    var t = this;
    userApi.addressList().then(function(res) {
      var list = Array.isArray(res) ? res : [];
      var defaultAddr = list.find(function(a) { return a.defaulted === 1; }) || list[0] || null;
      t.setData({ address: defaultAddr });
    }).catch(function(err) {
      console.error("加载地址失败", err);
    });
  },

  loadItems: function() {
    var t = this;
    cartApi.list().then(function(res) {
      var checkedItems = (res || []).filter(function(i) {
        return i.checked === 1 || i.checked === true;
      });
      var total = 0;
      checkedItems.forEach(function(i) {
        var product = i.productInfo || {};
        var price = parseFloat(product.price || i.price || 0);
        total += price * i.quantity;
      });
      t.setData({ items: checkedItems, totalAmount: total.toFixed(2) });
    }).catch(function(err) {
      console.error("加载购物车失败", err);
    });
  },

  goAddAddress: function() {
    wx.navigateTo({ url: "/subpages/address/list?select=1" });
  },

  onAddressSelected: function(addr) {
    this.setData({ address: addr });
  },

  onRemarkInput: function(e) {
    this.setData({ remark: e.detail.value });
  },

  submitOrder: function() {
    if (!this.data.address) {
      wx.showToast({ title: "请选择收货地址", icon: "none" });
      return;
    }
    if (this.data.items.length === 0) {
      wx.showToast({ title: "没有选中的商品", icon: "none" });
      return;
    }
    wx.showLoading({ title: "提交中..." });
    var dto = {
      addressId: this.data.address.id,
      items: this.data.items.map(function(i) {
        return {
          productId: i.productId || (i.productInfo && i.productInfo.id) || i.id,
          quantity: i.quantity
        };
      }),
      remark: this.data.remark
    };
    orderApi.create(dto).then(function(res) {
      wx.hideLoading();
      wx.showToast({ title: "下单成功", icon: "success" });
      setTimeout(function() {
        wx.redirectTo({ url: "/subpages/order/detail/detail?id=" + (res.orderId || res.id || res) });
      }, 1000);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || "下单失败，请重试", icon: "none" });
    });
  }
});
