import type { Result } from "../types/api"

export async function unwrap<T>(promise: Promise<unknown>): Promise<T> {
  const body = await promise as Result<T>
  if (!body || typeof body.code !== "number") throw new Error("响应格式异常")
  if (body.code !== 200) throw new Error(body.message || "请求失败")
  if (body.data === null || body.data === undefined) throw new Error("接口未返回数据")
  return body.data as T
}
