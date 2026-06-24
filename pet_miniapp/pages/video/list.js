const app = getApp();

const fallbackVideos = [
  {
    id: 1,
    title: "第一次接它回家",
    desc: "从到家动线、隔离区到第一晚观察，把小家伙安稳接回家。",
    cover: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800",
    url: "https://samplelib.com/preview/mp4/sample-5s.mp4",
    author: "暖窝小鱼",
    views: "2.3k",
    likes: "156",
    productName: "幼宠基础用品包",
    productId: 1
  },
  {
    id: 2,
    title: "狗狗兴奋乱扑怎么办",
    desc: "先让它学会坐下等待，再把奖励和社交绑定起来。",
    cover: "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800",
    url: "https://samplelib.com/preview/mp4/sample-10s.mp4",
    author: "布偶田田",
    views: "1.8k",
    likes: "89",
    productName: "耐咬训练玩具",
    productId: 3
  }
];

function formatCount(value) {
  const number = Number(value || 0);
  if (!Number.isFinite(number)) return value || "0";
  if (number >= 1000) return (number / 1000).toFixed(number >= 10000 ? 0 : 1) + "k";
  return String(number);
}

function extractRows(body) {
  const data = body && body.data ? body.data : body;
  if (!data) return [];
  if (Array.isArray(data)) return data;
  if (Array.isArray(data.records)) return data.records;
  return [];
}

function normalizeVideo(item, index) {
  const source = item || {};
  const fallback = fallbackVideos[index % fallbackVideos.length];
  return {
    id: source.id || fallback.id,
    title: source.title || fallback.title,
    desc: source.desc || source.description || fallback.desc,
    cover: source.cover || source.coverUrl || fallback.cover,
    url: source.url || source.videoUrl || fallback.url,
    author: source.author || source.userName || fallback.author,
    views: formatCount(source.playCount || source.views || fallback.views),
    likes: formatCount(source.likes || source.likeCount || fallback.likes),
    productName: source.productName || fallback.productName || "",
    productId: source.productId || fallback.productId || ""
  };
}

Page({
  data: {
    videos: [],
    loading: true,
    useMock: false
  },

  onShow() {
    this.loadVideos();
  },

  onPullDownRefresh() {
    this.loadVideos(() => wx.stopPullDownRefresh());
  },

  loadVideos(done) {
    this.setData({ loading: true });
    wx.request({
      url: app.globalData.baseUrl + "/api/video/feed",
      method: "GET",
      data: { page: 1, size: 20 },
      success: (res) => {
        const rows = extractRows(res.data);
        if (rows.length) {
          this.setData({
            videos: rows.map(normalizeVideo),
            loading: false,
            useMock: false
          });
        } else {
          this.useFallback();
        }
      },
      fail: () => this.useFallback(),
      complete: () => {
        if (done) done();
      }
    });
  },

  useFallback() {
    this.setData({
      videos: fallbackVideos,
      loading: false,
      useMock: true
    });
  },

  goDetail(event) {
    const id = event.currentTarget.dataset.id;
    wx.navigateTo({
      url: "/pages/video/detail?id=" + id,
      fail: () => wx.showToast({ title: "视频详情页打开失败", icon: "none" })
    });
  },

  goProduct(event) {
    const id = event.currentTarget.dataset.pid;
    if (!id) return;
    wx.navigateTo({ url: "/pages/detail/detail?id=" + id });
  }
});
