const assert = require("assert");
const fs = require("fs");
const path = require("path");
const vm = require("vm");

const root = path.resolve(__dirname, "..");

function loadRequestWithWx(wxOverrides) {
  const module = { exports: {} };
  const wx = Object.assign({
    getStorageSync: function() { return ""; },
    removeStorageSync: function() {},
    showToast: function() {},
    navigateTo: function() {},
    request: function() {}
  }, wxOverrides || {});

  vm.runInNewContext(
    fs.readFileSync(path.join(root, "utils", "request.js"), "utf8"),
    {
      getApp: function() {
        return { globalData: { baseUrl: "http://127.0.0.1:8080", token: "" } };
      },
      getCurrentPages: function() { return []; },
      wx: wx,
      module: module,
      exports: module.exports,
      Error: Error,
      Promise: Promise
    },
    { filename: "utils/request.js" }
  );

  return module.exports;
}

function loadIndexPage(videoApi, toasts) {
  let config;
  const context = {
    getApp: function() { return { globalData: {} }; },
    require: function(id) {
      if (id === "../../utils/api/video") {
        return videoApi;
      }
      throw new Error("Unexpected require: " + id);
    },
    wx: {
      showToast: function(options) { toasts.push(options.title); },
      stopPullDownRefresh: function() {},
      switchTab: function() {},
      navigateTo: function() {}
    },
    Page: function(pageConfig) { config = pageConfig; },
    console: console
  };

  vm.runInNewContext(
    fs.readFileSync(path.join(root, "pages", "index", "index.js"), "utf8"),
    context,
    { filename: "pages/index/index.js" }
  );

  assert.ok(config, "index Page config should be registered");
  assert.strictEqual(typeof context.extractRows, "function", "extractRows should be available");
  assert.strictEqual(typeof context.normalizeVideo, "function", "normalizeVideo should be available");
  return { config: config, helpers: context };
}

function makePage(config) {
  const page = Object.assign({}, config);
  page.data = JSON.parse(JSON.stringify(config.data));
  page.setData = function(update) {
    this.data = Object.assign({}, this.data, update);
  };
  return page;
}

async function runLoad(page) {
  await new Promise(function(resolve) {
    page.load(resolve);
  });
}

async function main() {
  const okRequest = loadRequestWithWx({
    request: function(options) {
      options.success({
        statusCode: 200,
        data: { code: 200, message: "success", data: { records: [{ id: 1 }] } }
      });
    }
  });
  const okData = await okRequest.get("/api/video/feed");
  assert.deepStrictEqual(okData.records, [{ id: 1 }]);

  const businessRequest = loadRequestWithWx({
    request: function(options) {
      options.success({
        statusCode: 200,
        data: { code: 500, message: "bad video", data: null }
      });
    }
  });
  await assert.rejects(
    businessRequest.get("/api/video/feed"),
    function(err) {
      assert.strictEqual(err.isBusinessError, true);
      assert.strictEqual(err.code, 500);
      assert.strictEqual(err.message, "bad video");
      return true;
    }
  );

  const networkRequest = loadRequestWithWx({
    request: function(options) {
      options.fail({ errMsg: "connect failed" });
    }
  });
  await assert.rejects(
    networkRequest.get("/api/video/feed"),
    function(err) {
      assert.strictEqual(err.isNetworkError, true);
      return true;
    }
  );

  let nextResult = Promise.resolve();
  const videoApi = {
    list: function() {
      return nextResult;
    }
  };
  const toasts = [];
  const loaded = loadIndexPage(videoApi, toasts);

  assert.deepStrictEqual(loaded.helpers.extractRows({ records: [{ id: 2 }] }), [{ id: 2 }]);
  assert.deepStrictEqual(loaded.helpers.extractRows({ data: { records: [{ id: 3 }] } }), [{ id: 3 }]);
  assert.strictEqual(loaded.helpers.normalizeVideo({ id: 4, videoUrl: "/v.mp4", coverUrl: "/c.jpg" }, 0).url, "/v.mp4");

  nextResult = Promise.resolve({
    records: [{ id: 88, title: "real", description: "desc", cover: "/c.jpg", url: "/v.mp4" }]
  });
  const successPage = makePage(loaded.config);
  await runLoad(successPage);
  assert.strictEqual(successPage.data.useMock, false);
  assert.strictEqual(successPage.data.videos.length, 1);
  assert.strictEqual(successPage.data.videos[0].url, "/v.mp4");

  nextResult = Promise.resolve({ records: [] });
  const emptyPage = makePage(loaded.config);
  await runLoad(emptyPage);
  assert.strictEqual(emptyPage.data.useMock, false);
  assert.strictEqual(emptyPage.data.videos.length, 0);

  nextResult = Promise.reject(Object.assign(new Error("video db failed"), { isBusinessError: true, code: 500 }));
  const businessPage = makePage(loaded.config);
  await runLoad(businessPage);
  assert.strictEqual(businessPage.data.useMock, true);
  assert.ok(toasts[toasts.length - 1].includes("video db failed"));
  assert.ok(!toasts[toasts.length - 1].includes("无法连接后端"));

  nextResult = Promise.reject(Object.assign(new Error("connect failed"), { isNetworkError: true }));
  const networkPage = makePage(loaded.config);
  await runLoad(networkPage);
  assert.strictEqual(networkPage.data.useMock, true);
  assert.ok(toasts[toasts.length - 1].includes("无法连接后端"));

  console.log("video feed miniapp validation passed");
}

main().catch(function(err) {
  console.error(err);
  process.exit(1);
});
