import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const pptxPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const presentation = await PresentationFile.importPptx(await FileBlob.load(pptxPath));

  const edits = {
    // --- Slide 1 (系统设计) ---
    "sh/u9knih4b": "接口规范与权限设计",
    "sh/judg7ads": "统一 Result 结构、BaseController 通用 CRUD、双拦截器鉴权体系",
    "sh/cj6tovah": "统一返回结构 Result(code/message/data)，code=200 成功\nBaseController 提供 10 个通用 CRUD 端点\nAuthInterceptor 解析 JWT 存 UserHolder\nRoleInterceptor 按角色分层拦截：admin/merchant/user",
    "sh/upwb2hwf": "接口与权限分层架构",
    "sh/2t83itgf": "请求 \u2192 AuthInterceptor(验证) \u2192 RoleInterceptor(校验) \u2192 Controller(过滤)",
    "sh/re1w7mp0": "/api/admin(admin) | /api/merchant(merchant) | /api/order(auth) | 其余公开",

    // --- Slide 2 (登录) ---
    "sh/rytsvu9k": "用户登录与注册实现",
    "sh/yd0z6103": "多策略认证体系：5 种登录方式，JWT 鉴权，密码 BCrypt 加盐哈希",
    "sh/3q5o7qpg": "密码登录：用户名/手机号 + 密码，PasswordEncoder 加盐哈希\n邮箱验证码：6位码\u2192Redis(5min)\u2192邮件发送\n短信登录：手机号+验证码，60秒倒计时\n微信登录：wx.login()获取code\u2192后端换openid\n人脸登录：FaceAuthService人脸识别校验\n管理端：/api/admin/login 强制密码",

    // --- Slide 3 (资料与地址) ---
    "sh/6hcrmhk3": "用户资料与地址管理",
    "sh/z25kb6xk": "GET/PUT /api/user/profile 资料；地址 CRUD + user_id 数据隔离",
    "sh/oj2t83it": "个人资料：查看/修改昵称、头像、手机、邮箱、生日\n收货地址：新增/编辑/删除，自动注入 userId\n默认地址：设置新默认前先取消旧默认\n安全校验：每次操作前查库验证 user_id 归属",
    "sh/61032lsz": "地址字段与接口表",
    "sh/vahoza1g": "receiver：收货人姓名",
    "sh/7adsji5s": "phone：联系电话",
    "sh/e1w7mp0b": "province/city：省市区",
    "sh/psfil0rq": "defaulted：是否默认(0/1)",
    "sh/ed8bat47": "接口：POST/GET/PUT/DELETE /api/user/address",

    // --- Slide 4 (鉴权) ---
    "sh/8jqh0fep": "鉴权与角色控制",
    "sh/x4nap4r6": "AuthInterceptor + RoleInterceptor 双拦截器，ThreadLocal 线程隔离",
    "sh/q5o7qpg3": "AuthInterceptor：提取 Bearer token \u2192 JWT 解析 \u2192 存 UserHolder\nRoleInterceptor：查 role \u2208 allowedRoles \u2192 放行/403\nUserHolder(ThreadLocal)：请求内任意层取 userId/username/role\n异常处理：401 未登录、403 无权限、token 过期自动拦截",
    "sh/8nqxs72x": "鉴权流程图",
    "sh/oza1gvy9": "角色边界：admin 全量 | merchant 自家店铺 | user: user_id 过滤",

    // --- Slide 5 (前端) ---
    "sh/6dcbulwv": "前端个人中心对接",
    "sh/vy90je9c": "小程序 5合1 登录页 + 个人中心 + 地址管理",
    "sh/gbq1oryt": "登录页(subpages/login)：5 Tab 切换，登录成功存 token\n个人中心(pages/user)：onShow 读 token \u2192 getProfile() 展示\n地址管理(subpages/address)：CRUD 列表 + 新增/编辑表单\nAPI 封装(utils/api)：auth.js / user.js，统一错误处理",
    "sh/yhgn29gr": "关键页面与文件",
    "sh/kre1w7mp": "从 Mock 切换到真实 API，改动集中在 utils/api/ 层",

    // --- Slide 6 (联调) ---
    "sh/g7adsji5": "联调问题与成果",
    "sh/9s36hcrm": "四大模块全部完成，支撑购物车/订单/视频/支付等下游流程",
    "sh/6hcrmhk3": "登录：5 种策略全覆盖，JWT 认证，token 存储与刷新\n资料：GET/PUT profile，含会员等级与折扣说明\n地址：完整 CRUD + 默认地址 + user_id 归属校验\n权限：双拦截器分层，SQL 层 user_id 过滤兜底",
    "sh/ovahoza1": "完成情况清单",
    "sh/dsji5sne": "登录：\u2705 5种方式 | JWT | 管理端分离",
    "sh/94fu18jq": "资料：\u2705 查看/修改 | 会员等级动态计算",
    "sh/gvy90je9": "地址：\u2705 CRUD | 默认地址 | user_id 隔离",
    "sh/ra5gbq1o": "权限：\u2705 双拦截器 | 角色分层 | SQL 兜底",
    "sh/kzupwb2h": "下游支撑：购物车(user_id) \u2192 订单(地址关联) \u2192 支付(身份校验)",
  };

  let applied = 0, skipped = 0;
  for (const [id, newText] of Object.entries(edits)) {
    try {
      const target = presentation.resolve(id);
      const old = target.text.plainText;
      if (old === newText) { console.log("unchanged: " + id); continue; }
      target.text.replace(old, newText);
      applied++;
    } catch(e) {
      skipped++;
      console.log("skip " + id + ": " + (e.message || e).toString().substring(0, 80));
    }
  }
  console.log("Applied " + applied + ", skipped " + skipped + " / " + Object.keys(edits).length + " edits");

  const outPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(presentation);
  await pptx.save(outPath);
  console.log("Saved to " + outPath);
}

main().catch(e => { console.error(e); process.exit(1); });