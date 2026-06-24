import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Product } from "../types/product"
export function listProducts(params: { current: number; size: number; keyword?: string; type?: string; category?: string; storeId?: string; status?: string }): Promise<PageResult<Product>> { return unwrap(http.get("/product/search", { params })) }
export function updateProduct(id: string, data: Partial<Product>): Promise<null> { return unwrap(http.put("/product/" + id, data)) }
export function deleteProduct(id: string): Promise<null> { return unwrap(http.delete("/product/" + id)) }
export function onlineProduct(id: string): Promise<Product> { return unwrap(http.put("/product/" + id + "/online")) }
export function offlineProduct(id: string): Promise<Product> { return unwrap(http.put("/product/" + id + "/offline")) }
