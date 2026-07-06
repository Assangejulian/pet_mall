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
      <view>\u8bf7\u524d\u5f80\u201c\u6211\u7684\u5730\u5740\u201d\u6dfb\u52a0\u6536\u8d27\u5730\u5740\uff08\u70b9\u6b64\u524d\u5f80\uff09</view>
      <view class="arrow">></view>
    </view>
    <view class="section items-sec">
      <view class="title">\u5546\u54c1\u660e\u7ec6</view>
      <view v-for="item in items" :key="item.id" class="item">
        <image class="img" :src="item.productInfo?.mainImage || item.productInfo?.image || item.image" mode="aspectFill" />
        <view class="info">
          <view class="name">{{item.productInfo?.productName || item.productInfo?.name || item.name}}</view>
          <view class="bottom"><text class="price">\u00a5{{item.productInfo?.price || item.price}}</text><text class="qty">x{{item.quantity}}</text></view>
        </view>
      </view>
    </view>
    <!-- \u4f1a\u5458\u4f18\u60e0 -->
    <view class="section discount-sec" v-if="discount">
      <view class="title">\u4f1a\u5458\u4f18\u60e0</view>
      <view class="d-row">
        <text class="level-tag">{{discount.levelName}}</text>
        <text class="d-desc">{{discount.desc}}</text>
      </view>
      <view class="d-row">
        <text>\u5546\u54c1\u91d1\u989d</text>
        <text>\u00a5{{totalAmount}}</text>
      </view>
      <view class="d-row d-saving">
        <text>\u4f1a\u5458\u4f18\u60e0</text>
        <text>-\u00a5{{discountAmount}}</text>
      </view>
    </view>
    <view class="b-bar">
      <view class="sum">\u5b9e\u4ed8\u6b3e\uff1a<text class="price">\u00a5{{payAmount}}</text></view>
      <view class="btn-submit" @tap="submitOrder">\u63d0\u4ea4\u8ba2\u5355</view>
    </view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
import cartApi from "@/utils/api/cart";
import userApi from "@/utils/api/user";
export default {
  data() { return { items: [], totalAmount: "0.00", address: null, remark: "", discount: null, payAmount: "0.00", discountAmount: "0.00" }; },
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
        this.loadUserDiscount();
      });
    },
    loadUserDiscount() {
      userApi.getProfile().then(user => {
        if (user && user.memberLevel && user.memberLevel > 0) {
          const rates = [1, 0.95, 0.9, 0.85];
          const rate = rates[user.memberLevel] || 1;
          const names = ["", "\u94f6\u5361\u4f1a\u5458", "\u91d1\u5361\u4f1a\u5458", "\u94bb\u77f3\u4f1a\u5458"];
          const descs = ["", "95\u6298", "9\u6298", "85\u6298"];
          const total = parseFloat(this.totalAmount);
          const pay = (total * rate).toFixed(2);
          const disc = (total - parseFloat(pay)).toFixed(2);
          this.discount = { levelName: names[user.memberLevel] || "\u4f1a\u5458", desc: descs[user.memberLevel] || "" };
          this.payAmount = pay;
          this.discountAmount = disc;
        } else {
          this.payAmount = this.totalAmount;
        }
      }).catch(() => { this.payAmount = this.totalAmount; });
    },
    goAddAddress() { uni.navigateTo({ url: "/subpages/address/list?select=1" }); },
    onAddressSelected(addr) { this.address = addr; },
    submitOrder() {
      if (!this.address) { uni.showToast({ title: "\u8bf7\u9009\u62e9\u6536\u8d27\u5730\u5740", icon: "none" }); return; }
      if (!this.items.length) { uni.showToast({ title: "\u6ca1\u6709\u9009\u4e2d\u7684\u5546\u54c1", icon: "none" }); return; }
      const dto = { addressId: this.address.id, items: this.items.map(i => ({ productId: i.productId || i.productInfo?.id || i.id, quantity: i.quantity })), remark: this.remark };
      const amount = this.payAmount || this.totalAmount;
      orderApi.create(dto).then(res => {
        const oid = res.orderId || res.id || res;
        uni.showToast({ title: "\u4e0b\u5355\u6210\u529f", icon: "success" });
        orderApi.detail(oid).then(order => {
          uni.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=" + (order.orderNo || "") + "&amount=" + (order.payAmount || amount) });
        }).catch(() => {
          uni.redirectTo({ url: "/subpages/order/pay?orderId=" + oid + "&orderNo=&amount=" + amount });
        });
      }).catch(err => { uni.showToast({ title: (err && err.message) || "\u4e0b\u5355\u5931\u8d25", icon: "none" }); });
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
.discount-sec{flex-direction:column;align-items:stretch}.discount-sec .title{font-size:30rpx;font-weight:600;margin-bottom:16rpx;border-bottom:1px solid #eee;padding-bottom:12rpx}
.d-row{display:flex;justify-content:space-between;align-items:center;padding:6rpx 0;font-size:26rpx;color:#333}
.level-tag{background:#f5e6d0;color:#b8860b;padding:4rpx 16rpx;border-radius:20rpx;font-size:24rpx;font-weight:600}
.d-desc{color:#e8927c;font-size:26rpx;font-weight:600}.d-saving{color:#e8927c}
.b-bar{position:fixed;bottom:0;left:0;right:0;height:100rpx;background:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 32rpx;padding-bottom:env(safe-area-inset-bottom);box-shadow:0 -2px 10px rgba(0,0,0,.05)}
.sum{font-size:28rpx}.btn-submit{background:#547b68;color:#fff;padding:16rpx 48rpx;border-radius:40rpx;font-size:30rpx;font-weight:600}
</style>
