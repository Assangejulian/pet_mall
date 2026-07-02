export function notify(message: string, type: "success" | "error" = "success") {
  document.dispatchEvent(new CustomEvent("toast", { detail: { message, type } }))
}
