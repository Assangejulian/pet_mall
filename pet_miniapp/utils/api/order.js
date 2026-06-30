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

  pay: function(orderNo) {
    return request.post("/api/order/pay", { orderNo: orderNo });
  },

  items: function(orderId) {
    return request.get("/api/order/item/search", { orderId: orderId });
  }
};
