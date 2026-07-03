<template>
  <view class="page">
    <view class="section-title">{{selecting ? "选择收货地址" : "我的收货地址"}}</view>
    <view class="card" v-for="(item, index) in list" :key="item.id" @tap="selectAddress" :data-index="index">
      <view class="at"><text class="an">{{item.receiverName}}</text><text class="ap">{{item.phone}}</text><text class="adef" v-if="item.defaulted">默认</text></view>
      <text class="aa">{{item.province}}{{item.city}}{{item.district}} {{item.detail}}</text>
      <view class="a-actions" v-if="!selecting">
        <text class="a-btn" @tap.stop="setDefault" :data-id="item.id">设为默认</text>
        <text class="a-btn del" @tap.stop="delAddr" :data-id="item.id">删除</text>
      </view>
    </view>
    <view class="empty-tip" v-if="!list.length && !loading"><text>还没有收货地址，请在下�添加</text></view>
    <view class="form-card">
      <view class="form-title">添加新地址</view>
      <view class="form-row"><text class="label">收件人</text><input class="input" placeholder="请输入收件人姓名" :value="form.receiverName" @input="onInput" data-field="receiverName" /></view>
      <view class="form-row"><text class="label">手机号</text><input class="input" placeholder="请输入手机号" type="number" :value="form.phone" @input="onInput" data-field="phone" /></view>
      <view class="form-row"><text class="label">省份</text><input class="input" placeholder="如：广东省" :value="form.province" @input="onInput" data-field="province" /></view>
      <view class="form-row"><text class="label">城市</text><input class="input" placeholder="如：深圳市" :value="form.city" @input="onInput" data-field="city" /></view>
      <view class="form-row"><text class="label">区县</text><input class="input" placeholder="如：南山区" :value="form.district" @input="onInput" data-field="district" /></view>
      <view class="form-row"><text class="label">详细地址</text><input class="input" placeholder="如：科技园路1号" :value="form.detail" @input="onInput" data-field="detail" /></view>
      <view class="form-row default-row"><text class="label">设为默认</text><switch :checked="form.defaulted" @change="onDefaultChange" color="#e8927c" /></view>
      <view class="btn-add" @tap="addAddr">保存地址</view>
    </view>
  </view>
</template>
<script>
import userApi from "@/utils/api/user";
const EMPTY_FORM = { receiverName: "", phone: "", province: "", city: "", district: "", detail: "", defaulted: 0 };
export default {
  data() { return { list: [], loading: true, selecting: false, form: { ...EMPTY_FORM } }; },
  onLoad(options) { if (options && options.select === "1") this.selecting = true; },
  onShow() { this.loadList(); },
  methods: {
    loadList() {
      this.loading = true;
      userApi.addressList().then(res => { this.list = Array.isArray(res) ? res : []; this.loading = false; }).catch(() => { this.loading = false; });
    },
    selectAddress(e) {
      if (!this.selecting) return;
      const addr = this.list[e.currentTarget.dataset.index]; if (!addr) return;
      const pages = getCurrentPages(); const prev = pages[pages.length - 2];
      if (prev && prev.onAddressSelected) prev.onAddressSelected(addr);
      uni.navigateBack();
    },
    onInput(e) { const field = e.currentTarget.dataset.field; this.form[field] = e.detail.value; this.form = { ...this.form }; },
    onDefaultChange(e) { this.form.defaulted = e.detail.value ? 1 : 0; },
    addAddr() {
      if (!this.form.receiverName || !this.form.phone || !this.form.detail) { uni.showToast({title:"请填写收件人、手机号和详细地址",icon:"none"}); return; }
      userApi.addAddress(this.form).then(() => { uni.showToast({title:"地址已保存",icon:"success"}); this.form = { ...EMPTY_FORM }; this.loadList(); }).catch(err => { uni.showToast({title:(err&&err.message)||"保存失败",icon:"none"}); });
    },
    setDefault(e) { userApi.updateAddress(e.currentTarget.dataset.id, { defaulted: 1 }).then(() => this.loadList()); },
    delAddr(e) { const id = e.currentTarget.dataset.id; uni.showModal({title:"删除地址",content:"确定删除此地址？",success:r => { if(r.confirm) userApi.delAddress(id).then(() => this.loadList()); }}); }
  }
}
</script>
<style scoped>
.page{padding:24rpx}.section-title{font-size:32rpx;font-weight:700;color:#3d2a1e;margin-bottom:20rpx}
.card{background:#fff;border-radius:16rpx;padding:24rpx;margin-bottom:20rpx;box-shadow:0 2rpx 12rpx rgba(0,0,0,.06)}
.at{display:flex;align-items:center;gap:16rpx;margin-bottom:8rpx}.an{font-size:30rpx;font-weight:600;color:#3d2a1e}
.ap{font-size:26rpx;color:#888}.adef{font-size:22rpx;color:#e8927c;border:1rpx solid #e8927c;border-radius:6rpx;padding:2rpx 8rpx}
.aa{font-size:26rpx;color:#555;line-height:1.6}.a-actions{display:flex;gap:24rpx;margin-top:12rpx;padding-top:12rpx;border-top:1rpx solid #f0ebe3}
.a-btn{font-size:26rpx;color:#e8927c}.a-btn.del{color:#c0392b}.empty-tip{text-align:center;padding:40rpx;color:#aaa;font-size:28rpx}
.form-card{background:#fff;border-radius:16rpx;padding:28rpx;box-shadow:0 2rpx 12rpx rgba(0,0,0,.06)}
.form-title{font-size:30rpx;font-weight:600;color:#3d2a1e;margin-bottom:20rpx}
.form-row{display:flex;align-items:center;padding:18rpx 0;border-bottom:1rpx solid #f5f0eb}.default-row{border-bottom:none}
.label{width:140rpx;font-size:28rpx;color:#666;flex-shrink:0}.input{flex:1;font-size:28rpx;color:#3d2a1e}
.btn-add{margin-top:28rpx;background:#e8927c;color:#fff;font-size:30rpx;font-weight:600;border-radius:50rpx;height:88rpx;display:flex;align-items:center;justify-content:center}
</style>
