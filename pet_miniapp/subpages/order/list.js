Page({
  data: {
    orders: [],
    active: "",
    sl: {
      "0": "待支付",
      "1": "已支付",
      "2": "已发货",
      "3": "已收货",
      "4": "已评价",
      "-1": "已取消",
      "-2": "退单中"
    },
    sc: {
      "0": "#e65100",
      "1": "#1565c0",
      "2": "#547b68",
      "3": "#547b68",
      "4": "#8f7366",
      "-1": "#8f7366",
      "-2": "#c0392b"
    },
    tabList: [
      { l: "全部", v: "" },
      { l: "待支付", v: "0" },
      { l: "已支付", v: "1" },
      { l: "已发货", v: "2" },
      { l: "已收货", v: "3" },
      { l: "已评价", v: "4" }
    ]
  },

  onLoad(options) {
    if (options.status) this.setData({ active: options.status });
  },

  onShow() {
    const mock = [
      {
        id: "1",
        orderNo: "PO20260624001",
        name: "金毛幼犬",
        price: "1888.00",
        status: "0",
        image: "/images/mock/golden.jpg"
      },
      {
        id: "2",
        orderNo: "PO20260623002",
        name: "英短蓝猫",
        price: "2580.00",
        status: "1",
        image: "/images/mock/blue-cat.jpg"
      }
    ];
    const active = this.data.active;
    const orders = active ? mock.filter((item) => item.status === active) : mock;
    this.setData({ orders });
  },

  switchTab(event) {
    this.setData({ active: event.currentTarget.dataset.v });
    this.onShow();
  },

  goDetail(event) {
    wx.navigateTo({ url: "/subpages/order/detail?id=" + event.currentTarget.dataset.id });
  }
});
