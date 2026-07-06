import request from "../request";
export default {
  addressList() { return request.get("/api/user/address/list"); },
  addAddress(data) { return request.post("/api/user/address", data); },
  updateAddress(id, data) { return request.put("/api/user/address/" + id, data); },
  defaultAddress() { return request.get("/api/user/address/default"); },
  delAddress(id) { return request.del("/api/user/address/" + id); },
  getProfile() { return request.get("/api/user/profile"); }
};
