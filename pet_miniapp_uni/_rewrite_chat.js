const fs = require('fs');
const path = require('path');
const base = 'D:\\厦理\\大四\\课程\\pet_miniapp_uni';

const template = `<template>
<view class="page">
  <!-- 聊天历史抽屉 -->
  <view class="history-mask" v-if="historyOpen" @tap="closeHistory"></view>
  <view class="history-drawer" :class="{ open: historyOpen }">
    <view class="drawer-head">
      <text>聊天历史</text>
      <view class="drawer-close" @tap="closeHistory">×</view>
    </view>
    <view class="drawer-new" @tap="clearChat">+ 新对话</view>
    <scroll-view scroll-y class="history-list">
      <view
        v-for="item in chatSessions"
        :key="item.id"
        class="history-item"
        :class="{ active: currentSessionKey === item.id }"
        @tap="selectSession"
        :data-id="item.id"
      >
        <view class="history-main">
          <text class="history-title">{{item.title}}</text>
          <text class="history-time">{{item.pinned ? '已置顶 · ' : ''}}{{item.displayTime}}</text>
        </view>
        <view class="history-actions">
          <view class="history-action" @tap.stop="togglePinSession" :data-id="item.id">{{item.pinned ? '取消' : '置顶'}}</view>
          <view class="history-action danger" @tap.stop="deleteSession" :data-id="item.id">删除</view>
        </view>
      </view>
      <view class="history-empty" v-if="!chatSessions.length">还没有聊天记录</view>
    </scroll-view>
  </view>

  <!-- Hero 区域 -->
  <view class="hero">
    <view class="history-btn" @tap="openHistory">☰</view>
    <view class="hero-dot">AI</view>
    <view class="hero-copy">
      <text class="eyebrow">PETNEST SERVICE</text>
      <text class="title">{{title}}</text>
      <text class="subtitle">{{subtitle}}</text>
    </view>
    <view class="status" :class="{ on: online }">
      <text class="status-dot"></text>
      <text>{{online ? '在线' : '离线'}}</text>
    </view>
  </view>

  <!-- 模型切换 -->
  <view class="control-row">
    <view class="model-switch">
      <view
        v-for="item in modelOptions"
        :key="item.id"
        class="model-item"
        :class="{ active: modelMode === item.id }"
        @tap="switchModel"
        :data-mode="item.id"
      >
        <text class="model-name">{{item.label}}</text>
        <text class="model-desc">{{item.desc}}</text>
      </view>
    </view>
    <view class="model-badge">{{modelLabel}}</view>
  </view>

  <!-- 快捷提问 -->
  <view class="prompt-strip">
    <scroll-view scroll-x class="prompt-scroll" :show-scrollbar="false">
      <view class="prompt-row">
        <view
          v-for="item in quickPrompts"
          :key="item"
          class="prompt-card"
          @tap="usePrompt"
          :data-text="item"
        >
          <text>{{item}}</text>
        </view>
      </view>
    </scroll-view>
  </view>

  <!-- 消息列表 -->
  <scroll-view class="msg-list" scroll-y :scroll-into-view="lastId" enhanced :show-scrollbar="false">
    <block v-for="item in messages" :key="item.id">
      <view v-if="item.from === 'me' || item.text" :id="'msg-'+item.id" class="msg" :class="{ right: item.from === 'me', left: item.from !== 'me' }">
        <view class="avatar" :class="{ me: item.from === 'me', ai: item.from !== 'me' }">{{item.from === 'me' ? '我' : 'AI'}}</view>
        <view class="bubble-wrap">
          <view class="bubble">
            <rich-text v-if="item.rich" :nodes="item.html"></rich-text>
            <block v-else>{{item.text}}</block>
          </view>
          <text class="time">{{item.time}}</text>
        </view>
      </view>
    </block>
    <view class="typing" v-if="loading && !streamingStarted">
      <view class="avatar ai">AI</view>
      <view class="typing-card"><text></text><text></text><text></text></view>
    </view>
  </scroll-view>

  <!-- 输入区域 -->
  <view class="input-wrap">
    <view class="tool-panel" v-if="showToolPanel">
      <view
        v-for="item in toolShortcuts"
        :key="item.id"
        class="tool-item"
        @tap="useToolShortcut"
        :data-id="item.id"
      >
        <view class="tool-icon">{{item.icon}}</view>
        <view class="tool-copy">
          <text class="tool-title">{{item.title}}</text>
          <text class="tool-desc">{{item.desc}}</text>
        </view>
      </view>
    </view>
    <view class="command-panel" v-if="showCommands">
      <view
        v-for="item in visibleCommands"
        :key="item.id"
        class="command-item"
        @tap="useCommand"
        :data-id="item.id"
      >
        <text class="command-label">{{item.label}}</text>
        <text class="command-desc">{{item.desc}}</text>
      </view>
    </view>
    <view class="input-bar">
      <view class="tool-toggle" :class="{ active: showToolPanel }" @tap="toggleToolPanel">+</view>
      <input placeholder="问商品、订单、售后或照护问题，输入 / 可选指令..." :value="input" @input="onInput" confirm-type="send" @confirm="send" />
      <view class="send" :class="{ active: input }" @tap="send">发送</view>
    </view>
    <view class="footer-row">
      <text>健康类建议仅作日常照护参考，急性或持续症状请联系宠物医生。</text>
      <text class="clear" @tap="clearChat">新对话</text>
    </view>
  </view>
</view>
</template>`;

// Read the existing script and style
const f = path.join(base, 'src/pages/chat/chat.vue');
const original = fs.readFileSync(f, 'utf8');
const sStart = original.indexOf('<script>');
const sEnd = original.indexOf('</script>') + 9;
const scriptSection = original.substring(sStart, sEnd);
const cStart = original.indexOf('<style');
const cEnd = original.lastIndexOf('</style>') + 8;
const styleSection = original.substring(cStart, cEnd);

// Compose new file
const newContent = template + '\n\n' + scriptSection + '\n\n' + styleSection + '\n';
fs.writeFileSync(f, newContent);
console.log('chat.vue rewritten successfully');