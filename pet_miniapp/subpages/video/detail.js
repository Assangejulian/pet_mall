var videoApi = require("../../utils/api/video");

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
    commentCount: Number(source.commentCount || fallback.commentCount || 0),
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
    time: item.time || "刚刚",
    isBuyer: !!item.isBuyer
  };
}

Page({
  data: {
    posts: [],
    currentIndex: 0,
    post: {},
    product: {},
    comments: [],
    commentTotal: 0,
    inputText: "",
    loading: true,
    useMock: false,
    followed: false,
    liked: false,
    showComments: false,
    isFirst: true,
    isLast: false
  },

  touchStartY: 0,
  touchStartIndex: 0,

  onLoad: function(options) {
    var id = (options && options.id) || 1;
    this.loadFeed(id);
  },

  loadFeed: function(id) {
    var that = this;
    that.setData({ loading: true });
    videoApi.list(1, 20).then(function(res) {
      var rows = extractRows(res);
      var posts = rows.length ? rows.map(normalizePost) : fallbackPosts.map(normalizePost);
      that.setPosts(posts, id, !rows.length);
    }).catch(function() {
      that.setPosts(fallbackPosts.map(normalizePost), id, true);
      wx.showToast({ title: "视频加载失败，已使用演示数据", icon: "none" });
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
    this.setData({
      posts: posts,
      currentIndex: index,
      loading: false,
      useMock: !!useMock
    });
    this.activatePost(index);
  },

  activatePost: function(index) {
    var posts = this.data.posts || [];
    if (!posts.length || index < 0 || index >= posts.length) return;
    var post = posts[index];
    this.setData({
      currentIndex: index,
      post: post,
      product: productMap[post.productId] || {},
      comments: [],
      commentTotal: Number(post.commentCount || 0),
      inputText: "",
      showComments: false,
      liked: false,
      isFirst: index === 0,
      isLast: index === posts.length - 1
    });
    this.loadComments(post.id);
    this.playCurrentVideo();
  },

  playCurrentVideo: function() {
    var that = this;
    setTimeout(function() {
      if (!wx.createVideoContext) return;
      var video = wx.createVideoContext("mainVideo", that);
      if (video && video.play) video.play();
    }, 120);
  },

  onSwiperChange: function(event) {
    var index = event.detail && typeof event.detail.current === "number"
      ? event.detail.current
      : this.data.currentIndex;
    if (index === this.data.currentIndex) return;
    this.activatePost(index);
  },

  onTouchStart: function(event) {
    var touch = event.touches && event.touches[0];
    this.touchStartY = touch ? touch.clientY : 0;
    this.touchStartIndex = this.data.currentIndex;
  },

  onTouchEnd: function(event) {
    var touch = event.changedTouches && event.changedTouches[0];
    if (!touch) return;
    var deltaY = touch.clientY - this.touchStartY;
    if (this.touchStartIndex === 0 && deltaY > 80) {
      wx.showToast({ title: "已经是第一个视频", icon: "none" });
    }
    if (this.touchStartIndex === this.data.posts.length - 1 && deltaY < -80) {
      wx.showToast({ title: "已经是最后一个视频", icon: "none" });
    }
  },

  loadComments: function(id) {
    var that = this;
    videoApi.comments(id).then(function(data) {
      var list = Array.isArray(data) ? data.map(normalizeComment) : fallbackComments;
      that.setCommentList(list);
    }).catch(function() {
      that.setCommentList(fallbackComments);
    });
  },

  setCommentList: function(list) {
    var post = Object.assign({}, this.data.post || {});
    post.commentCount = list.length;
    this.setData({
      comments: list,
      commentTotal: list.length,
      post: post
    });
  },

  goBack: function() {
    var pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
    } else {
      wx.switchTab({ url: "/pages/index/index" });
    }
  },

  toggleFollow: function() {
    this.setData({ followed: !this.data.followed });
  },

  toggleLike: function() {
    var nextLiked = !this.data.liked;
    this.setData({ liked: nextLiked });
    if (nextLiked && this.data.post.id) {
      videoApi.like(this.data.post.id);
    }
  },

  toggleComments: function() {
    this.setData({ showComments: !this.data.showComments });
  },

  closeComments: function() {
    this.setData({ showComments: false });
  },

  noop: function() {},

  goProduct: function() {
    var pid = this.data.post.productId;
    if (!pid) {
      wx.showToast({ title: "暂无关联商品", icon: "none" });
      return;
    }
    wx.navigateTo({ url: "/subpages/detail/detail?id=" + pid });
  },

  goChat: function() {
    wx.switchTab({ url: "/pages/chat/chat" });
  },

  onInput: function(event) {
    this.setData({ inputText: event.detail.value });
  },

  sendComment: function() {
    var text = (this.data.inputText || "").trim();
    if (!text) return;
    var comment = {
      id: Date.now(),
      user: "我",
      avatar: "/images/mock/cat-avatar.jpg",
      text: text,
      time: "刚刚"
    };
    this.setData({
      comments: [comment].concat(this.data.comments),
      commentTotal: this.data.commentTotal + 1,
      inputText: "",
      showComments: true
    });
    if (this.data.post.id) {
      var that = this;
      videoApi.addComment(this.data.post.id, text).then(function() {
        that.loadComments(that.data.post.id);
      });
    }
  }
});
