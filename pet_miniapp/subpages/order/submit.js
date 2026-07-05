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
      console.error("load address fail", err);
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
      console.error("load cart fail", err);
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
    var that = this;
    if (!this.data.address) {
      wx.showToast({ title: "请选择收货地址", icon: "none" });
      return;
    }
    if (this.data.items.length === 0) {
      wx.showToast({ title: "未选择商品", icon: "none" });
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
    var amount = that.data.totalAmount;
    orderApi.create(dto).then(function(res) {
      wx.hideLoading();
      var oid = res.orderId || res.id || res;
      wx.showToast({ title: "下单成功", icon: "success" });
      orderApi.detail(oid).then(function(order) {
        wx.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=" + (order.orderNo || "") + "&amount=" + (order.payAmount || amount) });
      }).catch(function() {
        wx.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=&amount=" + amount });
      });
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || "下单失败", icon: "none" });
    });
  }
});