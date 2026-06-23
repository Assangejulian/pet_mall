import http from "./index"
import type { Pet, FeaturedPet } from "../types/pet"
import type { Store, MarketFeedItem } from "../types/store"
import { works, featuredPet, homeStores, marketFeed } from "../data/pets"

export async function getWorks(): Promise<Pet[]> { return works }
export async function getFeaturedPet(): Promise<FeaturedPet> { return featuredPet }
export async function getStores(): Promise<Store[]> { return homeStores }
export async function getMarketFeed(): Promise<MarketFeedItem[]> { return marketFeed }