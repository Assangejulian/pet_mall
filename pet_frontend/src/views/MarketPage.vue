<template>
  <section class="page market-page">
    <div class="page-head">
      <p class="eyebrow">温柔相遇</p>
      <div><h2>暖窝市集</h2><p>看看附近谁在等家，也看看大家今天又被谁治愈了。</p></div>
    </div>
    <div class="market-focus">
      <article class="featured-pet-card" v-if="petStore.featuredPet">
        <img :src="petStore.featuredPet.image" :alt="petStore.featuredPet.name" />
        <div class="featured-pet-copy">
          <span>{{ petStore.featuredPet.status }}</span>
          <h3>{{ petStore.featuredPet.name }}</h3>
          <p class="featured-tag">{{ petStore.featuredPet.tag }}</p>
          <p>{{ petStore.featuredPet.desc }}</p>
          <div class="feature-facts"><em v-for="fact in petStore.featuredPet.facts" :key="fact">{{ fact }}</em></div>
        </div>
      </article>
      <aside class="market-side">
        <article class="flagship-compact">
          <span>安心门店</span>
          <h3>暖窝 思明总店</h3>
          <p>300㎡ 温柔体验空间，20+ 小家伙正在等你来看看。</p>
          <small>思明区中山路128号 · 09:00-21:00</small>
        </article>
        <div class="city-map"><div class="map-inner">
          <p>厦门城市地图</p>
          <span class="route route-a"></span><span class="route route-b"></span>
          <span class="pin pin-main" style="left:34%;top:34%"><i>思明店</i></span>
          <span class="pin" style="left:62%;top:42%"><i>湖里店</i></span>
          <span class="pin" style="left:42%;top:66%"><i>集美店</i></span>
          <span class="pin" style="left:76%;top:70%"><i>翔安店</i></span>
        </div></div>
      </aside>
    </div>
    <section class="market-flow">
      <h3>今天也被它们可爱到</h3>
      <div class="instagram-flow">
        <article v-for="item in petStore.marketFeed" :key="item.title" class="insta-card" :class="{ tall: item.tall, wide: item.wide }">
          <img :src="item.image" :alt="item.title" />
          <div><span>{{ item.type }}</span><h4>{{ item.title }}</h4><p>{{ item.meta }}</p><small>{{ item.text }}</small></div>
        </article>
      </div>
    </section>
  </section>
</template>
<script setup lang="ts">
import "../styles/global.css"
import { onMounted } from "vue"
import { usePetStore } from "../stores/pet"
const petStore = usePetStore()
onMounted(() => { if (!petStore.works.length) petStore.fetchAll() })
</script>