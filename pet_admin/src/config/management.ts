import type { ManagementRole } from "../types/user"

export interface ManagementMenuItem {
  path: string
  label: string
  icon: string
}

export const MANAGEMENT_ROLES: readonly ManagementRole[] = ["merchant", "auditor", "admin"]

export const ROLE_HOME: Record<ManagementRole, string> = {
  merchant: "/merchant/store",
  auditor: "/auditor/store",
  admin: "/dashboard",
}

export const ROLE_LABEL: Record<ManagementRole, string> = {
  merchant: "商家管理员",
  auditor: "审核管理员",
  admin: "超级管理员",
}

const dashboard: ManagementMenuItem = { path: "/dashboard", label: "概览", icon: "⌂" }

export const ROLE_MENUS: Record<ManagementRole, ManagementMenuItem[]> = {
  merchant: [
    dashboard,
    { path: "/merchant/store", label: "我的门店", icon: "店" },
    { path: "/merchant/product", label: "我的商品", icon: "品" },
    { path: "/merchant/order", label: "我的订单", icon: "单" },
  ],
  auditor: [
    dashboard,
    { path: "/auditor/store", label: "门店审核", icon: "审" },
    { path: "/auditor/product", label: "商品监管", icon: "监" },
  ],
  admin: [
    dashboard,
    { path: "/user", label: "用户管理", icon: "人" },
    { path: "/store", label: "门店管理", icon: "店" },
    { path: "/product", label: "商品管理", icon: "品" },
    { path: "/order", label: "订单管理", icon: "单" },
    { path: "/video", label: "视频管理", icon: "视" },
    { path: "/auditor/store", label: "门店审核", icon: "审" },
    { path: "/auditor/product", label: "商品监管", icon: "监" },
  ],
}

export function isManagementRole(value: unknown): value is ManagementRole {
  return typeof value === "string" && MANAGEMENT_ROLES.includes(value as ManagementRole)
}

