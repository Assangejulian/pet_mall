var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  listByStatus: function(status) {
    return request.get("/api/order/search", { orderStatus: status || "" })
      .catch(function() {
        if (!status) return mock.orders;
        return mock.orders.filter(function(o) { return o.orderStatus === status; });
      });
  },
  detail: function(id) {
    return request.get("/api/order/" + id)
      .catch(function() { return mock.orders.find(function(o) { return o.id == id; }) || mock.orders[0]; });
  }
};