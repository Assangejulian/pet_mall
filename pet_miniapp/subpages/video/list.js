var videoApi = require("../../utils/api/video");

var fallbackVideos = [
  {
    id: 1,
    title: "黑白猫的镜头日常",
    desc: "黑白小猫在镜头前放松伸展，适合慢慢看的一段陪伴视频。",
    cover: "/images/mock/cat-cover.jpg",
    url: "http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4",
    author: "暖窝小鱼",
    likes: 2300,
    playCount: 12000,
    productId: 2
  },
  {
    id: 2,
    title: "猫咪下巴挠挠时刻",
    desc: "室内猫咪被轻轻挠下巴，表情很放松。",
    cover: "/images/mock/ragdoll.jpg",
    url: "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4",
    author: "猫咖日记",
    likes: 5100,
    playCount: 35000,
    productId: 4
  },
  {
    id: 3,
    title: "猫咪小跑上楼梯",
    desc: "猫咪轻快地一路小跑上楼，动作灵活又可爱。",
    cover: "/images/mock/cat-avatar.jpg",
    url: "http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4",
    author: "暖窝小鱼",
    likes: 1800,
    playCount: 8500,
    productId: 2
  },
  {
    id: 4,
    title: "柯基毯上乖坐",
    desc: "柯基坐在毯子上看镜头，短腿和圆脸都很治愈。",
    cover: "/images/mock/corgi.jpg",
    url: "http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4",
    author: "布偶田田",
    likes: 3600,
    playCount: 22000,
    productId: 3
  },
  {
    id: 5,
    title: "金毛叼着郁金香",
    desc: "金毛叼着花靠近镜头，温柔又有春天感。",
    cover: "/images/mock/golden.jpg",
    url: "http://video.sonetto.online/Golden_retriever_holding_tulip_g%E2%80%A6_202607022323.mp4",
    author: "暖窝小暖",
    likes: 980,
    playCount: 5000,
    productId: 1
  },
  {
    id: 6,
    title: "开心比格犬户外跑跳",
    desc: "比格犬在户外开心活动，适合喜欢活泼狗狗的用户。",
    cover: "/images/mock/dog-avatar.jpg",
    url: "http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4",
    author: "暖窝小暖",
    likes: 1200,
    playCount: 6800,
    productId: 14
  },
  {
    id: 7,
    title: "橘猫木桌观察日记",
    desc: "橘猫趴在木桌上观察周围，节奏安静又舒服。",
    cover: "/images/mock/blue-cat.jpg",
    url: "http://video.sonetto.online/Orange_cat_on_wooden_table_202607022346.mp4",
    author: "猫咖日记",
    likes: 1800,
    playCount: 9200,
    productId: 2
  }
];

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

function normalizeVideo(item, index) {
  var source = item || {};
  var fallback = fallbackVideos[index % fallbackVideos.length];
  var layout = ["tile-tall", "tile-mid", "tile-short", "tile-tall", "tile-mid"][index % 5];
  return {
    id: source.id || fallback.id,
    title: source.title || fallback.title,
    desc: source.desc || source.description || fallback.desc,
    cover: source.cover || source.coverUrl || fallback.cover,
    url: source.url || source.videoUrl || fallback.url,
    author: source.author || source.userName || fallback.author || "暖窝用户",
    views: formatCount(source.playCount || source.views || fallback.playCount),
    likes: formatCount(source.likes || source.likeCount || fallback.likes),
    productId: source.productId || fallback.productId || "",
    tileClass: layout
  };
}

function splitColumns(videos) {
  var left = [];
  var right = [];
  videos.forEach(function(video, index) {
    if (index % 2 === 0) left.push(video);
    else right.push(video);
  });
  return { left: left, right: right };
}

Page({
  data: {
    videos: [],
    leftVideos: [],
    rightVideos: [],
    loading: true,
    useMock: false
  },

  onShow: function() {
    this.loadVideos();
  },

  onPullDownRefresh: function() {
    this.loadVideos(function() {
      wx.stopPullDownRefresh();
    });
  },

  loadVideos: function(done) {
    var that = this;
    that.setData({ loading: true });
    videoApi.list(1, 20).then(function(res) {
      var rows = extractRows(res);
      if (rows.length) {
        that.setVideoList(rows.map(normalizeVideo), false);
      } else {
        that.useFallback();
      }
      if (done) done();
    }).catch(function() {
      that.useFallback();
      if (done) done();
    });
  },

  setVideoList: function(videos, useMock) {
    var columns = splitColumns(videos);
    this.setData({
      videos: videos,
      leftVideos: columns.left,
      rightVideos: columns.right,
      loading: false,
      useMock: !!useMock
    });
  },

  useFallback: function() {
    this.setVideoList(fallbackVideos.map(normalizeVideo), true);
  },

  goDetail: function(event) {
    var id = event.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({ url: "/subpages/video/detail?id=" + id });
  },

  goProduct: function(event) {
    var id = event.currentTarget.dataset.pid;
    if (!id) return;
    wx.navigateTo({ url: "/subpages/detail/detail?id=" + id });
  }
});
