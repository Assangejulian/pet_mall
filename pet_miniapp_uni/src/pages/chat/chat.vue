<template>
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
</template>

<script>
const app = getApp();

const quickPrompts = [
  "怎么给刚接回家的猫适应环境？",
  "幼犬第一次驱虫要注意什么？",
  "我想问一下商品发货和售后",
  "猫咪突然不爱吃饭怎么办？"
];

const modelOptions = [
  { id: "flash", label: "Flash", desc: "快速回复" },
  { id: "pro", label: "Pro", desc: "深度分析" }
];

const commands = [
  { id: "model-flash", trigger: "/flash", type: "model", value: "flash", label: "/flash", desc: "切换到快速客服模型" },
  { id: "model-pro", trigger: "/pro", type: "model", value: "pro", label: "/pro", desc: "切换到深度客服模型" },
  { id: "session-new", trigger: "/new", type: "session", value: "new", label: "/new", desc: "开启一段新对话" }
];

const toolShortcuts = [
  { id: "products", icon: "商", title: "商品搜索", desc: "查商品和库存", prompt: "请调用商品搜索工具，推荐几件适合新手养宠准备的商品，并给出理由。" },
  { id: "stores", icon: "店", title: "店铺搜索", desc: "查营业门店", prompt: "请调用店铺搜索工具，帮我查可营业的宠物店，并说明适合咨询什么问题。" },
  { id: "videos", icon: "视", title: "视频Feed", desc: "查养宠视频", prompt: "请调用视频 feed 工具，找几条适合新手养宠参考的视频。" },
  { id: "cart", icon: "车", title: "购物车", desc: "查看购物车", prompt: "请调用购物车查询工具，帮我查看当前购物车，并总结还缺哪些常用用品。" }
];

const HISTORY_KEY = "aiCustomerChatSessions";

function buildWelcome(context) {
  return [
    {
      id: 1,
      from: "ai",
      text: context
        ? "你好，我是暖窝智能客服。你可以直接问我关于「" + context + "」的问题，也可以咨询商品、订单、售后和基础宠物照护。"
        : "你好，我是暖窝智能客服。商品推荐、订单售后、宠物照护和视频里的小家伙，都可以问我。",
      time: "刚刚"
    }
  ];
}

function fallbackReply(text) {
  if (text.indexOf("订单") > -1 || text.indexOf("发货") > -1 || text.indexOf("售后") > -1) {
    return "后端暂时没有返回，我先给你一个兜底建议：请先在订单页查看状态。如果是发货、取消或售后问题，把订单编号发给商家会更快处理。";
  }
  if (text.indexOf("猫") > -1 || text.indexOf("狗") > -1 || text.indexOf("宠物") > -1) {
    return "后端暂时没有返回，我先给你一个兜底建议：请补充年龄、体重、饮食、精神状态和持续时间。若症状急性或持续加重，请及时联系宠物医生。";
  }
  return "后端暂时没有返回。你可以检查后端 8080 是否运行，或稍后再试。";
}

function markdownToText(text) {
  return (text || "")
    .replace(/^#{1,6}\s+/gm, "")
    .replace(/\*\*(.*?)\*\*/g, "$1")
    .replace(/`([^`]+)`/g, "$1")
    .replace(/^\s*[-*]\s+/gm, "• ")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

function escapeHtml(text) {
  return String(text || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function renderInlineMarkdown(text) {
  return escapeHtml(text)
    .replace(/`([^`]+)`/g, "<code style=\"padding:2rpx 8rpx;border-radius:8rpx;background:#f2e8dd;color:#0b4a3d;font-size:26rpx;\">$1</code>")
    .replace(/\*\*([^*]+)\*\*/g, "<strong style=\"font-weight:900;color:#241912;\">$1</strong>")
    .replace(/\*([^*]+)\*/g, "<em style=\"font-style:normal;color:#0b4a3d;\">$1</em>");
}

function isMarkdownTableSeparator(line) {
  return /^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test((line || "").trim());
}

function isMarkdownTableRow(line) {
  const trimmed = (line || "").trim();
  return trimmed.indexOf("|") !== -1 && /^\|?.+\|.+\|?$/.test(trimmed) && !isMarkdownTableSeparator(trimmed);
}

function splitMarkdownTableRow(line) {
  return (line || "")
    .trim()
    .replace(/^\|/, "")
    .replace(/\|$/, "")
    .split("|")
    .map((cell) => cell.trim());
}

function renderMarkdownTable(header, rows) {
  const safeHeader = header || [];
  const safeRows = rows || [];
  const renderedRows = safeRows.map((row) => {
    const cells = row.map((cell, index) => {
      const width = index === 0 ? "34%" : "66%";
      const weight = index === 0 ? "900" : "500";
      return "<span style=\"display:inline-block;vertical-align:top;width:" + width + ";box-sizing:border-box;padding:12rpx 14rpx;font-weight:" + weight + ";line-height:1.45;\">" + renderInlineMarkdown(cell) + "</span>";
    }).join("");
    return "<div style=\"border-top:1rpx solid #eadfd3;\">" + cells + "</div>";
  }).join("");

  const renderedHeader = safeHeader.length
    ? "<div style=\"background:#f6eee5;color:#241912;\">" + safeHeader.map((cell, index) => {
        const width = index === 0 ? "34%" : "66%";
        return "<span style=\"display:inline-block;vertical-align:top;width:" + width + ";box-sizing:border-box;padding:12rpx 14rpx;font-weight:900;line-height:1.35;\">" + renderInlineMarkdown(cell) + "</span>";
      }).join("") + "</div>"
    : "";

  return "<div style=\"margin:12rpx 0;border:1rpx solid #eadfd3;border-radius:16rpx;overflow:hidden;background:#fffaf4;font-size:25rpx;\">" + renderedHeader + renderedRows + "</div>";
}

function markdownToHtml(text) {
  const lines = String(text || "").replace(/\r\n/g, "\n").split("\n");
  const parts = [];
  let listType = "";

  function closeList() {
    if (listType) {
      parts.push("</" + listType + ">");
      listType = "";
    }
  }

  for (let index = 0; index < lines.length; index += 1) {
    const raw = lines[index];
    const line = raw.trim();
    if (!line) {
      closeList();
      parts.push("<div style=\"height:12rpx;\"></div>");
      continue;
    }

    if (isMarkdownTableRow(line) && isMarkdownTableSeparator(lines[index + 1])) {
      closeList();
      const header = splitMarkdownTableRow(line);
      const rows = [];
      index += 2;
      while (index < lines.length && isMarkdownTableRow(lines[index])) {
        rows.push(splitMarkdownTableRow(lines[index]));
        index += 1;
      }
      index -= 1;
      parts.push(renderMarkdownTable(header, rows));
      continue;
    }

    const heading = line.match(/^(#{1,3})\s+(.+)$/);
    if (heading) {
      closeList();
      parts.push("<div style=\"margin:10rpx 0 8rpx;font-size:30rpx;font-weight:900;color:#241912;\">" + renderInlineMarkdown(heading[2]) + "</div>");
      continue;
    }

    const unordered = line.match(/^[-*]\s+(.+)$/);
    if (unordered) {
      if (listType !== "ul") {
        closeList();
        listType = "ul";
        parts.push("<ul style=\"margin:8rpx 0 8rpx 34rpx;padding:0;\">");
      }
      parts.push("<li style=\"margin:6rpx 0;line-height:1.55;\">" + renderInlineMarkdown(unordered[1]) + "</li>");
      continue;
    }

    const ordered = line.match(/^\d+[.)]\s+(.+)$/);
    if (ordered) {
      if (listType !== "ol") {
        closeList();
        listType = "ol";
        parts.push("<ol style=\"margin:8rpx 0 8rpx 34rpx;padding:0;\">");
      }
      parts.push("<li style=\"margin:6rpx 0;line-height:1.55;\">" + renderInlineMarkdown(ordered[1]) + "</li>");
      continue;
    }

    closeList();
    parts.push("<div style=\"margin:6rpx 0;line-height:1.55;\">" + renderInlineMarkdown(line) + "</div>");
  }

  closeList();
  return parts.join("").trim();
}

function decodeChunk(buffer) {
  if (!buffer) return "";
  if (typeof TextDecoder !== "undefined") {
    return new TextDecoder("utf-8").decode(new Uint8Array(buffer));
  }
  const bytes = new Uint8Array(buffer);
  let encoded = "";
  for (let i = 0; i < bytes.length; i += 1) {
    encoded += "%" + bytes[i].toString(16).padStart(2, "0");
  }
  try {
    return decodeURIComponent(encoded);
  } catch (error) {
    return "";
  }
}

function parseSse(buffer) {
  const chunks = buffer.split(/\r?\n\r?\n/);
  const rest = chunks.pop() || "";
  const events = chunks
    .map((chunk) => {
      const event = { type: "message", data: "" };
      const dataLines = [];
      chunk.split(/\r?\n/).forEach((line) => {
        if (line.indexOf("event:") === 0) {
          event.type = line.slice(6).trim();
        } else if (line.indexOf("data:") === 0) {
          dataLines.push(line.slice(5).trim());
        }
      });
      event.data = dataLines.join("\n");
      return event;
    })
    .filter((event) => event.data);
  return { events, rest };
}

function parseEventData(data) {
  try {
    return JSON.parse(data);
  } catch (error) {
    return {};
  }
}

function formatHistoryTime(value) {
  const date = value ? new Date(value) : new Date();
  const today = new Date();
  const sameDay =
    date.getFullYear() === today.getFullYear() &&
    date.getMonth() === today.getMonth() &&
    date.getDate() === today.getDate();
  if (sameDay) {
    return "今天 " + String(date.getHours()).padStart(2, "0") + ":" + String(date.getMinutes()).padStart(2, "0");
  }
  return String(date.getMonth() + 1).padStart(2, "0") + "/" + String(date.getDate()).padStart(2, "0");
}

function sortSessions(list) {
  return (list || []).slice().sort((a, b) => {
    if (!!a.pinned !== !!b.pinned) return a.pinned ? -1 : 1;
    return (b.updatedAt || 0) - (a.updatedAt || 0);
  });
}

function normalizeMessages(messages) {
  return (messages || []).map((item) => {
    if (item.from !== "ai" || !item.text) return item;
    return Object.assign({}, item, {
      html: markdownToHtml(item.text),
      rich: true
    });
  });
}

export default {
  data() { return {
    title: "暖窝智能客服",
    subtitle: "商品、订单、售后和照护问题都可以问",
    online: true,
    messages: [],
    input: "",
    lastId: "",
    sessionId: "",
    loading: false,
    quickPrompts,
    modelOptions,
    commands,
    toolShortcuts,
    visibleCommands: commands,
    modelMode: "flash",
    modelLabel: "Flash",
    showCommands: false,
    showToolPanel: false,
    contextLabel: "",
    streamingStarted: false,
    historyOpen: false,
    chatSessions: [],
    currentSessionKey: ""
    }
  },

  onLoad(options) {
    const context = options.store || options.topic || "";
    const sessionId = uni.getStorageSync("aiCustomerSessionId") || "";
    const chatSessions = this.loadHistory();
    const currentSessionKey = uni.getStorageSync("aiCustomerCurrentSessionKey") || "";
    const currentSession = chatSessions.find((item) => item.id === currentSessionKey);
    this.sessionId = currentSession ? currentSession.sessionId || "" : sessionId;
        this.contextLabel = context;
        this.messages = currentSession && currentSession.messages && currentSession.messages.length ? normalizeMessages(currentSession.messages) : buildWelcome(context);
        this.modelMode = currentSession ? currentSession.modelMode || "flash" : "flash";
        this.modelLabel = currentSession && currentSession.modelMode === "pro" ? "Pro" : "Flash";
        this.currentSessionKey = currentSession ? currentSession.id : "";
    this.scrollBottom();
  },

  onInput(event) {
    const value = event.detail.value;
    const keyword = value.trim();
    this.input = value;
        this.showToolPanel = false;
        this.showCommands = keyword.indexOf("/") === 0;
        this.visibleCommands = commands.filter((command) => command.trigger.indexOf(keyword) === 0 || keyword === "/");
  },

  toggleToolPanel() {
    this.showToolPanel = !this.showToolPanel;
        this.showCommands = false;
  },

  useToolShortcut(event) {
    const id = event.currentTarget.dataset.id;
    const tool = toolShortcuts.find((item) => item.id === id);
    if (!tool || this.loading) return;
    this.input = tool.prompt;
        this.showToolPanel = false;
        this.showCommands = false;
    this.send();
  },

  switchModel(event) {
    this.applyModel(event.currentTarget.dataset.mode);
  },

  applyModel(mode) {
    const nextMode = mode === "pro" ? "pro" : "flash";
    this.modelMode = nextMode;
        this.modelLabel = nextMode === "pro" ? "Pro" : "Flash";
        this.showCommands = false;
        this.showToolPanel = false;
        this.input = this.input.trim().indexOf("/") === 0 ? "" : this.input;
  },

  useCommand(event) {
    const id = event.currentTarget.dataset.id;
    const command = commands.find((item) => item.id === id);
    if (command) {
      this.runCommand(command);
    }
  },

  runCommand(command) {
    if (command.type === "model") {
      this.applyModel(command.value);
      return;
    }
    if (command.type === "session" && command.value === "new") {
      this.clearChat();
      this.input = ""; this.showCommands = false; this.showToolPanel = false;
    }
  },

  usePrompt(event) {
    const text = event.currentTarget.dataset.text;
    this.input = text;
    this.send();
  },

  send() {
    const text = (this.input || "").trim();
    if (!text || this.loading) return;

    const command = commands.find((item) => item.trigger === text);
    if (command) {
      this.runCommand(command);
      return;
    }

    const userMsg = { id: Date.now(), from: "me", text, time: "刚刚" };
    const aiMsg = { id: Date.now() + 1, from: "ai", text: "", time: "刚刚" };
    this.messages = this.messages.concat(userMsg, aiMsg);
        this.input = "";
        this.showCommands = false;
        this.showToolPanel = false;
        this.loading = true;
        this.streamingStarted = false;
        this.lastId = "msg-" + aiMsg.id;
    this.persistCurrentSession(text);
    this.scrollBottom();
    this.streamChat(text, aiMsg.id);
  },

  streamChat(text, aiId) {
    const payload = this.buildPayload(text);
    let buffer = "";
    let reply = "";
    let chunked = false;
    let completed = false;

    const requestTask = uni.request({
      url: app.globalData.baseUrl + "/api/ai/chat/stream",
      method: "POST",
      enableChunked: true,
      responseType: "arraybuffer",
      header: this.buildHeaders(),
      data: payload,
      success: () => {
        if (!chunked && !completed) {
          this.requestChatFallback(text, aiId);
        }
      },
      fail: () => {
        if (!completed) {
          this.requestChatFallback(text, aiId);
        }
      },
      complete: () => {
        if (chunked || completed) {
          this.loading = false;
        }
      }
    });

    if (!requestTask || !requestTask.onChunkReceived) {
      if (requestTask && requestTask.abort) {
        requestTask.abort();
      }
      this.requestChatFallback(text, aiId);
      return;
    }

    requestTask.onChunkReceived((res) => {
      chunked = true;
      buffer += decodeChunk(res.data);
      const parsed = parseSse(buffer);
      buffer = parsed.rest;
      parsed.events.forEach((event) => {
        const data = parseEventData(event.data);
        if (event.type === "meta" && data.sessionId) {
          this.saveSession(data.sessionId);
        }
        if (event.type === "delta" && data.content) {
          reply += data.content;
          this.updateAiMessage(aiId, reply);
        }
        if (event.type === "done") {
          completed = true;
          const finalReply = data.reply ? data.reply : reply;
          if (finalReply) {
            this.updateAiMessage(aiId, finalReply);
          }
          if (data.sessionId) {
            this.saveSession(data.sessionId);
          }
          this.loading = false;
        }
      });
      this.scrollBottom();
    });
  },

  requestChatFallback(text, aiId) {
    uni.request({
      url: app.globalData.baseUrl + "/api/ai/chat",
      method: "POST",
      header: this.buildHeaders(),
      data: this.buildPayload(text),
      success: (res) => {
        const body = res.data || {};
        const data = body.data || body;
        const reply = data.reply || fallbackReply(text);
        if (data.sessionId) {
          this.saveSession(data.sessionId);
        }
        this.updateAiMessage(aiId, reply);
      },
      fail: () => {
        uni.showToast({ title: "后端连接失败，已使用兜底回复", icon: "none" });
        this.updateAiMessage(aiId, fallbackReply(text));
      },
      complete: () => {
        this.loading = false;
        this.scrollBottom();
      }
    });
  },

  buildPayload(message) {
    return {
      userId: app.globalData.user && app.globalData.user.id,
      sessionId: this.sessionId,
      modelMode: this.modelMode,
      petProfile: this.contextLabel,
      message
    };
  },

  buildHeaders() {
    return {
      "content-type": "application/json",
      Authorization: app.globalData.token ? "Bearer " + app.globalData.token : ""
    };
  },

  saveSession(sessionId) {
    if (!sessionId) return;
    uni.setStorageSync("aiCustomerSessionId", sessionId);
    this.sessionId = sessionId;
    this.persistCurrentSession();
  },

  updateAiMessage(id, text) {
    const messages = this.messages.map((item) => {
      if (item.id !== id) return item;
      return Object.assign({}, item, {
        text,
        html: markdownToHtml(text),
        rich: item.from === "ai"
      });
    });
    this.lastId = "msg-" + id;
        this.streamingStarted = !!text || this.streamingStarted;
    this.persistCurrentSession();
  },

  clearChat() {
    const oldSessionId = this.sessionId;
    if (oldSessionId) {
      uni.request({ url: app.globalData.baseUrl + "/api/ai/session/" + oldSessionId, method: "DELETE" });
    }
    uni.removeStorageSync("aiCustomerSessionId");
    uni.removeStorageSync("aiCustomerCurrentSessionKey");
    this.sessionId = "";
        this.messages = buildWelcome(this.contextLabel);
        this.lastId = "";
        this.loading = false;
        this.streamingStarted = false;
        this.currentSessionKey = "";
        this.historyOpen = false;
    this.scrollBottom();
  },

  openHistory() {
    this.historyOpen = true; this.chatSessions = this.loadHistory();
  },

  closeHistory() {
    this.historyOpen = false;
  },

  loadHistory() {
    const list = uni.getStorageSync(HISTORY_KEY);
    return sortSessions(Array.isArray(list) ? list : []).map((item) =>
      Object.assign({}, item, { displayTime: formatHistoryTime(item.updatedAt) })
    );
  },

  saveHistory(list) {
    const sorted = sortSessions(list).map((item) => Object.assign({}, item, { displayTime: formatHistoryTime(item.updatedAt) }));
    uni.setStorageSync(HISTORY_KEY, sorted);
    this.chatSessions = sorted;
  },

  persistCurrentSession(titleText) {
    const meaningful = this.messages.filter((item) => item.from === "me" || item.text);
    const userMessages = meaningful.filter((item) => item.from === "me");
    if (!userMessages.length) return;

    const now = Date.now();
    const currentId = this.currentSessionKey || "local-" + now;
    const title = (titleText || userMessages[0].text || "新对话").slice(0, 18);
    const list = this.loadHistory();
    const existing = list.find((item) => item.id === currentId);
    const session = Object.assign({}, existing || {}, {
      id: currentId,
      sessionId: this.sessionId,
      title: existing && existing.title ? existing.title : title,
      messages: meaningful,
      modelMode: this.modelMode,
      contextLabel: this.contextLabel,
      updatedAt: now,
      pinned: existing ? !!existing.pinned : false
    });
    const next = list.filter((item) => item.id !== currentId).concat(session);
    uni.setStorageSync("aiCustomerCurrentSessionKey", currentId);
    this.currentSessionKey = currentId;
    this.saveHistory(next);
  },

  selectSession(event) {
    const id = event.currentTarget.dataset.id;
    const session = this.chatSessions.find((item) => item.id === id);
    if (!session) return;
    uni.setStorageSync("aiCustomerCurrentSessionKey", id);
    if (session.sessionId) uni.setStorageSync("aiCustomerSessionId", session.sessionId);
    this.currentSessionKey = id;
        this.sessionId = session.sessionId || "";
        this.messages = session.messages && session.messages.length ? normalizeMessages(session.messages) : buildWelcome(session.contextLabel || this.contextLabel);
        this.modelMode = session.modelMode || "flash";
        this.modelLabel = session.modelMode === "pro" ? "Pro" : "Flash";
        this.contextLabel = session.contextLabel || this.contextLabel;
        this.historyOpen = false;
        this.loading = false;
        this.streamingStarted = false;
    this.scrollBottom();
  },

  deleteSession(event) {
    const id = event.currentTarget.dataset.id;
    const session = this.chatSessions.find((item) => item.id === id);
    const next = this.chatSessions.filter((item) => item.id !== id);
    this.saveHistory(next);
    if (session && session.sessionId) {
      uni.request({ url: app.globalData.baseUrl + "/api/ai/session/" + session.sessionId, method: "DELETE" });
    }
    if (id === this.currentSessionKey) {
      uni.removeStorageSync("aiCustomerSessionId");
      uni.removeStorageSync("aiCustomerCurrentSessionKey");
      this.currentSessionKey = "";
        this.sessionId = "";
        this.messages = buildWelcome(this.contextLabel);
        this.loading = false;
        this.streamingStarted = false;
    }
  },

  togglePinSession(event) {
    const id = event.currentTarget.dataset.id;
    const next = this.chatSessions.map((item) => {
      if (item.id !== id) return item;
      return Object.assign({}, item, { pinned: !item.pinned, updatedAt: Date.now() });
    });
    this.saveHistory(next);
  },

  scrollBottom() {
    setTimeout(() => {
      const last = this.messages[this.messages.length - 1];
      if (last) this.lastId = "msg-" + last.id;
    }, 80);
  }

}</script>

<style scoped>
.page {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  background: linear-gradient(180deg, #fbf6ed 0%, #fffaf4 58%, #f7efe4 100%);
  color: #2d1f18;
}

.history-mask {
  position: fixed;
  inset: 0;
  z-index: 20;
  background: rgba(36, 25, 18, 0.18);
}

.history-drawer {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 21;
  width: 570rpx;
  max-width: 82vw;
  padding: 36rpx 24rpx calc(30rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: rgba(255, 250, 244, 0.96);
  border-right: 1rpx solid rgba(140, 104, 83, 0.12);
  box-shadow: 24rpx 0 58rpx rgba(36, 25, 18, 0.18);
  transform: translateX(-104%);
  transition: transform 0.22s ease;
}

.history-drawer.open {
  transform: translateX(0);
}

.drawer-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
  color: #241912;
  font-size: 32rpx;
  font-weight: 900;
}

.drawer-close {
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(36, 25, 18, 0.08);
  color: #8f7366;
  font-size: 36rpx;
  line-height: 1;
}

.drawer-new {
  height: 78rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
  background: #241912;
  color: #fff;
  font-size: 27rpx;
  font-weight: 900;
}

.history-list {
  height: calc(100vh - 172rpx - env(safe-area-inset-bottom));
}

.history-item {
  display: flex;
  gap: 14rpx;
  align-items: center;
  padding: 18rpx 14rpx 18rpx 18rpx;
  margin-bottom: 12rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.68);
  border: 1rpx solid rgba(140, 104, 83, 0.08);
}

.history-item.active {
  background: rgba(232, 146, 124, 0.16);
  border-color: rgba(232, 146, 124, 0.32);
}

.history-main {
  flex: 1;
  min-width: 0;
}

.history-title {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #2d1f18;
  font-size: 26rpx;
  font-weight: 800;
}

.history-time {
  display: block;
  margin-top: 6rpx;
  color: #9d7a6b;
  font-size: 21rpx;
}

.history-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex-shrink: 0;
}

.history-action {
  min-width: 66rpx;
  height: 38rpx;
  padding: 0 10rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(36, 25, 18, 0.07);
  color: #8f7366;
  font-size: 20rpx;
  font-weight: 800;
}

.history-action.danger {
  color: #e8927c;
}

.history-empty {
  margin-top: 80rpx;
  text-align: center;
  color: #b7a69a;
  font-size: 24rpx;
}

.hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 22rpx;
  flex-shrink: 0;
  padding: 30rpx 30rpx 14rpx;
}

.history-btn {
  position: absolute;
  left: 28rpx;
  top: 30rpx;
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.72);
  border: 1rpx solid rgba(140, 104, 83, 0.12);
  color: #241912;
  font-size: 34rpx;
  font-weight: 900;
  box-shadow: 0 14rpx 34rpx rgba(75, 45, 28, 0.08);
}

.hero-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #241912;
  color: #fff;
  font-size: 26rpx;
  font-weight: 800;
  box-shadow: 0 18rpx 40rpx rgba(36, 25, 18, 0.18);
  margin-left: 92rpx;
}

.hero-copy {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  display: block;
  color: #e8927c;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  margin-bottom: 8rpx;
}

.title {
  display: block;
  font-family: "Songti SC", serif;
  font-size: 48rpx;
  font-weight: 800;
  line-height: 1.12;
  color: #0b4a3d;
}

.subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 25rpx;
  color: #8f7366;
  line-height: 1.5;
}

.status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.72);
  color: #8f7366;
  font-size: 24rpx;
  border: 1rpx solid rgba(140, 104, 83, 0.1);
}

.status-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #b8a89d;
}

.status.on .status-dot {
  background: #3fb476;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
  padding: 0 30rpx 18rpx;
}

.model-switch {
  flex: 1;
  display: flex;
  padding: 6rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.72);
  border: 1rpx solid rgba(140, 104, 83, 0.1);
}

.model-item {
  flex: 1;
  min-width: 0;
  height: 68rpx;
  border-radius: 19rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8f7366;
}

.model-item.active {
  background: #241912;
  color: #fff;
  box-shadow: 0 10rpx 28rpx rgba(36, 25, 18, 0.18);
}

.model-name {
  font-size: 25rpx;
  font-weight: 900;
  line-height: 1.1;
}

.model-desc {
  margin-top: 4rpx;
  font-size: 19rpx;
  line-height: 1.1;
  opacity: 0.78;
}

.model-badge {
  width: 92rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(232, 146, 124, 0.14);
  color: #e8927c;
  font-size: 24rpx;
  font-weight: 900;
}

.prompt-strip {
  flex-shrink: 0;
  padding: 0 0 12rpx 30rpx;
}

.prompt-scroll {
  white-space: nowrap;
}

.prompt-row {
  display: inline-flex;
  gap: 16rpx;
  padding-right: 30rpx;
}

.prompt-card {
  width: 260rpx;
  min-height: 90rpx;
  padding: 20rpx 22rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.62);
  border: 1rpx solid rgba(140, 104, 83, 0.1);
  box-shadow: 0 18rpx 42rpx rgba(75, 45, 28, 0.08);
  white-space: normal;
}

.prompt-card text {
  font-size: 25rpx;
  line-height: 1.35;
  color: #2d1f18;
  font-weight: 650;
}

.msg-list {
  flex: 1;
  min-height: 0;
  height: 0;
  padding: 14rpx 30rpx 26rpx;
  box-sizing: border-box;
  overflow: hidden;
}

.msg,
.typing {
  display: flex;
  gap: 14rpx;
  margin-bottom: 24rpx;
}

.msg.right {
  flex-direction: row-reverse;
}

.avatar {
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22rpx;
  font-weight: 800;
}

.avatar.ai {
  background: #241912;
  color: #fff;
}

.avatar.me {
  background: #e8927c;
  color: #fff;
}

.bubble-wrap {
  max-width: 78%;
}

.msg.right .bubble-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.bubble {
  padding: 20rpx 24rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  line-height: 1.55;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg.left .bubble {
  background: rgba(255, 255, 255, 0.82);
  color: #2d1f18;
  border-top-left-radius: 8rpx;
  border: 1rpx solid rgba(140, 104, 83, 0.08);
}

.msg.right .bubble {
  background: #241912;
  color: #fff;
  border-top-right-radius: 8rpx;
}

.time {
  display: block;
  margin-top: 8rpx;
  font-size: 21rpx;
  color: #b7a69a;
}

.typing-card {
  display: flex;
  align-items: center;
  gap: 8rpx;
  height: 54rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.82);
}

.typing-card text {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: #e8927c;
}

.input-wrap {
  flex-shrink: 0;
  padding: 28rpx 28rpx calc(8rpx + env(safe-area-inset-bottom));
  background: rgba(255, 250, 244, 0.92);
  border-top: 1rpx solid rgba(140, 104, 83, 0.08);
}

.command-panel {
  margin-bottom: 12rpx;
  padding: 10rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid rgba(140, 104, 83, 0.12);
  box-shadow: 0 14rpx 38rpx rgba(75, 45, 28, 0.1);
}

.tool-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12rpx;
  margin-bottom: 12rpx;
  padding: 12rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid rgba(140, 104, 83, 0.12);
  box-shadow: 0 14rpx 38rpx rgba(75, 45, 28, 0.1);
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-width: 0;
  min-height: 76rpx;
  padding: 10rpx 12rpx;
  border-radius: 20rpx;
  background: rgba(251, 246, 237, 0.72);
  border: 1rpx solid rgba(140, 104, 83, 0.08);
  box-sizing: border-box;
}

.tool-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 17rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #241912;
  color: #fff;
  font-size: 22rpx;
  font-weight: 900;
}

.tool-copy {
  min-width: 0;
  flex: 1;
}

.tool-title {
  display: block;
  color: #241912;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 1.15;
}

.tool-desc {
  display: block;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #8f7366;
  font-size: 20rpx;
  line-height: 1.15;
}

.command-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-height: 62rpx;
  padding: 0 12rpx;
  border-radius: 18rpx;
}

.command-item + .command-item {
  margin-top: 4rpx;
}

.command-label {
  width: 104rpx;
  color: #241912;
  font-size: 25rpx;
  font-weight: 900;
}

.command-desc {
  flex: 1;
  min-width: 0;
  color: #8f7366;
  font-size: 23rpx;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-height: 86rpx;
  padding: 10rpx 12rpx 10rpx 26rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 1rpx solid rgba(140, 104, 83, 0.14);
}

.tool-toggle {
  width: 58rpx;
  height: 58rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(36, 25, 18, 0.08);
  color: #241912;
  font-size: 38rpx;
  font-weight: 800;
  line-height: 1;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.tool-toggle.active {
  transform: rotate(45deg);
  background: #241912;
  color: #fff;
}

.input-bar input {
  flex: 1;
  min-width: 0;
  height: 64rpx;
  font-size: 27rpx;
  color: #2d1f18;
}

.send {
  width: 112rpx;
  height: 64rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #9d9690;
  color: #fff;
  font-size: 27rpx;
  font-weight: 800;
}

.send.active {
  background: #241912;
}

.footer-row {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  margin-top: 12rpx;
  font-size: 21rpx;
  color: #9d7a6b;
  line-height: 1.4;
}

.footer-row text:first-child {
  flex: 1;
}

.clear {
  color: #e8927c;
  white-space: nowrap;
  font-weight: 700;
}

</style>
