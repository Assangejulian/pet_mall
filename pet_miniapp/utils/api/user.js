var request = require("../request");
module.exports = {
  addressList: function() {
    return request.get("/api/user/address/list");
  },
  addAddress: function(data) {
    return request.post("/api/user/address", data);
  },
  updateAddress: function(id, data) {
    return request.put("/api/user/address/" + id, data);
  },
  defaultAddress: function() {
    return request.get("/api/user/address/default");
  },
  delAddress: function(id) {
    return request.del("/api/user/address/" + id);
  },
  getProfile: function() {
    return request.get("/api/user/profile");
  }
};
