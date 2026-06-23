import { heroPets } from "./pets"

export const communityNav = ["我的动态", "我的收藏", "我的宠物", "附近宠友"]

export const myPets = [
  ["糯米", heroPets[0]],
  ["柚子", heroPets[1]],
  ["团子", heroPets[2]]
]

export const topics = [
  ["# 今天也被它可爱到", "1.2k 讨论"],
  ["# 它的小脾气好上头", "856 讨论"],
  ["# 新手陪伴日记", "723 讨论"],
  ["# 随手拍到的治愈瞬间", "512 讨论"],
  ["# 给等待一个家", "498 讨论"]
]

export const tweets = [
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
]

export const ranks = [
  ["1", "布偶猫 糯米", "2,380 人气", heroPets[0]],
  ["2", "柴犬 柚子", "1,956 人气", heroPets[1]],
  ["3", "金丝熊 团子", "1,284 人气", heroPets[2]],
  ["4", "玄凤 蓝蓝", "967 人气", heroPets[3]],
  ["5", "垂耳兔 棉花", "823 人气", heroPets[4]]
]

export const noteTabs = ["全部", "猫咪", "狗狗", "小宠", "鸟类", "水族"]

export const notes = [
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
]
