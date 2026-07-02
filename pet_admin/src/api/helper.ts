import type { Result } from "../types/api"

export async function unwrap<T>(promise: Promise<unknown>): Promise<T> {
  const body = await promise as Result<T>
  if (body.code !== 200) throw new Error(body.message || "请求失败")
  return body.data as T
}
