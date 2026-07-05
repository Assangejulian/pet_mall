import http from "./index"

export interface AiChatRequest {
  sessionId: string
  message: string
  modelMode: "flash" | "pro"
  petProfile?: string
}

export interface AiRecommendation {
  title: string
  reason: string
  tag: string
}

export interface PendingAction {
  id: string
  type: string
  label: string
  summary: string
  payload: Record<string, unknown>
}

export interface AiChatResponse {
  sessionId: string
  reply: string
  suggestions: string[]
  recommendations: AiRecommendation[]
  pendingActions: PendingAction[]
}

export type AiStreamEvent =
  | { type: "meta"; sessionId: string }
  | { type: "delta"; content: string }
  | { type: "done"; response: AiChatResponse }

export async function sendAiMessage(payload: AiChatRequest): Promise<AiChatResponse> {
  const result = await http.post<unknown, { data: AiChatResponse }>("/ai/chat", payload)
  return result.data
}

export async function confirmAiAction(actionId: string): Promise<{ data: unknown }> {
  const result = await http.post<unknown, { data: unknown }>("/ai/action/confirm", { actionId })
  return result as { data: unknown }
}

export async function streamAiMessage(
  payload: AiChatRequest,
  onEvent: (event: AiStreamEvent) => void | Promise<void>,
): Promise<void> {
  const response = await fetch("/api/ai/chat/stream", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  })

  if (!response.ok || !response.body) {
    throw new Error(`AI stream request failed: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ""

  const waitForPaint = () => new Promise<void>((resolve) => {
    requestAnimationFrame(() => resolve())
  })

  const flushBlock = async (block: string) => {
    const eventName = block.match(/^event:\s*(.+)$/m)?.[1]?.trim()
    const data = block
      .split("\n")
      .filter((line) => line.startsWith("data:"))
      .map((line) => line.slice(5).trimStart())
      .join("\n")

    if (!eventName || !data) return

    const parsed = JSON.parse(data)
    if (eventName === "meta") {
      await onEvent({ type: "meta", sessionId: parsed.sessionId })
    } else if (eventName === "delta") {
      await onEvent({ type: "delta", content: parsed.content ?? "" })
      await waitForPaint()
    } else if (eventName === "done") {
      await onEvent({ type: "done", response: parsed })
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    buffer += decoder.decode(value ?? new Uint8Array(), { stream: !done })

    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() ?? ""
    for (const block of blocks) {
      await flushBlock(block)
    }

    if (done) break
  }

  if (buffer.trim()) {
    await flushBlock(buffer)
  }
}

export async function deleteAiSession(sessionId: string): Promise<void> {
  await http.delete(`/ai/session/${encodeURIComponent(sessionId)}`)
}
