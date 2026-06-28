var app = getApp();
var authApi = require("../../utils/api/auth");

Page({
  data: {
    countdown: 3
  },

  onReady: function () {
    this.ctx = wx.createCameraContext();
    this.startCountdown();
  },

  startCountdown: function () {
    var t = this;
    t.setData({ countdown: 3 });
    var timer = setInterval(function () {
      var n = t.data.countdown - 1;
      t.setData({ countdown: n });
      if (n <= 0) {
        clearInterval(timer);
        t.captureAndLogin();
      }
    }, 1000);
    this.timer = timer;
  },

  captureAndLogin: function () {
    var t = this;
    if (this._capturing) return;
    this._capturing = true;

    wx.showLoading({ title: '识别中...', mask: true });
    this.ctx.takePhoto({
      quality: 'low',
      success: function (res) {
        wx.getFileSystemManager().readFile({
          filePath: res.tempImagePath,
          encoding: 'base64',
          success: function (fsRes) {
            wx.showLoading({ title: '登录中...', mask: true });
            authApi.login({ authType: 'face', faceToken: fsRes.data })
              .then(function (r) {
                wx.hideLoading();
                wx.navigateBack();
                // 延迟执行 doLogin 确保页面已返回
                setTimeout(function () {
                  var pages = getCurrentPages();
                  var prevPage = pages[pages.length - 1];
                  if (prevPage && prevPage.doLogin) {
                    prevPage.doLogin(r);
                  }
                }, 300);
              })
              .catch(function (err) {
                wx.hideLoading();
                wx.showModal({
                  title: '识别失败',
                  content: err.message || '人脸识别失败',
                  confirmText: '重试',
                  success: function (r) {
                    if (r.confirm) {
                      t._capturing = false;
                      t.startCountdown();
                    } else {
                      wx.navigateBack();
                    }
                  }
                });
              });
          },
          fail: function () {
            wx.hideLoading();
            wx.showToast({ title: '图片读取失败', icon: 'none' });
            setTimeout(function () { wx.navigateBack(); }, 1500);
          }
        });
      },
      fail: function () {
        wx.hideLoading();
        wx.showToast({ title: '拍照失败', icon: 'none' });
        setTimeout(function () { wx.navigateBack(); }, 1500);
      }
    });
  },

  onClose: function () {
    if (this.timer) clearInterval(this.timer);
    wx.navigateBack();
  },

  onError: function (e) {
    wx.showToast({ title: '摄像头启动失败: ' + e.detail.errMsg, icon: 'none' });
    setTimeout(function () { wx.navigateBack(); }, 2000);
  },

  onUnload: function () {
    if (this.timer) clearInterval(this.timer);
  }
});
