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

function loadIndexPage(videoApi) {
  let config;
  vm.runInNewContext(
    fs.readFileSync(path.join(root, "pages", "index", "index.js"), "utf8"),
    {
      require: function(id) {
        if (id === "../../utils/api/video") return videoApi;
        throw new Error("Unexpected require: " + id);
      },
      wx: {
        stopPullDownRefresh: function() {},
        navigateTo: function() {},
        showToast: function() {}
      },
      Page: function(pageConfig) { config = pageConfig; },
      console: console,
      Promise: Promise,
      Number: Number,
      String: String,
      Array: Array
    },
    { filename: "pages/index/index.js" }
  );

  assert.ok(config, "index Page config should be registered");
  return config;
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
  await new Promise(resolve => page.loadVideos(resolve));
}

async function main() {
  const okRequest = loadRequestWithWx({
    request: function(options) {
      options.success({
        statusCode: 200,
        data: { code: 200, data: { records: [{ id: 1 }] } }
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
  await assert.rejects(businessRequest.get("/api/video/feed"), function(err) {
    assert.strictEqual(err.isBusinessError, true);
    assert.strictEqual(err.code, 500);
    assert.strictEqual(err.message, "bad video");
    return true;
  });

  const networkRequest = loadRequestWithWx({
    request: function(options) {
      options.fail({ errMsg: "connect failed" });
    }
  });
  await assert.rejects(networkRequest.get("/api/video/feed"), function(err) {
    assert.strictEqual(err.isNetworkError, true);
    return true;
  });

  let nextResult = Promise.resolve();
  const config = loadIndexPage({
    list: function() { return nextResult; }
  });

  nextResult = Promise.resolve({
    records: [{ id: 88, title: "real", description: "desc", cover: "/c.jpg", url: "/v.mp4" }]
  });
  const successPage = makePage(config);
  await runLoad(successPage);
  assert.strictEqual(successPage.data.useMock, false);
  assert.strictEqual(successPage.data.videos.length, 1);
  assert.strictEqual(successPage.data.leftVideos.length, 1);
  assert.strictEqual(successPage.data.rightVideos.length, 0);

  nextResult = Promise.resolve({ records: [] });
  const emptyPage = makePage(config);
  await runLoad(emptyPage);
  assert.strictEqual(emptyPage.data.useMock, true);
  assert.ok(emptyPage.data.videos.length > 0);

  nextResult = Promise.reject(Object.assign(new Error("video db failed"), { isBusinessError: true, code: 500 }));
  const businessPage = makePage(config);
  await runLoad(businessPage);
  assert.strictEqual(businessPage.data.useMock, true);
  assert.ok(businessPage.data.videos.length > 0);

  nextResult = Promise.reject(Object.assign(new Error("connect failed"), { isNetworkError: true }));
  const networkPage = makePage(config);
  await runLoad(networkPage);
  assert.strictEqual(networkPage.data.useMock, true);
  assert.ok(networkPage.data.videos.length > 0);

  console.log("video feed miniapp validation passed");
}

main().catch(function(err) {
  console.error(err);
  process.exit(1);
});
