var request = require("../request");
module.exports = {
  list: function(params) {
    return request.get("/api/store/search", params || { current: 1, size: 20 });
  },
  detail: function(id) {
    return request.get("/api/store/" + id);
  },
  /** 附近门店搜索 */
  nearby: function(params) {
    return request.get("/api/store/nearby", params || { current: 1, size: 20, radiusKm: 10 });
  },
  /** 门店商品列表 */
  products: function(storeId) {
    return request.get("/api/store/" + storeId + "/products");
  }
};
