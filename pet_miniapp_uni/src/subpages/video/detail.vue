<template>
<view class="video-page" @touchstart="onTouchStart" @touchend="onTouchEnd">
  <swiper class="video-swiper" vertical :current="currentIndex" circular="{{false}}" duration="220" @change="onSwiperChange">
    <swiper-item v-for="item in posts" :key="id" class="video-slide">
      <video v-if="currentIndex === index && item.url" id="mainVideo" class="video-layer" :src="item.url" :poster="item.cover" autoplay loop controls enable-progress-gesture show-center-play-btn object-fit="cover"></video>
      <image v-else class="video-layer" :src="item.cover" mode="aspectFill" />
    </swiper-item>
  </swiper>

  <view class="top-fade"></view>
  <view class="bottom-fade"></view>

  <view class="topbar">
    <view class="back" @tap="goBack">‹</view>
    <text class="top-title">暖窝视频</text>
    <view class="mock-badge" v-if="useMock">演示</view>
  </view>

  <view class="side-actions">
    <view class="avatar-wrap" @tap="toggleFollow">
      <image class="avatar" :src="post.avatar" mode="aspectFill" />
      <view class="follow-dot">{{followed ? "✓" : "+"}}</view>
    </view>
    <view class="action {{liked ? 'active' : ''}}" @tap="toggleLike">
      <text class="action-icon">♥</text>
      <text class="action-label">{{post.likes}}</text>
    </view>
    <view class="action" @tap="toggleComments">
      <text class="action-icon">评</text>
      <text class="action-label">{{comments.length || post.commentCount}}</text>
    </view>
    <view class="action" @tap="goChat">
      <text class="action-icon">问</text>
      <text class="action-label">客服</text>
    </view>
    <view class="action buy" @tap="goProduct">
      <text class="action-icon">购</text>
      <text class="action-label">购买</text>
    </view>
  </view>

  <view class="caption">
    <view class="author-row">
      <text class="author">@{{post.author}}</text>
      <view class="follow-btn {{followed ? 'on' : ''}}" @tap="toggleFollow">{{followed ? "已关注" : "关注"}}</view>
    </view>
    <text class="title">{{post.title}}</text>
    <text class="desc">{{post.desc}}</text>
    <view class="product-bar" v-if="post.productId" @tap="goProduct">
      <text class="product-name"><//text>
      <text class="product-price" v-if="product.price">￥{{product.price}}</text>
      <text class="product-action">去看看</text>
    </view>
    <view class="position-indicator">{{currentIndex + 1}} / {{posts.length}}</view>
  </view>

  <view class="comment-mask" v-if="showComments" @tap="closeComments"></view>
  <view class="comment-panel {{showComments ? 'show' : ''}}" @tap.stop="noop">
    <view class="panel-handle"></view>
    <view class="panel-title">
      <text>评论 {{comments.length}}</text>
      <text class="panel-close" @tap="closeComments">关闭</text>
    </view>
    <scroll-view class="comment-scroll" scroll-y>
      <view class="comment" v-for="item in comments" :key="id">
        <image class="comment-avatar" :src="item.avatar" mode="aspectFill" />
        <view class="comment-body">
          <view class="comment-header">
            <text class="comment-name">{{item.user}}</text>
            <text class="buyer-tag" v-if="item.isBuyer">[买家评论]</text>
          </view>
          <text class="comment-text">{{item.text}}</text>
          <text class="comment-time">{{item.time}}</text>
        </view>
      </view>
    </scroll-view>
    <view class="comment-input">
      <input value="{{inputText}}" placeholder="说点什么..." @input="onInput" confirm-type="send" @confirm="sendComment" />
      <view class="send {{inputText ? 'active' : ''}}" @tap="sendComment">发送</view>
    </view>
  </view>
</view>

</template>

<script>import videoApi from "@/utils/api/video";

var fallbackPosts = [
  { id: 1, title: "黑白猫的镜头日常", desc: "黑白小猫在镜头前放松伸展，适合慢慢看的一段陪伴视频。", url: "http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4", cover: "/images/mock/cat-cover.jpg", author: "暖窝小鱼", avatar: "/images/mock/cat-avatar.jpg", likes: 2300, commentCount: 156, productId: 2 },
  { id: 2, title: "猫咪下巴挠挠时刻", desc: "室内猫咪被轻轻挠下巴，表情很放松。", url: "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4", cover: "/images/mock/ragdoll.jpg", author: "猫咖日记", avatar: "/images/mock/cat-avatar.jpg", likes: 5100, commentCount: 432, productId: 4 },
  { id: 3, title: "猫咪小跑上楼梯", desc: "猫咪轻快地一路小跑上楼，动作灵活又可爱。", url: "http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4", cover: "/images/mock/cat-avatar.jpg", author: "暖窝小鱼", avatar: "/images/mock/cat-avatar.jpg", likes: 1800, commentCount: 89, productId: 2 },
  { id: 4, title: "柯基毯上乖坐", desc: "柯基坐在毯子上看镜头，短腿和圆脸都很治愈。", url: "http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4", cover: "/images/mock/corgi.jpg", author: "布偶田田", avatar: "/images/mock/dog-avatar.jpg", likes: 3600, commentCount: 278, productId: 3 },
  { id: 5, title: "金毛叼着郁金香", desc: "金毛叼着花靠近镜头，温柔又有春天感。", url: "http://video.sonetto.online/Golden_retriever_holding_tulip_g%E2%80%A6_202607022323.mp4", cover: "/images/mock/golden.jpg", author: "暖窝小暖", avatar: "/images/mock/dog-avatar.jpg", likes: 980, commentCount: 45, productId: 1 },
  { id: 6, title: "开心比格犬户外跑跳", desc: "比格犬在户外开心活动，适合喜欢活泼狗狗的用户。", url: "http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4", cover: "/images/mock/dog-avatar.jpg", author: "暖窝小暖", avatar: "/images/mock/dog-avatar.jpg", likes: 1200, commentCount: 89, productId: 14 },
  { id: 7, title: "橘猫木桌观察日记", desc: "橘猫趴在木桌上观察周围，节奏安静又舒服。", url: "http://video.sonetto.online/Orange_cat_on_wooden_table_202607022346.mp4", cover: "/images/mock/blue-cat.jpg", author: "猫咖日记", avatar: "/images/mock/cat-avatar.jpg", likes: 1800, commentCount: 112, productId: 2 }
];

var fallbackComments = [
  { id: 1, user: "小鱼干", avatar: "/images/mock/cat-avatar.jpg", text: "这个镜头太治愈了，想一直循环看。", time: "2小时前" },
  { id: 2, user: "毛球控", avatar: "/images/mock/dog-avatar.jpg", text: "封面和视频都很清楚，商品入口也好找。", time: "5小时前" }
];

var productMap = {
  1: { name: "金毛幼犬", price: "1888" },
  2: { name: "英短蓝猫", price: "2580" },
  3: { name: "柯基犬", price: "3200" },
  4: { name: "布偶猫", price: "4500" },
  14: { name: "贵宾幼犬", price: "2800" }
};

function formatCount(value) {
  var number = Number(value || 0);
  if (!Number.isFinite(number)) return value || "0";
  if (number >= 10000) return (number / 10000).toFixed(1).replace(/\.0$/, "") + "w";
  if (number >= 1000) return (number / 1000).toFixed(1).replace(/\.0$/, "") + "k";
  return String(number);
}

function extractRows(body) {
  var data = body && body.data ? body.data : body;
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}

function normalizePost(item, index) {
  var source = item || {};
  var fallback = fallbackPosts[index % fallbackPosts.length];
  return {
    id: source.id || fallback.id,
    title: source.title || fallback.title,
    desc: source.desc || source.description || fallback.desc,
    url: source.url || source.videoUrl || fallback.url,
    cover: source.cover || source.coverUrl || fallback.cover,
    author: source.author || source.userName || fallback.author || "暖窝用户",
    avatar: source.avatar || fallback.avatar || "/images/mock/cat-avatar.jpg",
    likes: formatCount(source.likes || source.likeCount || fallback.likes),
    commentCount: source.commentCount || fallback.commentCount || 0,
    productId: source.productId || fallback.productId || ""
  };
}

function normalizeComment(item) {
  item = item || {};
  return {
    id: item.id || Date.now(),
    user: item.user || item.userName || "暖窝用户",
    avatar: item.avatar || "/images/mock/cat-avatar.jpg",
    text: item.text || item.content || "",
    time: item.time || "刚刚"
  };
}

export default {
  data() { return {
    posts: [],
    currentIndex: 0,
    post: {},
    product: {},
    comments: [],
    inputText: "",
    loading: true,
    useMock: false,
    followed: false,
    liked: false,
    showComments: false,
    isFirst: true,
    isLast: false
    }
  },

  touchStartY: 0,
  touchStartIndex: 0,

  onLoad: function(options) {
    var id = (options && options.id) || 1;
    this.loadFeed(id);
  },

  loadFeed: function(id) {
    const that = this;
    that.loading = true;
    videoApi.list(1, 20).then(function(res) {
      var rows = extractRows(res);
      var posts = rows.length ? rows.map(normalizePost) : fallbackPosts.map(normalizePost);
      that.setPosts(posts, id, !rows.length);
    }).catch(function() {
      that.setPosts(fallbackPosts.map(normalizePost), id, true);
      uni.showToast({ title: "视频加载失败，已使用演示数据", icon: "none" });
    });
  },

  setPosts: function(posts, id, useMock) {
    var index = 0;
    for (var i = 0; i < posts.length; i++) {
      if (String(posts[i].id) === String(id)) {
        index = i;
        break;
      }
    }
    this.posts = posts;
        this.currentIndex = index;
        this.loading = false;
        this.useMock = !!useMock;
    this.activatePost(index);
  },

  activatePost: function(index) {
    var posts = this.posts || [];
    if (!posts.length || index < 0 || index >= posts.length) return;
    var post = posts[index];
    this.currentIndex = index;
      this.post = post;
      this.product = productMap[post.productId] || {};
      this.comments = [];
      this.inputText = "";
      this.showComments = false;
      this.liked = false;
      this.isFirst = index === 0;
      this.isLast = index === posts.length - 1;
    this.loadComments(post.id);
    this.playCurrentVideo();
  },

  playCurrentVideo: function() {
    const that = this;
    setTimeout(function() {
      if (!uni.createVideoContext) return;
      var video = uni.createVideoContext("mainVideo", that);
      if (video && video.play) video.play();
    }, 120);
  },

  onSwiperChange: function(event) {
    var index = event.detail && typeof event.detail.current === "number"
      ? event.detail.current
      : this.currentIndex;
    if (index === this.currentIndex) return;
    this.activatePost(index);
  },

  onTouchStart: function(event) {
    var touch = event.touches && event.touches[0];
    this.touchStartY = touch ? touch.clientY : 0;
    this.touchStartIndex = this.currentIndex;
  },

  onTouchEnd: function(event) {
    var touch = event.changedTouches && event.changedTouches[0];
    if (!touch) return;
    var deltaY = touch.clientY - this.touchStartY;
    if (this.touchStartIndex === 0 && deltaY > 80) {
      uni.showToast({ title: "已经是第一个视频", icon: "none" });
    }
    if (this.touchStartIndex === this.posts.length - 1 && deltaY < -80) {
      uni.showToast({ title: "已经是最后一个视频", icon: "none" });
    }
  },

  loadComments: function(id) {
    const that = this;
    videoApi.comments(id).then(function(data) {
      var list = Array.isArray(data) ? data.map(normalizeComment) : fallbackComments;
      that.comments = list;
    }).catch(function() {
      that.comments = fallbackComments;
    });
  },

  goBack: function() {
    var pages = getCurrentPages();
    if (pages.length > 1) {
      uni.navigateBack();
    } else {
      uni.switchTab({ url: "/pages/index/index" });
    }
  },

  toggleFollow: function() {
    this.followed = !this.followed;
  },

  toggleLike: function() {
    var nextLiked = !this.liked;
    this.liked = nextLiked;
    if (nextLiked && this.post.id) {
      videoApi.like(this.post.id);
    }
  },

  toggleComments: function() {
    this.showComments = !this.showComments;
  },

  closeComments: function() {
    this.showComments = false;
  },

  noop: function() {},

  goProduct: function() {
    var pid = this.post.productId;
    if (!pid) {
      uni.showToast({ title: "暂无关联商品", icon: "none" });
      return;
    }
    uni.navigateTo({ url: "/subpages/detail/detail?id=" + pid });
  },

  goChat: function() {
    uni.switchTab({ url: "/pages/chat/chat" });
  },

  onInput: function(event) {
    this.inputText = event.detail.value;
  },

  sendComment: function() {
    var text = (this.inputText || "").trim();
    if (!text) return;
    var comment = {
      id: Date.now(),
      user: "我",
      avatar: "/images/mock/cat-avatar.jpg",
      text: text,
      time: "刚刚"
    };
    this.comments = [comment].concat(this.comments);
        this.inputText = "";
        this.showComments = true;
    if (this.post.id) {
      videoApi.addComment(this.post.id, text);
    }
  }
}</script>

<style scoped>
page {
  background: #050505;
  overflow: hidden;
}

.video-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #050505;
  color: #fff;
}

.video-swiper,
.video-slide {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}

.video-layer {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: block;
  background: #050505;
}

.top-fade,
.bottom-fade {
  position: absolute;
  left: 0;
  right: 0;
  pointer-events: none;
}

.top-fade {
  top: 0;
  height: 210rpx;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.62), rgba(0, 0, 0, 0));
}

.bottom-fade {
  bottom: 0;
  height: 470rpx;
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.78), rgba(0, 0, 0, 0));
}

.topbar {
  position: absolute;
  left: 22rpx;
  right: 22rpx;
  top: calc(22rpx + env(safe-area-inset-top));
  height: 64rpx;
  display: flex;
  align-items: center;
  z-index: 4;
}

.back {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.34);
  color: #fff;
  font-size: 52rpx;
  line-height: 1;
}

.top-title {
  margin-left: 18rpx;
  font-size: 30rpx;
  font-weight: 800;
}

.mock-badge {
  margin-left: auto;
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 22rpx;
}

.side-actions {
  position: absolute;
  right: 18rpx;
  bottom: calc(190rpx + env(safe-area-inset-bottom));
  width: 92rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 22rpx;
  z-index: 5;
}

.avatar-wrap {
  position: relative;
  width: 78rpx;
  height: 78rpx;
}

.avatar {
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.9);
  background: #3a302c;
}

.follow-dot {
  position: absolute;
  left: 22rpx;
  bottom: -13rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e85d50;
  color: #fff;
  font-size: 25rpx;
  font-weight: 900;
}

.action {
  width: 86rpx;
  min-height: 86rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.36);
  color: #fff;
}

.action.active {
  color: #ff6d64;
}

.action.buy {
  background: #f5eee6;
  color: #201815;
}

.action-icon {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 1;
}

.action-label {
  display: block;
  max-width: 78rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 19rpx;
  font-weight: 800;
  line-height: 1.1;
}

.caption {
  position: absolute;
  left: 26rpx;
  right: 126rpx;
  bottom: calc(34rpx + env(safe-area-inset-bottom));
  z-index: 4;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 14rpx;
}

.author {
  min-width: 0;
  max-width: 330rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 28rpx;
  font-weight: 850;
}

.follow-btn {
  height: 44rpx;
  line-height: 44rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #e85d50;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
}

.follow-btn.on {
  background: rgba(255, 255, 255, 0.22);
}

.title {
  display: block;
  color: #fff;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.18;
}

.desc {
  display: -webkit-box;
  margin-top: 12rpx;
  color: rgba(255, 255, 255, 0.82);
  font-size: 25rpx;
  line-height: 1.44;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.product-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 20rpx;
  max-width: 520rpx;
  height: 62rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: rgba(245, 238, 230, 0.94);
  color: #201815;
}

.product-name {
  min-width: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 24rpx;
  font-weight: 850;
}

.product-price {
  flex-shrink: 0;
  color: #c45d4d;
  font-size: 23rpx;
  font-weight: 900;
}

.product-action {
  flex-shrink: 0;
  color: #173e35;
  font-size: 22rpx;
  font-weight: 900;
}

.position-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 14rpx;
  height: 38rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.78);
  font-size: 21rpx;
  font-weight: 800;
}

.comment-mask {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 8;
  background: rgba(0, 0, 0, 0.22);
}

.comment-panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 64vh;
  padding: 12rpx 24rpx calc(20rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  border-top-left-radius: 16rpx;
  border-top-right-radius: 16rpx;
  background: #f7f1ea;
  color: #201815;
  transform: translateY(105%);
  transition: transform 180ms ease;
  z-index: 9;
}

.comment-panel.show {
  transform: translateY(0);
}

.panel-handle {
  width: 74rpx;
  height: 8rpx;
  margin: 0 auto 16rpx;
  border-radius: 999rpx;
  background: #d4c8bb;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
  font-size: 30rpx;
  font-weight: 900;
}

.panel-close {
  color: #8b7b70;
  font-size: 24rpx;
  font-weight: 800;
}

.comment-scroll {
  height: calc(64vh - 174rpx - env(safe-area-inset-bottom));
}

.comment {
  display: flex;
  gap: 14rpx;
  margin-bottom: 24rpx;
}

.comment-avatar {
  width: 58rpx;
  height: 58rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #ded4ca;
}

.comment-body {
  min-width: 0;
  flex: 1;
}

.comment-name {
  display: block;
  color: #201815;
  font-size: 24rpx;
  font-weight: 850;
}

.comment-text {
  display: block;
  margin-top: 6rpx;
  color: #4f433d;
  font-size: 26rpx;
  line-height: 1.45;
}

.comment-time {
  display: block;
  margin-top: 6rpx;
  color: #9b8b80;
  font-size: 21rpx;
}

.comment-input {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding-top: 14rpx;
}

.comment-input input {
  flex: 1;
  height: 70rpx;
  min-width: 0;
  padding: 0 22rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #201815;
  font-size: 25rpx;
}

.send {
  width: 104rpx;
  height: 70rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #9b8b80;
  color: #fff;
  font-size: 25rpx;
  font-weight: 850;
}

.send.active {
  background: #173e35;
}

</style>
