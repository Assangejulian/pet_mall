var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  login: function(username, password) {
    return request.post("/api/user/login", { username: username, password: password })
      .catch(function() {
        var token = "mock_token_" + username + "_" + Date.now();
        return { token: token, userId: "mock_" + username, username: username };
      });
  }
};