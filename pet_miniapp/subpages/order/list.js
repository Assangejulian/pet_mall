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
      "-2": "退单中"
    },
    sc: {
      "0": "#e65100",
      "1": "#1565c0",
      "2": "#547b68",
      "3": "#547b68",
      "4": "#8f7366",
      "-1": "#8f7366",
      "-2": "#c0392b"
    },
    tabList: [
      { l: "全部", v: "" },
      { l: "待支付", v: "0" },
      { l: "已支付", v: "1" },
      { l: "已发货", v: "2" },
      { l: "已收货", v: "3" },
      { l: "已评价", v: "4" }
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

  goDetail(event) {
    wx.navigateTo({ url: "/subpages/order/detail?id=" + event.currentTarget.dataset.id });
  }
});

