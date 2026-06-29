var userApi = require("../../utils/api/user");

var EMPTY_FORM = {
  receiverName: "",
  phone: "",
  province: "",
  city: "",
  district: "",
  detail: "",
  defaulted: 0
};

Page({
  data: { list: [], loading: true, form: JSON.parse(JSON.stringify(EMPTY_FORM)) },

  onShow: function() {
    this.loadList();
  },

  loadList: function() {
    var that = this;
    that.setData({ loading: true });
    userApi.addressList().then(function(res) {
      var list = Array.isArray(res) ? res : [];
      that.setData({ list: list, loading: false });
    }).catch(function() {
      that.setData({ loading: false });
    });
  },

  onInput: function(e) {
    var field = e.currentTarget.dataset.field;
    var form = this.data.form;
    form[field] = e.detail.value;
    this.setData({ form: form });
  },

  onDefaultChange: function(e) {
    var form = this.data.form;
    form.defaulted = e.detail.value ? 1 : 0;
    this.setData({ form: form });
  },

  addAddr: function() {
    var form = this.data.form;
    if (!form.receiverName || !form.phone || !form.detail) {
      wx.showToast({ title: "请填写收件人、手机号和详细地址", icon: "none" });
      return;
    }
    var that = this;
    wx.showLoading({ title: "保存中..." });
    userApi.addAddress(form).then(function() {
      wx.hideLoading();
      wx.showToast({ title: "地址已保存", icon: "success" });
      that.setData({ form: JSON.parse(JSON.stringify(EMPTY_FORM)) });
      that.loadList();
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: (err && err.message) || "保存失败", icon: "none" });
    });
  },

  setDefault: function(e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
    userApi.updateAddress(id, { defaulted: 1 }).then(function() {
      that.loadList();
    });
  },

  delAddr: function(e) {
    var id = e.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "删除地址",
      content: "确定删除此地址？",
      success: function(r) {
        if (r.confirm) {
          userApi.delAddress(id).then(function() {
            that.loadList();
          });
        }
      }
    });
  }
});
