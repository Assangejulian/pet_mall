<template>
  <view class="page">
    <view class="header">
      <text class="eyebrow">暖窝视频</text>
      <text class="title">发现正在发生的宠物瞬间</text>
      <text class="subtitle">瀑布流浏览，点击进入全屏短视频观看。</text>
    </view>

    <view class="mock-tip" v-if="useMock">当前使用本地演示数据</view>

    <view class="loading" v-if="loading">
      <view class="loading-cover"></view>
      <view class="loading-cover small"></view>
    </view>

    <view class="masonry" v-else>
      <view class="column">
        <view
          v-for="item in leftVideos"
          :key="item.id"
          class="video-tile"
          :class="item.tileClass"
          @tap="goDetail(item.id)"
          hover-class="tile-press"
          :hover-stay-time="80"
        >
          <image class="cover" :src="item.cover" mode="aspectFill" lazy-load />
          <view class="cover-shade"></view>
          <view class="play-mark">▶</view>
          <view class="tile-copy">
            <text class="video-title">{{ item.title }}</text>
            <text class="video-desc">{{ item.desc }}</text>
            <view class="meta">
              <text class="author">{{ item.author }}</text>
              <text class="count">{{ item.likes }} 赞</text>
            </view>
          </view>
          <view class="product-link" v-if="item.productId" @tap.stop="goProduct(item.productId)">关联商品</view>
        </view>
      </view>

      <view class="column">
        <view
          v-for="item in rightVideos"
          :key="item.id"
          class="video-tile"
          :class="item.tileClass"
          @tap="goDetail(item.id)"
          hover-class="tile-press"
          :hover-stay-time="80"
        >
          <image class="cover" :src="item.cover" mode="aspectFill" lazy-load />
          <view class="cover-shade"></view>
          <view class="play-mark">▶</view>
          <view class="tile-copy">
            <text class="video-title">{{ item.title }}</text>
            <text class="video-desc">{{ item.desc }}</text>
            <view class="meta">
              <text class="author">{{ item.author }}</text>
              <text class="count">{{ item.likes }} 赞</text>
            </view>
          </view>
          <view class="product-link" v-if="item.productId" @tap.stop="goProduct(item.productId)">关联商品</view>
        </view>
      </view>
    </view>

    <view class="empty-tip" v-if="!loading && !videos.length">
      <text>暂无视频</text>
    </view>
  </view>
</template>

<script>
import videoApi from "@/utils/api/video";

const fallbackVideos = [
  {
    id: 1, title: "黑白猫的长头日常", desc: "黑白小咪在镜前放松伸展，适合慢慢看的一段暖窝视频。",
    cover: "/static/images/mock/cat-cover.jpg",
    url: "http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4",
    author: "暖窝小咪", likes: 2300, playCount: 12000, productId: 2
  },
  {
    id: 2, title: "猫咪下巴挠指时刻", desc: "室内猫咪被轻抚下巴，表情很放松。",
    cover: "/static/images/mock/ragdoll.jpg",
    url: "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4",
    author: "猫咪日记", likes: 5100, playCount: 35000, productId: 4
  },
  {
    id: 3, title: "猫咪小跑上楼梯", desc: "猫咪轻快地一路小跑上楼，动作灵活又可爱。",
    cover: "/static/images/mock/cat-avatar.jpg",
    url: "http://video.sonetto.online/Cat_trotting_up_stairs_202607022329.mp4",
    author: "暖窝小咪", likes: 1800, playCount: 8500, productId: 2
  },
  {
    id: 4, title: "柯基毯上乖坐", desc: "柯基坐在毯子上看镜头，短腿和圆臀都很治愈。",
    cover: "/static/images/mock/corgi.jpg",
    url: "http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4",
    author: "布偶田田", likes: 3600, playCount: 22000, productId: 3
  },
  {
    id: 5, title: "金毛叼着郁金香", desc: "金毛叼着花靠近镜前，温柔又有春天感。",
    cover: "/static/images/mock/golden.jpg",
    url: "http://video.sonetto.online/Golden_retriever_holding_tulip_video_202607022323.mp4",
    author: "暖窝小暴", likes: 980, playCount: 5000, productId: 1
  },
  {
    id: 6, title: "开心比格犬户外跑跳", desc: "比格犬在户外开心活动，适合喜曲活泼狗狗的用户。",
    cover: "/static/images/mock/dog-avatar.jpg",
    url: "http://video.sonetto.online/Happy_beagle_dog_outdoors_202607022337.mp4",
    author: "暖窝小暴", likes: 1200, playCount: 6800, productId: 14
  }
];

function formatCount(value) {
  const number = Number(value || 0);
  if (!Number.isFinite(number)) return value || "0";
  if (number >= 10000) return (number / 10000).toFixed(1).replace(/\.0$/, "") + "w";
  if (number >= 1000) return (number / 1000).toFixed(1).replace(/\.0$/, "") + "k";
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
  const layout = ["tile-tall", "tile-mid", "tile-short", "tile-tall", "tile-mid"][index % 5];
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
  const left = [];
  const right = [];
  videos.forEach((video, index) => {
    if (index % 2 === 0) left.push(video);
    else right.push(video);
  });
  return { left, right };
}

export default {
  data() {
    return {
      videos: [],
      leftVideos: [],
      rightVideos: [],
      loading: true,
      useMock: false
    };
  },
  onShow() {
    this.loadVideos();
  },
  onPullDownRefresh() {
    this.loadVideos(() => {
      uni.stopPullDownRefresh();
    });
  },
  methods: {
    loadVideos(done) {
      this.loading = true;
      videoApi.list(1, 20).then((res) => {
        const rows = extractRows(res);
        if (rows.length) {
          this.setVideoList(rows.map(normalizeVideo), false);
        } else {
          this.useFallback();
        }
        if (done) done();
      }).catch(() => {
        this.useFallback();
        if (done) done();
      });
    },
    setVideoList(videos, useMock) {
      const columns = splitColumns(videos);
      this.videos = videos;
      this.leftVideos = columns.left;
      this.rightVideos = columns.right;
      this.loading = false;
      this.useMock = !!useMock;
    },
    useFallback() {
      this.setVideoList(fallbackVideos.map(normalizeVideo), true);
    },
    goDetail(id) {
      if (!id) return;
      uni.navigateTo({ url: "/subpages/video/detail?id=" + id });
    },
    goProduct(id) {
      if (!id) return;
      uni.navigateTo({ url: "/subpages/detail/detail?id=" + id });
    }
  }
};
</script>

<style scoped>
.page{min-height:100vh;padding:34rpx 22rpx 44rpx;box-sizing:border-box;color:#201815;background:#f4efe7}
.header{padding:8rpx 4rpx 24rpx}
.eyebrow{display:block;color:#c45d4d;font-size:22rpx;font-weight:800;line-height:1.2}
.title{display:block;margin-top:10rpx;color:#173e35;font-size:44rpx;font-weight:800;line-height:1.18}
.subtitle{display:block;margin-top:10rpx;color:#6d5e54;font-size:24rpx;line-height:1.45}
.mock-tip{margin:0 4rpx 18rpx;padding:13rpx 18rpx;border-radius:12rpx;color:#8b5a4d;background:rgba(196,93,77,.1);font-size:23rpx}
.loading{display:flex;gap:18rpx}
.loading-cover{flex:1;height:520rpx;border-radius:16rpx;background:#e6ded4}
.loading-cover.small{height:420rpx}
.masonry{display:flex;align-items:flex-start;gap:18rpx}
.column{flex:1;min-width:0}
.video-tile{position:relative;overflow:hidden;margin-bottom:18rpx;border-radius:16rpx;background:#211916;transform:translateZ(0);transition:transform 120ms ease,opacity 120ms ease}
.tile-press{opacity:.9;transform:scale(.985)}
.cover{width:100%;height:100%;display:block;background:#d9d0c5}
.tile-short{height:330rpx}.tile-mid{height:420rpx}.tile-tall{height:510rpx}
.cover-shade{position:absolute;left:0;right:0;top:0;bottom:0;background:linear-gradient(180deg,rgba(0,0,0,.03) 24%,rgba(0,0,0,.72) 100%)}
.play-mark{position:absolute;top:16rpx;right:16rpx;width:50rpx;height:50rpx;border-radius:50%;display:flex;align-items:center;justify-content:center;padding-left:4rpx;box-sizing:border-box;background:rgba(0,0,0,.48);color:#fff;font-size:20rpx;font-weight:800}
.tile-copy{position:absolute;left:16rpx;right:16rpx;bottom:54rpx}
.video-title{display:block;color:#fff;font-size:27rpx;font-weight:800;line-height:1.32}
.video-desc{display:-webkit-box;margin-top:7rpx;color:rgba(255,255,255,.78);font-size:22rpx;line-height:1.34;-webkit-box-orient:vertical;-webkit-line-clamp:2;overflow:hidden}
.meta{display:flex;align-items:center;justify-content:space-between;gap:10rpx;margin-top:12rpx;color:rgba(255,255,255,.72);font-size:21rpx}
.author{min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.count{flex-shrink:0}
.product-link{position:absolute;left:14rpx;bottom:14rpx;max-width:150rpx;height:34rpx;line-height:34rpx;padding:0 12rpx;border-radius:999rpx;background:rgba(255,255,255,.9);color:#173e35;font-size:20rpx;font-weight:800;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
.empty-tip{margin-top:90rpx;text-align:center;color:#8f8176;font-size:25rpx}
</style>
