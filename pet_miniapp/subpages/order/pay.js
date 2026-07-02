var orderApi = require("../../utils/api/order");

Page({
  data: {
    orderId: "",
    orderNo: "",
    amount: "0.00",
    payMethod: "wechat"
  },

  onLoad: function(options) {
    this.setData({
      orderId: options.orderId || "",
      orderNo: options.orderNo || "",
      amount: options.amount || "0.00"
    });
  },

  selectMethod: function(e) {
    this.setData({ payMethod: e.currentTarget.dataset.method });
  },

  confirmPay: function() {
    var that = this;
    if (!this.data.orderNo) {
      wx.showToast({ title: "订单信息异常", icon: "none" });
      return;
    }
    wx.showLoading({ title: "支付中...", mask: true });

    setTimeout(function() {
      orderApi.pay(that.data.orderNo).then(function() {
        wx.hideLoading();
        wx.showToast({ title: "支付成功", icon: "success" });
        setTimeout(function() {
          wx.redirectTo({ url: "/subpages/order/list?tab=1" });
        }, 1500);
      }).catch(function(err) {
        wx.hideLoading();
        wx.showToast({ title: (err && err.message) || "支付失败，请重试", icon: "none" });
      });
    }, 1000);
  }
});
