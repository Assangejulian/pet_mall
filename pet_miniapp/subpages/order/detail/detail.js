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
    statusLabels: {"0":"pending","1":"paid","2":"shipped","3":"received","4":"rated","-1":"cancelled","-2":"refunding","-3":"refunded"}
  },
  onLoad: function(options) {
    var id = options && options.id;
    if (!id) { wx.showToast({ title: "no order id", icon: "none" }); wx.navigateBack(); return; }
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
    wx.navigateTo({ url: "/subpages/order/pay?orderId=" + order.id + "&orderNo=" + order.orderNo + "&amount=" + (order.payAmount || "0.00") });
  },
  confirmReceive: function() {
    var that = this; var order = this.data.order; if (!order || !order.id) return;
    wx.showModal({ title: "confirm", content: "received?", success: function(r) {
      if (r.confirm) { orderApi.receive(order.id).then(function() { wx.showToast({ title: "received", icon: "success" }); that.loadOrder(order.id); }); }
    }});
  },
  evaluateOrder: function() { var id = this.data.order && this.data.order.id; if (id) wx.navigateTo({ url: "/subpages/order/evaluate?id=" + id }); },
  directRefund: function() {
    var that = this; var id = this.data.order && this.data.order.id; if (!id) return;
    wx.showModal({ title: "refund", content: "confirm?", success: function(res) {
      if (res.confirm) { orderApi.refundDirect(id).then(function() { wx.showToast({ title: "refunded" }); that.loadOrder(id); }); }
    }});
  },
  applyRefund: function() { var id = this.data.order && this.data.order.id; if (id) wx.navigateTo({ url: "/subpages/order/refund/refund?id=" + id }); },
  callService: function() { wx.showToast({ title: "400-000-0000", icon: "none" }); }
});