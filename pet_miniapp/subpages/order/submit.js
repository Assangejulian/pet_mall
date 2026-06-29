var orderApi = require("../../utils/api/order");
var cartApi = require("../../utils/api/cart");
var userApi = require("../../utils/api/user");

Page({
  data: {
    items: [],
    totalAmount: '0.00',
    address: null
  },

  onShow: function() {
    this.loadAddress();
    this.loadItems();
  },

  loadAddress: function() {
    var t = this;
    userApi.addressList().then(function(res) {
      var list = Array.isArray(res) ? res : [];
      if (list.length > 0) {
        var defaultAddr = list.find(function(a) { return a.defaulted === 1; }) || list[0];
        t.setData({ address: defaultAddr });
      } else {
        t.setData({ address: null });
      }
    }).catch(function(err) {
      console.error("加载地址失败", err);
    });
  },

  loadItems: function() {
    var t = this;
    cartApi.list().then(function(res) {
      var checkedItems = (res || []).filter(function(i) { return i.checked === 1 || i.checked === true; });
      var total = 0;
      checkedItems.forEach(function(i) {
        var price = i.productInfo ? parseFloat(i.productInfo.price || 0) : parseFloat(i.price || 0);
        total += price * i.quantity;
      });
      t.setData({ items: checkedItems, totalAmount: total.toFixed(2) });
    }).catch(function(err) {
      console.error("加载购物车失败", err);
    });
  },

  goAddAddress: function() {
    wx.navigateTo({ url: '/subpages/address/list' });
  },

  submitOrder: function() {
    if (!this.data.address) {
      wx.showToast({ title: '请先添加收货地址', icon: 'none' });
      return;
    }
    if (this.data.items.length === 0) {
      wx.showToast({ title: '没有选中的商品', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '提交中' });
    var dto = {
      addressId: this.data.address.id,
      cartIds: this.data.items.map(function(i) { return i.id; })
    };
    orderApi.create(dto).then(function(res) {
      wx.hideLoading();
      wx.showToast({ title: '下单成功', icon: 'success' });
      // Redirect to payment page instead of order list
      setTimeout(function() {
        wx.redirectTo({ url: '/subpages/order/pay?orderId=' + res.id + '&amount=' + res.payAmount + '&orderNo=' + res.orderNo });
      }, 1000);
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || '下单失败，请重试', icon: 'none' });
    });
  }
});
