<template>
<camera device-position="front" class="camera" flash="off" @error="onError" style="width:100%;height:100%;">
<cover-view class="tips"><cover-view class="tips-text">请将脸部对准摄像头</cover-view><cover-view class="tips-sub">自动识别中...</cover-view></cover-view>
<cover-view class="progress-ring"><cover-view class="progress-text">{{countdown}}</cover-view></cover-view>
<cover-view class="btn-close" @tap="onClose">取消</cover-view>
</camera>
</template>
<script>
import authApi from "@/utils/api/auth";
export default {
data() { return { countdown: 3, ctx: null, timer: null, _capturing: false }; },
onReady() { this.ctx = uni.createCameraContext(); this.startCountdown(); },
methods: {
startCountdown() { this.countdown = 3; this.timer = setInterval(() => { this.countdown--; if (this.countdown <= 0) { clearInterval(this.timer); this.captureAndLogin(); } }, 1000); },
captureAndLogin() { if (this._capturing) return; this._capturing = true; const that = this; this.ctx.takePhoto({ quality: "low", success(res) { uni.getFileSystemManager().readFile({ filePath: res.tempImagePath, encoding: "base64", success(fsRes) { authApi.login({ authType: "face", faceToken: fsRes.data }).then(r => { uni.navigateBack(); setTimeout(() => { const pages = getCurrentPages(); const prev = pages[pages.length-1]; if (prev && prev.doLogin) prev.doLogin(r); }, 300); }).catch(err => { uni.showModal({title:"识别失败",content:err.message||"人脸识别失败",confirmText:"重试",success(r2) { if(r2.confirm) { that._capturing = false; that.startCountdown(); } else uni.navigateBack(); }}); }); }, fail() { uni.showToast({title:"图片读取失败",icon:"none"}); setTimeout(()=>uni.navigateBack(),1500); } }); }, fail() { uni.showToast({title:"拍照失败",icon:"none"}); setTimeout(()=>uni.navigateBack(),1500); } }); },
onClose() { if (this.timer) clearInterval(this.timer); uni.navigateBack(); },
onError(e) { uni.showToast({title:"摄像头启动失败 "+e.detail.errMsg,icon:"none"}); setTimeout(()=>uni.navigateBack(),2000); }
},
onUnload() { if (this.timer) clearInterval(this.timer); }
}
</script>
<style scoped>
page{height:100%;overflow:hidden}.camera{position:relative;width:100%;height:100vh}
.tips{position:absolute;top:80rpx;left:0;right:0;text-align:center}
.tips-text{color:#fff;font-size:36rpx;font-weight:600;text-shadow:0 2rpx 8rpx rgba(0,0,0,.5)}
.tips-sub{color:rgba(255,255,255,.8);font-size:28rpx;margin-top:16rpx}
.progress-ring{position:absolute;bottom:160rpx;left:50%;transform:translateX(-50%);width:120rpx;height:120rpx;border:6rpx solid rgba(255,255,255,.6);border-radius:50%;display:flex;align-items:center;justify-content:center}
.progress-text{color:#fff;font-size:48rpx;font-weight:bold}
.btn-close{position:absolute;top:100rpx;right:40rpx;color:#fff;font-size:28rpx;padding:16rpx 32rpx;border:2rpx solid rgba(255,255,255,.6);border-radius:40rpx;background:rgba(0,0,0,.3)}
</style>
