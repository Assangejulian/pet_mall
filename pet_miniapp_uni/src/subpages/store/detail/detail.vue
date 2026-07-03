<template>
<view class="page">
<view class="store-hero" v-if="store">
<image class="hero-bg" :src="store.image" mode="aspectFill" v-if="store.image" /><view class="hero-bg hero-placeholder" v-else></view>
<view class="hero-overlay"></view><view class="hero-content"><text class="hero-name serif">{{store.storeName}}</text><text class="hero-status">营业中</text></view>
</view>
<view class="info-card" v-if="store">
<view class="info-row" @tap="navigateToStore"><text class="info-icon">📍</text><view class="info-text"><text class="info-label">地址</text><text class="info-value">{{store.address||"暂无"}}</text></view><text class="info-arrow">→</text></view>
<view class="info-row" v-if="store.storePhone" @tap="callPhone"><text class="info-icon">📓</text><view class="info-text"><text class="info-label">联系电话</text><text class="info-value">{{store.storePhone}}</text></view><text class="info-arrow">→</text></view>
</view>
<view class="desc-card" v-if="store && store.storeDesc"><text class="sec-title serif">门店介绍</text><text class="desc-text">{{store.storeDesc}}</text></view>
<view class="products-section" v-if="store">
<text class="sec-title serif">商品列表 ({{products.length}})</text>
<view class="product-grid" v-if="products.length">
<view class="product-card" v-for="item in products" :key="item.id" @tap="goProduct" :data-id="item.id">
<image class="p-img" :src="item.image" mode="aspectFill" /><view class="p-body"><text class="p-tag">{{item.productType==1?"宠物":"周边"}}</text><text class="p-name">{{item.name}}</text><text class="p-price">¥{{item.price}}</text></view>
</view>
</view>
<view class="products-empty" v-if="!loading && !products.length">暂无在售商品</view>
</view>
<view class="bottom-bar" v-if="store"><button class="nav-btn" @tap="navigateToStore"><text>📍 导航至此</text></button></view>
<view class="loading-tip" v-if="loading">加载中..</view>
<view class="empty-tip" v-if="!loading && !store">门店信息不存在</view>
</view>
</template>
<script>
import storeApi from "@/utils/api/store";
function normalizeStore(s){s=s||{};return{id:s.id,storeName:s.storeName||"",storePhone:s.storePhone||"",storeDesc:s.storeDesc||"",image:s.storeLogo||"",address:[s.province,s.city,s.district,s.address].filter(Boolean).join(""),longitude:Number(s.longitude)||0,latitude:Number(s.latitude)||0}}
function normalizeProduct(s){s=s||{};return{id:s.id,name:s.productName||s.name||"",price:s.price||"0.00",image:s.mainImage||s.image||"",category:s.category||"",stock:s.stock||0,productType:s.productType||1}}
export default{
data(){return{store:null,products:[],loading:true,_unloaded:false,_requestSeq:0,_storeId:null,_loadedOnce:false}},
onLoad(o){this._unloaded=false;if(!o.id){uni.showToast({title:"门店ID缺失",icon:"none"});uni.navigateBack();return}this._storeId=o.id;this._loadedOnce=false;this.loadStore(o.id)},
onShow(){this._unloaded=false;if(this._loadedOnce&&this._storeId)this.loadStore(this._storeId)},
onUnload(){this._unloaded=true;this._requestSeq++},
methods:{
loadStore(id,done){
if(!id){if(done)done();return}const seq=++this._requestSeq;this.loading=true;
Promise.all([storeApi.detail(id),storeApi.products(id)]).then(([s,p])=>{if(this._unloaded||seq!==this._requestSeq)return;this.store=normalizeStore(s);this.products=(p||[]).map(normalizeProduct);this.loading=false;this._loadedOnce=true;if(done)done();}).catch(()=>{if(this._unloaded||seq!==this._requestSeq)return;this.loading=false;this._loadedOnce=true;uni.showToast({title:"加载失败",icon:"none"});if(done)done();});
},
navigateToStore(){if(!this.store?.latitude||!this.store?.longitude){uni.showToast({title:"位置信息不完整",icon:"none"});return}uni.openLocation({latitude:Number(this.store.latitude),longitude:Number(this.store.longitude),name:this.store.storeName,address:this.store.address,scale:18})},
callPhone(){if(this.store?.storePhone)uni.makePhoneCall({phoneNumber:this.store.storePhone});else uni.showToast({title:"暂无联系电话",icon:"none"})},
goProduct(e){const id=e.currentTarget.dataset.id;if(id)uni.navigateTo({url:"/subpages/detail/detail?id="+encodeURIComponent(String(id))})}
}
}
</script>
<style scoped>
.page{padding-bottom:120rpx;background:#faf7ef;min-height:100vh}
.store-hero{height:360rpx;position:relative;overflow:hidden}.hero-bg{width:100%;height:100%;filter:blur(6rpx)}.hero-placeholder{background:linear-gradient(135deg,#e8c8b5,#9c7463)}
.hero-overlay{position:absolute;inset:0;background:linear-gradient(to bottom,rgba(0,0,0,.1),rgba(0,0,0,.5));z-index:1}
.hero-content{position:absolute;bottom:32rpx;left:32rpx;z-index:2;display:flex;flex-direction:column;gap:8rpx}
.hero-name{font-size:40rpx;font-weight:700;color:#fff;text-shadow:0 2rpx 8rpx rgba(0,0,0,.3)}
.hero-status{font-size:24rpx;color:#7dce94;background:rgba(0,0,0,.4);padding:4rpx 16rpx;border-radius:8rpx;align-self:flex-start}
.info-card{background:#fff;margin:24rpx 28rpx;border-radius:16rpx;overflow:hidden;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.info-row{display:flex;align-items:center;gap:20rpx;padding:24rpx 28rpx;border-bottom:1rpx solid #f5f0eb}
.info-icon{font-size:36rpx;width:48rpx;text-align:center}.info-text{flex:1;display:flex;flex-direction:column;gap:4rpx}
.info-label{font-size:22rpx;color:#8f7366}.info-value{font-size:28rpx;color:#3d2e26}.info-arrow{font-size:36rpx;color:#ccc}
.desc-card{background:#fff;margin:0 28rpx 24rpx;padding:24rpx 28rpx;border-radius:16rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.sec-title{font-size:30rpx;font-weight:700;color:#3d2e26;margin-bottom:16rpx;display:block}.desc-text{font-size:26rpx;color:#8f7366;line-height:1.6}
.products-section{margin:0 28rpx 24rpx}.product-grid{display:grid;grid-template-columns:1fr 1fr;gap:16rpx}
.product-card{background:#fff;border-radius:14rpx;overflow:hidden;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.p-img{width:100%;height:200rpx;display:block}.p-body{padding:12rpx 16rpx 16rpx;display:flex;flex-direction:column;gap:6rpx}
.p-tag{font-size:20rpx;color:#e8927c;background:rgba(232,146,124,.1);padding:2rpx 12rpx;border-radius:6rpx;align-self:flex-start}
.p-name{font-size:26rpx;color:#3d2e26;font-weight:600}.p-price{font-size:28rpx;color:#e8927c;font-weight:700}
.products-empty{padding:72rpx 0;text-align:center;font-size:26rpx;color:#bbb}
.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:16rpx 28rpx 40rpx;background:#fff;box-shadow:0 -2rpx 8rpx rgba(0,0,0,.06)}
.nav-btn{width:100%;height:88rpx;line-height:88rpx;text-align:center;font-size:30rpx;color:#fff;background:#e8927c;border-radius:44rpx;border:none}
.loading-tip,.empty-tip{padding:120rpx 0;text-align:center;font-size:28rpx;color:#bbb}
</style>
