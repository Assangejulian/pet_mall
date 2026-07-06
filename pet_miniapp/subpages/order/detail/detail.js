var orderApi = require("../../../utils/api/order");

function normalizeItem(item) {
  item = item || {};
  return {
    id: item.id,
    productName: item.productName || "",
    productImage: item.productImage || "",
    price: item.price || "0.00",
    quantity: item.quantity || 1
  };
}

Page({
  data: {
    order: null, items: [], loading: true, addressParsed: null, addressText: "",
    statusLabels: {"0":"待付款","1":"已支付","2":"已发货","3":"已收货","4":"已评价","-1":"已取消","-2":"退款中","-3":"已退款"}
  },
  onLoad: function(options) {
    var id = options && options.id;
    if (!id) { wx.showToast({ title: "订单ID缺失", icon: "none" }); wx.navigateBack(); return; }
    this.loadOrder(id);
  },
  loadOrder: function(id) {
    var that = this;
    that.setData({ loading: true });
    Promise.all([orderApi.detail(id), orderApi.items(id)]).then(function(results) {
      var order = results[0] || {};
      var items = (results[1] || []).map(normalizeItem);
      var ap = null;
      if (order.addressSnapshot) { try { ap = JSON.parse(order.addressSnapshot); } catch(e) {} }
      var at = ap ? [ap.province, ap.city, ap.district, ap.detail].filter(Boolean).join("") : "";
      that.setData({ order: order, items: items, addressParsed: ap, addressText: at, loading: false });
    }).catch(function() { that.setData({ loading: false }); });
  },
  payOrder: function() {
    var order = this.data.order;
    if (!order || !order.orderNo) return;
    wx.navigateTo({
      url: "/subpages/order/pay?orderId=" + order.id + "&orderNo="
        + encodeURIComponent(order.orderNo) + "&amount=" + encodeURIComponent(order.payAmount || "0.00")
    });
  },
  confirmReceive: function() {
    var that = this; var order = this.data.order; if (!order || !order.id) return;
    wx.showModal({ title: "确认收货", content: "确认收到商品？", success: function(r) {
      if (r.confirm) { orderApi.receive(order.id).then(function() { wx.showToast({ title: "已确认收货", icon: "success" }); that.loadOrder(order.id); }); }
    }});
  },
  evaluateOrder: function() { var id = this.data.order && this.data.order.id; if (id) wx.navigateTo({ url: "/subpages/order/evaluate?id=" + id }); },
  directRefund: function() {
    var that = this; var id = this.data.order && this.data.order.id; if (!id) return;
    wx.showModal({ title: "申请退款", content: "确认申请退款？", success: function(res) {
      if (res.confirm) { orderApi.refundDirect(id).then(function() { wx.showToast({ title: "退款申请已提交" }); that.loadOrder(id); }); }
    }});
  },
  applyRefund: function() { var id = this.data.order && this.data.order.id; if (id) wx.navigateTo({ url: "/subpages/order/refund/refund?id=" + id }); },
  callService: function() { wx.showToast({ title: "400-000-0000", icon: "none" }); }
});