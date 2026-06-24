var request = require("../request");
module.exports = {
  detail: function(id) {
    return request.get("/api/product/" + id);
  },
  list: function(params) {
    return request.get("/api/product/list", params);
  }
};
