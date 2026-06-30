var request = require("../request");
module.exports = {
  listByStatus: function(status) {
    return request.get("/api/order/search", { orderStatus: status || "" });
  },
  detail: function(id) {
    return request.get("/api/order/" + id);
  },
  /** 创建订单 */
  create: function(data) {
    return request.post("/api/order/create", data);
  },
  /** 支付订单 */
  pay: function(orderNo) {
    return request.post("/api/order/pay", { orderNo: orderNo });
  },

  /** 获取订单明细 */
  items: function(orderId) {
    return request.get("/api/order/item/search", { orderId: orderId });
  }
};
