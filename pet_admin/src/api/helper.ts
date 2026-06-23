import type { Result } from "../types/api"

export async function unwrap<T>(promise: Promise<any>): Promise<T> {
  const body: Result<T> = await promise
  if (body.code !== 200) throw new Error(body.message || "请求失败")
  return body.data as T
}
