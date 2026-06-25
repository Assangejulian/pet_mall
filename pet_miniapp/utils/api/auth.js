var request = require("../request");
module.exports = {
  login: function(dto) {
    return request.post("/api/user/login", dto);
  },
  sendEmailCode: function(email) {
    return request.post("/api/user/send-email-code", { email: email });
  }
};