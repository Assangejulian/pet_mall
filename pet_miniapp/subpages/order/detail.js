var orderApi = require("../../utils/api/order");

Page({
  data: {
    order: {},
    sl: {
      "0": "待支付", "1": "已支付", "2": "已发货",
      "3": "已收货", "4": "已评价"
    },
    sbg: {
      "0": "#fdf2ed", "1": "#eef4f0", "2": "#eef4f0", "3": "#eef4f0"
    },
    sic: {
      "0": "\u25cb", "1": "\u25d0", "2": "\u25d1", "3": "\u25cf", "4": "\u2605"
    }
  },

  onLoad(options) {
    var that = this;
    var id = options.id;
    if (!id) return;
    orderApi.detail(id).then(function(res) {
      that.setData({ order: res || {} });
    }).catch(function() {
      that.setData({ order: {} });
    });
  }
});

