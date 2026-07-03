var orderApi = require("../../utils/api/order");
Page({
  data: {
    orders: [],
    active: "",
    sl: {
      "0": "待支付",
      "1": "已支付",
      "2": "已发货",
      "3": "已收货",
      "4": "已评价",
      "-1": "已取消",
      "-2": "退单中",
      "-3": "已退款",
      "-4": "已取消(退单)"
    },
    sc: {
      "0": "#e65100",
      "1": "#1565c0",
      "2": "#547b68",
      "3": "#547b68",
      "4": "#8f7366",
      "-1": "#8f7366",
      "-2": "#c0392b",
      "-3": "#547b68",
      "-4": "#8f7366"
    },
    tabList: [
      { l: "全部", v: "" },
      { l: "待支付", v: "0" },
      { l: "已支付", v: "1" },
      { l: "待收货", v: "2" },
      { l: "待评价", v: "3" },
      { l: "已评价", v: "4" },
      { l: "退款/售后", v: "-99" }
    ]
  },

  onLoad(options) {
    if (options.status) this.setData({ active: options.status });
  },

  onShow() {
    var that = this;
    var active = this.data.active;
    orderApi.listByStatus(active).then(function(res) {
      var list = Array.isArray(res) ? res : (res.records || []);
      that.setData({ orders: list });
    }).catch(function() {
      that.setData({ orders: [] });
    });
  },

  switchTab(event) {
    this.setData({ active: event.currentTarget.dataset.v });
    this.onShow();
  },

  payOrder: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    var order = this.data.orders.find(function (o) { return o.id === id; });
    if (!order) return;
    wx.showLoading({ title: "支付中..." });
    orderApi.pay(order.orderNo).then(function () {
      wx.hideLoading();
      wx.showToast({ title: "支付成功", icon: "success" });
      that.onShow();
    }).catch(function () {
      wx.hideLoading();
      wx.showToast({ title: "支付失败", icon: "none" });
    });
  },

  goDetail(event) {
    wx.navigateTo({ url: "/subpages/order/detail/detail?id=" + event.currentTarget.dataset.id });
  }
});

