import { defineStore } from "pinia"; import { ref } from "vue"; import type { Order, OrderStatus } from "../types/order"; import { listOrders } from "../api/order"
export const useOrderStore = defineStore("admin_order", () => {
  const list = ref<Order[]>([]); const total = ref(0); const loading = ref(false)
  async function fetch(params: { page: number; size: number; status?: OrderStatus }) { loading.value = true; const r = await listOrders(params); list.value = r.records; total.value = r.total; loading.value = false }
  return { list, total, loading, fetch }
})