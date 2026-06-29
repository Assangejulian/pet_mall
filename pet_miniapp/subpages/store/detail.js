var storeApi = require("../../utils/api/store");

function normalizeStore(item) {
  item = item || {};
  return {
    id: item.id,
    storeName: item.storeName || "",
    storePhone: item.storePhone || "",
    storeDesc: item.storeDesc || "",
    image: item.storeLogo || "",
    address: [item.province, item.city, item.district, item.address].filter(Boolean).join(""),
    longitude: Number(item.longitude) || 0,
    latitude: Number(item.latitude) || 0,
    productCount: item.productCount || 0
  };
}

function normalizeProduct(item) {
  item = item || {};
  return {
    id: item.id,
    name: item.name || item.productName || "",
    image: item.image || item.mainImage || "",
    price: item.price || "0.00",
    stock: item.stock || 0
  };
}

Page({
  data: {
    store: {},
    products: [],
    loading: true
  },

  onLoad: function(options) {
    this.loadStore(options && options.id);
  },

  loadStore: function(id) {
    var that = this;
    if (!id) {
      wx.showToast({ title: "门店ID缺失", icon: "none" });
      that.setData({ loading: false });
      return;
    }
    that.setData({ loading: true });
    Promise.all([storeApi.detail(id), storeApi.products(id)])
      .then(function(results) {
        that.setData({
          store: normalizeStore(results[0]),
          products: (Array.isArray(results[1]) ? results[1] : []).map(normalizeProduct),
          loading: false
        });
      })
      .catch(function() {
        that.setData({ loading: false });
        wx.showToast({ title: "门店加载失败", icon: "none" });
      });
  },

  navigateToStore: function() {
    var store = this.data.store;
    if (!store || !store.latitude || !store.longitude) {
      wx.showToast({ title: "门店位置信息不完整", icon: "none" });
      return;
    }
    wx.openLocation({
      latitude: store.latitude,
      longitude: store.longitude,
      name: store.storeName,
      address: store.address,
      scale: 18
    });
  },

  callStore: function() {
    var phone = this.data.store && this.data.store.storePhone;
    if (!phone) {
      wx.showToast({ title: "暂无联系电话", icon: "none" });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  goProduct: function(e) {
    var id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: "/subpages/detail/detail?id=" + id });
  }
});
