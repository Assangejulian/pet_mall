export interface User { id: string; username: string; phone: string; avatar: string; email: string; memberLevel: number; realName: string; status: number; createTime: string }
export type ManagementRole = "merchant" | "auditor" | "admin"

export interface AdminInfo {
  userId: string
  username: string
  avatar: string
  role: ManagementRole
}

export interface LoginResult extends AdminInfo {
  token: string
}
