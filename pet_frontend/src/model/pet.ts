export function formatPrice(n: string | number): string {
  return "\u00a5" + (typeof n === "string" ? parseFloat(n) : n).toLocaleString("zh-CN")
}
export function formatStatus(s: string): string {
  const m: Record<string, string> = { "\u7b49\u4f60\u6765\u62b1\u62b1": "\u53ef\u9884\u7ea6", "\u6162\u6162\u7b49\u4f60": "\u5f85\u9886\u517b" }
  return m[s] ?? s
}