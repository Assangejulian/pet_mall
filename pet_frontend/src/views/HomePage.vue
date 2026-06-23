<script setup lang="ts">
import "../styles/HomePage.css"
import { onMounted } from "vue"
import { usePetStore } from "../stores/pet"
import HomeFooter from "../components/layout/HomeFooter.vue"

const petStore = usePetStore()

onMounted(() => {
  petStore.fetchAll()
})
</script>

<template>
  <section class="home-prototype">
    <!-- Hero -->
    <div class="home-hero">
      <div class="home-hero-bg"></div>
      <div class="home-hero-inner">
        <div class="home-hero-copy">
          <p class="hero-tag">♡ 今天也有人在等你</p>
          <h1>每一个生命<br />都值得被<em>温柔接住</em></h1>
          <p>它们不是路过的风景，是会把日子慢慢照亮的家人。也许今天，就能遇见那个让你心软的小家伙。</p>
          <router-link to="/market" class="hero-btn">去遇见它 <span>→</span></router-link>
        </div>
      </div>
    </div>

    <!-- Works -->
    <section class="home-section">
      <div class="home-section-head">
        <div>
          <p class="eyebrow">等一个拥抱</p>
          <h2>它们都在等你</h2>
          <p>慢慢看，别着急。也许下一张，就是让你忍不住停下来的小朋友。</p>
        </div>
        <router-link to="/market">去看看谁在等你 →</router-link>
      </div>
      <div class="home-portfolio">
        <article
          v-for="(item, index) in petStore.works.slice(0, 8)"
          :key="item.name"
          class="home-port"
          :class="{ lift: index % 4 === 1, drop: index % 4 === 2, deep: index % 4 === 3 }"
        >
          <img :src="item.image" :alt="item.name" />
          <div class="home-port-overlay"></div>
          <span>{{ item.type }}</span>
          <div>
            <h3>{{ item.name }}</h3>
            <p>{{ item.detail }}</p>
          </div>
        </article>
      </div>
    </section>

    <!-- Transition -->
    <section class="home-transition">
      <div>
        <p>慢慢靠近</p>
        <h2>从第一次对视<br />到成为<em>一家人</em></h2>
        <span>每一次靠近，都是信任开始生长。让它住进你心里，也住进你的家。</span>
      </div>
      <div class="transition-actions">
        <router-link to="/market" class="transition-btn">
          <span class="action-icon market-icon"></span>
          <strong>去见见它</strong>
          <small>到店里轻轻见一面，说不定就心软了</small>
        </router-link>
        <router-link to="/community" class="transition-btn">
          <span class="action-icon community-icon"></span>
          <strong>逛逛日常</strong>
          <small>看看大家今天又被谁可爱到了</small>
        </router-link>
        <router-link to="/notes" class="transition-btn">
          <span class="action-icon note-icon"></span>
          <strong>学着照顾</strong>
          <small>新手别慌，慢慢来就会越来越懂它</small>
        </router-link>
        <button type="button" class="transition-btn">
          <span class="action-icon home-icon"></span>
          <strong>陪它回家</strong>
          <small>确认过眼神，再把它带进你的日常</small>
        </button>
      </div>
    </section>

    <!-- Stores -->
    <section class="home-section">
      <div class="home-section-head">
        <div>
          <p class="eyebrow">身边的温柔</p>
          <h2>离你最近的陪伴</h2>
          <p>每一家门店，都有小家伙在认真生活，也在偷偷等你来看看。</p>
        </div>
        <router-link to="/market">去门店看看 →</router-link>
      </div>
      <div class="home-stores carousel">
        <div class="store-track">
          <article v-for="(store, index) in [...petStore.stores, ...petStore.stores]" :key="store.name + index">
            <img :src="store.image" :alt="store.name" />
            <div>
              <h3>{{ store.name }}</h3>
              <p>{{ store.address }}</p>
              <span v-for="tag in store.tags" :key="tag">{{ tag }}</span>
            </div>
          </article>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <HomeFooter />
  </section>
</template>
