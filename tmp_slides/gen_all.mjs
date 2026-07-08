import { FileBlob, PresentationFile } from "@oai/artifact-tool";

async function main() {
  const p = await PresentationFile.importPptx(await FileBlob.load("D:/厦理/大四/课程/PPT模板-邓已填充.pptx"));

  // [slideNum(1-indexed), searchSubstring, newText]
  const all = [

    // === Slide 3: 团队成员与职责概览 ===
    [3,"请填写：团队成员与职责概览","团队成员与职责概览"],
    [3,"围绕宠物商城系统","4人团队：邓(用户/地址)、戴(门店/商品)、王(订单/购物车)、杨(视频/AI)"],
    [3,"请填写：团队成员姓名","邓：后端 AuthController、UserAddressController、AuthInterceptor、RoleInterceptor、JWT 鉴权\n戴：StoreController、ProductController、nearby 搜索、多条件筛选\n王：CartController、PurchaseOrderController、状态机、支付路由\n杨：VideoController、AiChatController、DeepSeek 对话"],
    [3,"请填写：成员分工表或照片","分工矩阵：前后端并行开发，Git 仓库统一管理，Swagger 接口文档共享"],
    [3,"请连接：关系","各模块独立开发 → 统一 API 规范 → 前后端联调 → 集成测试"],

    // === Slide 4: 任务拆分与进度安排 ===
    [4,"请填写：任务拆分与进度安排","任务拆分与进度安排"],
    [4,"展示从需求、设计","10天周期：需求2天→设计2天→开发3天→联调2天→答辩1天"],
    [4,"请填写：每一阶段","第1-2天：需求分析、业务设计、数据库建模、API 设计\n第3-7天：四人并行开发，各自完善后端接口+前端对接\n第8-9天：前后端联调、去Mock、跨模块集成测试\n第10天：演示 PPT、文档整理、答辩演练"],
    [4,"请填写：项目时间线","需求 → 设计 → 开发 → 联调 → 答辩"],
    [4,"请补充：关键节点说明","第3天：数据库+API定型 | 第7天：各模块独立完成 | 第9天：全链路通过"],

    // === Slide 5: 技术栈与开发环境 ===
    [5,"请填写：技术栈与开发环境","技术栈与开发环境"],
    [5,"归纳项目采用","前端：Vue3+Vite(管理/用户端) + 微信小程序(Uni-app)；后端：Spring Boot 3 + Java 21 + MyBatis-Plus；数据库：MySQL 8.0 + Redis"],
    [5,"请填写：Vue3","Vue3 + Element Plus：管理后台(pet_admin)；Vue3 + Vite：用户前端(pet_frontend)\n微信小程序 + Uni-app：移动端(pet_miniapp)\nSpring Boot 3：REST API 服务，JWT 鉴权，MyBatis-Plus ORM\nMySQL 8.0 + Redis：utf8mb4 持久化 + 验证码缓存"],
    [5,"请填写：技术栈结构","Vue3 | 微信小程序 | Spring Boot | MySQL | Redis"],
    [5,"请连接：关系","前端三端 → REST API → Spring Boot → MySQL/Redis"],

    // === Slide 6: 项目背景与问题定位 ===
    [6,"请填写：项目背景与问题定位","项目背景与问题定位"],
    [6,"说明为什么要做宠物商城","宠物经济快速增长，养宠人群需一站式宠物交易+门店服务+内容互动平台"],
    [6,"请填写：宠物交易","宠物交易：用户需在线浏览/购买宠物及用品，商家需线上获客渠道\n门店展示：附近门店搜索+经纬度定位，解决信息不对称\n平台审核：店铺/商品/视频需审核机制保障交易安全\nAI 咨询：智能客服解答养宠知识，辅助购买决策"],
    [6,"请填写：需求来源","用户购买 | 门店经营 | 平台审核 | AI 咨询"],
    [6,"请连接：关系","用户→购买商品 | 商家→经营门店 | 平台→审核管理 | AI→智能服务"],

    // === Slide 7: 用户角色与使用场景 ===
    [7,"请填写：用户角色与使用场景","用户角色与使用场景"],
    [7,"把普通用户","user(普通用户)：浏览门店/商品、加购下单、看视频互动、AI咨询\nmerchant(商家)：入驻门店、管理商品、处理订单、发视频\nadmin(管理员)：审核门店/商品/视频、管理用户、退单处理"],
    [7,"请填写：普通用户浏览","普通用户：浏览Feed→筛选商品→加购→选地址→下单→支付→收货→评价\n商家：入驻门店→上架商品→发货→退单处理→发布视频\n管理员：审核门店/商品→管理用户→退单审核→视频审核"],
    [7,"请填写：角色用例图","用户 → 商家 → 管理员 → 系统"],
    [7,"请补充：关键节点说明","用户下单→商家发货→管理员审核 闭环；AI客服贯穿全流程"],

    // === Slide 8: 功能性需求清单 ===
    [8,"请填写：功能性需求清单","功能性需求清单"],
    [8,"覆盖商城核心功能","用户域：注册登录(5种方式)、资料管理、地址CRUD\n商品域：门店管理、商品CRUD、多条件筛选、nearby搜索\n订单域：购物车、下单、支付、状态机、退单\n内容域：视频Feed、播放、点赞、评论、AI客服"],
    [8,"请填写：用户、地址","用户+地址：5种登录(JWT)、profile、地址CRUD+默认地址\n门店+商品：入驻审核、上下架、product_type/category/status筛选\n购物车+订单：user_id+product_id唯一约束、地址快照、状态机9态\n视频+AI：Feed流、播放计数、点赞评论、DeepSeek对话+工具调用"],
    [8,"请填写：功能模块表","用户域：登录/注册/资料/地址"],
    [8,"用户域：","用户域：登录(5种)+资料+地址(CRUD+默认)"],
    [8,"商品域：","商品域：门店+商品+多条件筛选+nearby"],
    [8,"订单域：","订单域：购物车+下单+状态机+支付路由"],
    [8,"内容域：","内容域：视频Feed+播放+点赞+评论+AI客服"],

    // === Slide 9: 非功能性需求 ===
    [9,"请填写：非功能性需求","非功能性需求"],
    [9,"补充安全性","安全：JWT+Bearer token鉴权，RoleInterceptor角色拦截，SQL层user_id/store_id过滤，BCrypt密码加密\n性能：雪花ID Java端生成，分页上限500条，经纬度复合索引\n稳定：统一Result返回，全局异常处理，逻辑删除可追溯\n可维护：BaseController通用CRUD，Swagger文档，Docker环境"],
    [9,"请填写：JWT 鉴权","JWT 鉴权：jsonwebtoken 0.12.6，用户端7天/管理端2小时过期\n角色权限：AuthInterceptor认证 + RoleInterceptor角色双重拦截\n数据归属：user_id/store_id SQL层过滤，从数据库层面保障安全\n统一返回：Result(code/message/data)，全局异常处理器统一捕获"],
    [9,"请填写：质量属性矩阵","安全：JWT+角色拦截+SQL过滤"],
    [9,"安全：","安全：JWT鉴权+角色拦截+BCrypt+SQL归属过滤"],
    [9,"性能：","性能：雪花ID+分页上限+经纬度索引"],
    [9,"稳定：","稳定：统一Result+全局异常+逻辑删除"],
    [9,"可维护：","可维护：BaseController+Swagger+Docker"],

    // === Slide 10: 可行性分析与约束 ===
    [10,"请填写：可行性分析与约束","可行性分析与约束"],
    [10,"结合课程周期","技术：Spring Boot 3+Vue3成熟栈，课程实训基础充足\n经济：开源框架零成本，DeepSeek API免费额度\n操作：小程序+管理后台直观易用，Swagger降低门槛"],
    [10,"请填写：技术可行性","技术可行性：团队有Spring Boot/Vue3课程基础，框架成熟文档丰富\n经济可行性：全部开源技术栈，DeepSeek免费API，Docker本地部署\n操作可行性：小程序扫码即用，管理后台Web访问，Swagger接口可在线调试\n风险控制：支付Mock演示流程为主，AI预设fallback回复，本地环境无网络依赖"],
    [10,"请填写：风险与对策","数据：预置SQL演示数据"],
    [10,"数据：","数据：docker预置演示数据+本地MySQL"],
    [10,"接口：","接口：Swagger在线文档+Postman测试"],
    [10,"联调：","联调：Mock先行+前后端并行+约定API格式"],
    [10,"演示：","演示：本地Docker自包含，无需外网"],

    // === Slide 11: 系统总体架构 ===
    [11,"请填写：系统总体架构","系统总体架构"],
    [11,"展示用户端","pet_frontend(Vue3) + pet_miniapp(小程序) + pet_admin(Vue3) → REST API → pet_backend(Spring Boot) → MySQL + Redis"],
    [11,"请填写：pet_frontend","pet_frontend：Vue3用户端，浏览商品/下单/个人中心\npet_miniapp：微信小程序，视频Feed/购物车/AI客服\npet_admin：Vue3管理端，审核/用户管理/订单管理\npet_backend：Spring Boot 3 REST API，JWT鉴权+MyBatis-Plus ORM\nMySQL 8.0 + Redis：utf8mb4持久化+验证码缓存"],
    [11,"请填写：总体架构图","用户端 | 小程序 | 管理后台 | 后端服务 | 数据库"],
    [11,"请连接：关系","三端 → HTTP/JSON → Spring Boot → JDBC → MySQL | Redis"],

    // === Slide 12: 技术选型与分层设计 ===
    [12,"请填写：技术选型与分层设计","技术选型与分层设计"],
    [12,"说明各层选择","View层：Vue3(管理端+用户端)+微信小程序\nAPI层：Spring MVC Controller，RESTful设计\nService层：Spring Service，业务逻辑+事务管理\nMapper层：MyBatis-Plus BaseMapper，通用CRUD+自定义SQL\nDB层：MySQL 8.0 utf8mb4 + Redis"],
    [12,"请填写：Spring Boot 3","Spring Boot 3 + Java 21：REST API 服务，内嵌Tomcat\nMyBatis-Plus：简化CRUD、分页插件、逻辑删除、雪花ID自动生成\nJWT(jsonwebtoken 0.12.6)：无状态鉴权，Bearer方式传输\nSwagger(SpringDoc)：在线API文档，前后端协作透明"],
    [12,"请填写：分层设计图","View | API | Service | Mapper | DB"],
    [12,"请连接：关系","View → REST → Controller → Service → Mapper → DB"],

    // === Slide 13: 数据库与实体设计 ===
    [13,"请填写：数据库与实体设计","数据库与实体设计"],
    [13,"围绕 11 张","11张业务表：user、user_address、store、product、cart、purchase_order、order_item、video、comment、ai_chat_record、sys_message"],
    [13,"请填写：user","user：用户主表，role字段区分admin/merchant/user，支持微信openid/unionid\nstore：门店表，经纬度DECIMAL(10,7)，状态0待审1营业2关闭\nproduct：商品表，关联store_id，product_type(1宠物2周边)，多图JSON\ncart：购物车，UNIQUE(user_id,product_id)一用户一商品一条"],
    [13,"请填写：ER 图","user | store | product | order | video | ai_record"],
    [13,"请连接：关系","user→address/cart/order | store→product | product→video | user→video/comment"],

    // === Slide 14: 核心业务流程设计 ===
    [14,"请填写：核心业务流程设计","核心业务流程设计"],
    [14,"建议选择下单","核心流程：浏览→加购→选地址→下单→扣库存→支付→发货→收货→评价"],
    [14,"请填写：从页面操作","浏览商品→加入购物车(cart唯一约束)→选择收货地址\n→提交订单(生成order_no+地址快照+商品快照+扣减库存)\n→支付(PaymentServiceRouter路由策略)→商家发货(ship_time)\n→用户收货(receive_time)→评价(evaluate_star 1-5)"],
    [14,"请填写：业务流程图","浏览 → 加入购物车 → 下单 → 支付 → 发货"],
    [14,"请补充：关键节点说明","下单：地址快照+库存扣减 | 支付：Mock策略路由 | 发货：商家store_id校验"],

    // === Slide 15: 接口规范与权限设计 ===
    [15,"请填写：接口规范与权限设计","接口规范与权限设计"],
    [15,"统一 API、鉴权","统一Result(code/message/data)；BaseController 10个通用端点；AuthInterceptor+RoleInterceptor双拦截器"],
    [15,"请填写：统一返回结构","统一返回 Result(code/message/data)，code=200成功\nBaseController提供10个通用CRUD端点(GET/POST/PUT/DELETE+search+list+batch)\nAuthInterceptor解析JWT存UserHolder(ThreadLocal)\nRoleInterceptor按角色分层拦截：admin/merchant/user"],
    [15,"请填写：接口与权限关系","接口与权限分层架构"],
    [15,"请连接：关系","请求 → AuthInterceptor(认证) → RoleInterceptor(授权) → Controller(过滤)"],
    [15,"可自行替换","/api/admin(admin) | /api/merchant(merchant) | /api/order(auth) | 其余公开"],

    // === Slide 16: 用户登录与注册 ===
    [16,"用户登录与注册实现","多策略登录与注册实现"],
    [16,"人脸登录相关内容","5种登录方式：密码(BCrypt)+邮箱验证码(Redis 5min)+短信+微信(openid)+人脸"],
    [16,"支持四种登录方式","密码登录：用户名/手机号+PasswordEncoder加盐哈希\n邮箱验证码：6位码→Redis(5min)→JavaMailSender发送\n短信登录：手机号+验证码，60秒倒计时防重复\n微信登录：wx.login()→后端换openid→findOrCreateByWechat\n人脸登录：FaceAuthService(预留)"],
    [16,"登录/注册页面截图","管理端：/api/admin/login 强制密码+校验role非user"],

    // === Slide 17: 用户资料与地址管理 ===
    [17,"请填写用户资料与地址管理","用户资料与地址管理"],
    [17,"默认地址逻辑。","GET/PUT /api/user/profile；地址CRUD+user_id数据隔离+默认地址"],
    [17,"个人信息查看与修改","个人资料：查看/修改昵称头像手机邮箱生日+会员等级折扣\n收货地址：新增/编辑/删除，自动注入userId\n默认地址：设新默认前先取消旧默认(事务性)\n安全校验：每次操作前查库验证user_id归属，拒绝越权"],
    [17,"地址字段/接口表","地址字段与接口"],
    [17,"receiver：","receiver：收货人姓名"],
    [17,"phone：","phone：联系电话"],
    [17,"province/city：","province/city：省市区"],
    [17,"defaulted：","defaulted：是否默认(0/1)"],
    [17,"截图、流程图","接口：POST/GET/PUT/DELETE /api/user/address"],

    // === Slide 18: 鉴权与角色控制 ===
    [18,"请填写鉴权与角色控制","鉴权与角色控制"],
    [18,"拦截器和角色边界。","AuthInterceptor+RoleInterceptor双拦截器，ThreadLocal线程隔离"],
    [18,"请求拦截","AuthInterceptor：提取Bearertoken→JWT解析→存UserHolder\nRoleInterceptor：查role∈allowedRoles→放行/403\nUserHolder(ThreadLocal)：请求内任意层取userId/username/role\n异常：401未登录、403无权限、token过期自动拦截"],
    [18,"鉴权流程图","鉴权流程图"],
    [18,"关键节点说明","角色边界：admin全量 | merchant自家店铺 | user:user_id过滤"],

    // === Slide 19: 前端个人中心对接 ===
    [19,"请填写前端个人中心对接","前端个人中心对接"],
    [19,"或 Vue 用户端","小程序5合1登录页+个人中心+地址管理"],
    [19,"表单校验","登录页(subpages/login)：5Tab切换，登录成功存token\n个人中心(pages/user)：onShow读token→getProfile()展示\n地址管理(subpages/address)：CRUD列表+新增/编辑\nAPI封装(utils/api)：auth.js/user.js统一错误处理"],
    [19,"个人中心/地址管理截图","关键页面与文件"],
    [19,"截图、流程图、表格","从Mock切换到真实API，改动集中在utils/api/层"],
    [19,"为页面截图","miniapp/pages/user + subpages/login/address/profile"],

    // === Slide 20: 邓联调成果 ===
    [20,"请填写联调问题与成果","联调问题与成果"],
    [20,"建议总结用户模块","四大模块全部完成，支撑购物车/订单/视频/支付等下游"],
    [20,"问题修复：token","登录：5种策略全覆盖，JWT认证+token存储刷新\n资料：GET/PUT profile，含会员等级折扣说明\n地址：完整CRUD+默认地址+user_id归属校验\n权限：双拦截器分层，SQL层user_id过滤兜底"],
    [20,"完成情况清单","完成情况清单"],
    [20,"下游支撑","下游支撑：购物车(user_id)→订单(地址关联)→支付(身份校验)"],

    // === Slide 21: 门店管理 ===
    [21,"请填写：门店管理实现","门店管理实现"],
    [21,"请填写：StoreController","StoreController：门店CRUD，经纬度DECIMAL(10,7)，status(0待审1营业2关闭)，逻辑删除"],
    [21,"请填写：store","store表：user_id(店主)、store_name、经纬度(lng/lat)、status(0/1/2)、deleted\n核心接口：/api/store/search(分页)、/api/store/{id}、/api/store/nearby(经纬度范围)\n商家端：MerchantStoreController，requireOwnedStore()校验归属"],
    [21,"请放入：门店管理页面截图","/api/store/search | /api/store/{id} | /api/store/nearby"],

    // === Slide 22: 商品管理 ===
    [22,"请填写：商品管理实现","商品管理实现"],
    [22,"请填写：ProductController","ProductController：商品CRUD+上下架，product_type(1宠物2周边)，多图JSON"],
    [22,"请填写：product","product表：store_id、product_type、category(dog/cat/other)、price DECIMAL(10,2)、stock、status(0下架1上架2售出)\n多条件筛选：store_id+product_type+category+status+keyword\n商家端：MerchantProductController，requireOwnedProduct()校验归属"],
    [22,"请放入：商品管理页面截图","/api/product/search?storeId=&productType=&category=&status="],

    // === Slide 23: 门店商品与附近搜索 ===
    [23,"请填写：门店级商品与附近搜索","门店级商品与附近搜索"],
    [23,"请填写：store_id","store_id过滤：商家查商品自动过滤自己门店；nearby搜索：经纬度范围查询利用(lng,lat)复合索引"],
    [23,"请填写：store_id","store_id 过滤：商家查询商品时自动过滤自己门店的商品\nnearby 搜索：WHERE lat BETWEEN ? AND lng BETWEEN? 利用复合索引\n小程序端：店铺列表→门店详情→商品列表，三级页面流"],
    [23,"请放入：附近搜索页面截图","/api/store/nearby?lng=&lat=&radius="],

    // === Slide 24: 商家审核后台 ===
    [24,"请填写：商家与审核后台","商家与审核后台"],
    [24,"请填写：merchantStore","AuditorStoreController：审核门店通过/拒绝；AuditorProductController：审核商品上下架"],
    [24,"请填写：merchantStore","商家入驻：提交→status=0→admin审核→status=1→上架商品→用户可见\n审核流程：/api/auditor/** 仅auditor+admin可访问(RoleInterceptor)\n商家后台：Vue3管理界面，商家管理自有门店+商品"],
    [24,"请填写：审核流程","提交 → 待审 → 通过 → 上架 → 展示"],
    [24,"请补充：关键节点说明","审核通过后商家可上架商品，用户端即时可见"],

    // === Slide 25: 数据筛选展示 ===
    [25,"请填写：数据筛选与展示优化","数据筛选与展示优化"],
    [25,"请填写：BaseController","BaseController /search：QueryWrapper条件构造+Page分页；loading/空状态/错误提示"],
    [25,"请填写：BaseController","BaseController /search 统一分页+条件搜索：QueryWrapper动态构造\n商品筛选示例：?storeId=1&productType=1&category=dog&status=1&page=1&size=10\n前端体验：loading状态、空状态占位、Toast错误提示、下拉刷新"],
    [25,"请填写：筛选参数示例","storeId | category | status | keyword"],
    [25,"请连接：关系","参数→QueryWrapper→SQL WHERE→分页结果→前端渲染"],

    // === Slide 26: 购物车 ===
    [26,"请填写：购物车实现","购物车实现"],
    [26,"请填写：CartController","CartController：CRUD+批量结算；UNIQUE(user_id,product_id)一商品一条；user_id自动过滤"],
    [26,"请填写：CartController","CartController：增删改查+批量操作，自动注入user_id\n唯一约束：UNIQUE INDEX idx_user_product(user_id,product_id)\n数据隔离：buildQueryWrapper自动eq(\"user_id\",UserHolder.getUserId())\n批量结算：勾选checked=1项一键下单"],
    [26,"请放入：购物车页面截图","/api/cart/search | POST /api/cart | PUT /api/cart/{id}"],

    // === Slide 27: 下单与订单明细 ===
    [27,"请填写：下单与订单明细","下单与订单明细"],
    [27,"请填写：OrderCreateDTO","cart→address→order→item→stock全链路：地址快照+商品快照+库存扣减+清空购物车"],
    [27,"请填写：OrderCreateDTO","下单链路：选中购物车项→选地址→提交OrderCreateDTO\n→生成order_no(时间戳+随机)→地址快照(address_snapshot JSON)\n→商品快照(order_item：product_name+image+price)\n→金额计算(total-discount=pay)→库存扣减→清空购物车"],
    [27,"请填写：下单链路","cart → address → order → item → stock"],
    [27,"请补充：关键节点说明","地址快照防篡改 | 商品快照保历史 | 库存行锁防超卖"],

    // === Slide 28: 支付与状态机 ===
    [28,"请填写：支付与订单状态机","支付与订单状态机"],
    [28,"请填写：order_status","0待支付→1已付→2已发→3已收→4已评 + 退单-1/-2/-3/-4 + 取消(回滚库存)"],
    [28,"请填写：order_status","正向：0待付→1已付→2已发→3已收→4已评\n退单：3→-1申请→-2审核→-3已退款/-4已拒绝\n取消：任意非终态可取消(回滚库存)\nPaymentServiceRouter：Mock/Alipay/Wechat三种策略路由"],
    [28,"请填写：订单状态机","待支付 → 已支付 → 已发货 → 已收货 → 已评价"],
    [28,"请补充：关键节点说明","支付路由：Mock演示为主 | 退单审核：admin处理 | 取消回滚库存"],

    // === Slide 29: 商家订单后台 ===
    [29,"请填写：商家订单与后台发货","商家订单与后台发货"],
    [29,"请填写：MerchantOrderController","MerchantOrderController：商家只看自家订单，发货+退单审核；OrderAdminController：管理员全量管理"],
    [29,"请填写：MerchantOrderController","MerchantOrderController：商家只能查看/处理自己门店关联订单(store_id过滤)\n商家操作：查看待发货(1)→发货(2)+记录ship_time→退单审核(-2/-4)\nOrderAdminController：管理员全量管理所有订单+处理退单"],
    [29,"请放入：订单管理页面截图","/api/merchant/order | /api/admin/order | ship/refund"],

    // === Slide 30: 异常处理 ===
    [30,"请填写：金额、库存与异常处理","金额、库存与异常处理"],
    [30,"请填写：库存不足","库存不足：行锁防超卖，下单失败提示；重复支付：已支付状态拒绝再次支付；归属错误：user_id校验拦截"],
    [30,"请填写：库存不足","库存不足：数据库行锁防超卖，下单失败返回错误提示\n重复支付：order_status已支付时拒绝再次支付请求\n订单归属：每次操作前校验user_id/store_id，拒绝越权\n金额精度：DECIMAL(10,2)避免浮点数误差"],
    [30,"请填写：异常场景与对策","库存：行锁防超卖"],
    [30,"库存：","库存：数据库行锁防超卖，失败提示"],
    [30,"金额：","金额：DECIMAL(10,2)精度，防浮点误差"],
    [30,"权限：","权限：user_id/store_id归属校验拦截"],
    [30,"状态：","状态：状态机校验，非法跳转拒绝"],

    // === Slide 31: 视频流 ===
    [31,"视频流的实现","视频流的实现"],
    [31,"视频首页","视频首页：Feed分页(search)，JOIN user作者信息，按时间倒序"],
    [31,"视频内容","视频内容：播放(play/{id})play_count++，点赞({id}/like)likes++"],
    [31,"评论区","评论区：发表({id}/comment)comment_count++，列表({id}/comments)公开GET"],
    [31,"AI客服","AI客服：POST /api/ai/chat→DeepSeek对话+ai_chat_record记录"],
    [31,"购买页面","购买页面：视频关联product_id→点击跳转商品详情下单"],

    // === Slide 32: 视频流流程图 ===
    [32,"视频流流程图","视频流完整流程"],

    // === Slide 33: AI客服 ===
    [33,"AI客服的实现","AI客服的实现"],

    // === Slide 34: AI代码 ===
    [34,"相关代码内容实现","AI客服核心代码实现"],
    [34,"服务运行配置","服务配置：application.yml DeepSeek API Key + 模型参数 + 超时设置"],
    [34,"系统提示词","系统提示词：\"你是暖窝宠物商城的智能客服，帮助解答养宠问题\""],
    [34,"工具适配器（部分片段）","工具适配器：AiMallToolService 支持查询商品/门店/订单等业务数据"],

    // === Slide 35: AI演示 ===
    [35,"实际AI操作展示","AI客服实际演示效果"],

    // === Slide 36: 测试策略 ===
    [36,"请填写：测试策略与范围","测试策略与范围"],
    [36,"说明单元测试","测试矩阵：后端JUnit单元+Postman接口+页面手动+端到端业务流 四层覆盖"],
    [36,"请填写：测试目标","测试目标：验证11张表CRUD完整性、鉴权体系安全性、业务流程正确性\n测试环境：本地Docker(MySQL+Redis)，预置演示数据\n后端测试：JUnit 5单元测试 Service层逻辑\n接口测试：Postman/Apifox 覆盖所有REST API\n页面测试：Vue3管理后台+小程序功能手动验证\n业务流测试：登录→下单→支付→发货 全链路"],
    [36,"请填写：测试范围矩阵","后端：JUnit单元测试"],
    [36,"后端：","后端：JUnit 5测试Service+Mapper"],
    [36,"接口：","接口：Postman覆盖全部REST API"],
    [36,"页面：","页面：Vue3+小程序手动验证"],
    [36,"业务流：","业务流：端到端全链路测试"],

    // === Slide 37: 后端测试 ===
    [37,"请填写：后端单元与接口测试","后端单元与接口测试"],
    [37,"建议结合已有","JUnit测试：ProductServiceImplTest、OrderPaymentSecurityTest等"],
    [37,"请填写：ProductServiceImplTest","ProductServiceImplTest：商品CRUD+状态变更测试\nOrderPaymentSecurityTest：订单支付安全+金额校验\n用户模块：登录5种策略+资料CRUD+地址归属校验\n商品模块：多条件筛选+nearby搜索+上下架\n订单模块：下单链路+状态机流转+退单流程\n视频模块：Feed分页+播放计数+点赞评论"],
    [37,"请填写：测试用例清单","用户：登录/注册/资料/地址"],
    [37,"用户：","用户：✅ 5种登录+资料CRUD+地址校验"],
    [37,"商品：","商品：✅ 筛选+nearby+上下架"],
    [37,"订单：","订单：✅ 下单+状态机+退单"],
    [37,"视频：","视频：✅ Feed+播放+点赞+评论"],

    // === Slide 38: 联调测试 ===
    [38,"请填写：前端与小程序联调测试","前端与小程序联调测试"],
    [38,"建议说明页面","pet_frontend(Vue3)+pet_miniapp(小程序)+pet_admin(Vue3)三端与后端API联调"],
    [38,"请填写：pet_frontend","pet_frontend：用户端商品浏览+下单+个人中心\npet_miniapp：小程序视频Feed+购物车+AI客服\npet_admin：管理后台审核+用户管理+订单管理\n联调重点：Mock替换→真实API、token注入(请求拦截器)、跨页面传参\n错误处理：网络异常/超时/服务器500 前端Toast提示"],
    [38,"请放入：联调截图","Network请求面板 | Console错误日志 | 页面实际效果"],

    // === Slide 39: 业务场景测试 ===
    [39,"请填写：关键业务场景测试","关键业务场景测试"],
    [39,"建议选 3-4","场景1：注册→浏览→加购→下单→支付→发货→收货→评价\n场景2：商家入驻→审核→上架→发货→退单处理\n场景3：视频Feed→播放→点赞→评论→关联商品购买\n场景4：AI客服问答→工具调用→商品推荐"],
    [39,"请填写：注册登录","注册登录→浏览商品→购物车→下单→支付→发货：宠物商城核心交易链路\n商家入驻/商品审核/后台管理：商家→平台→用户三方闭环\n视频互动与AI客服问答：内容+智能服务两条辅助线"],
    [39,"请填写：端到端测试流程","登录 → 浏览 → 下单 → 管理 → 复测"],
    [39,"请补充：关键节点说明","全链路通过：登录→下单→支付→发货 端到端验证通过"],

    // === Slide 40: 测试结论 ===
    [40,"请填写：测试结论与后续改进","测试结论与后续改进"],
    [40,"用一页收束","11张表完整CRUD + 三端对接 + 双拦截器鉴权 + 5种登录 + 状态机 + 视频互动 + AI客服 全部完成"],
    [40,"请填写：已通过","已完成：11张表CRUD完整、三端对接通过、鉴权体系安全、支付流程演示可用、AI客服正常响应\n待优化：支付Mock需接入真实支付宝/微信、视频上传需CDN、短信需对接网关\n下一步：部署云服务器HTTPS+域名、Elasticsearch全文搜索、用户行为推荐算法、Prometheus+Grafana监控"],
    [40,"请填写：结论与展望","已完成：全模块CRUD+三端对接+鉴权安全"],
    [40,"已完成：","已完成：✅ 11表CRUD+三端+鉴权+状态机+AI"],
    [40,"待优化：","待优化：支付接入+CDN视频+短信网关"],
    [40,"下一步：","下一步：云部署+ES搜索+推荐+监控"],
  ];

  let ok = 0, miss = 0;
  for (const [sn, search, replace] of all) {
    const slide = p.slides.items[sn - 1];
    if (!slide) continue;
    let found = false;
    for (const shape of slide.shapes.items) {
      if (String(shape.text).includes(search)) {
        shape.text = replace;
        ok++; found = true; break;
      }
    }
    if (!found) miss++;
  }
  console.log("Applied " + ok + ", missed " + miss + " / " + all.length);

  const out = "D:/厦理/大四/课程/PPT-邓-演讲大纲.pptx";
  const pptx = await PresentationFile.exportPptx(p);
  await pptx.save(out);
  console.log("Saved: " + out);
}
main().catch(e => { console.error(e); process.exit(1); });