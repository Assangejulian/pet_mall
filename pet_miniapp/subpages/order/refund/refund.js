var orderApi = require("../../../utils/api/order");

Page({
  data: {
    orderId: null,
    reason: "",
    submitting: false
  },

  onLoad: function(options) {
    var id = options.id;
    if (!id) {
      wx.showToast({ title: "订单ID缺失", icon: "none" });
      setTimeout(function() { wx.navigateBack(); }, 1500);
      return;
    }
    this.setData({ orderId: id });
  },

  onInputReason: function(e) {
    this.setData({ reason: e.detail.value });
  },

  submitRefund: function() {
    var reason = (this.data.reason || "").trim();
    if (!reason) {
      wx.showToast({ title: "请填写退单原因", icon: "none" });
      return;
    }

    var that = this;
    that.setData({ submitting: true });
    wx.showLoading({ title: "提交中..." });

    orderApi.refundApply(this.data.orderId, reason).then(function() {
      wx.hideLoading();
      wx.showToast({ title: "申请已提交", icon: "success" });
      setTimeout(function() {
        wx.navigateBack({ delta: 1 });
      }, 1500);
    }).catch(function() {
      wx.hideLoading();
      that.setData({ submitting: false });
      wx.showToast({ title: "提交失败", icon: "none" });
    });
  }
});
