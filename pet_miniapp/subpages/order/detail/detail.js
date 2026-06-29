var orderApi = require("../../utils/api/order");

function normalizeItem(item) {
  item = item || {};
  return {
    id: item.id,
    productName: item.productName || "",
    productImage: item.productImage || "",
    price: item.price || "0.00",
    quantity: item.quantity || 1
  };
}

Page({
  data: {
    order: null,
    items: [],
    statusLabels: {
      "0": "\u5f85\u652f\u4ed8",
      "1": "\u5df2\u652f\u4ed8",
      "2": "\u5df2\u53d1\u8d27",
      "3": "\u5df2\u6536\u8d27",
      "4": "\u5df2\u8bc4\u4ef7",
      "-1": "\u5df2\u53d6\u6d88",
      "-2": "\u9000\u5355\u4e2d",
      "-3": "\u9000\u5355\u5df2\u901a\u8fc7"
    },
    addressParsed: null,
    loading: true
  },

  onLoad: function(options) {
    var id = options && options.id;
    if (!id) {
      wx.showToast({ title: "\u8ba2\u5355ID\u7f3a\u5931", icon: "none" });
      wx.navigateBack();
      return;
    }
    this.loadOrder(id);
  },

  loadOrder: function(id) {
    var that = this;
    that.setData({ loading: true });

    Promise.all([
      orderApi.detail(id),
      orderApi.items(id)
    ]).then(function(results) {
      var order = results[0] || {};
      var items = (results[1] || []).map(normalizeItem);
      that.setData({
        order: order,
        items: items,
        loading: false
      });
    }).catch(function() {
      that.setData({ loading: false });
      wx.showToast({ title: "\u52a0\u8f7d\u5931\u8d25", icon: "none" });
    });
  },

  /** \u62e8\u6253\u5ba2\u670d */
  callService: function() {
    wx.showToast({ title: "\u5ba2\u670d\u7535\u8bdd: 400-000-0000", icon: "none" });
  }
});

