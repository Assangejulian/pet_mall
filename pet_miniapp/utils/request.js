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

function request(method, url, data) {
  return new Promise(function(resolve, reject) {
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
        if (res.statusCode === 401) {
          wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
          if (app) { app.globalData.token = ""; }
          wx.removeStorageSync("token");
          reject(new Error("unauthorized"));
          return;
        }
        if (body && body.code === 200) {
          resolve(body.data);
        } else {
          var msg = (body && body.message) || "请求失败";
          reject(new Error(msg));
        }
      },
      fail: function(err) {
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