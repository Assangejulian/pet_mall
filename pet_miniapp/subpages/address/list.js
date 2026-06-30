var userApi = require("../../utils/api/user");

Page({
  data: { list: [], loading: true, selecting: false },

  onLoad: function(options) {
    if (options && options.select === "1") {
      this.setData({ selecting: true });
    }
  },

  onShow: function() {
    var that = this;
    that.setData({ loading: true });
    userApi.addressList().then(function(res) {
      var list = Array.isArray(res) ? res : [];
      that.setData({ list: list, loading: false });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },

  selectAddress: function(e) {
    if (!this.data.selecting) return;
    var idx = e.currentTarget.dataset.index;
    var addr = this.data.list[idx];
    if (!addr) return;
    var pages = getCurrentPages();
    var prev = pages[pages.length - 2];
    if (prev && prev.onAddressSelected) {
      prev.onAddressSelected(addr);
    }
    wx.navigateBack();
  }
});

