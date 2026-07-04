var orderApi = require("../../utils/api/order");
Page({
  data: {
    orders: [], active: "",
    sl: {"0":"pending","1":"paid","2":"shipped","3":"received","4":"rated","-1":"cancelled","-2":"refunding","-3":"refunded","-4":"rejected"},
    sc: {"0":"#e65100","1":"#1565c0","2":"#547b68","3":"#547b68","4":"#8f7366","-1":"#8f7366","-2":"#c0392b","-3":"#547b68","-4":"#8f7366"},
    tabList: [{l:"all",v:""},{l:"pending",v:"0"},{l:"paid",v:"1"},{l:"shipping",v:"2"},{l:"unrated",v:"3"},{l:"rated",v:"4"},{l:"refund",v:"-99"}]
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