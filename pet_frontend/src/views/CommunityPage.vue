<template>
  <section class="page community-page">
    <div class="page-head community-head">
      <p class="eyebrow">陪伴日常</p>
      <div>
        <h2>暖暖社区</h2>
        <p>这里有认真陪伴，也有每天被小家伙拿捏的真实日常。</p>
      </div>
    </div>
    <div class="community-search">
      <input v-model="search" type="search" placeholder="搜搜它的日常、话题和可爱瞬间..." />
      <button type="button">搜索</button>
    </div>
    <div class="community-layout">
      <aside class="comm-left">
        <div class="profile-card">
          <img :src="heroPets[0]" alt="爱心人小暖" />
          <h3>爱心人小暖</h3>
          <p>厦门 · 家有 3 只毛孩子</p>
          <div class="pc-stats">
            <span><strong>28</strong>动态</span>
            <span><strong>156</strong>粉丝</span>
            <span><strong>42</strong>关注</span>
          </div>
          <button type="button">整理我的小窝</button>
        </div>
        <div class="my-list">
          <button v-for="(item, index) in communityNav" :key="item" type="button" :class="{ active: index === 0 }">{{ item }}</button>
        </div>
        <div class="my-pets">
          <h3>我的小家人</h3>
          <div><span v-for="pet in myPets" :key="pet[0]"><img :src="pet[1]" :alt="pet[0]" /> {{ pet[0] }}</span></div>
        </div>
      </aside>
      <section class="feed-area">
        <div class="tweet-flow">
          <article v-for="tweet in tweets" :key="tweet.handle" class="tweet">
            <img class="tweet-avatar" :src="tweet.avatar" :alt="tweet.user" />
            <div class="tweet-body">
              <div class="tweet-meta"><strong>{{ tweet.user }}</strong><span>{{ tweet.handle }} · {{ tweet.time }}</span></div>
              <p>{{ tweet.text }}</p>
              <img class="tweet-image" :src="tweet.image" alt="" />
              <div class="tweet-actions">
                <span><b>↩</b> 回复 {{ tweet.stats[1] }}</span>
                <span><b>⟳</b> 转发 {{ tweet.stats[2] }}</span>
                <span><b>♡</b> 喜欢 {{ tweet.stats[0] }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>
      <aside class="twitter-side">
        <section v-if="showTopics" class="topics-panel">
          <h3>大家正在聊</h3>
          <button v-for="topic in topics" :key="topic[0]" type="button"><span>{{ topic[0] }}</span><small>{{ topic[1] }}</small></button>
        </section>
        <section class="rank-panel">
          <h3>今日人气小可爱</h3>
          <article v-for="item in ranks" :key="item[0]" class="rank-item">
            <span>{{ item[0] }}</span>
            <img :src="item[3]" :alt="item[1]" />
            <div><h4>{{ item[1] }}</h4><p>{{ item[2] }}</p></div>
          </article>
        </section>
      </aside>
    </div>
  </section>
</template>
<script setup lang="ts">
import "../styles/CommunityPage.css"
import { ref, computed } from "vue"
import { heroPets } from "../data/pets"
import { communityNav, myPets, topics, tweets, ranks } from "../data/community"
const search = ref("")
const showTopics = computed(() => search.value.trim().length > 0)
</script>
