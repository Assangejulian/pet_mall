import { defineStore } from "pinia"; import { ref } from "vue"; import type { Product } from "../types/product"; import { listProducts } from "../api/product"
export const useProductStore = defineStore("admin_product", () => {
  const list = ref<Product[]>([]); const total = ref(0); const loading = ref(false)
  async function fetch(params: { page: number; size: number; keyword?: string; type?: string }) { loading.value = true; const r = await listProducts(params); list.value = r.records; total.value = r.total; loading.value = false }
  return { list, total, loading, fetch }
})