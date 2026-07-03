<template>
  <view class="page">
    <view class="loading-block" v-if="loading">
      <view class="skel-hero"></view>
      <view class="skel-row"></view>
      <view class="skel-row short"></view>
    </view>

    <template v-if="!loading && product">
      <!-- Hero Image -->
      <view class="hero">
        <image class="hero-img" :src="product.image" mode="aspectFill" v-if="product.image" />
        <view class="hero-placeholder" v-else>
          <text class="hero-placeholder-icon">📦</text>
        </view>
        <view class="hero-tag" v-if="product.category">{{ product.category }}</view>
      </view>

      <!-- Price & Name -->
      <view class="info-card">
        <view class="price-row">
          <text class="price">¥{{ product.price }}</text>
          <text class="price-original" v-if="product.originalPrice && product.originalPrice > product.price">¥{{ product.originalPrice }}</text>
          <text class="stock-tag" :class="{ low: product.stock > 0 && product.stock < 10, none: product.stock <= 0 }">{{ product.stock > 0 ? (product.stock < 10 ? '仅剩' + product.stock + '件' : '有货') : '已售罄' }}</text>
        </view>
        <text class="name serif">{{ product.name }}</text>
        <text class="subtitle" v-if="product.subtitle">{{ product.subtitle }}</text>
      </view>

      <!-- Product Info -->
      <view class="info-card">
        <text class="sec-title serif">商品详情</text>
        <text class="desc-text">{{ product.description || '暂无描述' }}</text>
        <view class="info-rows" v-if="infoRows.length">
          <view class="info-row" v-for="(row, idx) in infoRows" :key="idx">
            <text class="row-label">{{ row.label }}</text>
            <text class="row-value">{{ row.value }}</text>
          </view>
        </view>
      </view>

      <!-- Reviews -->
      <view class="info-card" v-if="reviews.length">
        <view class="sec-header">
          <text class="sec-title serif">用户评价 ({{ reviews.length }})</text>
          <text class="sec-more" @tap="viewAllReviews">查看全部 ></text>
        </view>
        <view class="review-item" v-for="rv in reviews" :key="rv.id">
          <view class="review-top">
            <view class="review-avatar">{{ (rv.userName || '?')[0] }}</view>
            <view class="review-meta">
              <text class="review-name">{{ rv.userName || '匿名用户' }}</text>
              <text class="review-date">{{ rv.createTime || '' }}</text>
            </view>
            <view class="review-stars">
              <text v-for="s in 5" :key="s" :class="{ filled: s <= (rv.rating || 5) }">★</text>
            </view>
          </view>
          <text class="review-content">{{ rv.content }}</text>
          <view class="review-imgs" v-if="rv.images && rv.images.length">
            <image v-for="(img, i) in rv.images" :key="i" :src="img" mode="aspectFill" class="review-img" @tap="previewImg(rv.images, i)" />
          </view>
        </view>
      </view>
      <view class="info-card empty-reviews" v-else-if="!loading && product">
        <text class="sec-title serif">用户评价</text>
        <text class="empty-text">暂无评价</text>
      </view>

      <!-- Bottom padding for fixed bar -->
      <view class="bottom-spacer"></view>
    </template>

    <view class="empty-tip" v-if="!loading && !product">
      <text>商品不存在或已下架</text>
      <button class="btn-back" @tap="goBack">返回</button>
    </view>

    <!-- Bottom Bar -->
    <view class="bottom-bar" v-if="!loading && product">
      <view class="bar-icon" @tap="goCart">
        <text class="bar-icon-text">🛒</text>
        <view class="bar-badge" v-if="cartCount > 0">{{ cartCount > 99 ? '99+' : cartCount }}</view>
      </view>
      <button class="bar-btn add-cart" @tap="addToCart">加入购物车</button>
      <button class="bar-btn buy-now" @tap="buyNow">立即购买</button>
    </view>

    <!-- Quantity Picker Modal -->
    <view class="qty-modal-mask" v-if="showQtyPicker" @tap="showQtyPicker = false">
      <view class="qty-modal" @tap.stop="">
        <text class="qty-modal-title">选择数量</text>
        <view class="qty-picker">
          <view class="qty-btn" @tap="qtyDec">-</view>
          <text class="qty-val">{{ pickQty }}</text>
          <view class="qty-btn" @tap="qtyInc">+</view>
        </view>
        <view class="qty-modal-actions">
          <button class="qty-btn-cancel" @tap="showQtyPicker = false">取消</button>
          <button class="qty-btn-confirm" @tap="confirmQty">{{ pickAction === 'cart' ? '加入购物车' : '立即购买' }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import productApi from "@/utils/api/product";
import cartApi from "@/utils/api/cart";
import request from "@/utils/request";

const app = getApp();

function normalizeProduct(raw) {
  const s = raw || {};
  return {
    id: s.id || "",
    name: s.name || s.productName || "",
    subtitle: s.subtitle || "",
    price: s.price != null ? Number(s.price).toFixed(2) : "0.00",
    originalPrice: s.originalPrice != null ? Number(s.originalPrice).toFixed(2) : "",
    description: s.description || s.desc || "",
    image: s.mainImage || s.image || s.cover || "",
    images: Array.isArray(s.images) ? s.images : (s.image ? [s.image] : []),
    category: s.category || s.categoryName || "",
    stock: Number(s.stock) >= 0 ? Number(s.stock) : 0,
    sales: s.sales || s.salesCount || 0,
    specs: s.specs || s.specifications || "",
    weight: s.weight || "",
    origin: s.origin || "",
    brand: s.brand || ""
  };
}

export default {
  data() {
    return {
      product: null,
      loading: true,
      reviews: [],
      cartCount: 0,
      showQtyPicker: false,
      pickQty: 1,
      pickAction: "cart",
      _productId: null
    };
  },
  computed: {
    infoRows() {
      const p = this.product;
      if (!p) return [];
      const rows = [];
      if (p.specs) rows.push({ label: "规格", value: p.specs });
      if (p.weight) rows.push({ label: "重量", value: p.weight });
      if (p.origin) rows.push({ label: "产地", value: p.origin });
      if (p.brand) rows.push({ label: "品牌", value: p.brand });
      if (p.sales > 0) rows.push({ label: "已售", value: String(p.sales) });
      return rows;
    }
  },
  onLoad(options) {
    const id = options.id;
    if (!id) {
      uni.showToast({ title: "商品ID缺失", icon: "none" });
      setTimeout(() => { uni.navigateBack(); }, 1200);
      return;
    }
    this._productId = id;
    this.loadProduct(id);
  },
  onShow() {
    this.loadCartCount();
  },
  methods: {
    loadProduct(id) {
      this.loading = true;
      productApi.detail(id).then((res) => {
        this.product = normalizeProduct(res);
        this.loadReviews(id);
      }).catch((err) => {
        this.loading = false;
        uni.showToast({ title: (err && err.message) || "加载失败", icon: "none" });
      });
    },
    loadReviews(productId) {
      request.get("/api/product/" + productId + "/reviews", { size: 5 }).then((res) => {
        const rows = Array.isArray(res) ? res : (res && res.records ? res.records : []);
        this.reviews = (rows || []).map((r) => ({
          id: r.id,
          userName: r.userName || r.nickname || "",
          content: r.content || r.comment || "",
          rating: Number(r.rating || r.score || 5),
          createTime: r.createTime || r.createdAt || "",
          images: Array.isArray(r.images) ? r.images : (r.image ? [r.image] : [])
        }));
      }).catch(() => {}).finally(() => {
        this.loading = false;
      });
    },
    loadCartCount() {
      const token = app.globalData.token;
      if (!token) { this.cartCount = 0; return; }
      cartApi.list().then((res) => {
        const items = Array.isArray(res) ? res : [];
        this.cartCount = items.reduce((sum, i) => sum + (i.quantity || 1), 0);
      }).catch(() => {});
    },

    /* Actions */
    addToCart() {
      if (!app.globalData.token) {
        uni.navigateTo({ url: "/subpages/login/login" }); return;
      }
      if (this.product.stock <= 0) {
        uni.showToast({ title: "该商品已售罄", icon: "none" }); return;
      }
      this.pickQty = 1;
      this.pickAction = "cart";
      this.showQtyPicker = true;
    },
    buyNow() {
      if (!app.globalData.token) {
        uni.navigateTo({ url: "/subpages/login/login" }); return;
      }
      if (this.product.stock <= 0) {
        uni.showToast({ title: "该商品已售罄", icon: "none" }); return;
      }
      this.pickQty = 1;
      this.pickAction = "buy";
      this.showQtyPicker = true;
    },
    qtyDec() {
      if (this.pickQty > 1) this.pickQty--;
    },
    qtyInc() {
      if (this.pickQty < (this.product.stock || 99)) this.pickQty++;
    },
    confirmQty() {
      this.showQtyPicker = false;
      const item = {
        productId: this.product.id,
        quantity: this.pickQty,
        price: this.product.price,
        name: this.product.name,
        image: this.product.image
      };
      if (this.pickAction === "cart") {
        cartApi.add(item).then(() => {
          uni.showToast({ title: "已加入购物车", icon: "success" });
          this.loadCartCount();
        }).catch((err) => {
          uni.showToast({ title: (err && err.message) || "添加失败", icon: "none" });
        });
      } else {
        const orderItems = [{ productId: item.productId, quantity: item.quantity, price: item.price, name: item.name, image: item.image }];
        uni.setStorageSync("_checkoutItems", JSON.stringify(orderItems));
        uni.navigateTo({ url: "/subpages/order/submit" });
      }
    },

    /* Navigation */
    goCart() {
      uni.switchTab({ url: "/pages/cart/cart" });
    },
    goBack() {
      uni.navigateBack();
    },
    viewAllReviews() {
      uni.showToast({ title: "查看全部评价", icon: "none" });
    },
    previewImg(urls, current) {
      uni.previewImage({ urls, current: String(current || 0) });
    }
  }
};
</script>

<style scoped>
.page{min-height:100vh;background:#faf7ef;padding-bottom:120rpx}
.loading-block{padding:0 28rpx}
.skel-hero{width:100%;height:500rpx;border-radius:20rpx;background:#e8dfd5;margin-bottom:24rpx}
.skel-row{height:32rpx;background:#e8dfd5;border-radius:8rpx;margin-bottom:16rpx;width:80%}
.skel-row.short{width:50%}

.hero{position:relative;width:100%;height:540rpx;overflow:hidden}
.hero-img{width:100%;height:100%;display:block}
.hero-placeholder{width:100%;height:100%;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#e8dfd5,#d9cfc4)}
.hero-placeholder-icon{font-size:80rpx}
.hero-tag{position:absolute;top:24rpx;left:24rpx;padding:8rpx 20rpx;background:rgba(232,146,124,.85);color:#fff;font-size:22rpx;font-weight:700;border-radius:8rpx}

.info-card{margin:20rpx 28rpx 0;padding:28rpx;background:#fff;border-radius:20rpx;box-shadow:0 2rpx 12rpx rgba(140,104,83,.04)}
.price-row{display:flex;align-items:baseline;gap:14rpx}
.price{font-size:44rpx;font-weight:800;color:#e85d4d}
.price-original{font-size:26rpx;color:#c4b8ad;text-decoration:line-through}
.stock-tag{font-size:22rpx;padding:4rpx 14rpx;border-radius:8rpx;background:rgba(39,174,96,.12);color:#27ae60;font-weight:600}
.stock-tag.low{background:rgba(243,156,18,.12);color:#f39c12}
.stock-tag.none{background:rgba(231,76,60,.1);color:#e74c3c}
.name{display:block;margin-top:12rpx;font-size:32rpx;font-weight:700;color:#2d1f18;line-height:1.4}
.subtitle{display:block;margin-top:6rpx;font-size:24rpx;color:#8f7366}

.sec-title{display:block;font-size:28rpx;font-weight:700;color:#2d1f18;margin-bottom:16rpx}
.sec-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16rpx}
.sec-header .sec-title{margin-bottom:0}
.sec-more{font-size:24rpx;color:#e8927c}

.desc-text{font-size:26rpx;color:#8f7366;line-height:1.7}
.info-rows{margin-top:20rpx;display:flex;flex-direction:column;gap:12rpx}
.info-row{display:flex;align-items:center;justify-content:space-between;padding:10rpx 0;border-bottom:1rpx solid #f5f0eb}
.row-label{font-size:24rpx;color:#8f7366}
.row-value{font-size:24rpx;color:#2d1f18;font-weight:600}

.review-item{padding:20rpx 0;border-bottom:1rpx solid #f5f0eb}
.review-item:last-child{border-bottom:none}
.review-top{display:flex;align-items:center;gap:14rpx;margin-bottom:12rpx}
.review-avatar{width:56rpx;height:56rpx;border-radius:50%;background:#f5ece4;color:#e8927c;font-size:24rpx;font-weight:800;display:flex;align-items:center;justify-content:center;flex-shrink:0}
.review-meta{flex:1;display:flex;flex-direction:column}
.review-name{font-size:24rpx;color:#2d1f18;font-weight:600}
.review-date{font-size:20rpx;color:#c4b8ad;margin-top:2rpx}
.review-stars{flex-shrink:0}
.review-stars text{font-size:22rpx;color:#e0d6cc}
.review-stars text.filled{color:#f39c12}
.review-content{font-size:26rpx;color:#5c4a3e;line-height:1.6}
.review-imgs{display:flex;gap:10rpx;margin-top:12rpx;flex-wrap:wrap}
.review-img{width:140rpx;height:140rpx;border-radius:10rpx}

.empty-reviews{text-align:center;padding:40rpx 28rpx}
.empty-text{font-size:26rpx;color:#c4b8ad}

.empty-tip{display:flex;flex-direction:column;align-items:center;padding:120rpx 40rpx;font-size:28rpx;color:#bbb}
.btn-back{margin-top:32rpx;width:240rpx;height:72rpx;display:flex;align-items:center;justify-content:center;padding:0;background:#e8927c;color:#fff;font-size:26rpx;font-weight:700;border-radius:36rpx;border:none}
.btn-back::after{border:none}

.bottom-spacer{height:140rpx}

.bottom-bar{position:fixed;bottom:0;left:0;right:0;display:flex;align-items:center;padding:14rpx 24rpx 36rpx;background:#fff;box-shadow:0 -2rpx 12rpx rgba(0,0,0,.05);gap:14rpx}
.bar-icon{position:relative;width:72rpx;display:flex;flex-direction:column;align-items:center;justify-content:center}
.bar-icon-text{font-size:38rpx}
.bar-badge{position:absolute;top:-6rpx;right:-6rpx;min-width:32rpx;height:32rpx;line-height:32rpx;padding:0 8rpx;background:#e85d4d;color:#fff;font-size:20rpx;font-weight:700;border-radius:16rpx;text-align:center}
.bar-btn{flex:1;height:80rpx;display:flex;align-items:center;justify-content:center;padding:0;font-size:28rpx;font-weight:700;border-radius:40rpx;border:none}
.bar-btn::after{border:none}
.add-cart{background:rgba(232,146,124,.12);color:#e8927c}
.buy-now{background:#e8927c;color:#fff}

.qty-modal-mask{position:fixed;inset:0;z-index:999;background:rgba(0,0,0,.35);display:flex;align-items:flex-end;justify-content:center}
.qty-modal{width:100%;background:#fff;border-radius:32rpx 32rpx 0 0;padding:32rpx 36rpx 48rpx}
.qty-modal-title{display:block;text-align:center;font-size:30rpx;font-weight:700;color:#2d1f18;margin-bottom:28rpx}
.qty-picker{display:flex;align-items:center;justify-content:center;gap:40rpx;margin-bottom:32rpx}
.qty-btn{width:64rpx;height:64rpx;border-radius:50%;background:#f5ece4;color:#8f7366;font-size:36rpx;font-weight:800;display:flex;align-items:center;justify-content:center}
.qty-val{font-size:40rpx;font-weight:800;color:#2d1f18;min-width:80rpx;text-align:center}
.qty-modal-actions{display:flex;gap:20rpx}
.qty-btn-cancel{flex:1;height:80rpx;display:flex;align-items:center;justify-content:center;padding:0;background:#f5ece4;color:#8f7366;font-size:28rpx;font-weight:600;border-radius:16rpx;border:none}
.qty-btn-confirm{flex:1;height:80rpx;display:flex;align-items:center;justify-content:center;padding:0;background:#e8927c;color:#fff;font-size:28rpx;font-weight:700;border-radius:16rpx;border:none}
.qty-btn-cancel::after,.qty-btn-confirm::after{border:none}
</style>
