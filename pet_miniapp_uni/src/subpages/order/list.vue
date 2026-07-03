<template>
  <view class="page">
    <scroll-view class="tabs" scroll-x>
      <view v-for="tab in tabList" :key="tab.v" class="tab" :class="{on:active===tab.v}" @tap="switchTab" :data-v="tab.v">{{tab.l}}</view>
    </scroll-view>
    <view v-if="orders.length>0">
      <view v-for="item in orders" :key="item.id" class="card" @tap="goDetail" :data-id="item.id">
        <view class="oh"><text class="ono">{{item.orderNo}}</text><text class="os" :style="{color:sc[item.orderStatus]}">{{sl[item.orderStatus]}}</text></view>
        <view v-for="oItem in (item.items||[])" :key="oItem.id" class="oit">
          <image class="oimg" :src="oItem.productImage" mode="aspectFill" />
          <view class="oinfo"><text class="oinm">{{oItem.productName}}</text><text class="oiq">x{{oItem.quantity}}</text></view>
          <text class="oip">¥{{oItem.price}}</text>
        </view>
        <view class="of">合计 <text style="font-weight:700">¥{{item.payAmount || item.totalAmount}}</text></view>
        <view class="of-actions" v-if="item.orderStatus === 0">
          <button class="btn-pay" @tap.stop="payOrder" :data-id="item.id">去支付</button>
        </view>
      </view>
    </view>
    <view class="empty-tip" v-else><text>暂无订单</text></view>
  </view>
</template>
<script>
import orderApi from "@/utils/api/order";
export default {
  data() {
    return {
      orders: [], active: "",
      sl: {"0":"待支付","1":"已支付","2":"已发货","3":"已收货","4":"已评价","-1":"已取消","-2":"退单中","-3":"已退款","-4":"已取消/退单"},
      sc: {"0":"#e65100","1":"#1565c0","2":"#547b68","3":"#547b68","4":"#8f7366","-1":"#8f7366","-2":"#c0392b","-3":"#547b68","-4":"#8f7366"},
      tabList: [{l:"全部",v:""},{l:"待支付",v:"0"},{l:"已支付",v:"1"},{l:"待收货",v:"2"},{l:"待评价",v:"3"},{l:"已评价",v:"4"},{l:"退款/售后",v:"-99"}]
    };
  },
  onLoad(options) { if (options.status) this.active = options.status; },
  onShow() { this.loadOrders(); },
  methods: {
    loadOrders() {
      orderApi.listByStatus(this.active).then(res => {
        this.orders = Array.isArray(res) ? res : (res.records || []);
      }).catch(() => { this.orders = []; });
    },
    switchTab(e) { this.active = e.currentTarget.dataset.v; this.loadOrders(); },
    payOrder(e) {
      const id = e.currentTarget.dataset.id;
      const order = this.orders.find(o => o.id === id); if (!order) return;
      orderApi.pay(order.orderNo).then(() => { uni.showToast({title:"支付成功",icon:"success"}); this.loadOrders(); }).catch(() => { uni.showToast({title:"支付失败",icon:"none"}); });
    },
    goDetail(e) { uni.navigateTo({ url: "/subpages/order/detail/detail?id=" + e.currentTarget.dataset.id }); }
  }
}
</script>
<style scoped>
.page{padding:20rpx 28rpx 40rpx}.tabs{display:flex;white-space:nowrap;padding-bottom:18rpx}
.tab{display:inline-block;padding:10rpx 22rpx;margin-right:12rpx;border-radius:20rpx;font-size:24rpx;color:#8f7366;background:rgba(255,255,255,.6);border:1rpx solid rgba(140,104,83,.08)}
.tab.on{background:#e8927c;color:#fff;border-color:#e8927c}
.oh{display:flex;justify-content:space-between;margin-bottom:14rpx}.ono{font-size:24rpx;color:#8f7366}
.os{font-size:26rpx;font-weight:700}.oit{display:flex;align-items:center;gap:14rpx;padding:12rpx 0;border-top:1rpx solid rgba(140,104,83,.06)}
.oimg{width:90rpx;height:90rpx;border-radius:8rpx}.oinfo{flex:1}.oinm{font-size:26rpx;display:block}.oiq{font-size:22rpx;color:#8f7366}
.oip{font-size:26rpx;font-weight:600}
.of{display:flex;justify-content:space-between;padding-top:14rpx;margin-top:8rpx;border-top:1rpx solid rgba(140,104,83,.08);font-size:26rpx}
.of-actions{padding-top:12rpx;display:flex;justify-content:flex-end}
.btn-pay{height:56rpx;line-height:56rpx;padding:0 28rpx;background:linear-gradient(135deg,#e8927c,#d47a64);color:#fff;font-size:24rpx;font-weight:600;border-radius:28rpx;border:none}
.btn-pay::after{border:none}
</style>
