import request from "../request";
export default {
  list() { return request.get("/api/cart/search"); },
  add(item) { return request.post("/api/cart", item); },
  update(id, data) { return request.put("/api/cart/" + id, data); },
  remove(id) { return request.del("/api/cart/" + id); }
};
