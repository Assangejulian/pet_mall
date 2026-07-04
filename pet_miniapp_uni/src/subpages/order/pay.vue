<template>
  <view class="page">
    <view class="amount-section">
      <view class="amount-title">收款</view>
      <view class="amount-value"><text class="symbol">¥</text>{{amount}}</view>
      <view class="order-no">订单号 {{orderNo}}</view>
    </view>
    <view class="method-section">
      <view class="section-title">选择支付方式</view>
      <view class="method-item" @tap="selectMethod" data-method="wechat">
        <view class="method-left"><text class="method-icon wechat">W</text><text class="method-name">微信支付</text></view>
        <view class="method-right"><view class="radio" :class="{checked:payMethod==='wechat'}"></view></view>
      </view>
      <view class="method-item" @tap="selectMethod" data-method="alipay">
        <view class="method-left"><text class="method-icon alipay">支</text><text class="method-name">支付宝</text></view>
        <view class="method-right"><view class="radio" :class="{checked:payMethod==='alipay'}"></view></view>
      </view>
      <view class="method-item" @tap="selectMethod" data-method="mock">
        <view class="method-left"><text class="method-icon mock">M</text><text class="method-name">模拟支付</text><text class="method-desc">(开发测试)</text></view>
        <view class="method-right"><view class="radio" :class="{checked:payMethod==='mock'}"></view></view>
      </view>
    </view>
    <view class="bottom-bar"><view class="btn-pay" @tap="confirmPay">确认支付 ¥{{amount}}</view></view>
    <!-- 支付宝 webview 蒙层 -->
    <view class="alipay-overlay" v-if="showAlipay">
      <view class="alipay-card">
        <text class="alipay-title">即将跳转到支付宝</text>
        <text class="alipay-desc">请在支付宝中完成支付，支付后返回本页面查看结果</text>
        <view class="alipay-btns">
          <view class="btn-cancel" @tap="showAlipay=false;paying=false">取消</view>
          <view class="btn-go" @tap="openAlipay">去支付</view>
        </view>
      </view>
    </view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
export default {
  data() {
    return {
      orderId: "", orderNo: "", amount: "0.00",
      payMethod: "mock", paying: false,
      alipayUrl: "", showAlipay: false
    };
  },
  onLoad(options) {
    this.orderId = options.orderId || "";
    this.orderNo = options.orderNo || "";
    this.amount = options.amount || "0.00";
  },
  methods: {
    selectMethod(e) { this.payMethod = e.currentTarget.dataset.method; },
    confirmPay() {
      if (!this.orderNo) { uni.showToast({title:"订单信息异常",icon:"none"}); return; }
      if (this.paying) return;
      this.paying = true;

      // 前端标识 → 后端服务名映射
      const methodMap = { wechat: "WECHAT", alipay: "ALIPAY", mock: "mock" };
      const payMethod = methodMap[this.payMethod] || "WECHAT";

      orderApi.pay(this.orderNo, payMethod).then(res => {
        // --- 微信支付：调起 JSAPI ---
        if (this.payMethod === "wechat" && res && res.payParams) {
          let params;
          try { params = JSON.parse(res.payParams); } catch(e) { params = null; }
          if (params && params.paySign) {
            uni.requestPayment({
              timeStamp: params.timeStamp,
              nonceStr: params.nonceStr,
              package: params.package,
              signType: params.signType || "RSA",
              paySign: params.paySign,
              success: () => {
                uni.showToast({title:"支付成功",icon:"success"});
                setTimeout(() => uni.redirectTo({url:"/subpages/order/list"}), 1500);
              },
              fail: () => {
                uni.showToast({title:"支付取消",icon:"none"});
                this.paying = false;
              }
            });
            return;
          }
        }

        // --- 支付宝：显示跳转确认框 ---
        if (this.payMethod === "alipay" && res && res.payUrl) {
          this.alipayUrl = res.payUrl;
          this.showAlipay = true;
          return;
        }

        // --- fallback（模拟支付等）：直接成功 ---
        uni.showToast({title:"支付成功",icon:"success"});
        setTimeout(() => uni.redirectTo({url:"/subpages/order/list"}), 1500);
      }).catch(err => {
        this.paying = false;
        uni.showToast({title:(err && err.message) || "支付失败",icon:"none"});
      });
    },
    openAlipay() {
      this.showAlipay = false;
      // 小程序内用 webview 打开，H5 直接 location 跳转
      // #ifdef MP-WEIXIN
      uni.navigateTo({ url: "/subpages/order/webview?url=" + encodeURIComponent(this.alipayUrl) });
      // #endif
      // #ifdef H5
      window.location.href = this.alipayUrl;
      // #endif
      // #ifdef APP-PLUS
      plus.runtime.openURL(this.alipayUrl);
      // #endif
      this.paying = false;
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
.method-icon.wechat{background:#07c160}.method-icon.alipay{background:#1677ff}.method-icon.mock{background:#999}
.method-name{font-size:28rpx;color:#333}.method-desc{font-size:24rpx;color:#999}
.radio{width:36rpx;height:36rpx;border:2rpx solid #ccc;border-radius:50%;box-sizing:border-box}
.radio.checked{border:10rpx solid #e8927c}.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:20rpx 40rpx;background:#fff;padding-bottom:env(safe-area-inset-bottom)}
.btn-pay{background:#e8927c;color:#fff;text-align:center;height:88rpx;line-height:88rpx;border-radius:44rpx;font-size:32rpx;font-weight:600}
/* 支付宝跳转弹窗 */
.alipay-overlay{position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,.5);z-index:100;display:flex;align-items:center;justify-content:center}
.alipay-card{background:#fff;border-radius:16rpx;padding:48rpx 40rpx;width:600rpx;text-align:center}
.alipay-title{font-size:32rpx;font-weight:700;color:#333;display:block;margin-bottom:16rpx}
.alipay-desc{font-size:26rpx;color:#666;display:block;margin-bottom:40rpx;line-height:1.6}
.alipay-btns{display:flex;gap:24rpx}.btn-cancel,.btn-go{flex:1;height:80rpx;line-height:80rpx;border-radius:12rpx;font-size:28rpx}
.btn-cancel{background:#f5f5f5;color:#666}.btn-go{background:#1677ff;color:#fff}
</style>
