var request = require("../../utils/request");
var orderApi = require("../../utils/api/order");

Page({
  data: {
    order: null,
    submitting: false
  },

  onLoad: function(options) {
    var id = options.id;
    if (!id) {
      wx.navigateBack();
      return;
    }
    this.loadOrder(id);
  },

  loadOrder: function(id) {
    var that = this;
    orderApi.detail(id).then(function(res) {
      if (res && res.items) {
        // 初始化评价内容
        res.items.forEach(function(item) {
          item.content = "";
        });
      }
      that.setData({ order: res });
    }).catch(function() {
      wx.showToast({ title: "加载失败", icon: "none" });
    });
  },

  onInput: function(e) {
    var index = e.currentTarget.dataset.index;
    var value = e.detail.value;
    var items = this.data.order.items;
    items[index].content = value;
    this.setData({
      "order.items": items
    });
  },

  submitEvaluate: function() {
    var that = this;
    if (this.data.submitting) return;

    var items = this.data.order.items || [];
    var evaluateItems = items.map(function(item) {
      return {
        orderItemId: item.id,
        content: item.content || "默认好评" // 如果用户没写，给个默认好评，或者可以校验必须写
      };
    });

    var hasEmpty = items.some(function(item) { return !(item.content || "").trim(); });
    if (hasEmpty) {
      wx.showToast({ title: "请填写所有评价", icon: "none" });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: "提交中..." });

    request.post("/api/order/evaluate", {
      orderId: this.data.order.id,
      items: evaluateItems
    }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: "评价成功", icon: "success" });
      setTimeout(function() {
        wx.navigateBack();
      }, 1500);
    }).catch(function(err) {
      wx.hideLoading();
      that.setData({ submitting: false });
      wx.showToast({ title: err.message || "评价失败", icon: "none" });
    });
  }
});
