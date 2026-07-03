<template>
  <view class="page">
    <view class="refund-form">
      <text class="form-title">退单申请</text>
      <view class="form-group"><text class="label">退单原因说明：</text><textarea class="reason-input" placeholder="请详细说明您申请退款的原因..." @input="onInputReason" :value="reason"></textarea></view>
      <button class="submit-btn" @tap="submitRefund" :disabled="submitting">提交申请</button>
    </view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
export default {
  data() { return { orderId: null, reason: "", submitting: false }; },
  onLoad(options) {
    if (!options.id) { uni.showToast({title:"订单ID缺失",icon:"none"}); setTimeout(()=>uni.navigateBack(),1500); return; }
    this.orderId = options.id;
  },
  methods: {
    onInputReason(e) { this.reason = e.detail.value; },
    submitRefund() {
      if (!this.reason.trim()) { uni.showToast({title:"请填写退单原因",icon:"none"}); return; }
      this.submitting = true;
      orderApi.refundApply(this.orderId, this.reason).then(() => { uni.showToast({title:"申请已提交",icon:"success"}); setTimeout(()=>uni.navigateBack({delta:1}),1500); }).catch(() => { this.submitting = false; uni.showToast({title:"提交失败",icon:"none"}); });
    }
  }
}
</script>
<style scoped>
.page{padding:30rpx;background-color:#f7f7f7;min-height:100vh}
.refund-form{background-color:#fff;border-radius:16rpx;padding:30rpx}
.form-title{font-size:32rpx;font-weight:bold;color:#333;margin-bottom:30rpx;display:block}
.form-group{margin-bottom:40rpx}.label{font-size:28rpx;color:#666;margin-bottom:20rpx;display:block}
.reason-input{width:100%;height:240rpx;background-color:#f5f5f5;border-radius:12rpx;padding:20rpx;font-size:28rpx;box-sizing:border-box}
.submit-btn{background-color:#e65100;color:#fff;border-radius:40rpx;font-size:32rpx;height:80rpx;line-height:80rpx}
.submit-btn[disabled]{background-color:#ccc;color:#fff}
</style>
