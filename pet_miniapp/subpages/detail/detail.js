var app = getApp();
var productApi = require("../../utils/api/product");
var cartApi = require("../../utils/api/cart");

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
    loading: true,
    showQtyPicker: false,
    quantity: 1,
    reviews: []
  },

  onLoad: function (options) {
    this._productId = options && options.id;
    this._loadedOnce = false;
    this.loadProduct(this._productId);
  },

  onShow: function () {
    if (this._loadedOnce && this._productId) {
      this.loadProduct(this._productId);
    }
  },

  onPullDownRefresh: function () {
    this.loadProduct(this._productId, function () {
      wx.stopPullDownRefresh();
    });
  },

  loadProduct: function (id, done) {
    if (!id) {
      wx.showToast({ title: "商品ID缺失", icon: "none" });
      this.setData({ loading: false });
      if (done) done();
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
        that._loadedOnce = true;
        if (done) done();
      })
      .catch(function (err) {
        var msg = (err && err.message) || '加载失败';
        wx.showToast({ title: msg, icon: 'none' });
        that.setData({ loading: false });
        that._loadedOnce = true;
        if (done) done();
      });

    // 加载评价
    var request = require("../../utils/request");
    request.get("/api/store/product/" + id + "/evaluates").then(function(res) {
      that.setData({ reviews: res || [] });
    }).catch(function(){});
  },

  // 显示数量选择弹窗
  showQtyPicker: function () {
    this.setData({
      quantity: 1,
      showQtyPicker: true
    });
  },

  // 隐藏数量选择弹窗
  hideQtyPicker: function () {
    this.setData({ showQtyPicker: false });
  },

  // 加减数量
  onQtyChange: function (e) {
    var delta = parseInt(e.currentTarget.dataset.delta) || 0;
    var qty = this.data.quantity + delta;
    if (qty < 1) qty = 1;
    if (this.data.product.stock && qty > this.data.product.stock) {
      qty = this.data.product.stock;
      wx.showToast({ title: "已达库存上限", icon: "none" });
    }
    this.setData({ quantity: qty });
  },

  // 手动输入数量
  onQtyInput: function (e) {
    var val = parseInt(e.detail.value) || 1;
    if (val < 1) val = 1;
    if (this.data.product.stock && val > this.data.product.stock) {
      val = this.data.product.stock;
    }
    this.setData({ quantity: val });
  },

  // 加入购物车 — 先弹数量选择
  addCart: function () {
    if (!app.requireAuth()) return;
    if (!this.data.product || !this.data.product.id) {
      wx.showToast({ title: "商品不可加入购物车", icon: "none" });
      return;
    }
    if (!this.data.product.stock) {
      wx.showToast({ title: "商品暂无库存", icon: "none" });
      return;
    }
    this.showQtyPicker();
  },

  // 确认加入购物车
  confirmAddCart: function () {
    this.setData({ showQtyPicker: false });
    wx.showLoading({ title: "添加中..." });
    cartApi.add({
      productId: this.data.product.id,
      quantity: this.data.quantity,
      checked: 1
    }).then(function() {
      wx.hideLoading();
      wx.showToast({ title: "已加入购物车", icon: "success" });
    }).catch(function() {
      wx.hideLoading();
      wx.showToast({ title: "添加失败", icon: "none" });
    });
  },

  buyNow: function () {
    if (!app.requireAuth()) return;
    if (!this.data.product || !this.data.product.id) return;
    if (!this.data.product.stock) {
      wx.showToast({ title: "商品暂无库存", icon: "none" });
      return;
    }
    wx.showLoading({ title: "处理中..." });
    cartApi.add({
      productId: this.data.product.id,
      quantity: 1,
      checked: 1
    }).then(function() {
      wx.hideLoading();
      wx.switchTab({ url: "/pages/cart/cart" });
    }).catch(function() {
      wx.hideLoading();
      wx.showToast({ title: "系统繁忙", icon: "none" });
    });
  },

  goCart: function () {
    wx.switchTab({ url: "/pages/cart/cart" });
  }
});

