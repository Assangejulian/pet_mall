var request = require("../request");
module.exports = {
  listByStatus: function(status) {
    return request.get("/api/order/search", { orderStatus: status || "" });
  },
  detail: function(id) {
    return request.get("/api/order/" + id);
  }
};
