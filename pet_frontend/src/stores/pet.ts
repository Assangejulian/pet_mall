import { defineStore } from "pinia"
import { ref } from "vue"
import type { Pet, FeaturedPet } from "../types/pet"
import type { Store, MarketFeedItem } from "../types/store"
import { getWorks, getFeaturedPet, getStores, getMarketFeed } from "../api/pet"

export const usePetStore = defineStore("pet", () => {
  const works = ref<Pet[]>([])
  const featuredPet = ref<FeaturedPet | null>(null)
  const stores = ref<Store[]>([])
  const marketFeed = ref<MarketFeedItem[]>([])

  async function fetchAll() {
    works.value = await getWorks()
    featuredPet.value = await getFeaturedPet()
    stores.value = await getStores()
    marketFeed.value = await getMarketFeed()
  }

  return { works, featuredPet, stores, marketFeed, fetchAll }
})
