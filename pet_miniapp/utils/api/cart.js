var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  list: function() {
    return request.get("/api/cart/list")
      .catch(function() { return mock.cartItems; });
  },
  add: function(item) {
    mock.cartItems.push(item);
    return request.post("/api/cart", item)
      .catch(function() { return null; });
  },
  update: function(id, data) {
    return request.put("/api/cart/" + id, data)
      .catch(function() { return null; });
  },
  remove: function(id) {
    mock.cartItems = mock.cartItems.filter(function(i) { return i.id != id; });
    return request.del("/api/cart/" + id)
      .catch(function() { return null; });
  }
};