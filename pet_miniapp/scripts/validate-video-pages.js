const assert = require("assert");
const fs = require("fs");
const path = require("path");
const vm = require("vm");

const root = path.resolve(__dirname, "..");

function loadPage(file, videoApi, wxOverrides) {
  let config;
  const wxCalls = [];
  const wx = Object.assign({
    navigateTo: function(options) { wxCalls.push(["navigateTo", options.url]); },
    switchTab: function(options) { wxCalls.push(["switchTab", options.url]); },
    navigateBack: function() { wxCalls.push(["navigateBack"]); },
    showToast: function(options) { wxCalls.push(["showToast", options.title]); },
    stopPullDownRefresh: function() {},
    createVideoContext: function(id) {
      wxCalls.push(["createVideoContext", id]);
      return {
        play: function() { wxCalls.push(["play", id]); }
      };
    }
  }, wxOverrides || {});

  vm.runInNewContext(
    fs.readFileSync(path.join(root, file), "utf8"),
    {
      require: function(id) {
        if (id === "../../utils/api/video") return videoApi;
        throw new Error("Unexpected require: " + id);
      },
      getCurrentPages: function() { return [{ route: "subpages/video/list" }, { route: file }]; },
      wx: wx,
      Page: function(pageConfig) { config = pageConfig; },
      console: console,
      Promise: Promise,
      Date: Date,
      setTimeout: setTimeout,
      Number: Number,
      String: String,
      Array: Array
    },
    { filename: file }
  );

  assert.ok(config, file + " should register Page");
  return { config, wxCalls };
}

function makePage(config) {
  const page = Object.assign({}, config);
  page.data = JSON.parse(JSON.stringify(config.data));
  page.setData = function(update) {
    this.data = Object.assign({}, this.data, update);
  };
  return page;
}

function tick() {
  return new Promise(resolve => setTimeout(resolve, 0));
}

async function validateListPage() {
  const api = {
    list: function() {
      return Promise.resolve({
        records: [
          { id: 1, title: "one", description: "desc", cover: "/images/mock/cat-cover.jpg", url: "http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4", likes: 10, playCount: 20, productId: 2 },
          { id: 2, title: "two", description: "desc", cover: "/images/mock/ragdoll.jpg", url: "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4", likes: 11, playCount: 21, productId: 4 },
          { id: 3, title: "three", description: "desc", cover: "/images/mock/corgi.jpg", url: "http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4", likes: 12, playCount: 22, productId: 3 }
        ]
      });
    }
  };
  const loaded = loadPage("subpages/video/list.js", api);
  const page = makePage(loaded.config);
  await new Promise(resolve => page.loadVideos(resolve));
  assert.strictEqual(page.data.useMock, false);
  assert.strictEqual(page.data.videos.length, 3);
  assert.strictEqual(page.data.leftVideos.length, 2);
  assert.strictEqual(page.data.rightVideos.length, 1);
  assert.ok(page.data.leftVideos[0].tileClass.indexOf("tile-") === 0);

  page.goDetail({ currentTarget: { dataset: { id: 2 } } });
  page.goProduct({ currentTarget: { dataset: { pid: 4 } } });
  assert.deepStrictEqual(loaded.wxCalls.slice(-2), [
    ["navigateTo", "/subpages/video/detail?id=2"],
    ["navigateTo", "/subpages/detail/detail?id=4"]
  ]);
}

async function validateDetailPage() {
  const rows = [
    { id: 1, title: "one", description: "desc 1", cover: "/images/mock/cat-cover.jpg", url: "http://video.sonetto.online/Black-and-white_cat_video_202607022313.mp4", likes: 10, commentCount: 1, productId: 2 },
    { id: 2, title: "two", description: "desc 2", cover: "/images/mock/ragdoll.jpg", url: "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4", likes: 23, commentCount: 2, productId: 4 },
    { id: 3, title: "three", description: "desc 3", cover: "/images/mock/corgi.jpg", url: "http://video.sonetto.online/Corgi_sitting_on_blanket_202607022304.mp4", likes: 12, commentCount: 3, productId: 3 }
  ];
  let liked = false;
  let commentText = "";
  let commentsFor = null;
  const api = {
    list: function() { return Promise.resolve({ records: rows }); },
    comments: function(id) {
      commentsFor = id;
      return Promise.resolve([{ id: 7, user: "tester", content: "hello", avatar: "/images/mock/cat-avatar.jpg" }]);
    },
    like: function() { liked = true; return Promise.resolve(); },
    addComment: function(id, text) { commentText = text; return Promise.resolve(); }
  };
  const loaded = loadPage("subpages/video/detail.js", api);
  const page = makePage(loaded.config);
  page.onLoad({ id: 2 });
  await tick();
  await tick();

  assert.strictEqual(page.data.loading, false);
  assert.strictEqual(page.data.posts.length, 3);
  assert.strictEqual(page.data.currentIndex, 1);
  assert.strictEqual(page.data.post.url, "http://video.sonetto.online/Cat_chin_scratch_indoor_video_202607022322.mp4");
  assert.strictEqual(page.data.product.name, "布偶猫");
  assert.strictEqual(page.data.comments.length, 1);
  assert.strictEqual(commentsFor, 2);

  await new Promise(resolve => setTimeout(resolve, 150));
  assert.ok(loaded.wxCalls.some(call => call[0] === "play" && call[1] === "mainVideo"));

  page.onSwiperChange({ detail: { current: 2 } });
  await tick();
  assert.strictEqual(page.data.currentIndex, 2);
  assert.strictEqual(page.data.post.id, 3);
  assert.strictEqual(page.data.isLast, true);
  assert.strictEqual(commentsFor, 3);

  page.toggleLike();
  assert.strictEqual(page.data.liked, true);
  assert.strictEqual(liked, true);

  page.toggleComments();
  assert.strictEqual(page.data.showComments, true);
  page.onInput({ detail: { value: "new comment" } });
  page.sendComment();
  assert.strictEqual(commentText, "new comment");
  assert.strictEqual(page.data.comments[0].text, "new comment");

  page.goChat();
  page.goProduct();
  assert.deepStrictEqual(loaded.wxCalls.slice(-2), [
    ["switchTab", "/pages/chat/chat"],
    ["navigateTo", "/subpages/detail/detail?id=3"]
  ]);

  page.onTouchStart({ touches: [{ clientY: 300 }] });
  page.onTouchEnd({ changedTouches: [{ clientY: 100 }] });
  assert.deepStrictEqual(loaded.wxCalls[loaded.wxCalls.length - 1], ["showToast", "已经是最后一个视频"]);

  page.activatePost(0);
  page.onTouchStart({ touches: [{ clientY: 100 }] });
  page.onTouchEnd({ changedTouches: [{ clientY: 230 }] });
  assert.deepStrictEqual(loaded.wxCalls[loaded.wxCalls.length - 1], ["showToast", "已经是第一个视频"]);
}

async function main() {
  await validateListPage();
  await validateDetailPage();
  console.log("video subpage validation passed");
}

main().catch(function(err) {
  console.error(err);
  process.exit(1);
});
