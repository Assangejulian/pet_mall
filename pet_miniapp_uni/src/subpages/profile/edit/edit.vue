<template>
  <view class="page">
    <view class="form">
      <view class="field">
        <text class="label">头像</text>
        <view class="avatar-row" @tap="chooseAvatar">
          <image v-if="avatar" class="avatar" :src="avatar" mode="aspectFill" />
          <view v-else class="avatar placeholder">{{(realName||username||'?')[0]}}</view>
          <text class="hint">点击更换</text>
        </view>
      </view>
      <view class="field">
        <text class="label">用户名</text>
        <input class="input" :value="username" disabled placeholder="用户名不可修改" />
      </view>
      <view class="field">
        <text class="label">真实姓名</text>
        <input class="input" :value="realName" placeholder="请输入真实姓名" @input="onInput('realName', $event)" />
      </view>
      <view class="field">
        <text class="label">手机号</text>
        <input class="input" :value="phone" type="number" maxlength="11" placeholder="请输入手机号" @input="onInput('phone', $event)" />
      </view>
      <view class="field">
        <text class="label">邮箱</text>
        <input class="input" :value="email" placeholder="请输入邮箱" @input="onInput('email', $event)" />
      </view>
      <view class="field">
        <text class="label">生日</text>
        <picker mode="date" :value="birthday" @change="onBirthdayChange">
          <view class="input picker-value">{{birthday || '请选择生日'}}</view>
        </picker>
      </view>
    </view>
    <view class="actions">
      <button class="btn-save" @tap="saveProfile" :loading="saving">保存</button>
      <button class="btn-cancel" @tap="goBack">取消</button>
    </view>
  </view>
</template>

<script>
const app = getApp();
import userApi from "../../../utils/api/user";
export default {
  data() {
    return {
      avatar: "", username: "", realName: "", phone: "", email: "", birthday: "",
      saving: false
    };
  },
  onShow() {
    if (!app.globalData.token) {
      uni.showToast({ title: "请先登录", icon: "none" });
      return uni.navigateTo({ url: "/subpages/login/login" });
    }
    this.loadProfile();
  },
  methods: {
    loadProfile() {
      uni.showLoading({ title: "加载中..." });
      userApi.getProfile().then(data => {
        uni.hideLoading();
        this.avatar = data.avatar || "";
        this.username = data.username || "";
        this.realName = data.realName || "";
        this.phone = data.phone || "";
        this.email = data.email || "";
        this.birthday = data.birthday || "";
      }).catch(err => {
        uni.hideLoading();
        uni.showToast({ title: err.message || "加载失败", icon: "none" });
      });
    },
    onInput(field, e) {
      this[field] = e.detail.value;
    },
    onBirthdayChange(e) {
      this.birthday = e.detail.value;
    },
    chooseAvatar() {
      const t = this;
      uni.chooseImage({
        count: 1,
        sizeType: ["compressed"],
        sourceType: ["album", "camera"],
        success(res) {
          const tempPath = res.tempFilePaths[0];
          uni.showLoading({ title: "上传中..." });
          uni.uploadFile({
            url: (app.globalData.baseUrl || "http://localhost:8080") + "/api/upload",
            filePath: tempPath,
            name: "file",
            header: { Authorization: "Bearer " + app.globalData.token },
            success(upRes) {
              uni.hideLoading();
              const body = JSON.parse(upRes.data);
              if (body.code === 200) {
                t.avatar = body.data && (body.data.url || body.data);
              } else {
                uni.showToast({ title: body.message || "上传失败", icon: "none" });
              }
            },
            fail() {
              uni.hideLoading();
              uni.showToast({ title: "上传失败", icon: "none" });
            }
          });
        }
      });
    },
    saveProfile() {
      const data = {
        realName: this.realName,
        phone: this.phone,
        email: this.email,
        birthday: this.birthday,
        avatar: this.avatar
      };
      this.saving = true;
      userApi.updateProfile(data).then(() => {
        this.saving = false;
        uni.showToast({ title: "保存成功", icon: "success" });
        setTimeout(() => uni.navigateBack(), 1200);
      }).catch(err => {
        this.saving = false;
        uni.showToast({ title: err.message || "保存失败", icon: "none" });
      });
    },
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped>
.page{min-height:100vh;padding-bottom:40rpx;background:#f5f5f5}
.form{background:#fff;margin:24rpx 28rpx;border-radius:20rpx;overflow:hidden}
.field{display:flex;align-items:center;padding:28rpx 28rpx;border-bottom:1rpx solid rgba(140,104,83,.08)}
.field:last-child{border-bottom:none}
.label{width:140rpx;font-size:26rpx;color:#2d1f18;font-weight:600;flex-shrink:0}
.input{flex:1;font-size:26rpx;color:#333;padding:8rpx 0;text-align:right}
.input[disabled]{color:#999}
.picker-value{text-align:right;color:#333}
.avatar-row{flex:1;display:flex;align-items:center;justify-content:flex-end;gap:16rpx}
.avatar{width:80rpx;height:80rpx;border-radius:50%;overflow:hidden}
.avatar.placeholder{background:rgba(232,146,124,.25);color:#e8927c;font-size:28rpx;font-weight:800;display:flex;align-items:center;justify-content:center}
.hint{font-size:22rpx;color:#999}
.actions{padding:40rpx 28rpx}
.btn-save{width:100%;height:88rpx;line-height:88rpx;background:#e8927c;color:#fff;font-size:30rpx;font-weight:700;border-radius:16rpx;border:none;text-align:center;margin-bottom:20rpx}
.btn-save::after{border:none}
.btn-save:active{opacity:0.82}
.btn-cancel{width:100%;height:88rpx;line-height:88rpx;background:#fff;color:#8f7366;font-size:30rpx;font-weight:600;border-radius:16rpx;border:1rpx solid rgba(140,104,83,.2);text-align:center}
.btn-cancel::after{border:none}
.btn-cancel:active{opacity:0.7}
</style>
