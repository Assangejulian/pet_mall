export const STORE_STATUS_LABELS: Record<number, string> = {
  0: "待审核",
  1: "营业中",
  2: "已关闭",
  3: "审核驳回",
}

export const PRODUCT_STATUS_LABELS: Record<number, string> = {
  0: "下架",
  1: "上架",
  2: "已售出",
}

export function storeStatusLabel(status?: number) {
  return status === undefined ? "未知" : STORE_STATUS_LABELS[status] || "未知"
}

export function productStatusCode(status?: string | number, statusCode?: number) {
  const value = statusCode ?? Number(status)
  return Number.isFinite(value) ? value : 0
}

export function productStatusLabel(status?: string | number, statusCode?: number) {
  return PRODUCT_STATUS_LABELS[productStatusCode(status, statusCode)] || "未知"
}

export function statusBadge(status: number) {
  return status === 1 ? "badge-green" : status === 0 ? "badge-orange" : "badge-gray"
}
