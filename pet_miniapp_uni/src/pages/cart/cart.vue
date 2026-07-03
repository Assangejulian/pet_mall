<template>
  <view class="page">
    <view class="login-mask" v-if="!isLogin">
      <view class="login-mask-card">
        <view class="login-mask-icon"></view>
        <text class="login-mask-title">登录后查看购物车</text>
        <text class="login-mask-desc">登录后商品将同步到你的账号</text>
        <button class="login-mask-btn" @tap="goLogin">去登录</button>
        <text class="login-mask-skip" @tap="goHome">先去逛逛</text>
      </view>
    </view>
    <view v-else-if="cart.length>0" class="list">
      <view v-for="item in cart" :key="item.id" class="item">
        <view class="chk" @tap="toggleCheck" :data-id="item.id"><view class="dot" :class="{on:item.checked}"></view></view>
        <image class="img" :src="item.image" mode="aspectFill" />
        <view class="info">
          <text class="nm">{{item.name}}</text>
          <view class="row">
            <view class="price"><text class="price-symbol">¥</text>{{item.price}}</view>
            <view class="qty">
              <text class="b" @tap="dec" :data-id="item.id">-</text>
              <text class="n">{{item.quantity}}</text>
              <text class="b" @tap="inc" :data-id="item.id">+</text>
            </view>
          </view>
        </view>
        <text class="del" @tap="del" :data-id="item.id">x</text>
      </view>
    </view>
    <view class="empty-tip" v-else-if="cart.length===0">
      <text class="empty-icon">O</text>
      <text>购物车空空的</text>
      <text class="empty-sub">去暖窝逛逛吧</text>
      <button class="btn-accent" @tap="goHome">去逛逛</button>
    </view>
    <view class="b-bar" v-if="isLogin && cart.length>0">
      <view class="all" @tap="toggleAll"><view class="dot" :class="{on:allChecked}"></view><text>全选</text></view>
      <text class="sum">合计 <text class="sum-price">¥{{totalPrice}}</text></text>
      <view class="btn-accent btn-checkout" @tap="checkout">结算({{checkedCount}})</view>
    </view>
  </view>
</template>
<script>
import cartApi from "@/utils/api/cart";
const app = getApp();
export default {
  data() {
    return { cart: [], allChecked: true, totalPrice: "0.00", checkedCount: 0, isLogin: false };
  },
  onShow() {
    this.isLogin = !!app.globalData.token;
    this.load();
  },
  methods: {
    load() {
      if (!this.isLogin) return;
      cartApi.list().then(res => {
        const cartItems = (res || []).map(item => ({
          id: item.id, productId: item.productId,
          name: item.productInfo ? (item.productInfo.name || item.productInfo.productName) : "未知商品",
          price: item.productInfo ? item.productInfo.price : "0.00",
          image: item.productInfo ? (item.productInfo.image || item.productInfo.mainImage) : "",
          quantity: item.quantity, checked: item.checked === 1 || item.checked === true
        }));
        this.cart = cartItems; this.calc();
      });
    },
    calc() {
      let total = 0, cc = 0, ac = this.cart.length > 0;
      this.cart.forEach(i => { if (i.checked) { total += parseFloat(i.price) * i.quantity; cc += i.quantity; } else ac = false; });
      this.totalPrice = total.toFixed(2); this.checkedCount = cc; this.allChecked = ac;
    },
    toggleCheck(e) {
      const id = e.currentTarget.dataset.id;
      const item = this.cart.find(i => i.id === id); if (!item) return;
      cartApi.update(id, { checked: item.checked ? 0 : 1 }).then(() => this.load());
    },
    toggleAll() {
      const ac = !this.allChecked ? 1 : 0;
      Promise.all(this.cart.map(i => { if ((i.checked ? 1 : 0) !== ac) return cartApi.update(i.id, { checked: ac }); }).filter(Boolean)).then(() => this.load());
    },
    inc(e) {
      const id = e.currentTarget.dataset.id;
      const item = this.cart.find(i => i.id === id); if (!item) return;
      cartApi.update(id, { quantity: item.quantity + 1 }).then(() => this.load());
    },
    dec(e) {
      const id = e.currentTarget.dataset.id;
      const item = this.cart.find(i => i.id === id); if (!item) return;
      if (item.quantity <= 1) { uni.showToast({ title: "数量不能少于1", icon: "none" }); return; }
      cartApi.update(id, { quantity: item.quantity - 1 }).then(() => this.load());
    },
    del(e) {
      const id = e.currentTarget.dataset.id;
      uni.showModal({ title: "移除", content: "确定移除该商品？", success: (r) => { if (r.confirm) cartApi.remove(id).then(() => this.load()); } });
    },
    goLogin() { uni.navigateTo({ url: "/subpages/login/login" }); },
    checkout() {
      if (!app.requireAuth()) return;
      if (this.checkedCount === 0) { uni.showToast({ title: "请选择要结算的商品", icon: "none" }); return; }
      uni.navigateTo({ url: "/subpages/order/submit" });
    },
    goHome() { uni.switchTab({ url: "/pages/index/index" }); }
  }
}
</script>
<style scoped>
.page{padding-bottom:140rpx;min-height:100vh}
.list{padding:0 28rpx}.item{display:flex;align-items:center;gap:18rpx;padding:22rpx 0;border-bottom:1rpx solid rgba(140,104,83,.08)}
.chk{flex-shrink:0;padding:8rpx}.dot{width:32rpx;height:32rpx;border:2rpx solid #d5c8be;border-radius:50%;transition:background 180ms,border-color 180ms;box-sizing:border-box}
.dot.on{background:#e8927c;border-color:#e8927c;position:relative}.dot.on::after{content:"";position:absolute;top:6rpx;left:10rpx;width:8rpx;height:14rpx;border:solid #fff;border-width:0 3rpx 3rpx 0;transform:rotate(45deg)}
.img{width:140rpx;height:140rpx;border-radius:14rpx;flex-shrink:0}.info{flex:1;min-width:0}
.nm{display:block;font-size:26rpx;font-weight:700;color:#2d1f18;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:14rpx}
.row{display:flex;justify-content:space-between;align-items:center}.price{font-size:30rpx;font-weight:800;color:#2d1f18}
.price-symbol{font-size:22rpx;font-weight:600}.qty{display:flex;align-items:center;gap:8rpx}
.b{width:44rpx;height:44rpx;line-height:44rpx;text-align:center;border:1rpx solid #e8dcd0;border-radius:50%;font-size:28rpx;color:#8f7366}
.n{min-width:36rpx;text-align:center;font-size:26rpx;font-weight:700;color:#2d1f18}.del{padding:8rpx;font-size:28rpx;color:#bfafa5}
.empty-tip{display:flex;flex-direction:column;align-items:center;justify-content:center;padding-top:200rpx}
.empty-icon{font-size:80rpx;color:#d5c8be;margin-bottom:16rpx}.empty-tip text{font-size:26rpx;color:#8f7366}
.empty-sub{font-size:24rpx;color:#bfafa5;margin-top:8rpx}.empty-tip button{margin-top:36rpx}
.b-bar{position:fixed;bottom:0;left:0;right:0;display:flex;align-items:center;gap:20rpx;padding:18rpx 28rpx;padding-bottom:calc(18rpx + env(safe-area-inset-bottom));background:rgba(250,247,239,.92);border-top:1rpx solid rgba(140,104,83,.08)}
.all{display:flex;align-items:center;gap:12rpx;font-size:26rpx;color:#2d1f18}.sum{flex:1;text-align:right;font-size:24rpx;color:#8f7366}
.sum-price{font-size:30rpx;font-weight:800;color:#e8927c}.btn-checkout{padding:16rpx 40rpx}
.btn-accent{min-height:76rpx;line-height:76rpx;padding:0 36rpx;background:#e8927c;color:#fff;font-size:26rpx;font-weight:800;border-radius:38rpx;border:none;box-shadow:0 6rpx 20rpx rgba(232,146,124,.25)}
.btn-accent::after{border:none}
.login-mask{display:flex;align-items:center;justify-content:center;min-height:80vh;padding:40rpx}
.login-mask-card{display:flex;flex-direction:column;align-items:center;text-align:center;max-width:460rpx}
.login-mask-icon{width:100rpx;height:100rpx;border-radius:50%;background:#f5ece4;margin-bottom:28rpx;position:relative}
.login-mask-icon::before{content:"";position:absolute;top:28rpx;left:38rpx;width:24rpx;height:24rpx;border:3rpx solid #e8927c;border-radius:50%}
.login-mask-icon::after{content:"";position:absolute;bottom:22rpx;left:30rpx;width:40rpx;height:22rpx;border:3rpx solid #e8927c;border-radius:0 0 20rpx 20rpx;border-top:none}
.login-mask-title{font-size:32rpx;font-weight:700;color:#2d1f18;margin-bottom:12rpx}
.login-mask-desc{font-size:24rpx;color:#8f7366;margin-bottom:36rpx}
.login-mask-btn{width:280rpx;height:80rpx;display:flex;align-items:center;justify-content:center;padding:0;background:#e8927c;color:#fff;font-size:28rpx;font-weight:700;border-radius:16rpx;border:none;box-shadow:0 4rpx 16rpx rgba(232,146,124,.25)}
.login-mask-btn::after{border:none}.login-mask-skip{font-size:24rpx;color:#bfafa5;margin-top:24rpx}
</style>
