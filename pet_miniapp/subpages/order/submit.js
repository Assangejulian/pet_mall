var orderApi = require("../../utils/api/order");
var cartApi = require("../../utils/api/cart");
var request = require("../../utils/request");

Page({
  data: {
    items: [],
    totalAmount: '0.00',
    address: null
  },

  onShow: function() {
    this.loadData();
  },

  loadData: function() {
    var t = this;
    request.get("/api/user/address/list").then(function(res) {
      if (res && res.length > 0) {
        var defaultAddr = res.find(function(a) { return a.isDefault === 1; }) || res[0];
        t.setData({ address: defaultAddr });
      }
    });

    // Check if we have checkout items from storage
    var checkoutItems = wx.getStorageSync("checkout_items");
    if (checkoutItems && checkoutItems.length > 0) {
      var total = 0;
      checkoutItems.forEach(function(i) {
        total += parseFloat(i.productInfo ? i.productInfo.price : 0) * i.quantity;
      });
      t.setData({ items: checkoutItems, totalAmount: total.toFixed(2) });
    } else {
      cartApi.list().then(function(res) {
        var checkedItems = (res || []).filter(function(i) { return i.checked === 1 || i.checked === true; });
        var total = 0;
        checkedItems.forEach(function(i) {
          total += parseFloat(i.productInfo ? i.productInfo.price : 0) * i.quantity;
        });
        t.setData({ items: checkedItems, totalAmount: total.toFixed(2) });
      });
    }
  },

  submitOrder: function() {
    if (!this.data.address) {
      wx.showToast({ title: '请先添加收货地址', icon: 'none' });
      return;
    }
    if (this.data.items.length === 0) {
      wx.showToast({ title: '订单为空', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中' });
    var dto = {
      addressId: this.data.address.id,
      cartIds: this.data.items.map(function(i) { return i.id; })
    };
    orderApi.create(dto).then(function(res) {
      wx.hideLoading();
      wx.removeStorageSync("checkout_items");
      wx.showToast({ title: '下单成功', icon: 'success' });
      setTimeout(function() {
        wx.redirectTo({ url: '/subpages/order/list' });
      }, 1500);
    }).catch(function() {
      wx.hideLoading();
    });
  }
});
