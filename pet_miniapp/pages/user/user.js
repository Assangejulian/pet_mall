var app = getApp();
var userApi = require("../../utils/api/user");

Page({
  data: {
    userName: "",
    avatar: "",
    memberLevelName: "",
    isLogin: false,
    tabs: [
      { s: "0", i: "O", l: "待付款" },
      { s: "1", i: "O", l: "待发货" },
      { s: "2", i: "O", l: "待收货" },
      { s: "3", i: "O", l: "待评价" },
      { s: "-99", i: "O", l: "退款/售后" }
    ]
  },

  onShow: function () {
    var token = app.globalData.token;
    var user = app.globalData.user;
    var name = (user && user.name) || "";
    this.setData({
      isLogin: !!token,
      userName: name || (token ? "用户" : "")
    });
    if (token) {
      this.loadProfile();
    }
  },

  loadProfile: function () {
    var t = this;
    userApi.getProfile().then(function (data) {
      t.setData({
        userName: data.username || t.data.userName,
        avatar: data.avatar || "",
        memberLevelName: data.levelName || "暖窝会员"
      });
      // 更新全局用户信息
      if (app.globalData.user) {
        app.globalData.user.name = data.username;
      }
    }).catch(function () {
      // 静默失败，使用已有数据
    });
  },

  goLogin: function () {
    wx.navigateTo({ url: "/subpages/login/login" });
  },

  goProfile: function () {
    if (!this.data.isLogin) return this.goLogin();
    wx.navigateTo({ url: "/subpages/profile/edit/edit" });
  },

  goOrders: function (e) {
    if (!this.data.isLogin) return this.goLogin();
    wx.navigateTo({ url: "/subpages/order/list?status=" + e.currentTarget.dataset.s });
  },

  goAddr: function () {
    if (!this.data.isLogin) return this.goLogin();
    wx.navigateTo({ url: "/subpages/address/list" });
  },

  goStores: function () {
    wx.navigateTo({ url: "/pages/store/list" });
  },

  goVideos: function () {
    wx.navigateTo({ url: "/subpages/video/list" });
  },

  logout: function () {
    var that = this;
    wx.showModal({
      title: "退出确认",
      content: "确定要退出登录吗？",
      success: function (r) {
        if (r.confirm) {
          app.globalData.token = "";
          app.globalData.user = null;
          wx.removeStorageSync("token");
          wx.removeStorageSync("userId");
          that.setData({ isLogin: false, userName: "", avatar: "", memberLevelName: "" });
          wx.showToast({ title: "已退出", icon: "none" });
        }
      }
    });
  }
});
