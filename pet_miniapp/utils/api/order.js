var request = require("../request");

module.exports = {
  create: function(data) {
    return request.post("/api/order/create", data);
  },

  listByStatus: function(status) {
    return request.get("/api/order/search", { orderStatus: status || "" });
  },

  detail: function(id) {
    return request.get("/api/order/" + id);
  },

  pay: function(orderNo, payMethod) {
    return request.post("/api/order/pay", { orderNo: orderNo, payMethod: payMethod });
  },

  receive: function(id) {
    return request.post("/api/order/" + id + "/receive");
  },

  items: function(orderId) {
    return request.get("/api/order/item/search", { orderId: orderId });
  },

  refundDirect: function(id, reason) {
    return request.post("/api/order/" + id + "/refund_direct", { reason: reason || "" });
  },

  cancel: function(id, reason) {
    return request.post("/api/order/" + id + "/cancel", { reason: reason || "用户取消订单" });
  },

  refundApply: function(id, reason) {
    return request.post("/api/order/" + id + "/refund_apply", { reason: reason || "" });
  }
};
