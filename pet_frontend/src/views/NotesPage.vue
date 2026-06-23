<template>
  <section class="page notes-page">
    <div class="notes-page-inner">
      <div class="section-head">
        <div class="eyebrow">陪伴笔记</div>
        <div class="notes-head-row"><div>
          <h2>陪你慢慢养</h2>
          <p>从第一次准备，到每天被它的小动作治愈，我们一起把爱落到细节里。</p>
        </div>
        <div class="notes-search"><input placeholder="搜搜看，怎么更好地陪它..." /></div>
      </div></div>
      <div class="note-tabs"><button v-for="tab in noteTabs" :key="tab" class="note-tab" type="button" :class="{ active: tab === '全部' }">{{ tab }}</button></div>
      <div class="notes-layout">
        <article class="note-feature" v-if="notes.length">
          <span class="nf-label">★ {{ notes[0].label }}</span>
          <div class="nf-img" :style="{ backgroundImage: 'url(' + notes[0].image + ')' }"></div>
          <h3>{{ notes[0].title }}</h3>
          <p>{{ notes[0].desc }}</p>
          <small>{{ notes[0].meta }}</small>
        </article>
        <div class="note-list">
          <article v-for="note in notes.slice(1)" :key="note.title" class="note-item">
            <h4>{{ note.title }}</h4>
            <p>{{ note.desc }}</p>
            <div class="nm"><span class="nf-label">{{ note.label }}</span><span>{{ note.meta }}</span></div>
          </article>
        </div>
      </div>
      <div class="ai-bar">
        <div class="ai-bar-left">
          <div class="ai-bar-av"><span>AI</span></div>
          <div><strong>暖窝陪伴助手</strong><span class="ai-bar-status">在线</span></div>
        </div>
        <div class="ai-bar-input">
          <input type="text" placeholder="关于照顾它的小问题，都可以慢慢问..." />
          <button type="button">发送</button>
        </div>
      </div>
    </div>
  </section>
</template>
<script setup lang="ts">
import { noteTabs, notes } from "../data/community"
</script>