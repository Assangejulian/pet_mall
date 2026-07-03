<template>
  <view class="page">
    <view class="amount-section">
      <view class="amount-title">收款</view>
      <view class="amount-value"><text class="symbol">¥</text>{{amount}}</view>
      <view class="order-no">订单号{{orderNo}}</view>
    </view>
    <view class="method-section">
      <view class="section-title">选择支付方式</view>
      <view class="method-item" @tap="selectMethod" data-method="wechat">
        <view class="method-left"><text class="method-icon wechat">W</text><text class="method-name">微信支付</text></view>
        <view class="method-right"><view class="radio" :class="{checked:payMethod==='wechat'}"></view></view>
      </view>
      <view class="method-item" @tap="selectMethod" data-method="balance">
        <view class="method-left"><text class="method-icon balance">B</text><text class="method-name">余额支付</text><text class="method-desc">(余额)</text></view>
        <view class="method-right"><view class="radio" :class="{checked:payMethod==='balance'}"></view></view>
      </view>
    </view>
    <view class="bottom-bar"><view class="btn-pay" @tap="confirmPay">确认支付 ¥{{amount}}</view></view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
export default {
  data() { return { orderId: "", orderNo: "", amount: "0.00", payMethod: "wechat", paying: false }; },
  onLoad(options) { this.orderId = options.orderId || ""; this.orderNo = options.orderNo || ""; this.amount = options.amount || "0.00"; },
  methods: {
    selectMethod(e) { this.payMethod = e.currentTarget.dataset.method; },
    confirmPay() {
      if (!this.orderNo) { uni.showToast({title:"订单信息异常",icon:"none"}); return; }
      if (this.paying) return; this.paying = true;
      orderApi.pay(this.orderNo, this.payMethod).then(res => {
        if (this.payMethod === "wechat" && res && res.payUrl) {
          let params; try { params = JSON.parse(res.payUrl); } catch(e) { params = null; }
          if (params && params.paySign) {
            uni.requestPayment({ timeStamp: params.timeStamp, nonceStr: params.nonceStr, package: params.package, signType: params.signType || "RSA", paySign: params.paySign, success() { uni.showToast({title:"支付成功",icon:"success"}); setTimeout(()=>uni.redirectTo({url:"/subpages/order/list"}),1500); }, fail() { uni.showToast({title:"支付取消",icon:"none"}); that.paying = false; } });
            return;
          }
        }
        uni.showToast({title:"支付成功",icon:"success"}); setTimeout(()=>uni.redirectTo({url:"/subpages/order/list"}),1500);
      }).catch(() => { this.paying = false; uni.showToast({title:"支付失败",icon:"none"}); });
    }
  }
}
</script>
<style scoped>
.page{background:#f8f8f8;min-height:100vh;padding-bottom:120rpx}
.amount-section{background:#fff;padding:60rpx 40rpx;text-align:center;margin-bottom:20rpx}
.amount-title{font-size:28rpx;color:#666;margin-bottom:20rpx}.amount-value{font-size:72rpx;font-weight:700;color:#3d2a1e;margin-bottom:20rpx}
.symbol{font-size:40rpx;margin-right:8rpx}.order-no{font-size:24rpx;color:#999}
.method-section{background:#fff;padding:0 40rpx}.section-title{font-size:30rpx;font-weight:600;color:#333;padding:30rpx 0;border-bottom:1rpx solid #eee}
.method-item{display:flex;align-items:center;justify-content:space-between;padding:30rpx 0;border-bottom:1rpx solid #f5f5f5}
.method-left{display:flex;align-items:center;gap:20rpx}.method-icon{width:60rpx;height:60rpx;border-radius:8rpx;display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:32rpx}
.method-icon.wechat{background:#07c160}.method-icon.balance{background:#e8927c}
.method-name{font-size:28rpx;color:#333}.method-desc{font-size:24rpx;color:#999}
.radio{width:36rpx;height:36rpx;border:2rpx solid #ccc;border-radius:50%;box-sizing:border-box}
.radio.checked{border:10rpx solid #e8927c}.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:20rpx 40rpx;background:#fff;padding-bottom:env(safe-area-inset-bottom)}
.btn-pay{background:#e8927c;color:#fff;text-align:center;height:88rpx;line-height:88rpx;border-radius:44rpx;font-size:32rpx;font-weight:600}
</style>
