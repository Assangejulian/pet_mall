var orderApi = require("../../utils/api/order");

Page({
  data: {
    order: {},
    sl: {
      "0": "待支付", "1": "已支付", "2": "已发货",
      "3": "已收货", "4": "已评价", "-1": "已取消", "-2": "退单中"
    },
    sbg: {
      "0": "#fdf2ed", "1": "#eef4f0", "2": "#eef4f0", "3": "#eef4f0",
      "-1": "#f5f5f5", "-2": "#fff3f0"
    },
    sic: {
      "0": "○", "1": "◐", "2": "◑", "3": "●", "4": "★",
      "-1": "✕", "-2": "↩"
    }
  },

  onLoad: function(options) {
    var that = this;
    var id = options.id;
    if (!id) return;
    orderApi.detail(id).then(function(res) {
      that.setData({ order: res || {} });
    }).catch(function() {
      that.setData({ order: {} });
    });
  },

  payOrder: function () {
    var that = this;
    var order = this.data.order;
    if (!order || !order.orderNo) return;
    wx.showLoading({ title: "支付中..." });
    orderApi.pay(order.orderNo).then(function () {
      wx.hideLoading();
      wx.showToast({ title: "支付成功", icon: "success" });
      that.onLoad({ id: order.id });
    }).catch(function () {
      wx.hideLoading();
      wx.showToast({ title: "支付失败", icon: "none" });
    });
  },

  confirmReceive: function () {
    var that = this;
    var order = this.data.order;
    if (!order || !order.id) return;
    wx.showModal({
      title: "确认收货",
      content: "确定已收到商品吗？",
      success: function (r) {
        if (r.confirm) {
          wx.showLoading({ title: "处理中..." });
          orderApi.receive(order.id).then(function () {
            wx.hideLoading();
            wx.showToast({ title: "已确认收货", icon: "success" });
            that.onLoad({ id: order.id });
          }).catch(function () {
            wx.hideLoading();
            wx.showToast({ title: "操作失败", icon: "none" });
          });
        }
      }
    });
  },

  callService: function () {
    wx.showToast({ title: "客服电话: 400-000-0000", icon: "none" });
  }
});
