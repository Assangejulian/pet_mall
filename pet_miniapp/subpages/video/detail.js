const app = getApp();

const fallbackPosts = [
  {
    id: 1,
    title: "第一次接它回家",
    desc: "从隔离区到第一晚观察，把小家伙安稳接回家。",
    url: "",
    cover: "/images/mock/cat-cover.jpg",
    author: "暖窝小鱼",
    avatar: "/images/mock/cat-avatar.jpg",
    likes: 2300,
    commentCount: 156,
    productId: 1
  },
  {
    id: 2,
    title: "狗狗兴奋乱扑怎么办",
    desc: "先让它学会坐下等待，再把奖励和社交绑定起来。",
    url: "",
    cover: "/images/mock/golden.jpg",
    author: "布偶田田",
    avatar: "/images/mock/dog-avatar.jpg",
    likes: 1860,
    commentCount: 89,
    productId: 3
  }
];

const fallbackComments = [
  {
    id: 1,
    user: "小鱼干",
    avatar: "/images/mock/cat-avatar.jpg",
    text: "隔离区这个点很有用，第一晚确实别太频繁打扰。",
    time: "2小时前"
  },
  {
    id: 2,
    user: "毛球控",
    avatar: "/images/mock/dog-avatar.jpg",
    text: "坐下等待比直接压住它有效多了。",
    time: "5小时前"
  }
];

const products = {
  1: { name: "幼宠基础用品包", price: "99" },
  2: { name: "低敏主粮试吃装", price: "39" },
  3: { name: "耐咬训练玩具", price: "59" },
  4: { name: "观察记录卡", price: "19.9" }
};

function formatCount(value) {
  const number = Number(value || 0);
  if (!Number.isFinite(number)) return value || "0";
  if (number >= 1000) return (number / 1000).toFixed(number >= 10000 ? 0 : 1) + "k";
  return String(number);
}

function normalizePost(item, id) {
  const source = item || {};
  const fallback = fallbackPosts.find((post) => String(post.id) === String(id)) || fallbackPosts[0];
  return {
    id: source.id || fallback.id,
    title: source.title || fallback.title,
    desc: source.desc || source.description || fallback.desc,
    url: source.url || source.videoUrl || fallback.url,
    cover: source.cover || source.coverUrl || fallback.cover,
    author: source.author || source.userName || fallback.author || "暖窝用户",
    avatar: source.avatar || fallback.avatar,
    likes: formatCount(source.likes || source.likeCount || fallback.likes),
    commentCount: source.commentCount || fallback.commentCount || 0,
    productId: source.productId || fallback.productId || ""
  };
}

function normalizeComment(item) {
  return {
    id: item.id || Date.now(),
    user: item.user || item.userName || "暖窝用户",
    avatar: item.avatar || "/images/mock/cat-avatar.jpg",
    text: item.text || item.content || "",
    time: item.time || "刚刚"
  };
}

Page({
  data: {
    post: {},
    followed: false,
    liked: false,
    collected: false,
    product: {},
    comments: [],
    inputText: "",
    useMock: false
  },

  onLoad(options) {
    const id = options.id || 1;
    this.loadDetail(id);
    this.loadComments(id);
  },

  loadDetail(id) {
    wx.request({
      url: app.globalData.baseUrl + "/api/video/" + id,
      method: "GET",
      success: (res) => {
        const post = normalizePost(res.data && res.data.data, id);
        this.setData({
          post,
          product: products[post.productId] || {},
          useMock: false
        });
      },
      fail: () => {
        const post = normalizePost(null, id);
        this.setData({
          post,
          product: products[post.productId] || {},
          useMock: true
        });
        wx.showToast({ title: "后端连接失败，展示本地详情", icon: "none" });
      }
    });
  },

  loadComments(id) {
    wx.request({
      url: app.globalData.baseUrl + "/api/video/" + id + "/comments",
      method: "GET",
      success: (res) => {
        const data = res.data && res.data.data;
        const comments = Array.isArray(data) ? data.map(normalizeComment) : fallbackComments;
        this.setData({ comments });
      },
      fail: () => {
        this.setData({ comments: fallbackComments });
      }
    });
  },

  toggleFollow() {
    this.setData({ followed: !this.data.followed });
  },

  toggleLike() {
    const nextLiked = !this.data.liked;
    this.setData({ liked: nextLiked });
    if (!nextLiked) return;
    wx.request({
      url: app.globalData.baseUrl + "/api/video/" + this.data.post.id + "/like",
      method: "POST"
    });
  },

  toggleCollect() {
    this.setData({ collected: !this.data.collected });
  },

  goProduct() {
    const productId = this.data.post.productId;
    if (productId) wx.navigateTo({ url: "/subpages/detail/detail?id=" + productId });
  },

  goChat() {
    wx.switchTab({ url: "/pages/chat/chat" });
  },

  onInput(event) {
    this.setData({ inputText: event.detail.value });
  },

  sendComment() {
    const text = (this.data.inputText || "").trim();
    if (!text) return;
    const localComment = {
      id: Date.now(),
      user: "我",
      avatar: "/images/mock/cat-avatar.jpg",
      text,
      time: "刚刚"
    };
    this.setData({
      comments: [localComment].concat(this.data.comments),
      inputText: ""
    });
    wx.request({
      url: app.globalData.baseUrl + "/api/video/" + this.data.post.id + "/comment",
      method: "POST",
      header: { "content-type": "application/json" },
      data: {
        userId: app.globalData.user && app.globalData.user.id,
        content: text
      }
    });
  }
});
