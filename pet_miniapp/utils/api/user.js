var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  addressList: function() {
    return request.get("/api/user/address/list")
      .catch(function() { return mock.addresses; });
  },
  addAddress: function(data) {
    return request.post("/api/user/address", data)
      .catch(function() { return null; });
  },
  updateAddress: function(id, data) {
    return request.put("/api/user/address/" + id, data)
      .catch(function() { return null; });
  },
  delAddress: function(id) {
    return request.del("/api/user/address/" + id)
      .catch(function() { return null; });
  }
};