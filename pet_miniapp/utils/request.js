var app = getApp();

function getBaseUrl() {
  return (app && app.globalData && app.globalData.baseUrl) || "http://localhost:8080";
}

function getToken() {
  return (app && app.globalData && app.globalData.token) || "";
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

/** 请求/响应拦截器 — 参考 B端 Axios 模式 */
function request(method, url, data) {
  return new Promise(function(resolve, reject) {
    // Request interceptor: attach token
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
          if (app) {
            app.globalData.token = "";
            app.globalData.user = null;
          }
          wx.removeStorageSync("token");
          wx.removeStorageSync("userId");
          wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
          // 跳转到登录页（参考 B端 location.hash = "#/login"）
          wx.navigateTo({ url: "/subpages/login/login" });
          reject(new Error("unauthorized"));
          return;
        }

        // 403: 无权限
        if (res.statusCode === 403) {
          wx.showToast({ title: body?.message || "无权限访问", icon: "none" });
          reject(new Error(body?.message || "forbidden"));
          return;
        }

        // 成功: 检查业务状态码 (参考 B端 unwrap: body.code !== 200 → throw)
        if (body && body.code === 200) {
          resolve(body.data);
        } else {
          var msg = (body && body.message) || "请求失败";
          reject(new Error(msg));
        }
      },
      fail: function(err) {
        // 网络错误
        wx.showToast({ title: "网络异常，请检查连接", icon: "none" });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: function(url, params) {
    return request("GET", buildQuery(url, params));
  },
  post: function(url, data) { return request("POST", url, data); },
  put: function(url, data) { return request("PUT", url, data); },
  del: function(url) { return request("DELETE", url); }
};
