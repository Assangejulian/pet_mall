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
    storePhone: item.storePhone || "",
    storeDesc: item.storeDesc || "",
    rating: item.rating || "\u8425\u4e1a\u4e2d",
    distance: item.distance ? (Math.round(item.distance * 100) / 100).toFixed(2) + "km" : "",
    province: item.province || "",
    city: item.city || "",
    district: item.district || "",
    address: [item.province, item.city, item.district, item.address].filter(Boolean).join(""),
    tags: item.tags || [item.city, item.district].filter(Boolean),
    image: item.image || item.storeLogo || "",
    status: item.status,
    longitude: Number(item.longitude) || 0,
    latitude: Number(item.latitude) || 0,
    productCount: item.productCount || 0
  };
}

Page({
  data: {
    stores: [],
    storeMarkers: [],
    mapCenter: { latitude: 24.4547, longitude: 118.0822 },
    currentLat: 24.4547,
    currentLng: 118.0822,
    radius: 5,
    showMap: true,
    loading: true,
    selectedStore: null,
    detailVisible: false
  },

  onShow: function() {
    this.getLocationAndSearch();
  },

  /** 获取当前位置，然后搜索附近门店 */
  getLocationAndSearch: function() {
    var that = this;
    wx.getLocation({
      type: "gcj02",
      success: function(res) {
        var lat = res.latitude;
        var lng = res.longitude;
        that.setData({
          currentLat: lat,
          currentLng: lng,
          mapCenter: { latitude: lat, longitude: lng }
        });
        that.searchNearby();
      },
      fail: function() {
        // 定位失败，使用默认坐标搜索
        wx.showToast({ title: "\u5b9a\u4f4d\u5931\u8d25\uff0c\u4f7f\u7528\u9ed8\u8ba4\u4f4d\u7f6e", icon: "none" });
        that.searchNearby();
      }
    });
  },

  /** 搜索附近门店 */
  searchNearby: function() {
    var that = this;
    that.setData({ loading: true });
    var params = {
      latitude: that.data.currentLat,
      longitude: that.data.currentLng,
      radius: that.data.radius
    };
    storeApi.nearby(params).then(function(res) {
      var stores = extractRows(res).map(normalizeStore);
      var markers = stores.filter(function(s) { return s.latitude && s.longitude; }).map(function(s, i) {
        return {
          id: s.id || i,
          latitude: s.latitude,
          longitude: s.longitude,
          title: s.storeName,
          iconPath: "/images/map-marker.png",
          width: 30,
          height: 40,
          callout: {
            content: s.storeName,
            fontSize: 12,
            borderRadius: 4,
            bgColor: "#ffffff",
            padding: 6,
            display: "ALWAYS"
          }
        };
      });
      that.setData({
        stores: stores,
        storeMarkers: markers,
        loading: false
      });
      if (!stores.length) {
        wx.showToast({ title: "\u9644\u8fd1\u6682\u65e0\u95e8\u5e97", icon: "none" });
      }
    }).catch(function() {
      that.setData({ loading: false });
      wx.showToast({ title: "\u95e8\u5e97\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    });
  },

  /** 点击地图标注 -> 展示门店详情浮层 */
  onMarkerTap: function(e) {
    var markerId = e.markerId;
    var stores = this.data.stores;
    for (var i = 0; i < stores.length; i++) {
      if (stores[i].id == markerId) {
        this.setData({ selectedStore: stores[i], detailVisible: true });
        break;
      }
    }
  },

  /** 点击门店卡片 */
  onStoreTap: function(e) {
    var id = e.currentTarget.dataset.id;
    var stores = this.data.stores;
    for (var i = 0; i < stores.length; i++) {
      if (stores[i].id == id) {
        this.setData({ selectedStore: stores[i], detailVisible: true });
        break;
      }
    }
  },

  /** 关闭门店详情浮层 */
  closeDetail: function() {
    this.setData({ detailVisible: false, selectedStore: null });
  },

  /** 查看门店详情页 */
  goStoreDetail: function() {
    var store = this.data.selectedStore;
    if (!store || !store.id) return;
    this.closeDetail();
    wx.navigateTo({
      url: "/subpages/store/detail?id=" + store.id
    });
  },

  /** 导航到门店 (调用地图APP) */
  navigateToStore: function() {
    var store = this.data.selectedStore;
    if (!store || !store.latitude || !store.longitude) {
      wx.showToast({ title: "\u95e8\u5e97\u4f4d\u7f6e\u4fe1\u606f\u4e0d\u5b8c\u6574", icon: "none" });
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

  /** 切换搜索半径 */
  changeRadius: function(e) {
    var r = Number(e.detail.value);
    this.setData({ radius: r });
    this.searchNearby();
  },

  /** 切换列表/地图视图 */
  toggleView: function() {
    this.setData({ showMap: !this.data.showMap });
  }
});
