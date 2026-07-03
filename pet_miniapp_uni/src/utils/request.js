const getApp = () => {
  const app = getApp();
  return app;
};

function getBaseUrl() {
  const app = getApp();
  return (app && app.globalData && app.globalData.baseUrl) || "http://localhost:8080";
}

function getToken() {
  const app = getApp();
  let t = (app && app.globalData && app.globalData.token) || "";
  if (!t) t = uni.getStorageSync("token") || "";
  return t;
}

function buildQuery(url, params) {
  if (!params) return url;
  const qs = [];
  for (const k in params) {
    const v = params[k];
    if (v !== undefined && v !== null && v !== "") {
      qs.push(encodeURIComponent(k) + "=" + encodeURIComponent(v));
    }
  }
  return qs.length ? url + "?" + qs.join("&") : url;
}

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    const token = getToken();
    const header = { "Content-Type": "application/json" };
    if (token) header["Authorization"] = "Bearer " + token;

    uni.request({
      url: getBaseUrl() + url,
      method,
      data,
      header,
      timeout: 10000,
      success(res) {
        const body = res.data;
        if (res.statusCode === 401) {
          clearAuth();
          uni.showToast({ title: "登录已过期，请重新登录", icon: "none" });
          redirectToLogin();
          reject(new Error("unauthorized"));
          return;
        }
        if (res.statusCode === 403) {
          uni.showToast({ title: (body && body.message) || "无权限访问", icon: "none" });
          reject(new Error((body && body.message) || "forbidden"));
          return;
        }
        if (body && (body.code === 200 || body.code === undefined)) {
          resolve(body.code === 200 ? body.data : body);
        } else {
          const msg = (body && body.message) || "请求失败";
          const err = new Error(msg);
          err.isBusinessError = true;
          err.code = body && body.code;
          reject(err);
        }
      },
      fail(err) {
        err = err || {};
        err.isNetworkError = true;
        uni.showToast({ title: "网络异常，请检查连接", icon: "none" });
        reject(err);
      }
    });
  });
}

function clearAuth() {
  const app = getApp();
  if (app) {
    app.globalData.token = "";
    app.globalData.user = null;
  }
  uni.removeStorageSync("token");
  uni.removeStorageSync("userId");
}

function redirectToLogin() {
  const pages = getCurrentPages();
  const isOnLogin = pages.some(p => p.route && p.route.indexOf("login") !== -1);
  if (!isOnLogin) {
    uni.navigateTo({ url: "/subpages/login/login" });
  }
}

export default {
  get(url, params) { return request("GET", buildQuery(url, params)); },
  post(url, data) { return request("POST", url, data); },
  put(url, data) { return request("PUT", url, data); },
  del(url) { return request("DELETE", url); }
};
