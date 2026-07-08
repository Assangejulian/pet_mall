import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT模板-邓已填充.pptx"));

  const edits = [
    // Slide number (1-indexed), search substring, new text
    [15, "请填写：接口规范与权限设计", "接口规范与权限设计"],
    [15, "统一 API、鉴权", "统一 Result 结构、BaseController 通用 CRUD、双拦截器鉴权体系"],
    [15, "请填写：统一返回结构", "统一返回结构 Result(code/message/data)\nBaseController 提供 10 个通用 CRUD 端点\nAuthInterceptor 解析 JWT 存 UserHolder\nRoleInterceptor 按角色分层拦截"],
    [15, "请填写：接口与权限关系", "接口与权限分层架构"],
    [15, "请连接：关系", "请求 \u2192 AuthInterceptor \u2192 RoleInterceptor \u2192 Controller"],
    [15, "可自行替换", "/api/admin(admin) | /api/merchant(merchant) | /api/order(auth) | 其余公开"],
    [16, "用户登录与注册实现", "多策略登录与注册实现"],
    [16, "人脸登录相关内容", "5种登录方式，JWT鉴权，BCrypt加盐哈希"],
    [16, "支持四种登录方式", "密码登录：用户名/手机号+密码，PasswordEncoder加盐哈希\n邮箱验证码：6位码\u2192Redis(5min)\u2192邮件发送\n短信登录：手机号+验证码，60秒倒计时\n微信登录：wx.login()\u2192后端换openid\n人脸登录：FaceAuthService人脸识别\n管理端：/api/admin/login强制密码"],
    [17, "请填写用户资料与地址管理", "用户资料与地址管理"],
    [17, "默认地址逻辑。", "GET/PUT /api/user/profile；地址CRUD+user_id隔离"],
    [17, "个人信息查看与修改", "个人资料：查看/修改昵称头像手机邮箱生日\n收货地址：新增/编辑/删除，自动注入userId\n默认地址：设新默认前先取消旧默认\n安全校验：每次操作前查库验证user_id归属"],
    [17, "地址字段/接口表", "地址字段与接口表"],
    [17, "receiver：", "receiver：收货人姓名"],
    [17, "phone：", "phone：联系电话"],
    [17, "province/city：", "province/city：省市区"],
    [17, "defaulted：", "defaulted：是否默认(0/1)"],
    [17, "截图、流程图", "接口：POST/GET/PUT/DELETE /api/user/address"],
    [18, "请填写鉴权与角色控制", "鉴权与角色控制"],
    [18, "拦截器和角色边界。", "AuthInterceptor+RoleInterceptor双拦截器，ThreadLocal线程隔离"],
    [18, "请求拦截", "AuthInterceptor：提取Bearertoken\u2192JWT解析\u2192存UserHolder\nRoleInterceptor：查role\u2208allowedRoles\u2192放行/403\nUserHolder：请求内任意层取userId/username/role\n异常：401未登录、403无权限、token过期拦截"],
    [18, "鉴权流程图", "鉴权流程图"],
    [18, "关键节点说明", "角色边界：admin全量 | merchant自家店铺 | user:user_id过滤"],
    [19, "请填写前端个人中心对接", "前端个人中心对接"],
    [19, "或 Vue 用户端", "小程序5合1登录页+个人中心+地址管理"],
    [19, "表单校验", "登录页：5Tab切换，登录成功存token\n个人中心：onShow读token\u2192getProfile()展示\n地址管理：CRUD列表+新增/编辑表单\nAPI封装：auth.js/user.js，统一错误处理"],
    [19, "个人中心/地址管理截图", "关键页面与文件"],
    [19, "截图、流程图、表格", "从Mock切换到真实API，改动集中在utils/api/层"],
    [20, "请填写联调问题与成果", "联调问题与成果"],
    [20, "建议总结用户模块", "四大模块全部完成，支撑购物车/订单/视频/支付等下游"],
    [20, "问题修复：token", "登录：5种策略全覆盖，JWT认证+token存储刷新\n资料：GET/PUT profile，含会员等级折扣说明\n地址：完整CRUD+默认地址+user_id归属校验\n权限：双拦截器分层，SQL层user_id过滤兜底"],
    [20, "完成情况清单", "完成情况清单"],
    [20, "登录：", "登录：\u2705 5种方式|JWT|管理端分离"],
    [20, "资料：", "资料：\u2705 查看/修改|会员等级动态计算"],
    [20, "地址：", "地址：\u2705 CRUD|默认地址|user_id隔离"],
    [20, "权限：", "权限：\u2705 双拦截器|角色分层|SQL兜底"],
    [20, "截图、流程图、表格", "下游支撑：购物车(user_id)\u2192订单(地址关联)\u2192支付(身份校验)"],
  ];

  let applied = 0;
  const slides = p.slides.items;
  for (const [slideNum, searchText, newText] of edits) {
    const slide = slides[slideNum - 1];
    if (!slide) continue;
    let found = false;
    for (const shape of slide.shapes.items) {
      const t = shape.text?.plainText || "";
      if (t.includes(searchText)) {
        shape.text = newText;
        applied++;
        found = true;
        break;
      }
    }
    if (!found) console.log("MISS S" + slideNum + ": " + searchText.substring(0,40));
  }
  console.log("Applied " + applied + "/" + edits.length);

  const outPath = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save(outPath);
  console.log("Saved to " + outPath);
}
main().catch(e => { console.error(e); process.exit(1); });