import request from "../request";
export default {
  list(params) { return request.get("/api/store/search", params || { current: 1, size: 20 }); },
  detail(id) { return request.get("/api/store/" + id); },
  nearby(params) { return request.get("/api/store/nearby", params || { current: 1, size: 20, radiusKm: 10 }); },
  products(storeId) { return request.get("/api/store/" + storeId + "/products"); }
};
