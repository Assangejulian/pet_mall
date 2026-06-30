var app = getApp();
var productApi = require("../../utils/api/product");

function normalizeProduct(source) {
  source = source || {};
  return {
    id: source.id,
    name: source.name || source.productName || "",
    description: source.detail || source.productDesc || "",
    price: source.price || "0.00",
    image: source.image || source.mainImage || "",
    category: source.category || "",
    stock: source.stock || 0,
    store: source.store || null
  };
}

Page({
  data: {
    product: {},
    categoryName: "",
    cartCount: 0,
    loading: true
  },

  onLoad: function (options) {
    this.loadProduct(options && options.id);
  },

  onShow: function () {
    var cart = wx.getStorageSync("cart") || [];
    this.setData({ cartCount: cart.reduce(function(s, i) { return s + i.quantity; }, 0) });
  },

  loadProduct: function (id) {
    if (!id) {
      wx.showToast({ title: "商品ID缺失", icon: "none" });
      this.setData({ loading: false });
      return;
    }
    var that = this;
    that.setData({ loading: true });
    productApi.detail(id)
      .then(function (res) {
        var product = normalizeProduct(res);
        var categoryNames = { dog: "狗狗", cat: "猫咪", other: "小宠" };
        that.setData({
          product: product,
          categoryName: categoryNames[product.category] || product.category || "",
          loading: false
        });
      })
      .catch(function () {
        that.setData({ loading: false });
      });
  },

  addCart: function () {
    var product = this.data.product;
    if (!product || !product.id) {
      wx.showToast({ title: "商品不可加入购物车", icon: "none" });
      return;
    }
    if (!product.stock) {
      wx.showToast({ title: "商品暂无库存", icon: "none" });
      return;
    }
    app.addToCart(product);
    wx.showToast({ title: "已加入购物车", icon: "success" });
    this.onShow();
  },

  buyNow: function () {
    if (!app.requireAuth()) return;
    var product = this.data.product;
    if (!product || !product.id) return;
    if (!product.stock) {
      wx.showToast({ title: "商品暂无库存", icon: "none" });
      return;
    }
    app.addToCart(product);
    wx.switchTab({ url: "/pages/cart/cart" });
  },

  goCart: function () {
    wx.switchTab({ url: "/pages/cart/cart" });
  }
});