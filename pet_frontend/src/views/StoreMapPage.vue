<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { searchNearby, type NearbyStore } from '../api/store'

const stores = ref<NearbyStore[]>([])
const loading = ref(true)
const currentLat = ref(24.4547)
const currentLng = ref(118.0822)
const radius = ref(5)
const selectedStore = ref<NearbyStore | null>(null)
const errorMsg = ref('')

// 地图坐标范围
const mapBounds = computed(() => {
  if (!stores.value.length) return null
  const lats = stores.value.map(s => s.latitude).filter(Boolean)
  const lngs = stores.value.map(s => s.longitude).filter(Boolean)
  return {
    minLat: Math.min(...lats, currentLat.value),
    maxLat: Math.max(...lats, currentLat.value),
    minLng: Math.min(...lngs, currentLng.value),
    maxLng: Math.max(...lngs, currentLng.value)
  }
})

// 标准化门店坐标到百分比位置（用于CSS地图）
function calcPosition(store: NearbyStore) {
  if (!mapBounds.value) return { left: '50%', top: '50%' }
  const b = mapBounds.value
  const pad = 0.05 // 5% padding
  const latRange = (b.maxLat - b.minLat) || 0.01
  const lngRange = (b.maxLng - b.minLng) || 0.01
  const top = ((b.maxLat - store.latitude) / latRange) * 80 + 10
  const left = ((store.longitude - b.minLng) / lngRange) * 80 + 10
  return { left: left + '%', top: top + '%' }
}

onMounted(async () => {
  // 获取定位
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        currentLat.value = pos.coords.latitude
        currentLng.value = pos.coords.longitude
        fetchStores()
      },
      () => {
        fetchStores() // 定位失败用默认坐标
      },
      { enableHighAccuracy: true, timeout: 5000 }
    )
  } else {
    fetchStores()
  }
})

async function fetchStores() {
  loading.value = true
  errorMsg.value = ''
  try {
    stores.value = await searchNearby({
      latitude: currentLat.value,
      longitude: currentLng.value,
      radius: radius.value
    })
  } catch (e: any) {
    errorMsg.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function selectStore(store: NearbyStore) {
  selectedStore.value = store
}

function closeDetail() {
  selectedStore.value = null
}

/** 高德地图导航 */
function navigateWithGaode(store: NearbyStore) {
  const url = https://uri.amap.com/navigation?to=,,&mode=car&coordinate=gaode
  window.open(url, '_blank')
}

/** 百度地图导航 */
function navigateWithBaidu(store: NearbyStore) {
  const url = https://api.map.baidu.com/direction?destination=latlng:,|name:&coord_type=gcj02&mode=driving&output=html&src=petStoreApp
  window.open(url, '_blank')
}

function formatDistance(d: number | undefined): string {
  if (d === undefined || d === null) return ''
  if (d < 1) return Math.round(d * 1000) + 'm'
  return d.toFixed(1) + 'km'
}

function changeRadius(delta: number) {
  const newVal = radius.value + delta
  if (newVal >= 1 && newVal <= 50) {
    radius.value = newVal
    fetchStores()
  }
}
</script>

<template>
  <section class="store-map-page">
    <!-- 顶部搜索栏 -->
    <div class="top-bar">
      <h2 class="page-title">附近门店</h2>
      <div class="radius-control">
        <button class="radius-btn" @click="changeRadius(-1)">−</button>
        <span class="radius-text">{{ radius }}km</span>
        <button class="radius-btn" @click="changeRadius(1)">+</button>
      </div>
    </div>

    <!-- 地图区域 -->
    <div class="map-container">
      <div class="city-map">
        <div class="map-inner">
          <!-- 当前位置 -->
          <span class="pin current-location" style="left:50%;top:50%">
            <i>我的位置</i>
          </span>
          <!-- 门店标注 -->
          <span
            v-for="store in stores"
            :key="store.id"
            class="pin store-pin"
            :style="calcPosition(store)"
            @click="selectStore(store)"
            :title="store.storeName"
          >
            <i>{{ store.storeName }}</i>
          </span>
          <div class="map-placeholder">
            <p>📍 附近门店分布</p>
            <small>门店位置基于 GPS 坐标展示</small>
          </div>
        </div>
      </div>
    </div>

    <!-- 门店列表 -->
    <div class="store-list">
      <div class="list-header">
        <span>共 {{ stores.length }} 家门店</span>
        <button class="refresh-btn" @click="fetchStores">刷新</button>
      </div>

      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else-if="errorMsg" class="error-state">{{ errorMsg }}</div>

      <article
        v-for="store in stores"
        :key="store.id"
        class="store-card"
        :class="{ active: selectedStore?.id === store.id }"
        @click="selectStore(store)"
      >
        <div class="store-info">
          <h3 class="store-name">{{ store.storeName }}</h3>
          <p class="store-address">{{ store.address || [store.province, store.city, store.district].filter(Boolean).join(' ') || '地址待完善' }}</p>
          <div class="store-meta">
            <span class="distance" v-if="store.distance !== undefined">📏 {{ formatDistance(store.distance) }}</span>
            <span class="products" v-if="store.productCount">🛍️ {{ store.productCount }} 件商品</span>
            <span class="phone" v-if="store.storePhone">📞 {{ store.storePhone }}</span>
          </div>
        </div>
      </article>

      <div v-if="!loading && !stores.length && !errorMsg" class="empty-state">
        <p>附近暂无门店</p>
        <small>试试扩大搜索范围</small>
      </div>
    </div>

    <!-- 门店详情浮层 -->
    <div class="overlay" v-if="selectedStore" @click="closeDetail"></div>
    <div class="detail-panel" v-if="selectedStore">
      <div class="panel-header">
        <h3>{{ selectedStore.storeName }}</h3>
        <button class="close-btn" @click="closeDetail">✕</button>
      </div>
      <div class="panel-body">
        <div class="detail-row">
          <span class="label">地址</span>
          <span class="value">{{ selectedStore.address || '待完善' }}</span>
        </div>
        <div class="detail-row" v-if="selectedStore.storePhone">
          <span class="label">电话</span>
          <span class="value">{{ selectedStore.storePhone }}</span>
        </div>
        <div class="detail-row" v-if="selectedStore.distance !== undefined">
          <span class="label">距离</span>
          <span class="value">{{ formatDistance(selectedStore.distance) }}</span>
        </div>
        <div class="detail-row" v-if="selectedStore.storeDesc">
          <span class="label">简介</span>
          <span class="value">{{ selectedStore.storeDesc }}</span>
        </div>
      </div>
      <div class="panel-actions">
        <a class="nav-btn gaode" :href="https://uri.amap.com/navigation?to=,,&mode=car&coordinate=gaode" target="_blank">
          🗺️ 高德导航
        </a>
        <a class="nav-btn baidu" :href="https://api.map.baidu.com/direction?destination=latlng:,|name:&coord_type=gcj02&mode=driving&output=html&src=petStoreApp" target="_blank">
          🗺️ 百度导航
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped>
.store-map-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #3d2e26;
  margin: 0;
}
.radius-control {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f5f0eb;
  border-radius: 20px;
  padding: 4px;
}
.radius-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: #e8927c;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.radius-text {
  font-size: 15px;
  font-weight: 600;
  color: #3d2e26;
  min-width: 48px;
  text-align: center;
}

/* 地图 */
.map-container {
  margin-bottom: 24px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
}
.city-map {
  position: relative;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9, #a5d6a7);
  height: 360px;
  overflow: hidden;
}
.map-inner {
  position: relative;
  width: 100%;
  height: 100%;
}
.map-placeholder {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  opacity: .5;
  pointer-events: none;
}
.map-placeholder p {
  font-size: 18px;
  font-weight: 600;
  color: #2e7d32;
  margin: 0;
}
.map-placeholder small {
  font-size: 13px;
  color: #388e3c;
}
.pin {
  position: absolute;
  transform: translate(-50%, -100%);
  cursor: pointer;
  z-index: 10;
}
.pin::before {
  content: '📍';
  font-size: 24px;
  display: block;
  text-align: center;
}
.pin i {
  display: block;
  font-style: normal;
  background: rgba(255,255,255,.9);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #3d2e26;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(0,0,0,.15);
  margin-top: 2px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pin.current-location::before {
  content: '📍';
  font-size: 28px;
}
.pin.current-location i {
  background: #e8927c;
  color: #fff;
}
.pin.store-pin::before {
  content: '🏪';
}

/* 门店列表 */
.store-list {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06);
  overflow: hidden;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f5f0eb;
  font-size: 14px;
  color: #8f7366;
}
.refresh-btn {
  background: none;
  border: 1px solid #e8927c;
  color: #e8927c;
  padding: 4px 16px;
  border-radius: 12px;
  font-size: 13px;
  cursor: pointer;
}
.store-card {
  padding: 16px 20px;
  border-bottom: 1px solid #f8f5f0;
  cursor: pointer;
  transition: background .2s;
}
.store-card:hover,
.store-card.active {
  background: #fdf8f3;
}
.store-name {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #3d2e26;
}
.store-address {
  margin: 0 0 8px;
  font-size: 13px;
  color: #8f7366;
}
.store-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #aaa;
}

/* 详情浮层 */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,.3);
  z-index: 100;
}
.detail-panel {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 600px;
  background: #fff;
  border-radius: 20px 20px 0 0;
  z-index: 101;
  padding: 24px 24px 32px;
  box-shadow: 0 -4px 20px rgba(0,0,0,.12);
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.panel-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #3d2e26;
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #aaa;
  cursor: pointer;
  padding: 4px;
}
.panel-body {
  margin-bottom: 20px;
}
.detail-row {
  display: flex;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f5f0eb;
}
.detail-row:last-child {
  border-bottom: none;
}
.label {
  font-size: 13px;
  color: #8f7366;
  width: 48px;
  flex-shrink: 0;
}
.value {
  font-size: 14px;
  color: #3d2e26;
  flex: 1;
}
.panel-actions {
  display: flex;
  gap: 12px;
}
.nav-btn {
  flex: 1;
  text-align: center;
  padding: 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: opacity .2s;
}
.nav-btn:hover {
  opacity: .85;
}
.nav-btn.gaode {
  background: #1677ff;
  color: #fff;
}
.nav-btn.baidu {
  background: #4e6ef2;
  color: #fff;
}

/* 状态 */
.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #bbb;
  font-size: 14px;
}
.error-state {
  color: #e8927c;
}
.empty-state small {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: #ccc;
}
</style>
