const app = getApp();

const fallbackVideos = [
  {
    id: 1,
    title: "第一次接它回家",
    desc: "从隔离区到第一晚观察，把小家伙安稳接回家。",
    cover: "/images/mock/cat-cover.jpg",
    author: "暖窝小鱼",
    avatar: "/images/mock/cat-avatar.jpg",
    likes: "2.3k",
    commentCount: 156,
    size: "tall"
  },
  {
    id: 2,
    title: "狗狗兴奋乱扑怎么办",
    desc: "先让它学会坐下等待，再把奖励和社交绑定起来。",
    cover: "/images/mock/golden.jpg",
    author: "布偶田田",
    avatar: "/images/mock/dog-avatar.jpg",
    likes: "1.8k",
    commentCount: 89,
    size: "short"
  },
  {
    id: 3,
    title: "猫咪食欲变差怎么办",
    desc: "排查换粮、温度、压力和精神状态，先观察重点信号。",
    cover: "/images/mock/blue-cat.jpg",
    author: "猫咪日记",
    avatar: "/images/mock/cat-avatar.jpg",
    likes: "5.1k",
    commentCount: 432,
    size: "medium"
  },
  {
    id: 4,
    title: "幼宠用品清单",
    desc: "笼具、食盆、牵引和清洁用品先准备基础款，别一开始买太多。",
    cover: "/images/mock/corgi.jpg",
    author: "吱星日记",
    avatar: "/images/mock/dog-avatar.jpg",
    likes: "980",
    commentCount: 45,
    size: "tall"
  }
];

function formatCount(value) {
  const number = Number(value || 0);
  if (!Number.isFinite(number)) return value || "0";
  if (number >= 1000) return (number / 1000).toFixed(number >= 10000 ? 0 : 1) + "k";
  return String(number);
}

function normalizeVideo(item, index) {
  const source = item || {};
  const fallback = fallbackVideos[index % fallbackVideos.length];
  const sizes = ["tall", "short", "medium", "tall", "short", "medium"];
  return {
    id: source.id || fallback.id,
    title: source.title || fallback.title,
    desc: source.desc || source.description || fallback.desc,
    cover: source.cover || source.coverUrl || fallback.cover,
    url: source.url || source.videoUrl || "",
    author: source.author || source.userName || fallback.author || "暖窝用户",
    avatar: source.avatar || fallback.avatar,
    likes: formatCount(source.likes || source.likeCount || fallback.likes),
    commentCount: source.commentCount || fallback.commentCount || 0,
    productId: source.productId || fallback.productId || "",
    duration: source.duration || "",
    size: source.size || fallback.size || sizes[index % sizes.length]
  };
}

function extractRows(body) {
  const data = body && body.data ? body.data : body;
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}

Page({
  data: {
    videos: [],
    leftVideos: [],
    rightVideos: [],
    curTab: "recommend",
    loading: true,
    useMock: false,
    tabs: [
      { id: "recommend", label: "推荐" },
      { id: "care", label: "照护" },
      { id: "nearby", label: "附近" }
    ]
  },

  onLoad() {
    this.load();
  },

  onPullDownRefresh() {
    this.load(() => wx.stopPullDownRefresh());
  },

  load(done) {
    this.setData({ loading: true });
    wx.request({
      url: app.globalData.baseUrl + "/api/video/feed",
      method: "GET",
      data: { page: 1, size: 20 },
      success: (res) => {
        const rows = extractRows(res.data);
        if (rows.length) {
          this.setVideos(rows.map(normalizeVideo), false);
        } else {
          this.useFallback("后端暂无视频，临时展示本地演示数据");
        }
      },
      fail: () => this.useFallback("无法连接后端，临时展示本地演示数据"),
      complete: () => {
        if (done) done();
      }
    });
  },

  useFallback(message) {
    this.setVideos(fallbackVideos, true);
    if (message) {
      wx.showToast({ title: message, icon: "none" });
    }
  },

  setVideos(videos, useMock) {
    const leftVideos = [];
    const rightVideos = [];
    videos.forEach((item, index) => {
      if (index % 2 === 0) {
        leftVideos.push(item);
      } else {
        rightVideos.push(item);
      }
    });
    this.setData({
      videos,
      leftVideos,
      rightVideos,
      loading: false,
      useMock
    });
  },

  swTab(event) {
    this.setData({ curTab: event.currentTarget.dataset.id });
    this.load();
  },

  goDetail(event) {
    const id = event.currentTarget.dataset.id;
    if (!id) {
      wx.showToast({ title: "视频数据缺少 ID", icon: "none" });
      return;
    }
    wx.navigateTo({
      url: "/pages/video/detail?id=" + id,
      fail: () => {
        wx.showToast({ title: "视频详情页打开失败", icon: "none" });
      }
    });
  },

  goAi() {
    wx.switchTab({ url: "/pages/chat/chat" });
  }
});
