var storeApi = require("../../../utils/api/store");

function normalizeStore(source) {
  source = source || {};
  return {
    id: source.id,
    storeName: source.storeName || "",
    storePhone: source.storePhone || "",
    storeDesc: source.storeDesc || "",
    image: source.storeLogo || "",
    address: [source.province, source.city, source.district, source.address].filter(Boolean).join(""),
    longitude: Number(source.longitude) || 0,
    latitude: Number(source.latitude) || 0
  };
}

function normalizeProduct(source) {
  source = source || {};
  return {
    id: source.id,
    name: source.productName || source.name || "",
    price: source.price || "0.00",
    image: source.mainImage || source.image || "",
    category: source.category || "",
    stock: source.stock || 0,
    productType: source.productType || 1  // 1-宠物 2-周边
  };
}

Page({
  data: {
    store: null,
    products: [],
    loading: true
  },

  onLoad: function(options) {
    this._unloaded = false;
    var id = options && options.id;
    if (!id) {
      wx.showToast({ title: "\u95e8\u5e97ID\u7f3a\u5931", icon: "none" });
      wx.navigateBack();
      return;
    }
    this.loadStore(id);
  },

  onUnload: function() {
    this._unloaded = true;
    this._requestSeq = (this._requestSeq || 0) + 1;
  },

  loadStore: function(id) {
    var that = this;
    var seq = (that._requestSeq || 0) + 1;
    that._requestSeq = seq;
    that.setData({ loading: true });

    Promise.all([
      storeApi.detail(id),
      storeApi.products(id)
    ]).then(function(results) {
      if (that._unloaded || seq !== that._requestSeq) return;
      var store = normalizeStore(results[0]);
      var products = (results[1] || []).map(normalizeProduct);
      that.setData({
        store: store,
        products: products,
        loading: false
      });
    }).catch(function() {
      if (that._unloaded || seq !== that._requestSeq) return;
      that.setData({ loading: false });
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    });
  },

  /** 导航到门店 */
  navigateToStore: function() {
    var store = this.data.store;
    if (!store || !store.latitude || !store.longitude) {
      wx.showToast({ title: "\u4f4d\u7f6e\u4fe1\u606f\u4e0d\u5b8c\u6574", icon: "none" });
      return;
    }
    wx.openLocation({
      latitude: Number(store.latitude),
      longitude: Number(store.longitude),
      name: store.storeName,
      address: store.address,
      scale: 18
    });
  },

  /** 拨打电话 */
  callPhone: function() {
    var phone = this.data.store && this.data.store.storePhone;
    if (!phone) {
      wx.showToast({ title: "暂无联系电话", icon: "none" });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  /** 查看商品详情 */
  goProduct: function(e) {
    var id = e.currentTarget.dataset.id;
    if (id) {
      wx.navigateTo({ url: "/subpages/detail/detail?id=" + encodeURIComponent(String(id)) });
    }
  }
});
