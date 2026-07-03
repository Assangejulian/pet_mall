import request from "../request";
export default {
  detail(id) { return request.get("/api/product/" + id); },
  list(params) { return request.get("/api/product/list", params); }
};
