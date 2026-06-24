var now = "2026-06-24 12:00:00";

var videos = {
  records: [
    { id: 1, title: "金毛幼犬的日常撒娇", desc: "每天早上都会叼着拖鞋来叫醒我，太治愈了", cover: "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800", url: "", likes: "2.3k", commentCount: 156, playCount: 12000, productId: 1, createTime: now, author: "暖窝小暖", avatar: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=100" },
    { id: 2, title: "英短蓝猫卖萌合集", desc: "包子脸的终极奥义就是装无辜", cover: "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800", url: "", likes: "5.1k", commentCount: 432, playCount: 35000, productId: 2, createTime: now, author: "猫咪日记", avatar: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=100" },
    { id: 3, title: "柯基小短腿赛跑大赛", desc: "腿虽短但跑起来谁也不服", cover: "https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=800", url: "", likes: "1.8k", commentCount: 89, playCount: 8500, productId: 3, createTime: now, author: "短腿俱乐部", avatar: "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=100" },
    { id: 4, title: "布偶猫的仙女日常", desc: "每天醒来看到这张脸，感觉世界都温柔了", cover: "https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=800", url: "", likes: "3.6k", commentCount: 278, playCount: 22000, productId: 4, createTime: now, author: "仙女猫本仙", avatar: "https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=100" },
    { id: 5, title: "仓鼠跑轮停不下来", desc: "跑了一小时还在跑，这体力我服", cover: "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800", url: "", likes: "980", commentCount: 45, playCount: 5000, productId: 5, createTime: now, author: "吱星日记", avatar: "https://images.unsplash.com/photo-1535268647677-300dbf3d78d1?w=100" },
    { id: 6, title: "哈士奇拆家实况", desc: "出门两小时回来沙发没了，微笑面对", cover: "https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=800", url: "", likes: "4.2k", commentCount: 567, playCount: 48000, productId: 6, createTime: now, author: "拆家办主任", avatar: "https://images.unsplash.com/photo-1504208434309-cb69f4fe52b0?w=100" }
  ],
  total: 6, size: 10, current: 1, pages: 1
};

var videoDetail = function(id) {
  var v = videos.records.find(function(x) { return x.id == id; }) || videos.records[0];
  return {
    id: v.id, title: v.title, description: v.desc, cover: v.cover, url: v.url || "https://example.com/video.mp4",
    likes: v.likes, commentCount: v.commentCount, playCount: v.playCount, productId: v.productId,
    createTime: v.createTime, author: v.author, avatar: v.avatar, userId: v.id + 100
  };
};

var comments = [
  { id: 1, text: "太可爱了吧！每天都想看", time: "2小时前", user: "小鱼干", avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100" },
  { id: 2, text: "同款在哪里买的呀？", time: "5小时前", user: "毛球控", avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100" },
  { id: 3, text: "哈哈哈哈太治愈了", time: "昨天", user: "铲屎官小王", avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100" }
];

var products = [
  { id: 1, productName: "金毛幼犬", productDesc: "纯种金毛，温顺可爱，已打疫苗，健康活泼。", price: "1888.00", stock: 3, mainImage: "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800", category: "dog", status: 1, storeId: 1, videoId: 1 },
  { id: 2, productName: "英短蓝猫", productDesc: "包子脸，性格温顺粘人，品相极佳。", price: "2580.00", stock: 2, mainImage: "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800", category: "cat", status: 1, storeId: 1, videoId: 2 },
  { id: 3, productName: "柯基犬", productDesc: "小短腿，活泼可爱，智商高。", price: "3200.00", stock: 1, mainImage: "https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=800", category: "dog", status: 1, storeId: 2, videoId: 3 },
  { id: 4, productName: "布偶猫", productDesc: "仙女猫本仙，颜值担当。", price: "4500.00", stock: 1, mainImage: "https://images.unsplash.com/photo-1513360371669-4adf3dd7dff8?w=800", category: "cat", status: 1, storeId: 2, videoId: 4 },
  { id: 5, productName: "仓鼠", productDesc: "迷你小可爱，容易饲养。", price: "38.00", stock: 20, mainImage: "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=800", category: "other", status: 1, storeId: 3, videoId: 5 },
  { id: 6, productName: "哈士奇", productDesc: "拆迁办主任，搞笑担当。", price: "2200.00", stock: 2, mainImage: "https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=800", category: "dog", status: 1, storeId: 3, videoId: 6 },
  { id: 7, productName: "橘猫", productDesc: "十橘九胖，干饭之王。", price: "200.00", stock: 5, mainImage: "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=800", category: "cat", status: 1, storeId: 4, videoId: null },
  { id: 8, productName: "兔子", productDesc: "长耳萌宠，温顺亲人。", price: "168.00", stock: 8, mainImage: "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=800", category: "other", status: 1, storeId: 4, videoId: null }
];

var stores = [
  { id: 1, storeName: "暖窝·思明店", storeLogo: "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200", storePhone: "13800000001", storeDesc: "猫咪狗狗小宠一站式", province: "福建省", city: "厦门市", district: "思明区", address: "中山路128号", status: 1, tags: ["猫咪", "狗狗", "小宠"] },
  { id: 2, storeName: "暖窝·湖里店", storeLogo: "https://images.unsplash.com/photo-1534361960057-19889db9621e?w=200", storePhone: "13800000002", storeDesc: "猫咪鸟类水族专门店", province: "福建省", city: "厦门市", district: "湖里区", address: "万达广场3F", status: 1, tags: ["猫咪", "鸟类", "水族"] },
  { id: 3, storeName: "暖窝·集美店", storeLogo: "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=200", storePhone: "13800000003", storeDesc: "水族小宠专门店", province: "福建省", city: "厦门市", district: "集美区", address: "石鼓路56号", status: 1, tags: ["水族", "小宠"] },
  { id: 4, storeName: "暖窝·翔安店", storeLogo: "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=200", storePhone: "13800000004", storeDesc: "鸟类兔子专门店", province: "福建省", city: "厦门市", district: "翔安区", address: "新兴街99号", status: 1, tags: ["鸟类", "兔子"] },
  { id: 5, storeName: "暖窝·同安店", storeLogo: "https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=200", storePhone: "13800000005", storeDesc: "小宠兔子专门店", province: "福建省", city: "厦门市", district: "同安区", address: "环城西路88号", status: 1, tags: ["小宠", "兔子"] }
];

var addresses = [
  { id: 1, userId: 1, receiverName: "张三", phone: "13800138000", province: "福建省", city: "厦门市", district: "集美区", detail: "理工路600号", defaulted: 1 },
  { id: 2, userId: 1, receiverName: "张三", phone: "13900139000", province: "福建省", city: "厦门市", district: "思明区", detail: "中山路200号", defaulted: 0 }
];

var orders = [
  { id: 1, orderNo: "PO20260624001", userId: 1, name: "金毛幼犬", price: "1888.00", orderStatus: "0", image: "https://images.unsplash.com/photo-1552053831-71594a27632d?w=200", totalAmount: "1888.00", payAmount: "1888.00", createTime: now },
  { id: 2, orderNo: "PO20260623002", userId: 1, name: "英短蓝猫", price: "2580.00", orderStatus: "1", image: "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=200", totalAmount: "2580.00", payAmount: "2580.00", createTime: "2026-06-23 10:00:00" },
  { id: 3, orderNo: "PO20260622003", userId: 1, name: "柯基犬", price: "3200.00", orderStatus: "2", image: "https://images.unsplash.com/photo-1612536057832-2ff7ead58194?w=200", totalAmount: "3200.00", payAmount: "3200.00", createTime: "2026-06-22 08:00:00" },
  { id: 4, orderNo: "PO20260620004", userId: 1, name: "仓鼠", price: "38.00", orderStatus: "4", image: "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=200", totalAmount: "38.00", payAmount: "38.00", createTime: "2026-06-20 14:00:00" }
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