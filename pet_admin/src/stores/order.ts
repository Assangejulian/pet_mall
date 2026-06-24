import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Order, OrderStatus } from '../types/order'
import { listOrders } from '../api/order'

export const useOrderStore = defineStore('admin_order', () => {
  const list = ref<Order[]>([])
  const total = ref(0)
  const loading = ref(false)

  const pendingReturnCount = computed(() =>
    list.value.filter(o => o.status === '-2').length
  )

  async function fetch(params: { page: number; size: number; status?: OrderStatus }) {
    loading.value = true
    try {
      const r = await listOrders(params)
      list.value = r.records
      total.value = r.total
    } finally {
      loading.value = false
    }
  }

  function updateLocalStatus(id: string, status: OrderStatus, extra?: Partial<Order>) {
    const item = list.value.find(o => o.id === id)
    if (item) {
      item.status = status
      if (extra) Object.assign(item, extra)
    }
  }

  return { list, total, loading, pendingReturnCount, fetch, updateLocalStatus }
})