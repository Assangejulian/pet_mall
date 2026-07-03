import request from "../request";
export default {
  login(dto) { return request.post("/api/user/login", dto); },
  sendSmsCode(phone) { return request.post("/api/user/send-sms-code", { phone }); },
  sendEmailCode(email) { return request.post("/api/user/send-email-code", { email }); }
};
