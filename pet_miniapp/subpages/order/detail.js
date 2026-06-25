Page({
  data: {
    order: {},
    sl: {
      "0": "待支付",
      "1": "已支付",
      "2": "已发货",
      "3": "已收货",
      "4": "已评价"
    },
    sbg: {
      "0": "#fdf2ed",
      "1": "#eef4f0",
      "2": "#eef4f0",
      "3": "#eef4f0"
    },
    sic: {
      "0": "○",
      "1": "◐",
      "2": "◑",
      "3": "●",
      "4": "★"
    }
  },

  onLoad(options) {
    const mock = {
      id: options.id,
      orderNo: "PO" + Date.now(),
      name: "金毛幼犬",
      price: "1888.00",
      status: "0",
      image: "/images/mock/golden.jpg",
      address: "厦门市集美区理工路600号"
    };
    this.setData({ order: mock });
  }
});
