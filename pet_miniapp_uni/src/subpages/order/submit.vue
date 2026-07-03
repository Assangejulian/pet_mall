<template>
  <view class="page">
    <view class="section address-sec" v-if="address" @tap="goAddAddress">
      <view class="addr-info">
        <view class="user">{{address.receiverName}} <text class="phone">{{address.phone}}</text></view>
        <view class="detail">{{address.province}} {{address.city}} {{address.district}} {{address.detail}}</view>
      </view>
      <view class="arrow">></view>
    </view>
    <view class="section address-sec empty" v-else @tap="goAddAddress">
      <view>请前往"我的地址"添加收货地址（点此前往）</view>
      <view class="arrow">></view>
    </view>
    <view class="section items-sec">
      <view class="title">商品明细</view>
      <view v-for="item in items" :key="item.id" class="item">
        <image class="img" :src="item.productInfo?.mainImage || item.productInfo?.image || item.image" mode="aspectFill" />
        <view class="info">
          <view class="name">{{item.productInfo?.productName || item.productInfo?.name || item.name}}</view>
          <view class="bottom"><text class="price">¥{{item.productInfo?.price || item.price}}</text><text class="qty">x{{item.quantity}}</text></view>
        </view>
      </view>
    </view>
    <view class="b-bar">
      <view class="sum">实付款：<text class="price">¥{{totalAmount}}</text></view>
      <view class="btn-submit" @tap="submitOrder">提交订单</view>
    </view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
import cartApi from "@/utils/api/cart";
import userApi from "@/utils/api/user";
export default {
  data() { return { items: [], totalAmount: "0.00", address: null, remark: "" }; },
  onShow() { this.loadAddress(); this.loadItems(); },
  methods: {
    loadAddress() {
      userApi.addressList().then(res => {
        const list = Array.isArray(res) ? res : [];
        this.address = list.find(a => a.defaulted === 1) || list[0] || null;
      });
    },
    loadItems() {
      cartApi.list().then(res => {
        const checked = (res || []).filter(i => i.checked === 1 || i.checked === true);
        let total = 0;
        checked.forEach(i => { const p = i.productInfo || {}; total += parseFloat(p.price || i.price || 0) * i.quantity; });
        this.items = checked; this.totalAmount = total.toFixed(2);
      });
    },
    goAddAddress() { uni.navigateTo({ url: "/subpages/address/list?select=1" }); },
    onAddressSelected(addr) { this.address = addr; },
    submitOrder() {
      if (!this.address) { uni.showToast({ title: "请选择收货地址", icon: "none" }); return; }
      if (!this.items.length) { uni.showToast({ title: "没有选中的商品", icon: "none" }); return; }
      const dto = { addressId: this.address.id, items: this.items.map(i => ({ productId: i.productId || i.productInfo?.id || i.id, quantity: i.quantity })), remark: this.remark };
      orderApi.create(dto).then(res => {
        uni.showToast({ title: "下单成功", icon: "success" });
        setTimeout(() => { uni.redirectTo({ url: "/subpages/order/detail/detail?id=" + (res.orderId || res.id || res) }); }, 1000);
      }).catch(err => { uni.showToast({ title: (err && err.message) || "下单失败", icon: "none" }); });
    }
  }
}
</script>
<style scoped>
.page{background:#f7f7f7;min-height:100vh;padding-bottom:120rpx}
.section{background:#fff;margin:20rpx;border-radius:16rpx;padding:24rpx;display:flex;align-items:center;justify-content:space-between}
.address-sec .addr-info{flex:1}.user{font-size:32rpx;font-weight:600;margin-bottom:8rpx}
.phone{font-size:28rpx;color:#666;font-weight:normal;margin-left:12rpx}.detail{font-size:26rpx;color:#333;line-height:1.4}
.arrow{color:#ccc;font-size:32rpx;margin-left:20rpx}
.items-sec{flex-direction:column;align-items:stretch}.items-sec .title{font-size:30rpx;font-weight:600;margin-bottom:20rpx;border-bottom:1px solid #eee;padding-bottom:16rpx}
.item{display:flex;padding:16rpx 0;border-bottom:1px dashed #f0f0f0}.img{width:120rpx;height:120rpx;border-radius:8rpx;background:#f0f0f0;margin-right:20rpx}
.info{flex:1;display:flex;flex-direction:column;justify-content:space-between}.name{font-size:28rpx;color:#333}
.bottom{display:flex;justify-content:space-between;align-items:baseline}.price{color:#e8927c;font-size:32rpx;font-weight:600}
.qty{color:#999;font-size:26rpx}
.b-bar{position:fixed;bottom:0;left:0;right:0;height:100rpx;background:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 32rpx;padding-bottom:env(safe-area-inset-bottom);box-shadow:0 -2px 10px rgba(0,0,0,.05)}
.sum{font-size:28rpx}.btn-submit{background:#547b68;color:#fff;padding:16rpx 48rpx;border-radius:40rpx;font-size:30rpx;font-weight:600}
</style>
