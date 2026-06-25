var now = "2026-06-24 12:00:00";

var images = {
  golden: "/images/mock/golden.jpg",
  blueCat: "/images/mock/blue-cat.jpg",
  corgi: "/images/mock/corgi.jpg",
  ragdoll: "/images/mock/ragdoll.jpg",
  catCover: "/images/mock/cat-cover.jpg",
  dogAvatar: "/images/mock/dog-avatar.jpg",
  catAvatar: "/images/mock/cat-avatar.jpg"
};

var videos = {
  records: [
    { id: 1, title: "金毛幼犬的日常撒娇", desc: "每天早上都会叼着拖鞋来叫醒我，太治愈了", cover: images.golden, url: "", likes: "2.3k", commentCount: 156, playCount: 12000, productId: 1, createTime: now, author: "暖窝小暖", avatar: images.dogAvatar },
    { id: 2, title: "英短蓝猫卖萌合集", desc: "包子脸的终极奥义就是装无辜", cover: images.blueCat, url: "", likes: "5.1k", commentCount: 432, playCount: 35000, productId: 2, createTime: now, author: "猫咪日记", avatar: images.catAvatar },
    { id: 3, title: "柯基小短腿赛跑大赛", desc: "腿虽短但跑起来谁也不服", cover: images.corgi, url: "", likes: "1.8k", commentCount: 89, playCount: 8500, productId: 3, createTime: now, author: "短腿俱乐部", avatar: images.dogAvatar },
    { id: 4, title: "布偶猫的仙女日常", desc: "每天醒来看到这张脸，感觉世界都温柔了", cover: images.ragdoll, url: "", likes: "3.6k", commentCount: 278, playCount: 22000, productId: 4, createTime: now, author: "仙女猫本仙", avatar: images.catAvatar },
    { id: 5, title: "幼宠用品清单", desc: "笼具、食盆、牵引和清洁用品先准备基础款", cover: images.catCover, url: "", likes: "980", commentCount: 45, playCount: 5000, productId: 5, createTime: now, author: "吱星日记", avatar: images.catAvatar }
  ],
  total: 5,
  size: 10,
  current: 1,
  pages: 1
};

var videoDetail = function(id) {
  var v = videos.records.find(function(x) { return x.id == id; }) || videos.records[0];
  return {
    id: v.id,
    title: v.title,
    description: v.desc,
    cover: v.cover,
    url: v.url,
    likes: v.likes,
    commentCount: v.commentCount,
    playCount: v.playCount,
    productId: v.productId,
    createTime: v.createTime,
    author: v.author,
    avatar: v.avatar,
    userId: v.id + 100
  };
};

var comments = [
  { id: 1, text: "太可爱了吧！每天都想看", time: "2小时前", user: "小鱼干", avatar: images.catAvatar },
  { id: 2, text: "同款在哪里买的呀？", time: "5小时前", user: "毛球控", avatar: images.dogAvatar },
  { id: 3, text: "哈哈哈哈太治愈了", time: "昨天", user: "铲屎官小王", avatar: images.catAvatar }
];

var products = [
  { id: 1, productName: "金毛幼犬", productDesc: "纯种金毛，温顺可爱，已打疫苗，健康活泼。", price: "1888.00", stock: 3, mainImage: images.golden, category: "dog", status: 1, storeId: 1, videoId: 1 },
  { id: 2, productName: "英短蓝猫", productDesc: "包子脸，性格温顺粘人，品相极佳。", price: "2580.00", stock: 2, mainImage: images.blueCat, category: "cat", status: 1, storeId: 1, videoId: 2 },
  { id: 3, productName: "柯基犬", productDesc: "小短腿，活泼可爱，智商高。", price: "3200.00", stock: 1, mainImage: images.corgi, category: "dog", status: 1, storeId: 2, videoId: 3 },
  { id: 4, productName: "布偶猫", productDesc: "仙女猫本仙，颜值担当。", price: "4500.00", stock: 1, mainImage: images.ragdoll, category: "cat", status: 1, storeId: 2, videoId: 4 },
  { id: 5, productName: "低敏主粮", productDesc: "适合肠胃敏感宠物的日常主粮。", price: "168.00", stock: 20, mainImage: images.catCover, category: "food", status: 1, storeId: 1, videoId: 5 }
];

var stores = [
  { id: 1, storeName: "暖窝·思明店", storeLogo: images.catCover, storePhone: "13800000001", storeDesc: "猫咪狗狗小宠一站式", province: "福建省", city: "厦门市", district: "思明区", address: "中山路128号", status: 1, tags: ["猫咪", "狗狗", "小宠"] },
  { id: 2, storeName: "暖窝·湖里店", storeLogo: images.golden, storePhone: "13800000002", storeDesc: "猫咪鸟类水族专门店", province: "福建省", city: "厦门市", district: "湖里区", address: "万达广场3F", status: 1, tags: ["猫咪", "鸟类", "水族"] },
  { id: 3, storeName: "暖窝·集美店", storeLogo: images.blueCat, storePhone: "13800000003", storeDesc: "水族小宠专门店", province: "福建省", city: "厦门市", district: "集美区", address: "石鼓路56号", status: 1, tags: ["水族", "小宠"] }
];

var addresses = [
  { id: 1, userId: 1, receiverName: "张三", phone: "13800138000", province: "福建省", city: "厦门市", district: "集美区", detail: "理工路600号", defaulted: 1 },
  { id: 2, userId: 1, receiverName: "张三", phone: "13900139000", province: "福建省", city: "厦门市", district: "思明区", detail: "中山路200号", defaulted: 0 }
];

var orders = [
  { id: 1, orderNo: "PO20260624001", userId: 1, name: "金毛幼犬", price: "1888.00", orderStatus: "0", image: images.golden, totalAmount: "1888.00", payAmount: "1888.00", createTime: now },
  { id: 2, orderNo: "PO20260623002", userId: 1, name: "英短蓝猫", price: "2580.00", orderStatus: "1", image: images.blueCat, totalAmount: "2580.00", payAmount: "2580.00", createTime: "2026-06-23 10:00:00" },
  { id: 3, orderNo: "PO20260622003", userId: 1, name: "柯基犬", price: "3200.00", orderStatus: "2", image: images.corgi, totalAmount: "3200.00", payAmount: "3200.00", createTime: "2026-06-22 08:00:00" },
  { id: 4, orderNo: "PO20260620004", userId: 1, name: "布偶猫", price: "4500.00", orderStatus: "4", image: images.ragdoll, totalAmount: "4500.00", payAmount: "4500.00", createTime: "2026-06-20 14:00:00" }
];

var cartItems = [];

module.exports = {
  videos: videos,
  videoDetail: videoDetail,
  comments: comments,
  products: products,
  stores: stores,
  addresses: addresses,
  orders: orders,
  cartItems: cartItems
};
