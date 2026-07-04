<template>
  <view class="page">
    <view class="order-header" v-if="order">
      <text class="order-no">订单号 {{order.orderNo}}</text>
      <text class="order-status" :class="'s'+order.orderStatus">{{statusLabels[order.orderStatus]}}</text>
    </view>
    <view class="status-hint" v-if="order && order.orderStatus === 1">
      <text class="hint-icon">铃</text>
      <view class="hint-text">
        <text class="hint-title">已支付，等待商家发货</text>
        <text class="hint-desc">商家将尽快为你安排发货，请耐心等待</text>
      </view>
    </view>
    <view class="timeline-card" v-if="order">
      <view class="tl-row"><text class="tl-label">下单时间</text><text class="tl-value">{{order.createTime}}</text></view>
      <view class="tl-row" v-if="order.payTime"><text class="tl-label">支付时间</text><text class="tl-value">{{order.payTime}}</text></view>
      <view class="tl-row" v-if="order.shipTime"><text class="tl-label">发货时间</text><text class="tl-value">{{order.shipTime}}</text></view>
      <view class="tl-row" v-if="order.receiveTime"><text class="tl-label">收货时间</text><text class="tl-value">{{order.receiveTime}}</text></view>
      <view class="tl-row" v-if="order.cancelReason"><text class="tl-label">取消原因</text><text class="tl-value warn">{{order.cancelReason}}</text></view>
    </view>
    <view class="items-card" v-if="items.length">
      <text class="sec-title">商品信息</text>
      <view v-for="item in items" :key="item.id" class="item-row">
        <image class="item-img" :src="item.productImage" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{item.productName}}</text>
          <text class="item-price">¥{{item.price}}</text>
          <text class="item-qty">x{{item.quantity}}</text>
        </view>
      </view>
    </view>
    <view class="address-card" v-if="addressParsed">
      <text class="sec-title">收货地址</text>
      <text class="addr-name">{{addressParsed.receiverName}}  {{addressParsed.phone}}</text>
      <text class="addr-detail">{{addressText}}</text>
    </view>
    <view class="amount-card" v-if="order">
      <view class="amt-row"><text>商品金额</text><text>¥{{order.totalAmount}}</text></view>
      <view class="amt-row"><text>优惠</text><text>-¥{{order.discountAmount || '0.00'}}</text></view>
      <view class="amt-row total"><text>实付</text><text>¥{{order.payAmount}}</text></view>
    </view>
    <view class="bottom-bar" v-if="order">
      <button class="btn-primary" @tap="payOrder" v-if="order.orderStatus == 0">去支付</button>
      <button class="btn-outline" @tap="directRefund" v-if="order.orderStatus == 1">急速退款</button>
      <button class="btn-primary" @tap="confirmReceive" v-if="order.orderStatus == 2">确认收货</button>
      <button class="btn-primary" @tap="evaluateOrder" v-if="order.orderStatus == 3">去评价</button>
      <button class="btn-outline" @tap="applyRefund" v-if="order.orderStatus == 3 || order.orderStatus == 4">退款</button>
      <button class="btn-outline" @tap="callService">联系客服</button>
    </view>
    <view class="empty-tip" v-if="!loading && !order">订单不存在</view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
export default {
  data() {
    return {
      order: null, items: [], loading: true, addressParsed: null, addressText: "",
      statusLabels: {"0":"待支付","1":"已支付","2":"已发货","3":"已收货","4":"已评价","-1":"已取消","-2":"退单中","-3":"退单已通过"}
    };
  },
  onLoad(options) {
    if (!options.id) { uni.showToast({title:"订单ID缺失",icon:"none"}); uni.navigateBack(); return; }
    this.loadOrder(options.id);
  },
  methods: {
    loadOrder(id) {
      this.loading = true;
      Promise.all([orderApi.detail(id), orderApi.items(id)]).then(([order, items]) => {
        const normalizedItems = (items || []).map(i => ({ id: i.id, productName: i.productName || "", productImage: i.productImage || "", price: i.price || "0.00", quantity: i.quantity || 1 }));
        let addressParsed = null;
        if (order.addressSnapshot) { try { addressParsed = JSON.parse(order.addressSnapshot); } catch(e) {} }
        const addressText = addressParsed ? [addressParsed.province, addressParsed.city, addressParsed.district, addressParsed.detail].filter(Boolean).join("") : "";
        this.order = order; this.items = normalizedItems; this.addressParsed = addressParsed; this.addressText = addressText; this.loading = false;
      }).catch(() => { this.loading = false; uni.showToast({title:"加载失败",icon:"none"}); });
    },
    payOrder() { if (!this.order?.orderNo) return; uni.navigateTo({ url: '/subpages/order/pay?orderId=' + this.order.id + '&orderNo=' + this.order.orderNo + '&amount=' + (this.order.payAmount || '0.00') }); },
    confirmReceive() {
      uni.showModal({title:"确认收货",content:"确定已收到商品吗？",success:(r) => { if(r.confirm) orderApi.receive(this.order.id).then(() => { uni.showToast({title:"已确认收货",icon:"success"}); this.loadOrder(this.order.id); }); }});
    },
    evaluateOrder() { uni.navigateTo({ url: "/subpages/order/evaluate?id=" + this.order.id }); },
    directRefund() {
      uni.showModal({title:"急速退款",content:"确定要急速退款吗？",success:(r) => { if(r.confirm) orderApi.refundDirect(this.order.id).then(() => { uni.showToast({title:"退款成功",icon:"success"}); this.loadOrder(this.order.id); }); }});
    },
    applyRefund() { uni.navigateTo({ url: "/subpages/order/refund/refund?id=" + this.order.id }); },
    callService() { uni.showToast({title:"客服电话: 400-000-0000",icon:"none"}); }
  }
}
</script>
<style scoped>
.page{padding:0 0 120rpx;background:#faf7ef;min-height:100vh}
.order-header{display:flex;justify-content:space-between;align-items:center;padding:24rpx 28rpx;background:#fff;border-bottom:1rpx solid #f0ebe3}
.order-no{font-size:24rpx;color:#8f7366}.order-status{font-size:26rpx;font-weight:600;color:#e8927c}
.order-status.s1{color:#1565c0}.order-status.s2{color:#547b68}.order-status.s3{color:#547b68}
.order-status.s-1{color:#8f7366}.order-status.s-2{color:#c0392b}
.timeline-card,.items-card,.amount-card,.address-card{background:#fff;margin:16rpx 20rpx;border-radius:14rpx;padding:20rpx 24rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04)}
.tl-row{display:flex;justify-content:space-between;padding:8rpx 0}
.tl-label{font-size:24rpx;color:#8f7366}.tl-value{font-size:24rpx;color:#3d2e26}.tl-value.warn{color:#c0392b}
.sec-title{display:block;font-size:26rpx;font-weight:600;color:#3d2e26;margin-bottom:16rpx}
.item-row{display:flex;gap:16rpx;padding:12rpx 0;border-bottom:1rpx solid #f5f0eb}.item-img{width:120rpx;height:120rpx;border-radius:8rpx;flex-shrink:0}
.item-info{flex:1;display:flex;flex-direction:column;justify-content:center;gap:4rpx}
.item-name{font-size:26rpx;color:#3d2e26;font-weight:500}.item-price{font-size:24rpx;color:#e8927c}.item-qty{font-size:22rpx;color:#bbb}
.addr-name{display:block;font-size:28rpx;font-weight:600;color:#3d2e26;margin-bottom:8rpx}.addr-detail{display:block;font-size:24rpx;color:#8f7366;line-height:1.5}
.amt-row{display:flex;justify-content:space-between;padding:8rpx 0;font-size:24rpx;color:#8f7366}
.amt-row.total{font-size:28rpx;font-weight:700;color:#e8927c;border-top:1rpx solid #f5f0eb;margin-top:8rpx;padding-top:16rpx}
.bottom-bar{position:fixed;bottom:0;left:0;right:0;padding:16rpx 28rpx 40rpx;background:#fff;box-shadow:0 -2rpx 8rpx rgba(0,0,0,.06);display:flex;gap:16rpx}
.btn-outline{flex:1;height:80rpx;line-height:80rpx;text-align:center;font-size:28rpx;border-radius:12rpx;background:#fff;color:#e8927c;border:2rpx solid #e8927c}
.status-hint{display:flex;align-items:center;gap:16rpx;margin:16rpx 20rpx;padding:20rpx 24rpx;background:linear-gradient(135deg,#e8f4fd,#d0eafc);border-radius:14rpx}
.hint-icon{font-size:48rpx;flex-shrink:0}.hint-text{flex:1}.hint-title{display:block;font-size:28rpx;font-weight:600;color:#1565c0;margin-bottom:4rpx}
.hint-desc{display:block;font-size:22rpx;color:#5a8ab5}.empty-tip{text-align:center;padding:120rpx 0;font-size:28rpx;color:#bbb}
</style>

