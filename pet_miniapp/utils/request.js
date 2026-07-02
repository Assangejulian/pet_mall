var app = getApp();

function getBaseUrl() {
  return (app && app.globalData && app.globalData.baseUrl) || "http://localhost:8080";
}

function getToken() {
  var t = (app && app.globalData && app.globalData.token) || "";
  if (!t) t = wx.getStorageSync("token") || "";
  return t;
}

function buildQuery(url, params) {
  if (!params) return url;
  var qs = [];
  for (var k in params) {
    var v = params[k];
    if (v !== undefined && v !== null && v !== "") {
      qs.push(encodeURIComponent(k) + "=" + encodeURIComponent(v));
    }
  }
  return qs.length ? url + "?" + qs.join("&") : url;
}

/**
 * 小程序请求/响应拦截器 — 参考 B端 Axios 模式
 * 统一处理 token 注入、401 跳转登录、业务状态码检查
 */
function request(method, url, data) {
  return new Promise(function(resolve, reject) {
    // Request interceptor: 自动注入 token
    var token = getToken();
    var header = { "Content-Type": "application/json" };
    if (token) header["Authorization"] = "Bearer " + token;

    wx.request({
      url: getBaseUrl() + url,
      method: method,
      data: data,
      header: header,
      timeout: 10000,
      success: function(res) {
        var body = res.data;

        // Response interceptor: 401 → 清除 token → 跳转登录页
        if (res.statusCode === 401) {
          clearAuth();
          wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
          redirectToLogin();
          reject(new Error("unauthorized"));
          return;
        }

        // 403: 无权限
        if (res.statusCode === 403) {
          wx.showToast({ title: body && body.message || "无权限访问", icon: "none" });
          reject(new Error(body && body.message || "forbidden"));
          return;
        }

        // 成功: 检查业务状态码 (参考 B端 unwrap: body.code !== 200 → throw)
        if (body && (body.code === 200 || body.code === undefined)) {
          resolve(body.code === 200 ? body.data : body);
        } else {
          var msg = (body && body.message) || "请求失败";
          var error = new Error(msg);
          error.isBusinessError = true;
          error.code = body && body.code;
          reject(error);
        }
      },
      fail: function(err) {
        // 网络错误
        err = err || {};
        err.isNetworkError = true;
        wx.showToast({ title: "网络异常，请检查连接", icon: "none" });
        reject(err);
      }
    });
  });
}

function clearAuth() {
  if (app) {
    app.globalData.token = "";
    app.globalData.user = null;
  }
  wx.removeStorageSync("token");
  wx.removeStorageSync("userId");
}

function redirectToLogin() {
  var pages = getCurrentPages();
  var isOnLogin = false;
  for (var i = 0; i < pages.length; i++) {
    if (pages[i].route && pages[i].route.indexOf("login") !== -1) {
      isOnLogin = true;
      break;
    }
  }
  if (!isOnLogin) {
    wx.navigateTo({ url: "/subpages/login/login" });
  }
}

module.exports = {
  get: function(url, params) {
    return request("GET", buildQuery(url, params));
  },
  post: function(url, data) { return request("POST", url, data); },
  put: function(url, data) { return request("PUT", url, data); },
  del: function(url) { return request("DELETE", url); }
};
