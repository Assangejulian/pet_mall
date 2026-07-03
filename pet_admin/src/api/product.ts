import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Product } from "../types/product"
export function listProducts(params: { current: number; size: number; keyword?: string; productType?: number; category?: string; storeId?: string; status?: string }): Promise<PageResult<Product>> { return unwrap(http.get("/product/search", { params })) }
export function createProduct(data: Partial<Product>): Promise<Product> { return unwrap(http.post("/product", data)) }
export function updateProduct(id: string, data: Partial<Product>): Promise<void> { return unwrap(http.put("/product/" + id, data)) }
export function deleteProduct(id: string): Promise<void> { return unwrap(http.delete("/product/" + id)) }
export function onlineProduct(id: string): Promise<Product> { return unwrap(http.put("/product/" + id + "/online")) }
export function offlineProduct(id: string): Promise<Product> { return unwrap(http.put("/product/" + id + "/offline")) }
