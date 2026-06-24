var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  list: function() {
    return request.get("/api/store/list")
      .catch(function() { return mock.stores; });
  }
};