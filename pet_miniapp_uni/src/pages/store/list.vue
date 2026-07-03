<template>
  <view class="page">
    <!-- 地图区域 -->
    <view class="map-wrap" v-if="showMap">
      <map
        id="storeMap"
        class="map"
        :latitude="mapCenter.latitude"
        :longitude="mapCenter.longitude"
        scale="14"
        :markers="storeMarkers"
        @markertap="onMarkerTap"
        show-location
      ></map>
    </view>
    <view class="radius-bar">
      <text class="radius-label">搜索范围</text>
      <slider min="1" max="20" step="1" :value="radius" show-value @change="changeRadius" />
      <text class="radius-val">{{radius}}km</text>
    </view>
    <view class="view-toggle">
      <text @tap="toggleView">{{showMap ? '切换列表' : '切换地图'}}</text>
      <text class="store-count">共{{stores.length}} 家门店</text>
    </view>
    <view class="error-tip" v-if="errorText">
      <text>{{errorText}}</text>
    </view>
    <scroll-view class="store-list" scroll-y v-if="!showMap || stores.length">
      <view class="card" v-for="item in stores" :key="item.id" @tap="onStoreTap" :data-id="item.id">
        <view class="sr">
          <image class="si" :src="item.image" mode="aspectFill" />
          <view class="sc">
            <text class="sn serif">{{item.storeName}}</text>
            <view class="sm">
              <text>评分{{item.rating}}</text>
              <text v-if="item.distance">{{item.distance}}</text>
            </view>
            <text class="sa">{{item.address}}</text>
            <view class="st">
              <text class="tg" v-for="(t, ti) in item.tags" :key="ti">{{t}}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    <view class="detail-mask" v-if="detailVisible" @tap="closeDetail"></view>
    <view class="detail-sheet" v-if="detailVisible">
      <view class="ds-header">
        <text class="ds-name serif">{{selectedStore.storeName}}</text>
        <text class="ds-close" @tap="closeDetail">✕</text>
      </view>
      <view class="ds-body">
        <view class="ds-row"><text class="ds-label">地址</text><text class="ds-value">{{selectedStore.address}}</text></view>
        <view class="ds-row" v-if="selectedStore.storePhone"><text class="ds-label">电话</text><text class="ds-value">{{selectedStore.storePhone}}</text></view>
        <view class="ds-row" v-if="selectedStore.distance"><text class="ds-label">距离</text><text class="ds-value">{{selectedStore.distance}}</text></view>
      </view>
      <view class="ds-actions">
        <button class="ds-btn primary" @tap="navigateToStore">导航到此</button>
        <button class="ds-btn" @tap="goStoreDetail">查看门店详情</button>
      </view>
    </view>
    <view class="empty-tip" v-if="!loading && !stores.length">
      <text>暂无附近门店</text>
    </view>
  </view>
</template>
<script>
import storeApi from "@/utils/api/store";
const fallbackStores = [
  { id: 201, storeName: "暖窝宠物·思明店", storePhone: "0592-1234567", storeDesc: "专业宠物用品与美容服务", province: "福建省", city: "厦门市", district: "思明区", address: "福祥西路128号", rating: "4.8", distance: 1.2, tags: ["思明区", "宠物用品", "美容"], image: "/static/images/mock/cat-cover.jpg", status: 1, longitude: 118.092, latitude: 24.468 },
  { id: 202, storeName: "暖窝宠物·湖里店", storePhone: "0592-2345678", storeDesc: "宠物寄养与训练中心", province: "福建省", city: "厦门市", district: "湖里区", address: "华荣路6号", rating: "4.6", distance: 3.5, tags: ["湖里区", "寄养", "训练"], image: "/static/images/mock/golden.jpg", status: 1, longitude: 118.105, latitude: 24.515 },
  { id: 203, storeName: "暖窝宠物·集美店", storePhone: "0592-3456789", storeDesc: "宠物医院与健康咨询", province: "福建省", city: "厦门市", district: "集美区", address: "石鼓路8号", rating: "4.7", distance: 5.8, tags: ["集美区", "医疗", "咨询"], image: "/static/images/mock/blue-cat.jpg", status: 1, longitude: 118.098, latitude: 24.574 },
  { id: 204, storeName: "暖窝宠物·翔安店", storePhone: "0592-4567890", storeDesc: "宠物食品与玩具专柜", province: "福建省", city: "厦门市", district: "翔安区", address: "新兴路2号", rating: "4.5", distance: 12.3, tags: ["翔安区", "食品", "玩具"], image: "/static/images/mock/corgi.jpg", status: 1, longitude: 118.248, latitude: 24.618 }
];
function extractRows(data) {
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}
function normalizeStore(item) {
  item = item || {};
  const distanceText = formatDistance(item.distanceKm != null ? item.distanceKm : item.distance);
  return {
    id: item.id, storeName: item.storeName || "", storePhone: item.storePhone || "", storeDesc: item.storeDesc || "",
    rating: item.rating || "营业中", distance: distanceText, province: item.province || "", city: item.city || "",
    district: item.district || "", address: [item.province, item.city, item.district, item.address].filter(Boolean).join(""),
    tags: item.tags || [item.city, item.district].filter(Boolean), image: item.image || item.storeLogo || "",
    status: item.status, longitude: Number(item.longitude) || 0, latitude: Number(item.latitude) || 0, productCount: item.productCount || 0
  };
}
function formatDistance(value) {
  if (value === undefined || value === null || value === "") return "";
  const num = Number(value);
  return isNaN(num) ? "" : num.toFixed(2) + "km";
}
export default {
  data() {
    return {
      stores: [], storeMarkers: [], mapCenter: { latitude: 24.4547, longitude: 118.0822 },
      currentLat: null, currentLng: null, located: false, radius: 10, showMap: true,
      loading: true, errorText: "", selectedStore: null, detailVisible: false, _unloaded: false, _requestSeq: 0
    };
  },
  onLoad() { this._unloaded = false; },
  onShow() { this._unloaded = false; this.getLocationAndSearch(); },
  onUnload() { this._unloaded = true; this._requestSeq++; },
  methods: {
    safeSetData(data) { if (!this._unloaded) { Object.assign(this.$data, data); } },
    getLocationAndSearch() {
      const that = this;
      this.loading = true; this.errorText = "";
      const cached = uni.getStorageSync("cached_location");
      if (cached && cached.lat && cached.lng) {
        this.currentLat = cached.lat; this.currentLng = cached.lng;
        this.mapCenter = { latitude: cached.lat, longitude: cached.lng }; this.located = true;
        this.searchNearby();
      }
      uni.getLocation({
        type: "gcj02", isHighAccuracy: true, highAccuracyExpireTime: 3000,
        success(res) {
          const lat = res.latitude, lng = res.longitude, accuracy = res.accuracy || 0;
          if (accuracy > 500 && cached && cached.lat && cached.lng) return;
          uni.setStorageSync("cached_location", { lat, lng });
          that.currentLat = lat; that.currentLng = lng;
          that.mapCenter = { latitude: lat, longitude: lng }; that.located = true; that.showMap = true;
          that.searchNearby();
        },
        fail() { if (cached && cached.lat && cached.lng) return; that.useFallback("无法获取定位，已显示全部营业门店"); }
      });
    },
    searchNearby() {
      const that = this;
      if (this.currentLat === null || this.currentLng === null) { this.useFallback("无法获取定位"); return; }
      this.loading = true; this.errorText = "";
      const seq = ++this._requestSeq;
      storeApi.nearby({ latitude: this.currentLat, longitude: this.currentLng, radiusKm: this.radius, current: 1, size: 50 })
        .then(res => { if (this._unloaded || seq !== this._requestSeq) return; const stores = extractRows(res).map(normalizeStore); that.applyStores(stores, ""); if (!stores.length) uni.showToast({ title: "附近暂无门店", icon: "none" }); })
        .catch(() => { if (this._unloaded || seq !== this._requestSeq) return; that.useFallback("附近门店查询失败"); });
    },
    useFallback(message) {
      const that = this; const seq = ++this._requestSeq;
      storeApi.list({ current: 1, size: 50 }).then(res => {
        if (that._unloaded || seq !== that._requestSeq) return;
        const stores = extractRows(res).map(normalizeStore);
        stores.length ? that.applyStores(stores, message || "") : that.applyStores(fallbackStores.map(normalizeStore), message || "暂无门店数据");
      }).catch(() => { if (that._unloaded || seq !== that._requestSeq) return; that.applyStores(fallbackStores.map(normalizeStore), message || "无法连接后端"); });
    },
    applyStores(stores, message) {
      stores.forEach((s, i) => s.markerId = i + 1);
      const markers = stores.filter(s => s.latitude && s.longitude).map(s => ({ id: s.markerId, latitude: s.latitude, longitude: s.longitude, title: s.storeName, iconPath: "/static/images/map-marker.png", width: 30, height: 40, callout: { content: s.storeName, fontSize: 12, borderRadius: 4, bgColor: "#ffffff", padding: 6, display: "ALWAYS" } }));
      this.stores = stores; this.storeMarkers = markers; this.loading = false; this.errorText = message || "";
    },
    onMarkerTap(e) {
      const markerId = e.markerId;
      const store = this.stores.find(s => s.markerId == markerId);
      if (store) { this.selectedStore = store; this.detailVisible = true; }
    },
    onStoreTap(e) {
      const id = e.currentTarget.dataset.id;
      const store = this.stores.find(s => s.id == id);
      if (store) { this.selectedStore = store; this.detailVisible = true; }
    },
    closeDetail() { this.detailVisible = false; this.selectedStore = null; },
    goStoreDetail() {
      if (!this.selectedStore || !this.selectedStore.id) return;
      const id = this.selectedStore.id;
      this.closeDetail();
      uni.navigateTo({ url: "/subpages/store/detail/detail?id=" + encodeURIComponent(String(id)) });
    },
    navigateToStore() {
      const store = this.selectedStore;
      if (!store || !store.latitude || !store.longitude) { uni.showToast({ title: "位置信息不完整", icon: "none" }); return; }
      const that = this;
      uni.showActionSheet({
        itemList: ["高德地图导航", "百度地图导航", "系统地图"],
        success(res) {
          const lat = store.latitude, lng = store.longitude, name = encodeURIComponent(store.storeName);
          if (res.tapIndex === 0) {
            const url = "https://uri.amap.com/navigation?from=" + that.currentLng + "," + that.currentLat + "(我的位置)&to=" + lng + "," + lat + "(" + name + ")&mode=car&coordinate=gaode";
            uni.setClipboardData({ data: url, success() { uni.showModal({ title: "已复制高德导航链接", content: "请打开浏览器粘贴地址", showCancel: false }); } });
          } else if (res.tapIndex === 1) {
            const url = "baidumap://map/direction?origin=myLocation&destination=latlng:" + lat + "," + lng + "|name:" + name + "&coord_type=gcj02&mode=driving";
            uni.setClipboardData({ data: url, success() { uni.showModal({ title: "已复制百度导航链接", content: "请打开浏览器粘贴地址", showCancel: false }); } });
          } else {
            uni.openLocation({ latitude: lat, longitude: lng, name: store.storeName, address: store.address, scale: 18 });
          }
        }
      });
    },
    changeRadius(e) { this.radius = Number(e.detail.value); this.searchNearby(); },
    toggleView() { this.showMap = !this.showMap; if (this.showMap && !this.located && !this.currentLat) this.getLocationAndSearch(); }
  }
}
</script>
<style scoped>
.page{display:flex;flex-direction:column;height:100vh;overflow:hidden;background:#faf7ef}
.map-wrap{height:500rpx;width:100%;flex-shrink:0}.map{width:100%;height:100%}
.radius-bar{display:flex;align-items:center;gap:12rpx;padding:12rpx 28rpx;background:#fff;border-bottom:1rpx solid #f0ebe3;flex-shrink:0}
.radius-label{font-size:24rpx;color:#8f7366;white-space:nowrap}.radius-bar slider{flex:1}
.radius-val{font-size:26rpx;color:#e8927c;font-weight:600;min-width:60rpx;text-align:right}
.view-toggle{display:flex;justify-content:space-between;align-items:center;padding:12rpx 28rpx;background:#fff;flex-shrink:0}
.view-toggle text:first-child{font-size:26rpx;color:#e8927c;font-weight:600}.store-count{font-size:24rpx;color:#8f7366}
.error-tip{padding:12rpx 28rpx;background:#fff7ef;color:#a96c54;font-size:24rpx;border-top:1rpx solid #f0ebe3;border-bottom:1rpx solid #f0ebe3}
.store-list{flex:1;padding:0 28rpx 20rpx}
.card{background:#fff;border-radius:14rpx;padding:20rpx;margin-bottom:16rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.sr{display:flex;gap:18rpx}.si{width:150rpx;height:150rpx;border-radius:10rpx;flex-shrink:0}
.sc{flex:1;display:flex;flex-direction:column;justify-content:space-between;min-height:150rpx}
.sn{font-size:30rpx;font-weight:700;color:#3d2e26}.sm{display:flex;gap:16rpx;font-size:24rpx;color:#8f7366}
.sa{font-size:24rpx;color:#8f7366}.st{display:flex;gap:8rpx;flex-wrap:wrap}
.tg{font-size:20rpx;background:rgba(232,146,124,.1);color:#e8927c;padding:4rpx 12rpx;border-radius:6rpx}
.detail-mask{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.3);z-index:90}
.detail-sheet{position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;z-index:100;padding:32rpx 28rpx 60rpx;max-height:60vh;overflow-y:auto}
.ds-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:24rpx}
.ds-name{font-size:36rpx;font-weight:700;color:#3d2e26}.ds-close{font-size:36rpx;color:#bbb;padding:8rpx}
.ds-body{margin-bottom:32rpx}.ds-row{display:flex;gap:16rpx;padding:12rpx 0;border-bottom:1rpx solid #f5f0eb}
.ds-label{font-size:26rpx;color:#8f7366;width:80rpx;flex-shrink:0}.ds-value{font-size:26rpx;color:#3d2e26;flex:1}
.ds-actions{display:flex;gap:16rpx}.ds-btn{flex:1;height:80rpx;line-height:80rpx;text-align:center;font-size:28rpx;border-radius:12rpx;background:#f5f0eb;color:#3d2e26;border:none}.ds-btn.primary{background:#e8927c;color:#fff}
.empty-tip{flex:1;display:flex;align-items:center;justify-content:center;font-size:28rpx;color:#bbb;padding:60rpx 0}
</style>
