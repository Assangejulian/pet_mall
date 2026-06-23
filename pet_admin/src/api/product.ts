import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Product } from "../types/product"
export function listProducts(params: { page: number; size: number; keyword?: string; type?: string }): Promise<PageResult<Product>> { return unwrap(http.get("/product/list", { params })) }
export function updateProduct(id: string, data: Partial<Product>): Promise<null> { return unwrap(http.put("/product/" + id, data)) }
export function deleteProduct(id: string): Promise<null> { return unwrap(http.delete("/product/" + id)) }
