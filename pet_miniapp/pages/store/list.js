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
    distance: item.distanceKm != null ? item.distanceKm + "km" : (item.distance || ""),
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
    loading: true,
    errorText: ""
  },

  onShow: function() {
    this.loadStores();
  },

  loadStores: function() {
    var that = this;
    that.setData({ loading: true, errorText: "" });
    wx.getLocation({
      type: "gcj02",
      success: function(location) {
        that.loadNearbyStores(location.longitude, location.latitude);
      },
      fail: function() {
        that.loadSearchStores("无法获取定位，已显示全部营业门店");
      }
    });
  },

  loadNearbyStores: function(longitude, latitude) {
    var that = this;
    storeApi.nearby({
      longitude: longitude,
      latitude: latitude,
      radiusKm: 10,
      current: 1,
      size: 50
    }).then(function(res) {
      that.setData({
        stores: extractRows(res).map(normalizeStore),
        loading: false,
        errorText: ""
      });
    }).catch(function() {
      that.loadSearchStores("附近门店查询失败，已显示全部营业门店");
    });
  },

  loadSearchStores: function(message) {
    var that = this;
    storeApi.list({ current: 1, size: 50 }).then(function(res) {
      that.setData({
        stores: extractRows(res).map(normalizeStore),
        loading: false,
        errorText: message || ""
      });
      if (message) {
        wx.showToast({ title: message, icon: "none" });
      }
    }).catch(function() {
      var text = "门店加载失败，请稍后重试";
      that.setData({ stores: [], loading: false, errorText: text });
      wx.showToast({ title: text, icon: "none" });
    });
  }
});

