<template>
<view class="page">
<view class="evaluate-list" v-if="order && order.items">
<view class="evaluate-item" v-for="(item, index) in order.items" :key="item.id">
<view class="product-info"><image class="product-img" :src="item.productImage" mode="aspectFill" /><view class="product-detail"><text class="product-name">{{item.productName}}</text></view></view>
<view class="evaluate-content"><textarea class="evaluate-textarea" placeholder="写下你的真实评价..." maxlength="200" :value="item.content" :data-index="index" @input="onInput" /></view>
</view>
</view>
<view class="bottom-bar"><button class="btn-primary" @tap="submitEvaluate" :disabled="submitting">提交评价</button></view>
</view>
</template>
<script>
import request from "@/utils/request";
import orderApi from "@/utils/api/order";
export default {
data() { return { order: null, submitting: false }; },
onLoad(options) { if (!options.id) { uni.navigateBack(); return; } this.loadOrder(options.id); },
methods: {
loadOrder(id) { orderApi.detail(id).then(res => { if (res && res.items) res.items.forEach(i => i.content = ""); this.order = res; }).catch(() => { uni.showToast({title:"加载失败",icon:"none"}); }); },
onInput(e) { const idx = e.currentTarget.dataset.index; this.order.items[idx].content = e.detail.value; },
submitEvaluate() {
if (this.submitting) return;
const items = (this.order?.items || []).map(i => ({ orderItemId: i.id, content: i.content || "默认好评" }));
if (items.some(i => !i.content.trim())) { uni.showToast({title:"请填写所有评价",icon:"none"}); return; }
this.submitting = true;
request.post("/api/order/evaluate", { orderId: this.order.id, items }).then(() => { uni.showToast({title:"评价成功",icon:"success"}); setTimeout(()=>uni.navigateBack(),1500); }).catch(() => { this.submitting = false; uni.showToast({title:"评价失败",icon:"none"}); });
}
},

}
</script>
<style scoped>
.page{padding:30rpx;background-color:#f7f7f7;min-height:100vh}.evaluate-list{padding-bottom:120rpx}
.evaluate-item{background-color:#fff;border-radius:16rpx;padding:30rpx;margin-bottom:30rpx}
.product-info{display:flex;align-items:center;margin-bottom:20rpx}.product-img{width:100rpx;height:100rpx;border-radius:8rpx;margin-right:20rpx;background-color:#f0f0f0}
.product-detail{flex:1}.product-name{font-size:28rpx;font-weight:500;color:#333}
.evaluate-content{background-color:#fafafa;border-radius:12rpx;padding:20rpx}
.evaluate-textarea{width:100%;height:200rpx;font-size:28rpx;line-height:1.5;color:#333}
.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:20rpx 30rpx;padding-bottom:calc(20rpx + env(safe-area-inset-bottom));background-color:#fff}
.btn-primary{background-color:#e8927c;color:#fff;border-radius:40rpx;font-size:32rpx;font-weight:600;width:100%}
</style>
