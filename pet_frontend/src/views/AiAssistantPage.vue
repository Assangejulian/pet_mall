<template>
  <section class="ai-page">
    <aside class="ai-rail">
      <router-link to="/" class="ai-rail-brand">
        <span>♡</span>
        <strong>暖窝 AI</strong>
      </router-link>

      <button class="ai-new-chat" type="button" @click="startNewChat">
        <span>＋</span>
        新对话
      </button>

      <div class="ai-history">
        <p>历史聊天</p>
        <div v-for="item in histories" :key="item.id" class="ai-history-item">
          <button type="button" :class="{ active: item.id === activeHistoryId }" @click="loadHistory(item.id)">
            <span>{{ item.time }}</span>
            {{ item.title }}
          </button>
          <button class="ai-history-delete" type="button" :aria-label="`删除${item.title}`" @click="deleteHistory(item.id)">
            ×
          </button>
        </div>
      </div>
    </aside>

    <main class="ai-workspace" :class="{ active: messages.length > 0 }">
      <div v-if="messages.length === 0" class="ai-topline">
        <div>
          <p class="eyebrow">PETNEST AI CARE</p>
          <h1>今天想照顾哪位小家伙？</h1>
        </div>
        <div class="ai-top-actions">
          <div class="ai-model-switch" aria-label="模型选择">
            <button type="button" :class="{ active: modelMode === 'flash' }" @click="modelMode = 'flash'">Flash</button>
            <button type="button" :class="{ active: modelMode === 'pro' }" @click="modelMode = 'pro'">Pro</button>
          </div>
          <div class="ai-online">
            <span></span>
            在线
          </div>
        </div>
      </div>

      <div ref="messageList" class="ai-messages" :class="{ empty: messages.length === 0 }">
        <div v-if="messages.length === 0" class="ai-empty">
          <h2>从一个具体问题开始</h2>
          <p>可以问照护、饮食、行为训练、到家准备，或者让暖窝 AI 生成一周照护计划。</p>
        </div>

        <template v-else>
          <article v-for="(message, messageIndex) in messages" :key="message.id" class="ai-message" :class="message.role">
            <div class="ai-message-avatar">{{ message.role === "assistant" ? "AI" : "我" }}</div>
            <div class="ai-message-body">
              <div class="ai-markdown" v-html="renderMarkdown(message.content)"></div>
              <div v-if="message.recommendations?.length" class="ai-recommendations">
                <div v-for="item in message.recommendations" :key="item.title">
                  <span>{{ item.tag }}</span>
                  <strong>{{ item.title }}</strong>
                  <small>{{ item.reason }}</small>
                </div>
              </div>
              <div v-if="message.pendingActions?.length" class="ai-pending-actions">
                <div v-for="action in message.pendingActions" :key="action.id" class="ai-pending-action">
                  <span class="ai-pending-badge">{{ action.type === 'CREATE_ORDER' ? '下单' : action.label }}</span>
                  <span class="ai-pending-summary">{{ action.summary }}</span>
                  <button
                    type="button"
                    class="ai-confirm-btn"
                    :disabled="confirming === action.id"
                    @click="confirmAction(action)"
                  >
                    {{ confirming === action.id ? '处理中...' : '确认' }}
                  </button>
                  <button
                    v-if="confirming !== action.id"
                    type="button"
                    class="ai-cancel-btn"
                    @click="dismissAction(action.id, messageIndex)"
                  >
                    忽略
                  </button>
                </div>
              </div>
              <div v-if="message.suggestions?.length" class="ai-suggestions">
                <button v-for="suggestion in message.suggestions" :key="suggestion" type="button" @click="ask(suggestion)">
                  {{ suggestion }}
                </button>
              </div>
            </div>
          </article>
        </template>
      </div>

      <div v-if="messages.length === 0" class="ai-toolbar">
        <div class="ai-prompt-strip" aria-label="快捷问题">
          <button v-for="prompt in quickPrompts" :key="prompt.title" type="button" class="ai-prompt" @click="ask(prompt.text)">
            <span>{{ prompt.kicker }}</span>
            <strong>{{ prompt.title }}</strong>
          </button>
        </div>
      </div>

      <div class="ai-composer-shell">
        <div v-if="commandMenuOpen" class="ai-command-menu">
          <button
            v-for="(command, index) in filteredCommands"
            :key="command.id"
            type="button"
            :class="{ active: index === activeCommandIndex }"
            @mousedown.prevent="runCommand(command)"
          >
            <span>{{ command.label }}</span>
            <small>{{ command.description }}</small>
          </button>
        </div>

        <form class="ai-composer" @submit.prevent="submit">
          <textarea
            v-model="draft"
            rows="1"
            placeholder="问问养宠、护理、饮食、行为训练或选品问题..."
            @keydown="handleComposerKeydown"
          ></textarea>
          <button type="submit" :disabled="loading || !draft.trim()">
            {{ loading ? "思考中" : "发送" }}
          </button>
        </form>
      </div>
      <p class="ai-disclaimer">健康类建议仅作日常照护参考，急性、持续或严重症状请及时联系宠物医生。</p>
    </main>

    <aside class="ai-context">
      <section class="ai-pet-profile">
        <div class="ai-profile-head">
          <p>当前宠物档案</p>
          <button type="button">更改</button>
        </div>
        <h2>小橘 · 2岁</h2>
        <dl>
          <div><dt>品种</dt><dd>中华田园猫</dd></div>
          <div><dt>体重</dt><dd>4.2kg</dd></div>
          <div><dt>状态</dt><dd>已绝育，肠胃敏感</dd></div>
        </dl>
      </section>

      <section class="ai-care-list">
        <p>今日照护</p>
        <ul>
          <li v-for="todo in careTodos" :key="todo">{{ todo }}</li>
        </ul>
      </section>

      <section class="ai-care-list">
        <p>用品推荐占位</p>
        <ul>
          <li v-for="item in mockSupplies" :key="item">{{ item }}</li>
        </ul>
      </section>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from "vue"
import { deleteAiSession, sendAiMessage, streamAiMessage, type AiRecommendation } from "../api/ai"
import "../styles/AiAssistantPage.css"

type Message = {
  id: number
  role: "user" | "assistant"
  content: string
  suggestions?: string[]
  recommendations?: AiRecommendation[]
  pendingActions?: PendingAction[]
}

type HistoryItem = {
  id: number
  sessionId: string
  time: string
  title: string
  prompt: string
  messages: Message[]
}

type AiCommand = {
  id: string
  label: string
  description: string
  match: string[]
  run: () => void
}

const sessionId = ref(`pet-${Date.now()}`)
const activeHistoryId = ref<number | null>(null)
const modelMode = ref<"flash" | "pro">("flash")
const draft = ref("")
const confirming = ref<string | null>(null)
const loading = ref(false)
const messages = ref<Message[]>([])
const messageList = ref<HTMLDivElement | null>(null)
const activeCommandIndex = ref(0)
const HISTORY_STORAGE_KEY = "petnest-ai-histories"

const defaultHistories: HistoryItem[] = [
  { id: 1, sessionId: "mock-cat-food", time: "今天", title: "猫咪换粮计划", prompt: "猫咪肠胃敏感，换粮应该怎么安排？", messages: [] },
  { id: 2, sessionId: "mock-puppy-home", time: "昨天", title: "幼犬到家清单", prompt: "幼犬第一次到家需要准备什么？", messages: [] },
  { id: 3, sessionId: "mock-vaccine", time: "本周", title: "驱虫和疫苗提醒", prompt: "猫咪驱虫和疫苗一般怎么安排？", messages: [] },
]

const readSavedHistories = (): HistoryItem[] => {
  try {
    const raw = localStorage.getItem(HISTORY_STORAGE_KEY)
    if (!raw) return defaultHistories

    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return defaultHistories

    return parsed.filter((item): item is HistoryItem => (
      typeof item?.id === "number"
      && typeof item?.sessionId === "string"
      && typeof item?.title === "string"
      && Array.isArray(item?.messages)
    ))
  } catch {
    return defaultHistories
  }
}

const histories = ref<HistoryItem[]>(readSavedHistories())

watch(
  histories,
  (value) => {
    localStorage.setItem(HISTORY_STORAGE_KEY, JSON.stringify(value))
  },
  { deep: true },
)

const quickPrompts = [
  { kicker: "新手", title: "第一次接猫回家", text: "第一次接猫回家，需要提前准备什么？" },
  { kicker: "健康", title: "猫咪食欲变差", text: "猫咪今天食欲变差，我应该先观察哪些情况？" },
  { kicker: "饮食", title: "怎么挑猫粮", text: "怎么给肠胃敏感的猫挑猫粮？" },
  { kicker: "行为", title: "狗狗乱叫训练", text: "狗狗总是听到门外声音就叫，怎么训练？" },
  { kicker: "计划", title: "生成一周照护计划", text: "帮我给小橘生成一周日常照护计划。" },
]

const careTodos = ["晚饭少量多餐", "检查饮水量", "梳毛 5 分钟", "观察便便状态"]
const mockSupplies = ["低敏主粮", "慢食碗", "益生菌", "逗猫棒"]

const commandDefinitions: AiCommand[] = [
  {
    id: "model-flash",
    label: "/model flash",
    description: "切换到 Flash，适合日常快速问答",
    match: ["model", "flash", "模型", "快速"],
    run: () => {
      modelMode.value = "flash"
      draft.value = ""
    },
  },
  {
    id: "model-pro",
    label: "/model pro",
    description: "切换到 Pro，适合复杂照护规划",
    match: ["model", "pro", "模型", "推理"],
    run: () => {
      modelMode.value = "pro"
      draft.value = ""
    },
  },
]

const commandQuery = computed(() => {
  const value = draft.value.trimStart()
  return value.startsWith("/") ? value.slice(1).trim().toLowerCase() : ""
})

const filteredCommands = computed(() => {
  if (!draft.value.trimStart().startsWith("/")) {
    return []
  }
  if (!commandQuery.value) {
    return commandDefinitions
  }
  return commandDefinitions.filter((command) => (
    command.label.toLowerCase().includes(commandQuery.value)
    || command.description.toLowerCase().includes(commandQuery.value)
    || command.match.some((keyword) => keyword.toLowerCase().includes(commandQuery.value))
  ))
})

const commandMenuOpen = computed(() => filteredCommands.value.length > 0)

watch(filteredCommands, () => {
  activeCommandIndex.value = 0
})

const startNewChat = () => {
  const id = Date.now()
  sessionId.value = `pet-${id}`
  activeHistoryId.value = id
  messages.value = []
  draft.value = ""
  histories.value.unshift({
    id,
    sessionId: sessionId.value,
    time: "刚刚",
    title: "新对话",
    prompt: "",
    messages: [],
  })
}

const ask = (text: string) => {
  if (activeHistoryId.value === null) {
    startNewChat()
  }
  draft.value = text
  submit()
}

const runCommand = (command: AiCommand) => {
  command.run()
}

const handleComposerKeydown = (event: KeyboardEvent) => {
  if (commandMenuOpen.value) {
    if (event.key === "ArrowDown") {
      event.preventDefault()
      activeCommandIndex.value = (activeCommandIndex.value + 1) % filteredCommands.value.length
      return
    }
    if (event.key === "ArrowUp") {
      event.preventDefault()
      activeCommandIndex.value = (activeCommandIndex.value - 1 + filteredCommands.value.length) % filteredCommands.value.length
      return
    }
    if (event.key === "Enter") {
      event.preventDefault()
      runCommand(filteredCommands.value[activeCommandIndex.value])
      return
    }
    if (event.key === "Escape") {
      event.preventDefault()
      draft.value = ""
      return
    }
  }

  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault()
    submit()
  }
}

const deleteHistory = async (id: number) => {
  const target = histories.value.find((item) => item.id === id)
  histories.value = histories.value.filter((item) => item.id !== id)
  if (target) {
    deleteAiSession(target.sessionId).catch(() => {})
  }
  if (activeHistoryId.value === id) {
    activeHistoryId.value = null
    sessionId.value = `pet-${Date.now()}`
    messages.value = []
    draft.value = ""
  }
}

const loadHistory = (id: number) => {
  const target = histories.value.find((item) => item.id === id)
  if (!target) return

  activeHistoryId.value = target.id
  sessionId.value = target.sessionId
  draft.value = ""
  messages.value = target.messages.map((message) => ({ ...message }))

  if (messages.value.length === 0 && target.prompt) {
    ask(target.prompt)
  } else {
    scrollToBottom()
  }
}

const submit = async () => {
  const message = draft.value.trim()
  if (!message || loading.value || commandMenuOpen.value) return

  if (activeHistoryId.value === null) {
    startNewChat()
  }

  messages.value.push({ id: Date.now(), role: "user", content: message })
  const assistantIndex = messages.value.length
  messages.value.push({ id: Date.now() + 1, role: "assistant", content: "" })
  syncActiveHistory(message)
  draft.value = ""
  loading.value = true
  await scrollToBottom()

  try {
    await streamAiMessage({
      sessionId: sessionId.value,
      message,
      modelMode: modelMode.value,
      petProfile: "小橘，2岁，中华田园猫，4.2kg，已绝育，肠胃敏感",
    }, async (event) => {
      if (event.type === "meta") {
        sessionId.value = event.sessionId
      } else if (event.type === "delta") {
        messages.value[assistantIndex].content += event.content
        await scrollToBottom()
      } else if (event.type === "done") {
        sessionId.value = event.response.sessionId
        messages.value[assistantIndex].content = event.response.reply
        messages.value[assistantIndex].suggestions = event.response.suggestions
        messages.value[assistantIndex].recommendations = event.response.recommendations
        messages.value[assistantIndex].pendingActions = event.response.pendingActions
      }
    })
    syncActiveHistory(message)
  } catch {
    try {
      const response = await sendAiMessage({
        sessionId: sessionId.value,
        message,
        modelMode: modelMode.value,
        petProfile: "小橘，2岁，中华田园猫，4.2kg，已绝育，肠胃敏感",
      })
      sessionId.value = response.sessionId
      messages.value[assistantIndex].content = response.reply
      messages.value[assistantIndex].suggestions = response.suggestions
      messages.value[assistantIndex].recommendations = response.recommendations
      messages.value[assistantIndex].pendingActions = response.pendingActions
    } catch {
      messages.value[assistantIndex].content = "我现在连不上后端服务，先给你一个本地建议：先记录它的饮食、精神、排便和持续时间，情况明显或持续加重时及时联系宠物医生。"
      messages.value[assistantIndex].suggestions = ["新手养猫清单", "猫咪呕吐怎么办", "如何选择低敏猫粮"]
      messages.value[assistantIndex].recommendations = [
        { title: "观察记录卡", reason: "方便记录症状变化，后续就医更清楚", tag: "工具" },
        { title: "低敏主粮", reason: "肠胃敏感宠物可优先关注配方稳定性", tag: "用品" },
      ]
    }
    syncActiveHistory(message)
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const confirmAction = async (action: PendingAction) => {
  confirming.value = action.id
  try {
    const result = await confirmAiAction(action.id) as unknown as { data: number }
    if (action.type === 'CREATE_ORDER') {
      const orderId = result.data
      window.open(/order/, '_blank')
    }
  } catch {
    alert('操作失败，请重试')
  } finally {
    confirming.value = null
  }
}

const dismissAction = (actionId: string, messageIndex: number) => {
  const msg = messages.value[messageIndex]
  if (!msg?.pendingActions) return
  msg.pendingActions = msg.pendingActions.filter(a => a.id !== actionId)
}

const syncActiveHistory = (fallbackTitle: string) => {
  const id = activeHistoryId.value
  if (id === null) return

  const target = histories.value.find((item) => item.id === id)
  if (!target) return

  target.sessionId = sessionId.value
  target.prompt = fallbackTitle
  target.messages = messages.value.map((message) => ({ ...message }))
  if (target.title === "新对话") {
    target.title = fallbackTitle.length > 12 ? `${fallbackTitle.slice(0, 12)}...` : fallbackTitle
  }
  target.time = "刚刚"
}

const escapeHtml = (value: string) => value
  .replace(/&/g, "&amp;")
  .replace(/</g, "&lt;")
  .replace(/>/g, "&gt;")
  .replace(/"/g, "&quot;")
  .replace(/'/g, "&#39;")

const renderInlineMarkdown = (value: string) => escapeHtml(value)
  .replace(/`([^`]+)`/g, "<code>$1</code>")
  .replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>")
  .replace(/__([^_]+)__/g, "<strong>$1</strong>")

const renderMarkdown = (value: string) => {
  if (!value.trim()) {
    return "<span class=\"ai-stream-caret\"></span>"
  }

  const lines = value.replace(/\r\n/g, "\n").split("\n")
  const html: string[] = []
  let listType: "ul" | "ol" | null = null
  let paragraph: string[] = []

  const closeParagraph = () => {
    if (!paragraph.length) return
    html.push(`<p>${renderInlineMarkdown(paragraph.join(" "))}</p>`)
    paragraph = []
  }

  const closeList = () => {
    if (!listType) return
    html.push(`</${listType}>`)
    listType = null
  }

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) {
      closeParagraph()
      closeList()
      continue
    }

    const heading = trimmed.match(/^(#{1,3})\s+(.+)$/)
    if (heading) {
      closeParagraph()
      closeList()
      html.push(`<h${heading[1].length}>${renderInlineMarkdown(heading[2])}</h${heading[1].length}>`)
      continue
    }

    const unordered = trimmed.match(/^[-*]\s+(.+)$/)
    const ordered = trimmed.match(/^\d+\.\s+(.+)$/)
    if (unordered || ordered) {
      closeParagraph()
      const nextType = unordered ? "ul" : "ol"
      if (listType !== nextType) {
        closeList()
        listType = nextType
        html.push(`<${nextType}>`)
      }
      html.push(`<li>${renderInlineMarkdown((unordered ?? ordered)?.[1] ?? "")}</li>`)
      continue
    }

    closeList()
    paragraph.push(trimmed)
  }

  closeParagraph()
  closeList()
  return html.join("")
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageList.value) {
    messageList.value.scrollTop = messageList.value.scrollHeight
  }
}
</script>
