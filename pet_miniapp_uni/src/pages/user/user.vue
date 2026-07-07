<template>
  <view class="page">
    <view class="login-mask" v-if="!isLogin">
      <view class="login-mask-card">
        <view class="login-mask-icon"></view>
        <text class="login-mask-title">登录个人中心</text>
        <text class="login-mask-desc">登录后查看订单、收货地址等</text>
        <button class="login-mask-btn" @tap="goLogin">去登录</button>
      </view>
    </view>
    <view v-else>
      <view class="profile" @tap="goProfile">
        <view class="pf-bg"></view>
        <view class="pf-inner">
          <image v-if="avatar" class="pf-avatar-img" :src="avatar" mode="aspectFill" />
          <view v-else class="pf-avatar">{{(userName||"?")[0]}}</view>
          <view class="pf-info">
            <text class="pf-name serif">{{userName||"用户"}}</text>
            <text class="pf-sub">{{memberLevelName || "暖窝会员"}}</text>
          </view>
          <text class="pf-arrow">></text>
        </view>
      </view>
      <view class="card">
        <text class="sec serif">我的订单</text>
        <view class="og">
          <view v-for="tab in tabs" :key="tab.s" class="oi" @tap="goOrders" :data-s="tab.s">
            <text class="oi-icon">{{tab.i}}</text>
            <text class="oi-lb">{{tab.l}}</text>
          </view>
        </view>
      </view>
      <view class="card">
        <view class="mi" @tap="goProfile"><text>个人资料</text><text class="ar">></text></view>
        <view class="mi" @tap="goAddr"><text>收货地址</text><text class="ar">></text></view>
        <view class="mi" @tap="goStores"><text>附近门店</text><text class="ar">></text></view>
        <view class="mi" @tap="goVideos"><text>暖窝视频</text><text class="ar">></text></view>
      </view>
      <view class="card" @tap="logout"><view class="mi logout-btn"><text>退出登录</text></view></view>
    </view>
  </view>
</template>

<script>
const app = getApp();
import userApi from "../../utils/api/user";
export default {
  data() {
    return {
      userName: "", isLogin: false, avatar: "", memberLevelName: "",
      tabs: [{s:"0",i:"O",l:"待付款"},{s:"1",i:"O",l:"待发货"},{s:"2",i:"O",l:"待收货"},{s:"3",i:"O",l:"待评价"},{s:"-99",i:"O",l:"退款/售后"}]
    };
  },
  onShow() {
    const token = app.globalData.token;
    const user = app.globalData.user;
    const name = (user && user.name) || "";
    this.isLogin = !!token;
    this.userName = name || (token ? "用户" : "");
    if (token) this.loadProfile();
  },
  methods: {
    loadProfile() {
      userApi.getProfile().then(data => {
        this.userName = data.username || this.userName;
        this.avatar = data.avatar || "";
        this.memberLevelName = data.levelName || "暖窝会员";
        if (app.globalData.user) app.globalData.user.name = data.username;
      }).catch(() => {});
    },
    goLogin() { uni.navigateTo({ url: "/subpages/login/login" }); },
    goProfile() { if (!this.isLogin) return this.goLogin(); uni.navigateTo({ url: "/subpages/profile/edit/edit" }); },
    goOrders(e) {
      if (!this.isLogin) return this.goLogin();
      uni.navigateTo({ url: "/subpages/order/list?status=" + e.currentTarget.dataset.s });
    },
    goAddr() { if (!this.isLogin) return this.goLogin(); uni.navigateTo({ url: "/subpages/address/list" }); },
    goStores() { uni.navigateTo({ url: "/pages/store/list" }); },
    goVideos() { uni.navigateTo({ url: "/subpages/video/list" }); },
    logout() {
      uni.showModal({
        title: "退出确认", content: "确定要退出登录吗？",
        success: (r) => {
          if (r.confirm) {
            app.globalData.token = ""; app.globalData.user = null;
            uni.removeStorageSync("token"); uni.removeStorageSync("userId");
            this.isLogin = false; this.userName = ""; this.avatar = ""; this.memberLevelName = "";
            uni.showToast({ title: "已退出", icon: "none" });
          }
        }
      });
    }
  }
}
</script>

<style>
.page{min-height:100vh;padding-bottom:40rpx}
.profile{position:relative;padding:50rpx 32rpx 36rpx;overflow:hidden}
.pf-bg{position:absolute;inset:0;background:linear-gradient(135deg,#2d1f18,#4a3428);border-radius:0 0 40rpx 40rpx}
.pf-inner{position:relative;display:flex;align-items:center;gap:24rpx}
.pf-avatar,.pf-avatar-img{width:100rpx;height:100rpx;border-radius:50%;background:rgba(232,146,124,.25);color:#e8927c;font-size:36rpx;font-weight:800;display:flex;align-items:center;justify-content:center;overflow:hidden;flex-shrink:0}
.pf-info{flex:1}
.pf-name{display:block;font-family:"Noto Serif SC","Songti SC",serif;font-size:34rpx;font-weight:700;color:#fff}
.pf-sub{display:block;margin-top:6rpx;font-size:22rpx;color:rgba(255,255,255,.5)}
.pf-arrow{font-size:28rpx;color:rgba(255,255,255,.3);font-weight:700}
.card{margin:24rpx 28rpx 0;padding:28rpx;background:rgba(255,255,255,.7);border:1rpx solid rgba(140,104,83,.18);border-radius:20rpx}
.sec{display:block;font-size:28rpx;font-weight:700;margin-bottom:20rpx;padding-bottom:16rpx;border-bottom:1rpx solid rgba(140,104,83,.1)}
.og{display:flex}
.oi{flex:1;display:flex;flex-direction:column;align-items:center;gap:10rpx;padding:8rpx 0}
.oi-icon{font-size:32rpx;color:#8f7366}
.oi-lb{font-size:22rpx;color:#8f7366;font-weight:600}
.mi{display:flex;align-items:center;justify-content:space-between;padding:22rpx 0;font-size:26rpx;color:#2d1f18;font-weight:600}
.mi:not(:last-child){border-bottom:1rpx solid rgba(140,104,83,.06)}
.ar{color:#d5c8be;font-weight:700}
.logout-btn{justify-content:center;color:#c0392b}
.login-mask{display:flex;align-items:center;justify-content:center;min-height:80vh;padding:40rpx}
.login-mask-card{display:flex;flex-direction:column;align-items:center;text-align:center;max-width:460rpx}
.login-mask-icon{width:100rpx;height:100rpx;border-radius:50%;background:#f5ece4;margin-bottom:28rpx;position:relative}
.login-mask-icon::before{content:'';position:absolute;top:28rpx;left:38rpx;width:24rpx;height:24rpx;border:3rpx solid #e8927c;border-radius:50%}
.login-mask-icon::after{content:'';position:absolute;bottom:22rpx;left:30rpx;width:40rpx;height:22rpx;border:3rpx solid #e8927c;border-radius:0 0 20rpx 20rpx;border-top:none}
.login-mask-title{font-size:32rpx;font-weight:700;color:#2d1f18;margin-bottom:12rpx}
.login-mask-desc{font-size:24rpx;color:#8f7366;margin-bottom:36rpx}
.login-mask-btn{width:280rpx;height:80rpx;display:flex;align-items:center;justify-content:center;padding:0;background:#e8927c;color:#fff;font-size:28rpx;font-weight:700;border-radius:16rpx;border:none}
.login-mask-btn::after{border:none}
.login-mask-btn:active{opacity:0.82;transform:scale(0.97)}
</style>
