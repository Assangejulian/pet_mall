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
    mapCenter: { latitude: 0, longitude: 0 },
    currentLat: 0,
    currentLng: 0,
    located: false,
    radius: 5,
    showMap: false,
    loading: true,
    selectedStore: null,
    detailVisible: false
  },

  onShow: function() {
    // 每次显示页面都重新定位（用户在移动中）
    this.getLocationAndSearch();
  },


  onLoad: function() {
    // 页面加载时尝试定位
    this.getLocationAndSearch();
  },

  /** 获取当前位置，然后搜索附近门店 */
  getLocationAndSearch: function() {
    var that = this;

    // 用缓存定位快速展示（如果有）
    var cached = wx.getStorageSync("cached_location");
    if (cached && cached.lat && cached.lng) {
      that.setData({
        currentLat: cached.lat,
        currentLng: cached.lng,
        mapCenter: { latitude: cached.lat, longitude: cached.lng }
      });
      that.searchNearby();
    }

    // 高精度 GPS 定位覆盖缓存结果
    wx.getLocation({
      type: "gcj02",
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000,
      success: function(res) {
        var lat = res.latitude;
        var lng = res.longitude;
        var accuracy = res.accuracy || 0;

        // 精度 > 500m 时弃用 GPS 结果，保留缓存
        if (accuracy > 500 && cached && cached.lat && cached.lng) {
          return;
        }

        wx.setStorageSync("cached_location", { lat: lat, lng: lng });
        that.setData({
          currentLat: lat,
          currentLng: lng,
          mapCenter: { latitude: lat, longitude: lng },
          located: true
        });
        that.setData({ showMap: true });
        that.searchNearby();
      },
      fail: function() {
        // 定位失败但已有缓存 → 不打扰用户
        if (cached && cached.lat && cached.lng) {
          return;
        }
        // 真的无法定位
        that.setData({ loading: false });
        wx.showToast({ title: "无法获取位置", icon: "none" });
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
          id: Number(s.id) || i,
          latitude: s.latitude,
          longitude: s.longitude,
          title: s.storeName,
          // 使用默认 marker
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
    var that = this;
    wx.showActionSheet({
      itemList: ["\u9ad8\u5fb7\u5730\u56fe\u5bfc\u822a", "\u767e\u5ea6\u5730\u56fe\u5bfc\u822a", "\u7cfb\u7edf\u5730\u56fe"],
      success: function(res) {
        var lat = store.latitude;
        var lng = store.longitude;
        var name = encodeURIComponent(store.storeName);
        if (res.tapIndex === 0) {
          // \u9ad8\u5fb7\u5730\u56fe - \u7528uri.amap.com\u6253\u5f00\u5bfc\u822a
          var url = "https://uri.amap.com/navigation?from=" + that.data.currentLng + "," + that.data.currentLat + "(%E6%88%91%E7%9A%84%E4%BD%8D%E7%BD%AE)&to=" + lng + "," + lat + "(" + name + ")&mode=car&coordinate=gaode";
          wx.setClipboardData({
            data: url,
            success: function() {
              wx.showModal({
                title: "\u5df2\u590d\u5236\u9ad8\u5fb7\u5bfc\u822a\u94fe\u63a5",
                content: "\u8bf7\u6253\u5f00\u6d4f\u89c8\u5668\u7c98\u8d34\u5730\u5740\uff0c\u5373\u53ef\u8df3\u8f6c\u9ad8\u5fb7\u5730\u56feAPP\u5bfc\u822a\uff0c\u4eab\u53d7\u9ad8\u7cbe\u5ea6\u5b9a\u4f4d",
                showCancel: false
              });
            }
          });
        } else if (res.tapIndex === 1) {
          // \u767e\u5ea6\u5730\u56fe
          var url = "baidumap://map/direction?origin=myLocation&destination=latlng:" + lat + "," + lng + "|name:" + name + "&coord_type=gcj02&mode=driving";
          wx.setClipboardData({
            data: url,
            success: function() {
              wx.showModal({
                title: "\u5df2\u590d\u5236\u767e\u5ea6\u5bfc\u822a\u94fe\u63a5",
                content: "\u8bf7\u6253\u5f00\u6d4f\u89c8\u5668\u7c98\u8d34\u5730\u5740\uff0c\u5373\u53ef\u8df3\u8f6c\u767e\u5ea6\u5730\u56feAPP\u5bfc\u822a",
                showCancel: false
              });
            }
          });
        } else {
          // \u7cfb\u7edf\u5730\u56fe\uff08\u9ed8\u8ba4\u9ad8\u5fb7\uff09
          wx.openLocation({
            latitude: lat,
            longitude: lng,
            name: store.storeName,
            address: store.address,
            scale: 18
          });
        }
      }
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
    var show = !this.data.showMap;
    this.setData({ showMap: show });
    if (show && !this.data.located && !this.data.currentLat) {
      this.getLocationAndSearch();
    }
  }
});
