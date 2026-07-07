var app = getApp();
var userApi = require("../../../utils/api/user");

Page({
  data: {
    avatar: "",
    username: "",
    realName: "",
    phone: "",
    email: "",
    birthday: "",
    saving: false
  },

  onShow: function () {
    if (!app.globalData.token) {
      wx.showToast({ title: "请先登录", icon: "none" });
      return wx.navigateTo({ url: "/subpages/login/login" });
    }
    this.loadProfile();
  },

  loadProfile: function () {
    var t = this;
    wx.showLoading({ title: "加载中..." });
    userApi.getProfile().then(function (data) {
      wx.hideLoading();
      t.setData({
        avatar: data.avatar || "",
        username: data.username || "",
        realName: data.realName || "",
        phone: data.phone || "",
        email: data.email || "",
        birthday: data.birthday || ""
      });
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: err.message || "加载失败", icon: "none" });
    });
  },

  onInput: function (e) {
    var field = e.currentTarget.dataset.field;
    var val = e.detail.value;
    var obj = {};
    obj[field] = val;
    this.setData(obj);
  },

  onBirthdayChange: function (e) {
    this.setData({ birthday: e.detail.value });
  },

  chooseAvatar: function () {
    var t = this;
    wx.chooseImage({
      count: 1,
      sizeType: ["compressed"],
      sourceType: ["album", "camera"],
      success: function (res) {
        var tempPath = res.tempFilePaths[0];
        wx.showLoading({ title: "上传中..." });
        wx.uploadFile({
          url: (app.globalData.baseUrl || "http://localhost:8080") + "/api/upload",
          filePath: tempPath,
          name: "file",
          header: { Authorization: "Bearer " + app.globalData.token },
          success: function (upRes) {
            wx.hideLoading();
            var body = JSON.parse(upRes.data);
            if (body.code === 200) {
              var url = body.data && (body.data.url || body.data);
              t.setData({ avatar: url });
            } else {
              wx.showToast({ title: body.message || "上传失败", icon: "none" });
            }
          },
          fail: function () {
            wx.hideLoading();
            wx.showToast({ title: "上传失败", icon: "none" });
          }
        });
      }
    });
  },

  saveProfile: function () {
    var data = {
      realName: this.data.realName,
      phone: this.data.phone,
      email: this.data.email,
      birthday: this.data.birthday,
      avatar: this.data.avatar
    };
    var t = this;
    t.setData({ saving: true });
    userApi.updateProfile(data).then(function () {
      t.setData({ saving: false });
      wx.showToast({ title: "保存成功", icon: "success" });
      setTimeout(function () { wx.navigateBack(); }, 1200);
    }).catch(function (err) {
      t.setData({ saving: false });
      wx.showToast({ title: err.message || "保存失败", icon: "none" });
    });
  },

  goBack: function () {
    wx.navigateBack();
  }
});
