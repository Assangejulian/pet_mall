import { defineStore } from "pinia"
import { ref, computed } from "vue"

interface CartItem { id: string; name: string; price: number; quantity: number }

export const useCartStore = defineStore("cart", () => {
  const items = ref<CartItem[]>([])
  const count = computed(() => items.value.length)

  function add(item: CartItem) { items.value.push(item) }
  function remove(id: string) { items.value = items.value.filter(i => i.id !== id) }
  function clear() { items.value = [] }

  return { items, count, add, remove, clear }
})
