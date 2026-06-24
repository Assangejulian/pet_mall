var storeApi = require("../../utils/api/store");

function extractRows(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}

function normalizeStore(item) {
  item = item || {};
  return {
    id: item.id,
    storeName: item.storeName || "",
    rating: item.rating || "营业中",
    distance: item.distance || "",
    address: [item.province, item.city, item.district, item.address].filter(Boolean).join(""),
    tags: item.tags || [item.city, item.district].filter(Boolean),
    image: item.image || item.storeLogo || "",
    storePhone: item.storePhone || "",
    status: item.status
  };
}

Page({
  data: {
    stores: [],
    loading: true
  },

  onShow: function() {
    this.loadStores();
  },

  loadStores: function() {
    var that = this;
    that.setData({ loading: true });
    storeApi.list({ current: 1, size: 50 }).then(function(res) {
      that.setData({
        stores: extractRows(res).map(normalizeStore),
        loading: false
      });
    }).catch(function() {
      that.setData({ loading: false });
    });
  }
});
