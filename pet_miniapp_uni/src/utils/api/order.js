import request from "../request";
export default {
  create(data) { return request.post("/api/order/create", data); },
  listByStatus(status) { return request.get("/api/order/search", { orderStatus: status || "" }); },
  detail(id) { return request.get("/api/order/" + id); },
  pay(orderNo, payMethod) { return request.post("/api/order/pay", { orderNo, payMethod }); },
  receive(id) { return request.post("/api/order/" + id + "/receive"); },
  items(orderId) { return request.get("/api/order/item/search", { orderId }); },
  refundDirect(id, reason) { return request.post("/api/order/" + id + "/refund_direct", { reason: reason || "" }); },
  refundApply(id, reason) { return request.post("/api/order/" + id + "/refund_apply", { reason: reason || "" }); }
};
