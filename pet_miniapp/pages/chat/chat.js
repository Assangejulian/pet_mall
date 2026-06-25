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

Page({
  data: {
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
    visibleCommands: commands,
    modelMode: "flash",
    modelLabel: "Flash",
    showCommands: false,
    contextLabel: "",
    streamingStarted: false,
    historyOpen: false,
    chatSessions: [],
    currentSessionKey: ""
  },

  onLoad(options) {
    const context = options.store || options.topic || "";
    const sessionId = wx.getStorageSync("aiCustomerSessionId") || "";
    const chatSessions = this.loadHistory();
    const currentSessionKey = wx.getStorageSync("aiCustomerCurrentSessionKey") || "";
    const currentSession = chatSessions.find((item) => item.id === currentSessionKey);
    this.setData({
      sessionId: currentSession ? currentSession.sessionId || "" : sessionId,
      contextLabel: context,
      messages: currentSession && currentSession.messages && currentSession.messages.length ? currentSession.messages : buildWelcome(context),
      modelMode: currentSession ? currentSession.modelMode || "flash" : "flash",
      modelLabel: currentSession && currentSession.modelMode === "pro" ? "Pro" : "Flash",
      chatSessions,
      currentSessionKey: currentSession ? currentSession.id : ""
    });
    this.scrollBottom();
  },

  onInput(event) {
    const value = event.detail.value;
    const keyword = value.trim();
    this.setData({
      input: value,
      showCommands: keyword.indexOf("/") === 0,
      visibleCommands: commands.filter((command) => command.trigger.indexOf(keyword) === 0 || keyword === "/")
    });
  },

  switchModel(event) {
    this.applyModel(event.currentTarget.dataset.mode);
  },

  applyModel(mode) {
    const nextMode = mode === "pro" ? "pro" : "flash";
    this.setData({
      modelMode: nextMode,
      modelLabel: nextMode === "pro" ? "Pro" : "Flash",
      showCommands: false,
      input: this.data.input.trim().indexOf("/") === 0 ? "" : this.data.input
    });
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
      this.setData({ input: "", showCommands: false });
    }
  },

  usePrompt(event) {
    const text = event.currentTarget.dataset.text;
    this.setData({ input: text });
    this.send();
  },

  send() {
    const text = (this.data.input || "").trim();
    if (!text || this.data.loading) return;

    const command = commands.find((item) => item.trigger === text);
    if (command) {
      this.runCommand(command);
      return;
    }

    const userMsg = { id: Date.now(), from: "me", text, time: "刚刚" };
    const aiMsg = { id: Date.now() + 1, from: "ai", text: "", time: "刚刚" };
    this.setData({
      messages: this.data.messages.concat(userMsg, aiMsg),
      input: "",
      showCommands: false,
      loading: true,
      streamingStarted: false,
      lastId: "msg-" + aiMsg.id
    });
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

    const requestTask = wx.request({
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
          this.setData({ loading: false });
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
          this.updateAiMessage(aiId, markdownToText(reply));
        }
        if (event.type === "done") {
          completed = true;
          const finalReply = data.reply ? data.reply : reply;
          if (finalReply) {
            this.updateAiMessage(aiId, markdownToText(finalReply));
          }
          if (data.sessionId) {
            this.saveSession(data.sessionId);
          }
          this.setData({ loading: false });
        }
      });
      this.scrollBottom();
    });
  },

  requestChatFallback(text, aiId) {
    wx.request({
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
        this.updateAiMessage(aiId, markdownToText(reply));
      },
      fail: () => {
        wx.showToast({ title: "后端连接失败，已使用兜底回复", icon: "none" });
        this.updateAiMessage(aiId, fallbackReply(text));
      },
      complete: () => {
        this.setData({ loading: false });
        this.scrollBottom();
      }
    });
  },

  buildPayload(message) {
    return {
      userId: app.globalData.user && app.globalData.user.id,
      sessionId: this.data.sessionId,
      modelMode: this.data.modelMode,
      petProfile: this.data.contextLabel,
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
    wx.setStorageSync("aiCustomerSessionId", sessionId);
    this.setData({ sessionId });
    this.persistCurrentSession();
  },

  updateAiMessage(id, text) {
    const messages = this.data.messages.map((item) => {
      if (item.id !== id) return item;
      return Object.assign({}, item, { text });
    });
    this.setData({
      messages,
      lastId: "msg-" + id,
      streamingStarted: !!text || this.data.streamingStarted
    });
    this.persistCurrentSession();
  },

  clearChat() {
    const oldSessionId = this.data.sessionId;
    if (oldSessionId) {
      wx.request({ url: app.globalData.baseUrl + "/api/ai/session/" + oldSessionId, method: "DELETE" });
    }
    wx.removeStorageSync("aiCustomerSessionId");
    wx.removeStorageSync("aiCustomerCurrentSessionKey");
    this.setData({
      sessionId: "",
      messages: buildWelcome(this.data.contextLabel),
      lastId: "",
      loading: false,
      streamingStarted: false,
      currentSessionKey: "",
      historyOpen: false
    });
    this.scrollBottom();
  },

  openHistory() {
    this.setData({ historyOpen: true, chatSessions: this.loadHistory() });
  },

  closeHistory() {
    this.setData({ historyOpen: false });
  },

  loadHistory() {
    const list = wx.getStorageSync(HISTORY_KEY);
    return sortSessions(Array.isArray(list) ? list : []).map((item) =>
      Object.assign({}, item, { displayTime: formatHistoryTime(item.updatedAt) })
    );
  },

  saveHistory(list) {
    const sorted = sortSessions(list).map((item) => Object.assign({}, item, { displayTime: formatHistoryTime(item.updatedAt) }));
    wx.setStorageSync(HISTORY_KEY, sorted);
    this.setData({ chatSessions: sorted });
  },

  persistCurrentSession(titleText) {
    const meaningful = this.data.messages.filter((item) => item.from === "me" || item.text);
    const userMessages = meaningful.filter((item) => item.from === "me");
    if (!userMessages.length) return;

    const now = Date.now();
    const currentId = this.data.currentSessionKey || "local-" + now;
    const title = (titleText || userMessages[0].text || "新对话").slice(0, 18);
    const list = this.loadHistory();
    const existing = list.find((item) => item.id === currentId);
    const session = Object.assign({}, existing || {}, {
      id: currentId,
      sessionId: this.data.sessionId,
      title: existing && existing.title ? existing.title : title,
      messages: meaningful,
      modelMode: this.data.modelMode,
      contextLabel: this.data.contextLabel,
      updatedAt: now,
      pinned: existing ? !!existing.pinned : false
    });
    const next = list.filter((item) => item.id !== currentId).concat(session);
    wx.setStorageSync("aiCustomerCurrentSessionKey", currentId);
    this.setData({ currentSessionKey: currentId });
    this.saveHistory(next);
  },

  selectSession(event) {
    const id = event.currentTarget.dataset.id;
    const session = this.data.chatSessions.find((item) => item.id === id);
    if (!session) return;
    wx.setStorageSync("aiCustomerCurrentSessionKey", id);
    if (session.sessionId) wx.setStorageSync("aiCustomerSessionId", session.sessionId);
    this.setData({
      currentSessionKey: id,
      sessionId: session.sessionId || "",
      messages: session.messages && session.messages.length ? session.messages : buildWelcome(session.contextLabel || this.data.contextLabel),
      modelMode: session.modelMode || "flash",
      modelLabel: session.modelMode === "pro" ? "Pro" : "Flash",
      contextLabel: session.contextLabel || this.data.contextLabel,
      historyOpen: false,
      loading: false,
      streamingStarted: false
    });
    this.scrollBottom();
  },

  deleteSession(event) {
    const id = event.currentTarget.dataset.id;
    const session = this.data.chatSessions.find((item) => item.id === id);
    const next = this.data.chatSessions.filter((item) => item.id !== id);
    this.saveHistory(next);
    if (session && session.sessionId) {
      wx.request({ url: app.globalData.baseUrl + "/api/ai/session/" + session.sessionId, method: "DELETE" });
    }
    if (id === this.data.currentSessionKey) {
      wx.removeStorageSync("aiCustomerSessionId");
      wx.removeStorageSync("aiCustomerCurrentSessionKey");
      this.setData({
        currentSessionKey: "",
        sessionId: "",
        messages: buildWelcome(this.data.contextLabel),
        loading: false,
        streamingStarted: false
      });
    }
  },

  togglePinSession(event) {
    const id = event.currentTarget.dataset.id;
    const next = this.data.chatSessions.map((item) => {
      if (item.id !== id) return item;
      return Object.assign({}, item, { pinned: !item.pinned, updatedAt: Date.now() });
    });
    this.saveHistory(next);
  },

  scrollBottom() {
    setTimeout(() => {
      const last = this.data.messages[this.data.messages.length - 1];
      if (last) this.setData({ lastId: "msg-" + last.id });
    }, 80);
  }
});

