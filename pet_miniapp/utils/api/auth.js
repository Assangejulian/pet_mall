var request = require("../request");
module.exports = {
  login: function(dto) {
    return request.post("/api/user/login", dto);
  },
  sendSmsCode: function(phone) {
    return request.post("/api/user/send-sms-code", { phone: phone });
  },
  sendEmailCode: function(email) {
    return request.post("/api/user/send-email-code", { email: email });
  }
};