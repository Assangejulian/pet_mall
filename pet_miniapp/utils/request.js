function getBase() {
  var app = getApp();
  var d = app && app.globalData;
  return { baseUrl: (d && d.baseUrl) || "http://localhost:8080", token: (d && d.token) || "" };
}

function request(method, url, data) {
  return new Promise(function(resolve, reject) {
    var cfg = getBase();
    var header = { "Content-Type": "application/json" };
    if (cfg.token) header["Authorization"] = "Bearer " + cfg.token;
    wx.request({
      url: cfg.baseUrl + url,
      method: method,
      data: data,
      header: header,
      timeout: 10000,
      success: function(res) {
        var body = res.data;
        if (res.statusCode === 401) {
          wx.showToast({ title: "登录已过期，请重新登录", icon: "none" });
          var app = getApp();
          if (app) { app.globalData.token = ""; }
          wx.removeStorageSync("token");
          reject(new Error("unauthorized"));
          return;
        }
        if (body && body.code === 200) {
          resolve(body.data);
        } else {
          var msg = (body && body.message) || "请求失败";
          wx.showToast({ title: msg, icon: "none" });
          reject(new Error(msg));
        }
      },
      fail: function(err) { reject(err); }
    });
  });
}

module.exports = {
  get: function(url, params) {
    if (params) {
      var qs = [];
      for (var k in params) {
        if (params[k] !== undefined && params[k] !== null && params[k] !== "") {
          qs.push(k + "=" + encodeURIComponent(params[k]));
        }
      }
      if (qs.length) url = url + "?" + qs.join("&");
    }
    return request("GET", url);
  },
  post: function(url, data) { return request("POST", url, data); },
  put: function(url, data) { return request("PUT", url, data); },
  del: function(url) { return request("DELETE", url); }
};