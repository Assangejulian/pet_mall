<template>
  <view></view>
</template>

﻿<script>
export default {
  globalData: {
    baseUrl: "http://127.0.0.1:8080",
    user: null,
    token: ""
  },
  onLaunch() {
    const token = uni.getStorageSync("token") || "";
    this.globalData.token = token;
  },
  methods: {
    requireAuth() {
      if (this.globalData.token) return true;
      uni.navigateTo({ url: "/subpages/login/login" });
      return false;
    }
  }
}
</script>
<style>
@import "./uni.scss";
</style>
