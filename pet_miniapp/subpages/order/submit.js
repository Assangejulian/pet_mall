var orderApi = require("../../utils/api/order");
var cartApi = require("../../utils/api/cart");
var userApi = require("../../utils/api/user");

Page({
  data: {
    items: [],
    totalAmount: "0.00",
    address: null,
    remark: "",
    discount: null,
    payAmount: "0.00"
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
      // load discount after items are ready
      t.loadUserDiscount();
    }).catch(function(err) {
      console.error("load cart fail", err);
    });
  },

  loadUserDiscount: function() {
    var t = this;
    userApi.getProfile().then(function(user) {
      if (user && user.memberLevel && user.memberLevel > 0) {
        var total = parseFloat(t.data.totalAmount);
        var rates = [1, 0.95, 0.9, 0.85];
        var rate = rates[user.memberLevel] || 1;
        var names = ["", "\u94f6\u5361\u4f1a\u5458", "\u91d1\u5361\u4f1a\u5458", "\u94bb\u77f3\u4f1a\u5458"];
        var descs = ["", "95\u6298", "9\u6298", "85\u6298"];
        var pay = (total * rate).toFixed(2);
        var disc = (total - parseFloat(pay)).toFixed(2);
        t.setData({
          discount: {
            levelName: names[user.memberLevel] || "\u4f1a\u5458",
            desc: descs[user.memberLevel] || ""
          },
          payAmount: pay,
          discountAmount: disc
        });
      } else {
        t.setData({ payAmount: t.data.totalAmount });
      }
    }).catch(function() {
      t.setData({ payAmount: t.data.totalAmount });
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
      wx.showToast({ title: "\u8bf7\u9009\u62e9\u6536\u8d27\u5730\u5740", icon: "none" });
      return;
    }
    if (this.data.items.length === 0) {
      wx.showToast({ title: "\u672a\u9009\u62e9\u5546\u54c1", icon: "none" });
      return;
    }
    wx.showLoading({ title: "\u63d0\u4ea4\u4e2d..." });
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
    var amount = that.data.payAmount || that.data.totalAmount;
    orderApi.create(dto).then(function(res) {
      wx.hideLoading();
      var oid = res.orderId || res.id || res;
      wx.showToast({ title: "\u4e0b\u5355\u6210\u529f", icon: "success" });
      orderApi.detail(oid).then(function(order) {
        wx.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=" + (order.orderNo || "") + "&amount=" + (order.payAmount || amount) });
      }).catch(function() {
        wx.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=&amount=" + amount });
      });
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || "\u4e0b\u5355\u5931\u8d25", icon: "none" });
    });
  }
});
