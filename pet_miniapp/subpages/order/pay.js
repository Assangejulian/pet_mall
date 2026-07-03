var orderApi = require("../../utils/api/order");

Page({
  data: {
    orderId: "",
    orderNo: "",
    amount: "0.00",
    payMethod: "WECHAT",
    paying: false
  },

  onLoad: function(options) {
    this.setData({
      orderId: options.orderId || "",
      orderNo: options.orderNo || "",
      amount: options.amount || "0.00"
    });
  },

  confirmPay: function() {
    var that = this;
    if (!this.data.orderNo) {
      wx.showToast({ title: "订单信息异常", icon: "none" });
      return;
    }
    if (this.data.paying) return;
    that.setData({ paying: true });
    wx.showLoading({ title: "支付中...", mask: true });

    orderApi.pay(that.data.orderNo, that.data.payMethod).then(function(res) {
      wx.hideLoading();

      // 微信小程序支付：调用 wx.requestPayment
      if (that.data.payMethod === "WECHAT" && res && res.payUrl) {
        var params;
        try { params = JSON.parse(res.payUrl); } catch (e) { params = null; }
        if (params && params.paySign) {
          wx.requestPayment({
            timeStamp: params.timeStamp,
            nonceStr: params.nonceStr,
            package: params.package,
            signType: params.signType || "RSA",
            paySign: params.paySign,
            success: function() {
              wx.showToast({ title: "支付成功", icon: "success" });
              setTimeout(function() {
                wx.redirectTo({ url: "/subpages/order/list?tab=1" });
              }, 1500);
            },
            fail: function(err) {
              wx.showToast({ title: (err && err.errMsg) || "支付取消", icon: "none" });
              that.setData({ paying: false });
            }
          });
          return;
        }
      }

      // Mock / Alipay：直接跳转成功
      wx.showToast({ title: "支付成功", icon: "success" });
      setTimeout(function() {
        wx.redirectTo({ url: "/subpages/order/list?tab=1" });
      }, 1500);
    }).catch(function(err) {
      wx.hideLoading();
      that.setData({ paying: false });
      wx.showToast({ title: (err && err.message) || "支付失败，请重试", icon: "none" });
    });
  }
});
