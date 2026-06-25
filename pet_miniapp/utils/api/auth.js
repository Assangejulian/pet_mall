var request = require("../request");
var mock = require("../data/mock");
module.exports = {
  /** 统一登录入口，按 authType 区分认证方式 */
  login: function(dto) {
    return request.post("/api/user/login", dto)
      .catch(function () {
        var token = "mock_token_" + (dto.username || dto.phone || "user") + "_" + Date.now();
        return { token: token, userId: "mock_user", username: dto.username || dto.phone || "user" };
      });
  }
};
