var userApi = require("../../utils/api/user");

Page({
  data: { list: [], loading: true },

  onShow: function() {
    var that = this;
    that.setData({ loading: true });
    userApi.addressList().then(function(res) {
      var list = Array.isArray(res) ? res : [];
      that.setData({ list: list, loading: false });
    }).catch(function() {
      that.setData({ loading: false });
    });
  }
});
