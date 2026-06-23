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
import "../styles/global.css"
import { ref, computed } from "vue"
import { heroPets } from "../data/pets"
const search = ref("")
const showTopics = computed(() => search.value.trim().length > 0)
const communityNav = ["我的动态", "我的收藏", "我的宠物", "附近宠友"]
const myPets: [string, string][] = [["米糝", heroPets[0]], ["柚子", heroPets[1]], ["团子", heroPets[2]]]
const topics: [string, string][] = [["# 今天也被它可爱到", "1.2k 讨论"], ["# 它的小脾气好上头", "856 讨论"], ["# 每个生命都值得暖窝", "2.1k 讨论"], ["# 新手铲屎官报到", "634 讨论"]]
const tweets = [
  { user: "爱心人小暖", handle: "@xiaonuan", time: "2小时前", avatar: heroPets[0], text: "米糝今天第一次主动踭手。被它选中的那一秒，真的有点想原地截图保存。", image: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=85", stats: ["128", "24", "36"] as [string, string, string] },
  { user: "柴犬爱好者", handle: "@shiba_daily", time: "5小时前", avatar: heroPets[1], text: "柚子今天把拖鞋叼到门口等我。", image: "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=85", stats: ["256", "42", "77"] as [string, string, string] },
  { user: "团子饲养员", handle: "@hamster_tuan", time: "昨天", avatar: heroPets[2], text: "给团子做了新窝，它把脑袋埋进棉花里。", image: "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=85", stats: ["89", "15", "19"] as [string, string, string] }
]
const ranks: [string, string, string, string][] = [
  ["1", "布偶猫 米糝", "2,380 人气", heroPets[0]],
  ["2", "柴犬 柚子", "1,956 人气", heroPets[1]],
  ["3", "金丝熊 团子", "1,284 人气", heroPets[2]],
  ["4", "玄凤 蓝蓝", "967 人气", heroPets[3]],
  ["5", "垂耳兔 棉花", "823 人气", heroPets[4]]
]
</script>