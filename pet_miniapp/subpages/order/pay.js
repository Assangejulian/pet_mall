var orderApi = require("../../utils/api/order");

Page({
  data: {
    orderId: "",
    orderNo: "",
    amount: "0.00",
    payMethod: "mock",
    paying: false,
    alipayUrl: "",
    showAlipay: false
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
    if (this.data.paying) return;
    that.setData({ paying: true });
    wx.showLoading({ title: "支付中...", mask: true });

    var methodMap = { wechat: "WECHAT", alipay: "ALIPAY", mock: "mock" };
    var payMethod = methodMap[that.data.payMethod] || "mock";

    orderApi.pay(that.data.orderNo, payMethod).then(function(res) {
      wx.hideLoading();

      if (that.data.payMethod === "wechat") {
        var params = null;
        if (res && res.payParams) {
          try { params = JSON.parse(res.payParams); } catch (e) {}
        }
        if (params && params.paySign) {
          wx.requestPayment({
            timeStamp: params.timeStamp,
            nonceStr: params.nonceStr,
            package: params.package,
            signType: params.signType || "RSA",
            paySign: params.paySign,
            success: function() {
              wx.showToast({ title: "支付成功", icon: "success" });
              setTimeout(function() { wx.redirectTo({ url: "/subpages/order/list" }); }, 1500);
            },
            fail: function() {
              wx.showToast({ title: "支付取消", icon: "none" });
              that.setData({ paying: false });
            }
          });
        } else {
          wx.showToast({ title: "微信支付暂不可用", icon: "none" });
          that.setData({ paying: false });
        }
        return;
      }

      if (that.data.payMethod === "alipay") {
        if (res && res.payUrl) {
          that.setData({ alipayUrl: res.payUrl, showAlipay: true });
        } else if (res && res.form) {
          that.setData({ alipayUrl: res.form, showAlipay: true });
        } else {
          wx.showToast({ title: "支付宝支付暂不可用，请稍后重试", icon: "none" });
          that.setData({ paying: false });
        }
        return;
      }

      // 模拟支付
      if (that.data.payMethod === "mock") {
        wx.showToast({ title: "模拟支付成功", icon: "success" });
        setTimeout(function() { wx.redirectTo({ url: "/subpages/order/list" }); }, 1500);
        return;
      }
    }).catch(function(err) {
      wx.hideLoading();
      that.setData({ paying: false });
      wx.showToast({ title: (err && err.message) || "支付失败", icon: "none" });
    });
  },

  closeAlipay: function() {
    this.setData({ showAlipay: false, paying: false });
  },

  openAlipay: function() {
    var url = this.data.alipayUrl;
    this.setData({ showAlipay: false, paying: false });
    if (!url) return;
    wx.navigateTo({ url: "/subpages/order/webview/webview?url=" + encodeURIComponent(url) });
  }
});