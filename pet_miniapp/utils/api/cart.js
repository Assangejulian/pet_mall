var request = require("../request");
module.exports = {
  list: function() {
    return request.get("/api/cart/search");
  },
  add: function(item) {
    return request.post("/api/cart", item);
  },
  update: function(id, data) {
    return request.put("/api/cart/" + id, data);
  },
  remove: function(id) {
    return request.del("/api/cart/" + id);
  }
};
