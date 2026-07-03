var storeApi = require("../../utils/api/store");

var fallbackStores = [
  { id: 201, storeName: "暖窝宠物·思明店", storePhone: "0592-1234567", storeDesc: "专业宠物用品与美容服务", province: "福建省", city: "厦门市", district: "思明区", address: "禾祥西路128号", rating: "4.8", distance: 1.2, tags: ["思明区", "宠物用品", "美容"], image: "/images/mock/cat-cover.jpg", status: 1, longitude: 118.092, latitude: 24.468 },
  { id: 202, storeName: "暖窝宠物·湖里店", storePhone: "0592-2345678", storeDesc: "宠物寄养与训练中心", province: "福建省", city: "厦门市", district: "湖里区", address: "华昌路56号", rating: "4.6", distance: 3.5, tags: ["湖里区", "寄养", "训练"], image: "/images/mock/golden.jpg", status: 1, longitude: 118.105, latitude: 24.515 },
  { id: 203, storeName: "暖窝宠物·集美店", storePhone: "0592-3456789", storeDesc: "宠物医院与健康咨询", province: "福建省", city: "厦门市", district: "集美区", address: "石鼓路88号", rating: "4.7", distance: 5.8, tags: ["集美区", "医疗", "咨询"], image: "/images/mock/blue-cat.jpg", status: 1, longitude: 118.098, latitude: 24.574 },
  { id: 204, storeName: "暖窝宠物·翔安店", storePhone: "0592-4567890", storeDesc: "宠物食品与玩具专卖", province: "福建省", city: "厦门市", district: "翔安区", address: "新兴路12号", rating: "4.5", distance: 12.3, tags: ["翔安区", "食品", "玩具"], image: "/images/mock/corgi.jpg", status: 1, longitude: 118.248, latitude: 24.618 }
];

function extractRows(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}

function normalizeStore(item) {
  item = item || {};
  var distanceText = formatDistance(item.distanceKm != null ? item.distanceKm : item.distance);
  return {
    id: item.id,
    storeName: item.storeName || "",
    storePhone: item.storePhone || "",
    storeDesc: item.storeDesc || "",
    rating: item.rating || "营业中",
    distance: distanceText,
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

function formatDistance(value) {
  if (value === undefined || value === null || value === "") return "";
  var num = Number(value);
  if (isNaN(num)) return "";
  return num.toFixed(2) + "km";
}

Page({
  data: {
    stores: [],
    storeMarkers: [],
    mapCenter: { latitude: 24.4547, longitude: 118.0822 },
    currentLat: null,
    currentLng: null,
    located: false,
    radius: 10,
    showMap: true,
    loading: true,
    errorText: "",
    selectedStore: null,
    detailVisible: false
  },

  onLoad: function() {
    this._unloaded = false;
  },

  onShow: function() {
    this._unloaded = false;
    this.getLocationAndSearch();
  },

  onUnload: function() {
    this._unloaded = true;
    this._requestSeq = (this._requestSeq || 0) + 1;
  },

  safeSetData: function(data) {
    if (!this._unloaded) {
      this.setData(data);
    }
  },

  getLocationAndSearch: function() {
    var that = this;
    that.safeSetData({ loading: true, errorText: "" });

    var cached = wx.getStorageSync("cached_location");
    if (cached && cached.lat && cached.lng) {
      that.safeSetData({
        currentLat: cached.lat,
        currentLng: cached.lng,
        mapCenter: { latitude: cached.lat, longitude: cached.lng },
        located: true
      });
      that.searchNearby();
    }

    wx.getLocation({
      type: "gcj02",
      isHighAccuracy: true,
      highAccuracyExpireTime: 3000,
      success: function(res) {
        var lat = res.latitude;
        var lng = res.longitude;
        var accuracy = res.accuracy || 0;
        if (accuracy > 500 && cached && cached.lat && cached.lng) return;

        wx.setStorageSync("cached_location", { lat: lat, lng: lng });
        that.safeSetData({
          currentLat: lat,
          currentLng: lng,
          mapCenter: { latitude: lat, longitude: lng },
          located: true,
          showMap: true
        });
        that.searchNearby();
      },
      fail: function() {
        if (cached && cached.lat && cached.lng) return;
        that.useFallback("无法获取定位，已显示全部营业门店");
      }
    });
  },

  searchNearby: function() {
    var that = this;
    if (that.data.currentLat === null || that.data.currentLng === null) {
      that.useFallback("无法获取定位，已显示全部营业门店");
      return;
    }
    that.safeSetData({ loading: true, errorText: "" });
    var seq = that.nextRequestSeq();
    var params = {
      latitude: that.data.currentLat,
      longitude: that.data.currentLng,
      radiusKm: that.data.radius,
      current: 1,
      size: 50
    };
    storeApi.nearby(params).then(function(res) {
      if (!that.isActiveRequest(seq)) return;
      var stores = extractRows(res).map(normalizeStore);
      that.applyStores(stores, "");
      if (!stores.length) {
        wx.showToast({ title: "附近暂无门店", icon: "none" });
      }
    }).catch(function() {
      if (!that.isActiveRequest(seq)) return;
      that.useFallback("附近门店查询失败，已显示全部营业门店");
    });
  },

  useFallback: function(message) {
    var that = this;
    var seq = that.nextRequestSeq();
    that.safeSetData({ loading: true });
    storeApi.search({ current: 1, size: 50 }).then(function(res) {
      if (!that.isActiveRequest(seq)) return;
      var stores = extractRows(res).map(normalizeStore);
      if (stores.length) {
        that.applyStores(stores, message || "");
      } else {
        that.applyStores(fallbackStores.map(normalizeStore), message || "暂无门店数据，展示示例门店");
      }
    }).catch(function() {
      if (!that.isActiveRequest(seq)) return;
      that.applyStores(fallbackStores.map(normalizeStore), message || "无法连接后端，展示示例门店");
    });
  },

  applyStores: function(stores, message) {
    stores.forEach(function(s, i) {
      s.markerId = i + 1;
    });
    var markers = stores.filter(function(s) {
      return s.latitude && s.longitude;
    }).map(function(s) {
      return {
        id: s.markerId,
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
    this.safeSetData({
      stores: stores,
      storeMarkers: markers,
      loading: false,
      errorText: message || ""
    });
  },

  nextRequestSeq: function() {
    this._requestSeq = (this._requestSeq || 0) + 1;
    return this._requestSeq;
  },

  isActiveRequest: function(seq) {
    return !this._unloaded && seq === this._requestSeq;
  },

  onMarkerTap: function(e) {
    var markerId = e.markerId;
    var stores = this.data.stores;
    for (var i = 0; i < stores.length; i++) {
      if (stores[i].markerId == markerId) {
        this.setData({ selectedStore: stores[i], detailVisible: true });
        break;
      }
    }
  },

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

  closeDetail: function() {
    this.setData({ detailVisible: false, selectedStore: null });
  },

  goStoreDetail: function() {
    var store = this.data.selectedStore;
    if (!store || !store.id) return;
    this.closeDetail();
    wx.navigateTo({
      url: "/subpages/store/detail/detail?id=" + encodeURIComponent(String(store.id))
    });
  },

  navigateToStore: function() {
    var store = this.data.selectedStore;
    if (!store || !store.latitude || !store.longitude) {
      wx.showToast({ title: "门店位置信息不完整", icon: "none" });
      return;
    }
    var that = this;
    wx.showActionSheet({
      itemList: ["高德地图导航", "百度地图导航", "系统地图"],
      success: function(res) {
        var lat = store.latitude;
        var lng = store.longitude;
        var name = encodeURIComponent(store.storeName);
        if (res.tapIndex === 0) {
          var url = "https://uri.amap.com/navigation?from=" + that.data.currentLng + "," + that.data.currentLat + "(我的位置)&to=" + lng + "," + lat + "(" + name + ")&mode=car&coordinate=gaode";
          wx.setClipboardData({
            data: url,
            success: function() {
              wx.showModal({
                title: "已复制高德导航链接",
                content: "请打开浏览器粘贴地址，即可跳转高德地图APP导航，享受高精度定位",
                showCancel: false
              });
            }
          });
        } else if (res.tapIndex === 1) {
          var url = "baidumap://map/direction?origin=myLocation&destination=latlng:" + lat + "," + lng + "|name:" + name + "&coord_type=gcj02&mode=driving";
          wx.setClipboardData({
            data: url,
            success: function() {
              wx.showModal({
                title: "已复制百度导航链接",
                content: "请打开浏览器粘贴地址，即可跳转百度地图APP导航",
                showCancel: false
              });
            }
          });
        } else {
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

  changeRadius: function(e) {
    var r = Number(e.detail.value);
    this.setData({ radius: r });
    this.searchNearby();
  },

  toggleView: function() {
    var show = !this.data.showMap;
    this.setData({ showMap: show });
    if (show && !this.data.located && !this.data.currentLat) {
      this.getLocationAndSearch();
    }
  }
});