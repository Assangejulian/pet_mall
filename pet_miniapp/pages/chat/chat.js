const app = getApp();
const cartApi = require("../../utils/api/cart");

function getToken() {
  return (app.globalData && app.globalData.token) || wx.getStorageSync("token") || "";
}

function getCurrentUserId() {
  return (app.globalData && app.globalData.user && app.globalData.user.id) || wx.getStorageSync("userId") || null;
}

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

function isConfirmText(text) {
  const value = String(text || "").trim().toLowerCase();
  return ["确认", "确定", "好的", "好", "可以", "执行", "同意", "ok", "yes"].indexOf(value) !== -1;
}

function findLatestPendingAction(messages) {
  const list = messages || [];
  for (let index = list.length - 1; index >= 0; index -= 1) {
    if (list[index].pendingAction && list[index].pendingAction.id) {
      return list[index].pendingAction;
    }
  }
  return null;
}

function actionSuccessText(action) {
  const type = action && action.type;
  const payload = (action && action.payload) || {};
  if (type === "ADD_CART") return "已执行：" + (payload.productName || "商品") + "已加入购物车。";
  if (type === "UPDATE_CART") return "已执行：购物车已更新。";
  if (type === "DELETE_CART") return "已执行：购物车商品已删除。";
  if (type === "CREATE_ORDER") return "已执行：订单已创建。";
  return "已执行确认操作。";
}

function actionFailureText(action) {
  const type = action && action.type;
  if (type === "ADD_CART") return "确认接口返回成功，但购物车里没有找到要加入的商品，请稍后重试。";
  if (type === "UPDATE_CART") return "确认接口返回成功，但购物车更新结果没有校验通过，请稍后重试。";
  if (type === "DELETE_CART") return "确认接口返回成功，但购物车里仍能看到该商品，请稍后重试。";
  return "确认接口返回成功，但操作结果没有校验通过，请稍后重试。";
}

function isSuccessResponse(res, body) {
  return res && res.statusCode >= 200 && res.statusCode < 300 && body && body.code === 200;
}

function sameId(left, right) {
  return String(left) === String(right);
}

function getActionPayload(action, data) {
  return (data && data.payload) || (action && action.payload) || {};
}

function isCartAction(action) {
  return action && ["ADD_CART", "UPDATE_CART", "DELETE_CART"].indexOf(action.type) !== -1;
}

function verifyCartAction(action, data, cartItems) {
  if (!isCartAction(action)) return true;
  const payload = getActionPayload(action, data);
  const list = cartItems || [];
  if (action.type === "ADD_CART") {
    return list.some((item) => sameId(item.productId, payload.productId));
  }
  if (action.type === "UPDATE_CART") {
    return list.some((item) => {
      if (!sameId(item.id, payload.cartId)) return false;
      if (payload.quantity != null && Number(item.quantity) !== Number(payload.quantity)) return false;
      if (payload.checked != null && Number(item.checked) !== Number(payload.checked)) return false;
      return true;
    });
  }
  if (action.type === "DELETE_CART") {
    return !list.some((item) => sameId(item.id, payload.cartId));
  }
  return true;
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
    currentSessionKey: "",
    latestPendingAction: null
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
      messages: currentSession && currentSession.messages && currentSession.messages.length ? normalizeMessages(currentSession.messages) : buildWelcome(context),
      modelMode: currentSession ? currentSession.modelMode || "flash" : "flash",
      modelLabel: currentSession && currentSession.modelMode === "pro" ? "Pro" : "Flash",
      chatSessions,
      currentSessionKey: currentSession ? currentSession.id : "",
      latestPendingAction: currentSession ? findLatestPendingAction(currentSession.messages) : null
    });
    this.scrollBottom();
  },

  onInput(event) {
    const value = event.detail.value;
    const keyword = value.trim();
    this.setData({
      input: value,
      showToolPanel: false,
      showCommands: keyword.indexOf("/") === 0,
      visibleCommands: commands.filter((command) => command.trigger.indexOf(keyword) === 0 || keyword === "/")
    });
  },

  toggleToolPanel() {
    this.setData({
      showToolPanel: !this.data.showToolPanel,
      showCommands: false
    });
  },

  useToolShortcut(event) {
    const id = event.currentTarget.dataset.id;
    const tool = toolShortcuts.find((item) => item.id === id);
    if (!tool || this.data.loading) return;
    this.setData({
      input: tool.prompt,
      showToolPanel: false,
      showCommands: false
    });
    this.send();
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
      showToolPanel: false,
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
      this.setData({ input: "", showCommands: false, showToolPanel: false });
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

    if (this.data.latestPendingAction && isConfirmText(text)) {
      this.sendPendingActionConfirmation(text, this.data.latestPendingAction.id);
      return;
    }

    const userMsg = { id: Date.now(), from: "me", text, time: "刚刚" };
    const aiMsg = { id: Date.now() + 1, from: "ai", text: "", time: "刚刚" };
    this.setData({
      messages: this.data.messages.concat(userMsg, aiMsg),
      input: "",
      showCommands: false,
      showToolPanel: false,
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
          this.updateAiMessage(aiId, reply);
        }
        if (event.type === "done") {
          completed = true;
          const finalReply = data.reply ? data.reply : reply;
          if (finalReply) {
            this.updateAiMessage(aiId, finalReply, data.pendingActions || []);
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
        this.updateAiMessage(aiId, reply, data.pendingActions || []);
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
      userId: getCurrentUserId(),
      sessionId: this.data.sessionId,
      modelMode: this.data.modelMode,
      petProfile: this.data.contextLabel,
      message
    };
  },

  buildHeaders() {
    const token = getToken();
    return {
      "content-type": "application/json",
      Authorization: token ? "Bearer " + token : ""
    };
  },

  saveSession(sessionId) {
    if (!sessionId) return;
    wx.setStorageSync("aiCustomerSessionId", sessionId);
    this.setData({ sessionId });
    this.persistCurrentSession();
  },

  updateAiMessage(id, text, pendingActions) {
    const hasPendingUpdate = arguments.length >= 3;
    const nextPendingAction = hasPendingUpdate && pendingActions && pendingActions.length ? pendingActions[0] : null;
    const messages = this.data.messages.map((item) => {
      if (item.id !== id) return item;
      const next = Object.assign({}, item, {
        text,
        html: markdownToHtml(text),
        rich: item.from === "ai"
      });
      if (hasPendingUpdate) {
        next.pendingAction = nextPendingAction;
      }
      return next;
    });
    this.setData({
      messages,
      lastId: "msg-" + id,
      streamingStarted: !!text || this.data.streamingStarted,
      latestPendingAction: hasPendingUpdate ? nextPendingAction : this.data.latestPendingAction
    });
    this.persistCurrentSession();
  },

  sendPendingActionConfirmation(text, actionId) {
    if (this.data.loading || !actionId) return;
    const action = this.data.latestPendingAction || findLatestPendingAction(this.data.messages);
    const userMsg = { id: Date.now(), from: "me", text, time: "刚刚" };
    const aiMsg = { id: Date.now() + 1, from: "ai", text: "正在执行确认操作...", time: "刚刚", rich: false };
    this.setData({
      messages: this.clearPendingActions(this.data.messages).concat(userMsg, aiMsg),
      input: "",
      showCommands: false,
      showToolPanel: false,
      loading: true,
      streamingStarted: true,
      lastId: "msg-" + aiMsg.id,
      latestPendingAction: null
    });
    this.persistCurrentSession(text);
    this.scrollBottom();
    this.confirmPendingAction(actionId, aiMsg.id, action);
  },

  tapConfirmPendingAction(event) {
    if (this.data.loading) return;
    const actionId = event.currentTarget.dataset.actionId;
    if (!actionId) return;
    const action = this.data.latestPendingAction || findLatestPendingAction(this.data.messages);
    const aiMsg = { id: Date.now(), from: "ai", text: "正在执行确认操作...", time: "刚刚", rich: false };
    this.setData({
      messages: this.clearPendingActions(this.data.messages).concat(aiMsg),
      loading: true,
      streamingStarted: true,
      lastId: "msg-" + aiMsg.id,
      latestPendingAction: null
    });
    this.persistCurrentSession();
    this.scrollBottom();
    this.confirmPendingAction(actionId, aiMsg.id, action);
  },

  confirmPendingAction(actionId, aiId, action) {
    wx.request({
      url: app.globalData.baseUrl + "/api/ai/action/confirm",
      method: "POST",
      header: this.buildHeaders(),
      data: {
        actionId,
        userId: getCurrentUserId()
      },
      success: (res) => {
        const body = res.data || {};
        if (!isSuccessResponse(res, body)) {
          this.updateAiMessage(aiId, body.message || body.description || "确认操作失败，请稍后再试。");
          return;
        }
        this.verifyPendingActionResult(action, body.data)
          .then((verified) => {
            this.updateAiMessage(aiId, verified ? actionSuccessText(action) : actionFailureText(action));
          })
          .catch((err) => {
            this.updateAiMessage(aiId, (err && err.message) || "操作已提交，但购物车结果核验失败，请刷新购物车后再看。");
          });
      },
      fail: () => {
        this.updateAiMessage(aiId, "确认操作失败：后端连接失败，请检查服务是否正常。");
      },
      complete: () => {
        this.setData({ loading: false, latestPendingAction: null });
        this.scrollBottom();
      }
    });
  },

  verifyPendingActionResult(action, data) {
    if (!isCartAction(action)) {
      return Promise.resolve(true);
    }
    if (data && Array.isArray(data.cartItems)) {
      return Promise.resolve(verifyCartAction(action, data, data.cartItems));
    }
    return cartApi.list().then((items) => verifyCartAction(action, data, items || []));
  },

  clearPendingActions(messages) {
    return (messages || []).map((item) => {
      if (!item.pendingAction) return item;
      const next = Object.assign({}, item);
      delete next.pendingAction;
      return next;
    });
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
      messages: session.messages && session.messages.length ? normalizeMessages(session.messages) : buildWelcome(session.contextLabel || this.data.contextLabel),
      modelMode: session.modelMode || "flash",
      modelLabel: session.modelMode === "pro" ? "Pro" : "Flash",
      contextLabel: session.contextLabel || this.data.contextLabel,
      historyOpen: false,
      loading: false,
      streamingStarted: false,
      latestPendingAction: findLatestPendingAction(session.messages)
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

