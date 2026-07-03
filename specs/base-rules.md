# 项目基础规则（Base Rules）

> AI 代码生成全局规则，所有功能 Spec 自动继承，无需重复声明。
>
> 项目：pet_backend | 技术栈：Spring Boot 3 + Java 21 + MyBatis-Plus 3.5.7 + MySQL 8.0 + Redis

---

## 1. 三层职责边界

| 层 | 能做什么 | 绝对不能做 |
|----|---------|-----------|
| **Controller** | 参数校验(@Valid)、调用 Service、组装 Result<T> 返回 | ❌ 写业务逻辑、❌ 直接调 Mapper |
| **Service** | 业务逻辑、事务管理、DTO ↔ DO 转换、调用 Mapper | ❌ 处理 HttpServletRequest/Response |
| **Mapper** | 数据访问、SQL 执行（继承 BaseMapper<T>） | ❌ 写业务判断、❌ 跨 Mapper 调用 |
| **Helper/Util** | 第三方 API 封装、可复用工具方法 | ❌ 持有业务状态 |

**Service 层接口 / 实现命名规则**：
- 接口：I{Name}Service.java（如 ICartService）
- 实现：{Name}ServiceImpl.java（如 CartServiceImpl）
- 简单 CRUD 如果没有扩展计划，直接用实现类，省去接口（YAGNI）

> 阿里分层补充：第三方 RPC / 缓存 / 中间件统一封装在 Helper 层，不直接在 Service 中调用 SDK 裸方法。

---

## 2. 包结构约定

按业务领域分包（已在项目中采用）：

`
com.pat
├── {module}/                  # 业务模块（按领域）
│   ├── controller/            # XxxController
│   ├── service/               # IXxxService / XxxServiceImpl
│   ├── mapper/                # XxxMapper（继承 BaseMapper）
│   ├── domain/                # DO / DTO / VO / Query / 枚举
│   ├── helper/                # 模块内辅助类、第三方适配层（可选）
│   └── task/                  # 定时任务（可选）
├── common/                    # 全局通用
│   ├── config/                # 配置类
│   ├── constant/              # 常量
│   ├── domain/                # 通用实体（BaseEntity / Result / ResultError）
│   ├── enums/                 # 通用枚举（ErrorCode）
│   ├── exception/             # 全局异常处理
│   ├── interceptor/           # 拦截器
│   └── util/                  # 工具类
└── Application.java
`

> 每个业务模块 domain/ 下的类按以下约定组织：
> - **DO**（Data Object）：{TableName}.java，与数据库表一一对应，加 MyBatis-Plus 注解
> - **DTO**（Data Transfer Object）：{Name}DTO.java，接收前端入参
> - **VO**（View Object）：{Name}VO.java，返回给前端的视图对象
> - **Query**：{Name}Query.java，分页 / 列表查询条件
> - 分层模型严格区分，**禁止混用**：DO 不入 Controller，DTO 不入 Mapper

---

## 3. 命名统一规范

### 3.1 阿里强制红线

| 规则 | 正例 | ❌ 反例 |
|------|------|--------|
| 禁止下划线 / 美元符首尾 | userName | _userName / user$ |
| 禁止中文、拼音、中英混合 | discountPromotion | daZhePromotion / getPingfen() |
| 包名全小写、单数 | com.pat.order.service | com.pat.order.services |
| 类名大驼峰 | OrderService | — |
| 抽象类 Abstract 前缀 | AbstractPayChannel | — |
| 异常类 Exception 结尾 | OrderPayException | — |
| 测试类 {被测类}Test | OrderServiceTest | — |
| 方法 / 变量小驼峰 | calculateTotalAmount() | — |
| 常量全大写 + 下划线 | MAX_ORDER_RETRY_COUNT | MAX_NUM（语义不清） |
| long 字面量大写 L | 86400L | 86400l（像数字 1） |
| 数组括号紧跟类型 | String[] args | String args[] |
| 布尔 POJO 变量**禁止 is 前缀** | deleted | isDeleted（序列化框架坑） |

### 3.2 项目统一方法动词

| 语义 | 统一用词 | 示例 | ❌ 禁止 |
|------|---------|------|--------|
| 新增 | dd | ddOrder() | — |
| 保存 | save | saveOrder() | — |
| 修改 | update | updateOrder() | edit / modify |
| 删除 | delete / emove | deleteOrder() / emoveCartItem() | erase |
| 分页查询 | page | pageOrders() | list / queryPage |
| 列表查询 | list | listOrders() | indAll / getAll |
| 详情 | getById / getDetail | getById(id) | indById |
| 批量操作 | atch 前缀 | atchDelete() | — |

### 3.3 名副其实原则

- 变量 / 函数名要回答「为什么存在、做什么、怎么用」
- 类 / 对象用名词短语：OrderService、User
- 方法用动词 + 名词：calculatePrice()、deleteOrder()
- 布尔方法加判断前缀：hasPermission、isValid、canSubmit
- 禁止无意义缩写，除非行业通用（id、url、dao）
- 作用域越小名字越短：循环临时变量可单字符，全局变量必须完整语义

---

## 4. 统一返回格式

所有 Controller 返回类型为 Result<T>（com.pat.common.domain.Result）：

`java
// 成功（有数据）
return Result.success(orderVO);

// 成功（无数据，如新增/修改/删除后）
return Result.success();

// 成功（自定义消息）
return Result.success("操作成功", orderVO);

// 失败（错误码）
return Result.error(ErrorCode.PARAM_ERROR);

// 失败（错误码 + 自定义消息）
return Result.error(ErrorCode.PARAM_ERROR, "订单号不能为空");

// 失败（直传 code + message）
return Result.error(500, "系统繁忙");
`

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 成功，其他为错误码 |
| message | String | 提示信息 |
| data | T | 返回数据，无数据时为 null |
| description | String | 补充描述（可选，调试用） |

---

## 5. 通用禁止项

| 禁止项 | 说明 |
|--------|------|
| ❌ BeanUtils.copyProperties | 性能差，字段映射不可控。替代：手写转换方法或 MapStruct |
| ❌ Controller 直接调 Mapper | 跨层调用，破坏三层职责 |
| ❌ Mapper 写业务判断 | 数据访问层只做 SQL |
| ❌ DO 直接返回给前端 | 暴露表结构。必须转 VO |
| ❌ SQL 字符串拼接 | 一律参数化，防 SQL 注入 |
| ❌ System.out.println | 统一用 @Slf4j 日志 |
| ❌ 空 catch 块 | 至少记录 error 日志 |
| ❌ 捕获 Exception / Throwable | 精准捕获业务异常 |
| ❌ 魔法值 | 状态 / 类型必须用枚举常量 |
| ❌ 方法返回 null 集合 | 统一返回 
ew ArrayList<>() |
| ❌ 包装类用 == 比较 | 必须用 equals()，如 ORDER_PAID.equals(status) |
| ❌ 布尔标识参数 | 拆成两个独立方法，如 enderSuite() + enderSingleRoom() |
| ❌ 超长链式调用 | order.getUser().getAddr().getPhone() → 封装 getUserContactPhone() |
| ❌ 一行声明多个变量 | int a, b, c; → 分开写 |
| ❌ 日志字符串拼接 | 用参数化：log.info("用户{}下单", userId) |
| ❌ 敏感信息未脱敏 | 手机号、身份证、银行卡号必须脱敏输出 |

---

## 6. 格式规范

### 6.1 强制规则

- **缩进统一 4 个空格，禁止 Tab 与空格混用**
- 左大括号紧跟语句末尾不换行；右大括号单独一行
- 运算符换行时运算符放行首
- 一行只声明一个变量
- 不同业务逻辑块空行分隔，同一段逻辑不插空行
- 覆写方法必须加 @Override 注解

### 6.2 类内部成员顺序

`java
public class OrderService {
    // 1. 静态常量
    private static final int MAX_RETRY = 3;

    // 2. 私有成员变量
    private final OrderMapper orderMapper;

    // 3. 构造函数
    public OrderService(OrderMapper orderMapper) { ... }

    // 4. 公共方法（按调用顺序）
    public OrderVO createOrder(OrderCreateDTO dto) { ... }
    public OrderVO getById(Long id) { ... }

    // 5. 私有工具方法
    private void validateOrder(OrderCreateDTO dto) { ... }
}
`

### 6.3 行宽与垂直排版

- 单行 ≤ 120 字符，超长换行
- 概念相关代码垂直靠近，变量定义紧贴使用位置
- 方法按调用顺序排列，上层方法在前，底层工具在后

---

## 7. 可读性铁律

### 7.1 DRY（不要重复自己）—— 复用的前提是语义一致

> **重复的是「业务语义」才需要 DRY，重复的只是「代码行数」不需要。**

- 核心本质：**单一信息源** —— 同一份业务规则、同一份数据定义、同一段核心逻辑，在系统中只能有一处权威实现
- 典型误区：为了少写两行代码强行抽取公共方法，把业务语义完全不同、只是「代码长得像」的逻辑硬揉在一起

`java
// ❌ 错误：两个方法代码像，但业务语义完全无关
private String formatValue(Object val) {
    return val == null ? "--" : val.toString();
}
// calculateTotal 和 getDisplayName 都用到了，但一个是金额一个是名称
// 一旦金额要加 "元" 后缀，formatValue 就必须拆，反而多一次重构

// ✅ 正确：各自独立实现，语义清晰
private String formatAmount(BigDecimal amount) {
    return amount == null ? "--" : amount + "元";
}
private String getDisplayName(User user) {
    return user == null ? "--" : user.getNickname();
}
`

### 7.2 KISS（保持简单）—— 简单 ≠ 简陋

> 代码的**阅读次数远多于编写次数**，优先保证人能看懂。

- 简单判断用 if-else，不要为了「优雅」上策略模式
- 禁止嵌套多层三目运算符
- 禁止超长流式调用（> 3 个链式操作拆开写）

### 7.3 YAGNI（你不会需要它）—— 不预测未来，但留好边界

> **不做功能预埋，但做架构预留。**

`java
// ❌ 极端 YAGNI：全量硬编码，后续迭代牵一发动全身
// ❌ 过度设计：提前做万能配置项、可插拔架构，三年都没用到

// ✅ 正确姿势：
// 1. 不用提前写好「多支付渠道」的完整策略体系
// 2. 但把支付逻辑封装在独立模块里，未来新增渠道不用改动主流程
// 3. 占位类只抛「尚未接入」异常，不提前写未上线业务逻辑

public class WechatPayService implements PayService {
    @Override
    public PayResult pay(Order order) {
        throw new UnsupportedOperationException("微信支付尚未接入");
    }
}
`

### 7.4 方法体量约束

| 指标 | 红线 | 说明 |
|------|------|------|
| 单方法上限 | ≤ 50 行 | 含空行和注释，超过必须拆分 |
| Controller 方法 | ≤ 20 行 | 理想情况 |
| 嵌套层数 | ≤ 2 层 | if/for/while 嵌套 |
| 参数个数 | ≤ 3 个 | 超过封装为 DTO 传参 |

### 7.5 方法提取行业共识（Martin Fowler + Google Java Style）

| 场景 | 决策 |
|------|------|
| 逻辑被 **≥ 2 处** 调用 | 抽取 |
| 私有方法只有 1 处调用，且逻辑 **≤ 5 行** | **不抽**（过度拆分伤害可读性） |
| 私有方法 **≥ 10 行**且有独立语义 | 抽取 |
| 第三方 API 调用 | 全部抽到 Helper |
| 长方法重构 | 按业务步骤拆为同层级子方法，主方法只保留流程 |

`java
// ✅ 正确：主方法只看流程，细节下沉
public double calculateOrderTotal(Order order) {
    validateOrder(order);
    double itemTotal = sumItemPrices(order);
    double discountTotal = applyDiscount(itemTotal);
    return addShippingFee(discountTotal, order);
}
// 每个子方法见名知意，不需要注释解释
`

### 7.6 禁止无意义抽象（YAGNI）

- 只有一个实现 → 不写接口，直接用实现类
- 只有一个子类 → 不抽抽象基类
- 只在一处使用 → 不抽通用工具方法
- 只有一种支付方式 → 不上策略模式

### 7.7 业务语义优先

| ❌ 模糊命名 | ✅ 直白命名 |
|------------|------------|
| process() | createOrder() / pproveRefund() |
| handle() | deductStock() / sendNotify() |
| data | orderCreateDTO |
| lag / 	emp | hasPaid / isExpired |
| userInfo / userData | getBasicUser() / getUserDetailWithOrder() |

---

## 8. 重构模式分类

> 参考 Martin Fowler《重构》与 Joshua Kerievsky《重构与模式》。

| 重构类型 | 目标 | 典型场景 | 关键操作 |
|---------|------|---------|---------|
| **实现模式** | 将现有代码直接重构为明确模式 | 已存在明显坏味道，需立即引入模式 | 如将 if-else 重构为策略模式 |
| **趋向模式** | 为未来模式应用铺垫中间结构 | 需求可能变化但尚未明确，先降低重构成本 | 如用 Creation Method 替换构造函数 |
| **去除模式** | 安全移除已失效的模式 | 模式因需求变更变得冗余 | 简化接口，合并类，避免「为模式而模式」 |

> 优先使用枚举替代魔法值（用类取代类型码），优先用空对象消除 null 判断（引入空对象），只在分支 ≥ 5 且明确会扩展时才引入策略 / 工厂。

---

## 9. 数据库规范

### 9.1 表命名

- 格式：	_{模块}_{表名}，如 	_order、	_order_item
- 全部小写 + 下划线

### 9.2 必备字段

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO_INCREMENT | 主键 |
| create_time | DATETIME DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| deleted | TINYINT DEFAULT 0 | 逻辑删除（如有需要） |

### 9.3 索引规范

- 主键索引：PRIMARY KEY (id)
- 唯一索引：uk_{字段简称}，如 uk_order_no
- 普通索引：idx_{字段简称}，如 idx_user_id
- 组合索引：idx_{字段1}_{字段2}，如 idx_status_create

### 9.4 DO 实体注解

`java
@Data
@TableName("t_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;  // 强制

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
`

---

## 10. 事务规范

- 只在 CUD（Create/Update/Delete）操作加 @Transactional
- 必须指定 ollbackFor = Exception.class
- 读操作不加事务
- 事务粒度：一个业务操作一个事务，不跨多个 HTTP 请求

---

## 11. 分页规范

- 统一使用 MyBatis-Plus Page<T>
- 前端传入参数：page（页码，从 1 开始）、size（每页条数）
- Query 对象建议继承或组合分页参数

`java
@GetMapping("/page")
public Result<IPage<OrderVO>> page(OrderQuery query) {
    IPage<OrderVO> page = orderService.pageOrders(query);
    return Result.success(page);
}
`

---

## 12. 异常处理规范

### 12.1 阿里强制红线

- **禁止空 catch 块不打印日志、不上抛**
- 禁止直接捕获 Exception / Throwable，精准捕获业务异常
- 自定义业务异常统一携带错误码 + 业务描述
- 异常日志必须打印完整堆栈：log.error("订单创建失败", e)，不能只 log.error(e.getMessage())

### 12.2 异常码规范

- 错误码统一在 com.pat.common.enums.ErrorCode 枚举中定义
- 格式：模块_错误类型（如 ORDER_STATUS_FORBIDDEN）
- 业务异常统一抛出，由全局异常处理器（@RestControllerAdvice）拦截后返回 Result.error()

| 场景 | 错误码示例 | HTTP 状态 |
|------|-----------|----------|
| 参数校验失败 | PARAM_ERROR | 400 |
| 数据不存在 | DATA_NOT_FOUND | 404 |
| 重复操作 | DUPLICATE_OPERATION | 400 |
| 状态不允许 | STATUS_FORBIDDEN | 400 |
| 权限不足 | FORBIDDEN | 403 |
| 系统异常 | SYSTEM_ERROR | 500 |

### 12.3 日志规范

| 级别 | 用途 |
|------|------|
| DEBUG | 仅开发调试 |
| INFO | 正常业务流程（如"用户{}下单成功"） |
| WARN | 业务预警（如"库存不足"） |
| ERROR | 程序异常，必须带完整堆栈 |

- 使用参数化日志：log.info("用户{}下单，金额{}", userId, amount)
- 禁止字符串拼接：log.info("用户" + userId + "下单")
- 敏感信息必须脱敏：手机号 138****1234

---

## 13. 边界与第三方隔离

- 第三方 SDK / API 调用**必须封装在 Helper 层**，不在 Service 中直接调用裸 SDK 方法
- SDK 更新 / 切换时只改适配层，不污染业务代码
- 缓存 / 消息队列同理，通过 Helper 隔离

`java
// ❌ 错误：Service 直接调用第三方 SDK
public void sendSms(String phone, String code) {
    AliyunSmsClient client = new AliyunSmsClient(...);
    client.send(phone, code);  // 换短信服务商需要改所有 Service
}

// ✅ 正确：封装在 Helper 中
@Component
public class SmsHelper {
    public void sendVerifyCode(String phone, String code) { ... }
}
// Service 只依赖 SmsHelper，不感知底层实现
`

---

> **使用方式**：本文件放项目根目录 specs/base-rules.md，AI 生成代码时自动读取。各模块 Spec 只写差异化内容，不重复本文件已覆盖的规则。
