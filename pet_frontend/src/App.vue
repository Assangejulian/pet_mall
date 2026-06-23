<script setup>
import { computed, onMounted, ref, watch } from "vue";

const activePage = ref("home");
const communitySearch = ref("");

const pages = [
  { id: "home", label: "作品" },
  { id: "market", label: "市集" },
  { id: "community", label: "社区" },
  { id: "notes", label: "笔记" }
];

const heroPets = [
  "https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?auto=format&fit=crop&w=900&q=85",
  "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=85",
  "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=85",
  "https://images.unsplash.com/photo-1552728089-57bdde30beb3?auto=format&fit=crop&w=900&q=85",
  "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=900&q=85"
];

const featuredPet = {
  name: "布偶猫 糯米",
  tag: "猫咪 · 2个月 · 已驱虫",
  image: "https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?auto=format&fit=crop&w=1200&q=85",
  desc: "亲人、安静，喜欢把呼噜声轻轻放在你身边。想被一只小猫治愈的话，可以先来见见它。",
  status: "正在等你来看看",
  facts: ["可到店轻轻见一面", "基础体检已完成", "适合新手陪伴"]
};

const works = [
  {
    name: "布偶猫·糯米",
    type: "猫咪",
    detail: "Ragdoll · 2个月 · 已驱虫",
    status: "等你来抱抱",
    image: heroPets[0]
  },
  {
    name: "柴犬·柚子",
    type: "狗狗",
    detail: "Shiba Inu · 3个月 · 疫苗齐全",
    status: "等你来抱抱",
    image: heroPets[1]
  },
  {
    name: "金丝熊·团子",
    type: "小宠",
    detail: "Syrian Hamster · 1个月",
    status: "慢慢等你",
    image: heroPets[2]
  },
  {
    name: "玄凤鹦鹉·蓝蓝",
    type: "鸟类",
    detail: "Cockatiel · 2个月",
    status: "慢慢等你",
    image: heroPets[3]
  },
  {
    name: "龙鱼·赤焰",
    type: "水族",
    detail: "亚洲红龙 · 35cm",
    status: "慢慢等你",
    image: "https://images.unsplash.com/photo-1520366498724-709889c0c685?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "垂耳兔·棉花",
    type: "小宠",
    detail: "Holland Lop · 1个月",
    status: "慢慢等你",
    image: heroPets[4]
  },
  {
    name: "暹罗猫·奶茶",
    type: "猫咪",
    detail: "Siamese · 2个月",
    status: "慢慢等你",
    image: "https://images.unsplash.com/photo-1495360010541-f48722b34f7d?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "柯基·花生",
    type: "狗狗",
    detail: "Pembroke · 2个月",
    status: "慢慢等你",
    image: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=900&q=85"
  },
  {
    name: "英短蓝猫·灰灰",
    type: "猫咪",
    detail: "British Shorthair · 3个月",
    status: "慢慢等你",
    image: "https://images.unsplash.com/photo-1574231164645-d6f0e8553590?auto=format&fit=crop&w=900&q=85"
  }
];

const homeStores = [
  {
    name: "暖窝·思明店",
    address: "思明区中山路128号",
    image: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=85",
    tags: ["猫咪", "狗狗", "小宠"]
  },
  {
    name: "暖窝·湖里店",
    address: "湖里万达广场3F",
    image: "https://images.unsplash.com/photo-1534361960057-19889db9621e?auto=format&fit=crop&w=800&q=85",
    tags: ["猫咪", "鸟类", "水族"]
  },
  {
    name: "暖窝·集美店",
    address: "集美区石鼓路56号",
    image: "https://images.unsplash.com/photo-1574158622682-e40e69881006?auto=format&fit=crop&w=800&q=85",
    tags: ["水族", "小宠"]
  },
  {
    name: "暖窝·翔安店",
    address: "翔安区新兴街99号",
    image: "https://images.unsplash.com/photo-1552728089-57bdde30beb3?auto=format&fit=crop&w=800&q=85",
    tags: ["鸟类"]
  },
  {
    name: "暖窝·同安店",
    address: "同安区环城西路88号",
    image: "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=800&q=85",
    tags: ["小宠", "兔子"]
  }
];

const marketFeed = [
  {
    type: "门店",
    title: "暖窝 思明店",
    meta: "旗舰 · ★ 4.8 · 1.2km",
    image: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=85",
    text: "这里有安静的角落，也有一群正在偷偷期待你的小家伙。",
    tall: true
  },
  {
    type: "今日推荐",
    title: "柴犬 柚子",
    meta: "狗狗 · 3个月 · 疫苗齐全",
    image: "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=800&q=85",
    text: "尾巴摇得很认真，像是在说：今天要不要一起出门？"
  },
  {
    type: "社区打卡",
    title: "湖里店猫咪午睡角",
    meta: "5 小时前 · 256 喜欢",
    image: "https://images.unsplash.com/photo-1534361960057-19889db9621e?auto=format&fit=crop&w=800&q=85",
    text: "午睡角今日营业中，路过的人都被可爱到慢了半拍。",
    wide: true
  },
  {
    type: "小宠",
    title: "垂耳兔 棉花",
    meta: "1个月 · 性格稳定",
    image: "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=800&q=85",
    text: "软乎乎的一小只，适合从一个安静的小窝开始陪伴。"
  },
  {
    type: "水族",
    title: "集美店开缸日",
    meta: "新店 · ★ 4.5",
    image: "https://images.unsplash.com/photo-1520366498724-709889c0c685?auto=format&fit=crop&w=800&q=85",
    text: "一缸小小的水，也能让家里多一点会发光的安静。",
    tall: true
  },
  {
    type: "鸟类",
    title: "玄凤 蓝蓝",
    meta: "翔安店 · 可预约",
    image: "https://images.unsplash.com/photo-1552728089-57bdde30beb3?auto=format&fit=crop&w=800&q=85",
    text: "会回应口哨的小朋友，正在等一个愿意和它聊天的人。"
  },
  {
    type: "猫咪",
    title: "英短 灰灰",
    meta: "集美店 · 3个月",
    image: "https://images.unsplash.com/photo-1574231164645-d6f0e8553590?auto=format&fit=crop&w=800&q=85",
    text: "慢热但很甜，熟了以后会把小脑袋放心交给你。"
  },
  {
    type: "狗狗",
    title: "柯基 花生",
    meta: "思明店 · 疫苗齐全",
    image: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=85",
    text: "短腿快乐制造机，谁看了不想陪它多玩五分钟？",
    wide: true
  }
];

const communityNav = ["我的动态", "我的收藏", "我的宠物", "附近宠友"];
const myPets = [
  ["糯米", heroPets[0]],
  ["柚子", heroPets[1]],
  ["团子", heroPets[2]]
];

const topics = [
  ["# 今天也被它可爱到", "1.2k 讨论"],
  ["# 它的小脾气好上头", "856 讨论"],
  ["# 新手陪伴日记", "723 讨论"],
  ["# 随手拍到的治愈瞬间", "512 讨论"],
  ["# 给等待一个家", "498 讨论"]
];

const tweets = [
  {
    user: "爱心人小暖",
    handle: "@xiaonuan",
    time: "2小时前",
    avatar: heroPets[0],
    text: "糯米今天第一次主动蹭手。被它选中的那一秒，真的有点想原地截图保存。",
    image: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=900&q=85",
    stats: ["128", "24", "36"]
  },
  {
    user: "柴犬爱好者",
    handle: "@shiba_daily",
    time: "5小时前",
    avatar: heroPets[1],
    text: "柚子今天把拖鞋叼到门口等我。虽然拖鞋很惨，但回家有人迎接这件事，太加分了。",
    image: "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=85",
    stats: ["256", "42", "77"]
  },
  {
    user: "团子饲养员",
    handle: "@hamster_tuan",
    time: "昨天",
    avatar: heroPets[2],
    text: "给团子做了新窝，它把脑袋埋进棉花里。小小一团，也能把人可爱到没脾气。",
    image: "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=900&q=85",
    stats: ["89", "15", "19"]
  }
];

const ranks = [
  ["1", "布偶猫 糯米", "2,380 人气", heroPets[0]],
  ["2", "柴犬 柚子", "1,956 人气", heroPets[1]],
  ["3", "金丝熊 团子", "1,284 人气", heroPets[2]],
  ["4", "玄凤 蓝蓝", "967 人气", heroPets[3]],
  ["5", "垂耳兔 棉花", "823 人气", heroPets[4]]
];

const noteTabs = ["全部", "猫咪", "狗狗", "小宠", "鸟类", "水族"];
const notes = [
  {
    title: "第一次接它回家 · 准备清单",
    label: "精选",
    image: heroPets[0],
    desc: "从猫砂盆到小毯子，把它回家的第一晚准备得安心一点。",
    meta: "♡ 1,280 · 56 评论 · 5 分钟"
  },
  {
    title: "陪柴犬慢慢长大 · 训练指南",
    label: "狗狗",
    desc: "理解它的小固执，用温柔但稳定的方式陪它学会规则。",
    meta: "♡ 956 · 8 分钟"
  },
  {
    title: "给小鱼一个安心的家 · 开缸指南",
    label: "水族",
    desc: "从养水、选鱼到日常维护，慢慢搭起一个清亮的小世界。",
    meta: "♡ 723 · 10 分钟"
  },
  {
    title: "看懂猫咪的小信号 · 健康照护",
    label: "猫咪",
    desc: "学会观察它的变化，把关心放在每一次日常里。",
    meta: "♡ 2,103 · 6 分钟"
  },
  {
    title: "金丝熊的小日子 · 饲养手册",
    label: "小宠",
    desc: "笼子、食物和小窝都准备好，让它安心过自己的小日子。",
    meta: "♡ 534 · 4 分钟"
  }
];

const showTopics = computed(() => communitySearch.value.trim().length > 0);

function goPage(page) {
  activePage.value = page;
  if (window.location.hash !== `#${page}`) {
    window.location.hash = page;
  }
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function syncPageFromHash() {
  const hashPage = window.location.hash.replace("#", "");
  if (pages.some((page) => page.id === hashPage)) {
    activePage.value = hashPage;
  }
}

onMounted(() => {
  syncPageFromHash();
  window.addEventListener("hashchange", syncPageFromHash);
});

watch(activePage, (page) => {
  document.title = `PetNest 暖窝 · ${pages.find((item) => item.id === page)?.label ?? "作品"}`;
});
</script>

<template>
  <header class="topbar">
    <button class="brand" type="button" @click="goPage('home')">
      <span class="brand-mark">♡</span>
      <span>Pet<span>Nest</span></span>
    </button>

    <nav class="tabs" aria-label="页面导航">
      <button
        v-for="page in pages"
        :key="page.id"
        type="button"
        :class="{ active: activePage === page.id }"
        @click="goPage(page.id)"
      >
        {{ page.label }}
      </button>
    </nav>

    <div class="nav-actions">
      <button class="cart-button" type="button" aria-label="购物车">
        <span class="cart-glyph">▢</span>
        <span class="cart-badge">3</span>
      </button>
      <div class="user-menu">
        <button class="user-trigger" type="button">
          <span class="user-avatar">人</span>
          <span>我的暖窝</span>
          <small>▾</small>
        </button>
        <div class="user-dropdown">
          <button type="button">我的主页</button>
          <button type="button">我的毛孩子</button>
          <button type="button">我的订单</button>
          <button type="button">我的收藏</button>
          <hr />
          <button type="button">账号设置</button>
        </div>
      </div>
    </div>
  </header>

  <main>
    <section v-if="activePage === 'home'" class="home-prototype">
      <div class="home-hero">
        <div class="home-hero-bg"></div>
        <div class="home-hero-inner">
          <div class="home-hero-copy">
            <p class="hero-tag">♡ 今天也有人在等你</p>
            <h1>每一个生命<br />都值得被<em>温柔接住</em></h1>
            <p>它们不是路过的风景，是会把日子慢慢照亮的家人。也许今天，就能遇见那个让你心软的小家伙。</p>
            <button class="hero-btn" type="button" @click="goPage('market')">去遇见它 <span>→</span></button>
          </div>
        </div>
      </div>

      <section class="home-section">
        <div class="home-section-head">
          <div>
            <p class="eyebrow">等一个拥抱</p>
            <h2>它们都在等你</h2>
            <p>慢慢看，别着急。也许下一张，就是让你忍不住停下来的小朋友。</p>
          </div>
          <button type="button" @click="goPage('market')">去看看谁在等你 →</button>
        </div>
        <div class="home-portfolio">
          <article
            v-for="(item, index) in works.slice(0, 8)"
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

      <section class="home-transition">
        <div>
          <p>慢慢靠近</p>
          <h2>从第一次对视<br />到成为<em>一家人</em></h2>
          <span>每一次靠近，都是信任开始生长。让它住进你心里，也住进你的家。</span>
        </div>
        <div class="transition-actions">
          <button type="button" @click="goPage('market')">
            <span class="action-icon market-icon" aria-hidden="true"></span>
            <strong>去见见它</strong>
            <small>到店里轻轻见一面，说不定就心软了</small>
          </button>
          <button type="button" @click="goPage('community')">
            <span class="action-icon community-icon" aria-hidden="true"></span>
            <strong>逛逛日常</strong>
            <small>看看大家今天又被谁可爱到了</small>
          </button>
          <button type="button" @click="goPage('notes')">
            <span class="action-icon note-icon" aria-hidden="true"></span>
            <strong>学着照顾</strong>
            <small>新手别慌，慢慢来就会越来越懂它</small>
          </button>
          <button type="button">
            <span class="action-icon home-icon" aria-hidden="true"></span>
            <strong>陪它回家</strong>
            <small>确认过眼神，再把它带进你的日常</small>
          </button>
        </div>
      </section>

      <section class="home-section">
        <div class="home-section-head">
          <div>
            <p class="eyebrow">身边的温柔</p>
            <h2>离你最近的陪伴</h2>
            <p>每一家门店，都有小家伙在认真生活，也在偷偷等你来看看。</p>
          </div>
          <button type="button" @click="goPage('market')">去门店看看 →</button>
        </div>
        <div class="home-stores carousel">
          <div class="store-track">
            <article v-for="(store, index) in [...homeStores, ...homeStores]" :key="`${store.name}-${index}`">
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

      <footer class="home-footer">
        <div class="footer-brand">
          <h3><span>♥</span> Pet<em>Nest</em></h3>
          <p>让每一次心动，都能慢慢变成陪伴。</p>
        </div>
        <div class="footer-dot"></div>
        <nav>
          <strong>浏览</strong>
          <button type="button" @click="goPage('home')">首页</button>
          <button type="button" @click="goPage('market')">去遇见它</button>
          <button type="button" @click="goPage('community')">社区</button>
          <button type="button" @click="goPage('notes')">百科</button>
        </nav>
        <nav>
          <strong>帮助</strong>
          <button type="button">陪伴指南</button>
          <button type="button">健康照护</button>
          <button type="button">常见问题</button>
          <button type="button">联系我们</button>
        </nav>
        <nav>
          <strong>关于</strong>
          <button type="button">我们的初心</button>
          <button type="button">爱心合作</button>
          <button type="button">加入我们</button>
          <button type="button">隐私政策</button>
        </nav>
      </footer>
    </section>

    <section v-if="activePage === 'market'" class="page market-page">
      <div class="page-head">
        <p class="eyebrow">温柔相遇</p>
        <div>
          <h2>暖窝市集</h2>
          <p>看看附近谁在等家，也看看大家今天又被谁治愈了。</p>
        </div>
      </div>

      <div class="market-focus">
        <article class="featured-pet-card">
          <img :src="featuredPet.image" :alt="featuredPet.name" />
          <div class="featured-pet-copy">
            <span>{{ featuredPet.status }}</span>
            <h3>{{ featuredPet.name }}</h3>
            <p class="featured-tag">{{ featuredPet.tag }}</p>
            <p>{{ featuredPet.desc }}</p>
            <div class="feature-facts">
              <em v-for="fact in featuredPet.facts" :key="fact">{{ fact }}</em>
            </div>
          </div>
        </article>

        <aside class="market-side">
          <article class="flagship-compact">
            <span>安心门店</span>
            <h3>暖窝 思明总店</h3>
            <p>300㎡ 温柔体验空间，60+ 小家伙正在等你来看看。</p>
            <small>思明区中山路128号 · 09:00-21:00 · 0592-1234567</small>
          </article>
          <div class="city-map">
            <div class="map-inner">
              <p>厦门城市地图</p>
              <span class="route route-a"></span>
              <span class="route route-b"></span>
              <span class="pin pin-main" style="left: 34%; top: 34%"><i>思明店</i></span>
              <span class="pin" style="left: 62%; top: 42%"><i>湖里店</i></span>
              <span class="pin" style="left: 42%; top: 66%"><i>集美店</i></span>
              <span class="pin" style="left: 76%; top: 70%"><i>翔安店</i></span>
            </div>
          </div>
        </aside>
      </div>

      <section class="market-flow">
        <h3>今天也被它们可爱到</h3>
        <div class="instagram-flow">
          <article
            v-for="item in marketFeed"
            :key="item.title"
            class="insta-card"
            :class="{ tall: item.tall, wide: item.wide }"
          >
            <img :src="item.image" :alt="item.title" />
            <div>
              <span>{{ item.type }}</span>
              <h4>{{ item.title }}</h4>
              <p>{{ item.meta }}</p>
              <small>{{ item.text }}</small>
            </div>
          </article>
        </div>
      </section>
    </section>

    <section v-if="activePage === 'community'" class="page community-page">
      <div class="page-head community-head">
        <p class="eyebrow">陪伴日常</p>
        <div>
          <h2>暖暖社区</h2>
          <p>这里有认真陪伴，也有每天被小家伙拿捏的真实日常。</p>
        </div>
      </div>

      <div class="community-search">
        <input v-model="communitySearch" type="search" placeholder="搜搜它的日常、话题和可爱瞬间..." />
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
            <button
              v-for="(item, index) in communityNav"
              :key="item"
              type="button"
              :class="{ active: index === 0 }"
            >
              {{ item }}
            </button>
          </div>
          <div class="my-pets">
            <h3>我的小家人</h3>
            <div>
              <span v-for="pet in myPets" :key="pet[0]">
                <img :src="pet[1]" :alt="pet[0]" />
                {{ pet[0] }}
              </span>
            </div>
          </div>
        </aside>

        <section class="feed-area">
          <div class="tweet-flow">
            <article v-for="tweet in tweets" :key="tweet.handle" class="tweet">
              <img class="tweet-avatar" :src="tweet.avatar" :alt="tweet.user" />
              <div class="tweet-body">
                <div class="tweet-meta">
                  <strong>{{ tweet.user }}</strong>
                  <span>{{ tweet.handle }} · {{ tweet.time }}</span>
                </div>
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
            <button v-for="topic in topics" :key="topic[0]" type="button">
              <span>{{ topic[0] }}</span>
              <small>{{ topic[1] }}</small>
            </button>
          </section>

          <section class="rank-panel">
            <h3>今日人气小可爱</h3>
            <article v-for="item in ranks" :key="item[0]" class="rank-item">
              <span>{{ item[0] }}</span>
              <img :src="item[3]" :alt="item[1]" />
              <div>
                <h4>{{ item[1] }}</h4>
                <p>{{ item[2] }}</p>
              </div>
            </article>
          </section>
        </aside>
      </div>
    </section>

    <section v-if="activePage === 'notes'" class="page notes-page">
      <div class="notes-page-inner">
        <div class="section-head">
          <div class="eyebrow">陪伴笔记</div>
          <div class="notes-head-row">
            <div>
              <h2>陪你慢慢养</h2>
              <p>从第一次准备，到每天被它的小动作治愈，我们一起把爱落到细节里。</p>
            </div>
            <div class="notes-search">
              <input placeholder="搜搜看，怎么更好地陪它..." />
            </div>
          </div>
        </div>

        <div class="note-tabs">
          <button v-for="tab in noteTabs" :key="tab" class="note-tab" type="button" :class="{ active: tab === '全部' }">
            {{ tab }}
          </button>
        </div>

        <div class="notes-layout">
          <article class="note-feature">
            <span class="nf-label">★ {{ notes[0].label }}</span>
            <div class="nf-img" :style="{ backgroundImage: `url(${notes[0].image})` }"></div>
            <h3>{{ notes[0].title }}</h3>
            <p>{{ notes[0].desc }}</p>
            <small>{{ notes[0].meta }}</small>
          </article>
          <div class="note-list">
            <article v-for="note in notes.slice(1)" :key="note.title" class="note-item">
              <h4>{{ note.title }}</h4>
              <p>{{ note.desc }}</p>
              <div class="nm">
                <span class="nf-label">{{ note.label }}</span>
                <span>{{ note.meta }}</span>
              </div>
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
  </main>
</template>
