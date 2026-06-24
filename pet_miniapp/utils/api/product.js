var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  detail: function(id) {
    return request.get("/api/product/" + id)
      .catch(function() { return mock.products.find(function(p) { return p.id == id; }) || mock.products[0]; });
  },
  list: function(params) {
    return request.get("/api/product/list", params)
      .catch(function() { return mock.products; });
  }
};