var orderApi = require("../../utils/api/order");
Page({
  data: {
    orders: [], active: "",
    sl: {"0":"待付款","1":"已支付","2":"已发货","3":"已收货","4":"已评价","-1":"已取消","-2":"退款中","-3":"已退款","-4":"已拒绝"},
    sc: {"0":"#e65100","1":"#1565c0","2":"#547b68","3":"#547b68","4":"#8f7366","-1":"#8f7366","-2":"#c0392b","-3":"#547b68","-4":"#8f7366"},
    tabList: [{l:"全部",v:""},{l:"待付款",v:"0"},{l:"已支付",v:"1"},{l:"已发货",v:"2"},{l:"待评价",v:"3"},{l:"已评价",v:"4"},{l:"退款",v:"-99"}]
  },
  onLoad(options) { if (options.status) this.setData({ active: options.status }); },
  onShow() {
    var that = this;
    orderApi.listByStatus(this.data.active).then(function(res) {
      var list = Array.isArray(res) ? res : (res.records || []);
      that.setData({ orders: list });
    }).catch(function() { that.setData({ orders: [] }); });
  },
  switchTab(e) { this.setData({ active: e.currentTarget.dataset.v }); this.onShow(); },
  payOrder: function(e) {
    var id = e.currentTarget.dataset.id;
    var order = this.data.orders.find(function(o) { return o.id === id; });
    if (!order) return;
    wx.navigateTo({ url: "/subpages/order/pay?orderId=" + order.id + "&orderNo=" + order.orderNo + "&amount=" + (order.payAmount || order.totalAmount || "0.00") });
  },
  goDetail(e) { wx.navigateTo({ url: "/subpages/order/detail/detail?id=" + e.currentTarget.dataset.id }); }
});