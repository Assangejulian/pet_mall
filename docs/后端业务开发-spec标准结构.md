# 后端业务开发 Spec 标准结构

> 一份从需求到代码的全链路设计产出规范，每部分与对应设计图一一对应。
>
> 适用范围：Spring Boot / MyBatis-Plus 技术栈的 Java 后端项目。

---

## 目录

1. [分档策略：按复杂度选择 Spec 深度](#1-分档策略)
2. [顶层架构](#2-顶层架构)
   - [2.1 功能边界规格 ← 用例图](#21-功能边界规格--用例图)
   - [2.2 架构分层规则（全局）](#22-架构分层规则全局)
   - [2.3 业务规则规格 ← 活动图 + 状态图](#23-业务规则规格--活动图--状态图)
3. [中层模块结构](#3-中层模块结构)
   - [3.1 数据结构规格 ← ER 图](#31-数据结构规格--er-图)
   - [3.2 代码结构规格 ← 类图 + 顺序图](#32-代码结构规格--类图--顺序图)
   - [3.3 技术约束规格](#33-技术约束规格)
4. [实现层：可读性铁律](#4-实现层可读性铁律)
5. [验收层](#5-验收层)
6. [附录：Spec 模板](#6-附录spec-模板)
7. [设计原则映射表](#7-设计原则映射表)
8. [整洁代码核心标准](#8-整洁代码核心标准方法论基底)

---

## 1. 分档策略

| 档位 | 适用场景 | 必需内容 | 可省略内容 |
|------|---------|---------|-----------|
| **L1 极简** | 字典/配置/基础资料（纯 CRUD） | 表结构 + 接口清单 + 通用返回规范 | 活动图、状态图、业务流程描述 |
| **L2 标准** | 普通业务模块 | L1 + 功能边界 + 业务流程 + 分层要求 | 状态机、并发控制 |
| **L3 完整** | 核心复杂业务（订单/支付/审批） | L2 + 状态机 + 事务边界 + 并发控制 + 幂等 + 第三方交互 | — |

---

## 2. 顶层架构

### 2.1 功能边界规格 ← 用例图

#### 2.1.1 参与者与权限

| 角色 | 权限范围 | 数据隔离规则 |
|------|---------|-------------|
| 系统管理员 | 全部功能、全部数据 | 无限制 |
| 门店管理员 | 本门店及下级数据 | `store_id = ?` |
| 普通用户 | 仅本人数据 | `user_id = ?` |

#### 2.1.2 功能范围（接口清单）

| 序号 | 接口 | Method | URL | 说明 |
|------|------|--------|-----|------|
| 1 | 新增 | POST | `/api/xxx` | — |
| 2 | 删除 | DELETE | `/api/xxx/{id}` | 软删除 |
| 3 | 修改 | PUT | `/api/xxx/{id}` | — |
| 4 | 分页查询 | GET | `/api/xxx/page` | — |
| 5 | 详情 | GET | `/api/xxx/{id}` | — |

#### 2.1.3 明确排除项（AI 场景重中之重）

- ❌ 本次**不做**：批量导入/导出（二期）
- ❌ 本次**不做**：操作日志审计（单独模块）
- ❌ 本次**不做**：消息推送通知

#### 2.1.4 验收标准

- [ ] 所有接口通过 Swagger 可调通
- [ ] 权限校验覆盖所有接口
- [ ] 异常场景有明确的错误提示

---

### 2.2 架构分层规则（全局）

> ⚠️ 写入项目级 `specs/base-rules.md`，AI 每次生成代码自动读取，不重复写入功能 Spec。

#### 2.2.1 三层职责边界

| 层 | 能做什么 | 绝对不能做 |
|----|---------|-----------|
| **Controller** | 参数校验、调用 Service、组装 Response | ❌ 写业务逻辑、❌ 直接调 Mapper |
| **Service** | 业务逻辑、事务管理、调用 Mapper | ❌ 处理 HTTP 请求/响应对象 |
| **Mapper** | 数据访问、SQL 执行 | ❌ 写业务判断、❌ 调其他 Mapper |
| **Helper/Util** | 第三方 API 封装、可复用工具方法 | ❌ 持有业务状态 |

#### 2.2.2 包结构约定（按业务领域分包）

```
com.pat
├── {module}/                  # 业务模块（按领域）
│   ├── controller/            # XxxController
│   ├── service/               # IXxxService / XxxServiceImpl
│   ├── mapper/                # XxxMapper（继承 BaseMapper）
│   ├── domain/                # DO / DTO / VO / Query / 枚举
│   ├── helper/                # 模块内辅助类（可选）
│   └── task/                  # 定时任务（可选）
├── common/                    # 全局通用
│   ├── config/
│   ├── constant/
│   ├── domain/                # BaseEntity / Result / ResultError
│   ├── enums/                 # ErrorCode
│   ├── exception/
│   ├── interceptor/
│   └── util/
└── Application.java
```

> 推荐按业务领域分包（而非按技术层分包），理由：高内聚、模块边界清晰、易于拆微服务。
> 分层模型严格区分：DO（数据库）、DTO（入参）、VO（出参），**禁止混用**。

#### 2.2.3 命名统一规范

| 语义 | 统一用词 | ❌ 禁止 |
|------|---------|--------|
| 新增 | `add` | `save` / `insert` |
| 修改 | `update` | `edit` / `modify` |
| 删除 | `delete` / `remove` | `erase` |
| 分页查询 | `page` | `list` / `queryPage` |
| 列表查询 | `list` | `findAll` / `getAll` |
| 详情 | `getById` | `findById` / `detail` |

#### 2.2.4 阿里命名红线

| 规则 | 正例 | ❌ 反例 |
|------|------|--------|
| 禁止下划线/美元符首尾 | `userName` | `_userName` / `user$` |
| 禁止中文、拼音、中英混合 | `discountPromotion` | `daZhePromotion` |
| 常量全大写+下划线 | `MAX_RETRY_COUNT` | `MAX_NUM` |
| 布尔 POJO 变量禁止 is 前缀 | `deleted` | `isDeleted` |
| 包名全小写单数 | `com.pat.order.service` | `com.pat.order.services` |

#### 2.2.5 通用禁止项

- ❌ 禁止使用 `BeanUtils.copyProperties`（性能差、字段映射不可控）
- ❌ 禁止跨层调用（Controller 直接调 Mapper）
- ❌ 禁止为了设计模式而设计模式
- ❌ 禁止过度抽象：当前只有一个实现就不写接口
- ❌ 禁止布尔标识参数：拆成两个独立方法
- ❌ 禁止链式调用：`order.getUser().getAddr().getPhone()` → 封装方法
- ❌ 禁止日志字符串拼接：用参数化 `log.info("用户{}下单", userId)`

---

### 2.3 业务规则规格 ← 活动图 + 状态图

#### 2.3.1 正常业务流程

```
步骤 1：校验请求参数（必填项、格式、业务规则）
  ├─ 失败 → 返回 400 + 具体错误提示
  └─ 通过 ↓
步骤 2：查询关联数据是否存在
  ├─ 不存在 → 返回 404 + "关联数据不存在"
  └─ 存在 ↓
步骤 3：执行业务计算 / 状态检查
  ├─ 不满足条件 → 返回 400 + 业务错误提示
  └─ 满足 ↓
步骤 4：事务写入数据库
步骤 5：返回成功结果
```

#### 2.3.2 异常处理矩阵

| 异常场景 | 错误码 | HTTP 状态 | 提示文案 |
|---------|--------|-----------|---------|
| 参数校验失败 | `PARAM_ERROR` | 400 | 具体字段提示 |
| 数据不存在 | `DATA_NOT_FOUND` | 404 | "xxx 不存在" |
| 重复操作 | `DUPLICATE_OP` | 400 | "请勿重复操作" |
| 状态不允许 | `STATUS_FORBIDDEN` | 400 | "当前状态不支持此操作" |
| 权限不足 | `FORBIDDEN` | 403 | "无操作权限" |
| 系统异常 | `SYSTEM_ERROR` | 500 | "系统繁忙，请稍后重试" |

#### 2.3.3 状态流转（仅 L3 完整档位）

```
          ┌──────────┐
          │  待提交   │
          └────┬─────┘
               │ submit
          ┌────▼─────┐
     ┌────│  待审核   │────┐
     │    └────┬─────┘    │
     │ reject  │ approve  │ reject
     │    ┌────▼─────┐    │
     │    │  已通过   │    │
     │    └──────────┘    │
     │                    │
     └────►  已驳回  ◄────┘
          └──────────┘

合法流转：
- 待提交 → 待审核（submit）
- 待审核 → 已通过（approve）
- 待审核 → 已驳回（reject）
- 已通过 → 待审核（reject，回退审核）

非法流转（代码必须拦截）：
- 待提交 → 已通过（跳过审核）
- 已驳回 → 已通过（需重新提交）
```

#### 2.3.4 事务边界（仅 L3 完整档位）

| 操作 | 事务范围 | 隔离级别 |
|------|---------|---------|
| 创建订单 + 扣库存 | 整个方法 | READ_COMMITTED |
| 创建订单 + 扣库存 + 写流水 | 整个方法 | READ_COMMITTED |
| 定时扫描过期订单 | 每条记录独立事务 | READ_COMMITTED |

> 只写 CUD 需要事务；纯读操作不加事务。

#### 2.3.5 并发控制（仅 L3 完整档位）

| 场景 | 策略 | 实现方式 |
|------|------|---------|
| 扣库存 | 乐观锁 | `WHERE stock >= ? AND version = ?` |
| 防重复提交 | 幂等键 | Redis `SETNX` + 唯一索引 |
| 抢单 | 悲观锁 | `SELECT ... FOR UPDATE` |

#### 2.3.6 幂等要求（仅 L3 完整档位）

- 幂等键设计：`业务类型 + 业务单号`（如 `PAY_202607030001`）
- 重复请求返回首次结果（不抛异常）
- 幂等有效期：24 小时（Redis TTL）

---

## 3. 中层模块结构

### 3.1 数据结构规格 ← ER 图

#### 3.1.1 数据库表设计

```sql
CREATE TABLE `t_order` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`     VARCHAR(32)  NOT NULL                COMMENT '订单号（唯一）',
  `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL               COMMENT '订单总金额',
  `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0待提交 1待审核 2已通过 3已驳回',
  `remark`       VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0正常 1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_create` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
```

#### 3.1.2 实体对应关系

| 概念 | Java 类 | 说明 |
|------|---------|------|
| DO（Data Object） | `Order.java` | 与表字段一一对应，放在 `domain/` |
| DTO（Data Transfer Object） | `OrderCreateDTO.java` | 接收前端入参，放在 `domain/` |
| VO（View Object） | `OrderVO.java` | 返回给前端的视图对象，放在 `domain/` |
| Query | `OrderQuery.java` | 分页/列表查询条件，放在 `domain/` |

#### 3.1.3 DO/DTO/VO 转换规则

- **禁止**：DO 直接返回给 Controller（会暴露表结构）
- **禁止**：DTO 直接传给 Mapper（字段不对齐）
- **推荐**：Service 层手动转换，或用 MapStruct（需统一评审后引入）

---

### 3.2 代码结构规格 ← 类图 + 顺序图

#### 3.2.1 调用链路（顺序）

```
Client
  │
  ▼
Controller.orderCreate(OrderCreateDTO)
  │  1. 参数校验（@Valid）
  │  2. 调用 Service
  ▼
OrderService.createOrder(OrderCreateDTO)
  │  1. 业务规则校验
  │  2. 状态检查
  │  3. 数据组装（DTO → DO）
  │  4. 调用 Helper（第三方交互）
  │  5. 调用 Mapper
  ▼
OrderMapper.insert(Order)
  │  SQL: INSERT INTO t_order ...
  ▼
  DB
```

#### 3.2.2 类设计约束

| 层 | 设计约束 |
|----|---------|
| Controller | 不加 `@Transactional`，只做参数校验与结果封装 |
| Service | 接口 + 实现类（简单 CRUD 可直接用实现类，省去接口） |
| ServiceImpl | 加 `@Transactional(rollbackFor = Exception.class)` |
| Mapper | 继承 `BaseMapper<T>`（MyBatis-Plus），复杂 SQL 用 XML |
| DO | 加 `@TableName`、`@TableId`、`@TableLogic`、实现 `Serializable` |
| DTO | 加 `@Valid` 校验注解，每个字段加注释 |
| VO | 按前端需求裁剪字段，不暴露敏感数据 |
| Helper | 封装第三方 SDK / 缓存 / 消息队列，隔离外部依赖 |

---

### 3.3 技术约束规格

#### 3.3.1 技术栈限制

| 类别 | 必须使用 | 禁止引入 |
|------|---------|---------|
| 框架 | Spring Boot 3.x | — |
| ORM | MyBatis-Plus 3.5+ | JPA / Hibernate |
| JSON | Jackson（Spring Boot 默认） | Fastjson |
| 工具类 | Hutool | Apache Commons（避免重复） |
| 数据库 | MySQL 8.0+ | — |
| 缓存 | Redis | — |

#### 3.3.2 设计原则（KISS / YAGNI / DRY）

| 原则 | 核心 | 落地规则 |
|------|------|---------|
| **KISS** | 代码阅读次数 ≫ 编写次数 | 简单判断用 `if-else`，不为了优雅上策略模式 |
| **YAGNI** | 不做功能预埋，但做架构预留 | 当前只有一个实现，不做接口抽象；但把逻辑封装在独立模块 |
| **DRY** | 重复的是业务语义才需要统一 | 改 A 处 B 处必须跟着改 → 合并；否则各自独立 |

#### 3.3.3 非功能要求

| 要求 | 规范 |
|------|------|
| 分页 | 统一使用 MyBatis-Plus `Page<T>`，页码从 1 开始 |
| 统一返回 | `Result<T>` 封装：`code`、`message`、`data`、`description` |
| 异常码 | 统一枚举类 `ErrorCode`，格式：`模块_错误类型` |
| 时间格式 | 统一 `yyyy-MM-dd HH:mm:ss`，Jackson 全局配置 |
| 安全 | 所有 SQL 参数化，禁止拼接字符串 |
| 日志 | 参数化日志，禁止字符串拼接；敏感信息脱敏 |
| 集合返回 | 禁止返回 null，统一返回 `new ArrayList<>()` |

---

## 4. 实现层：可读性铁律

> 核心原则：**代码是写给人看的，顺便让机器执行。**

### 4.1 禁止为了设计模式而设计模式

```java
// ❌ 错误示范：只有一种支付方式就上策略模式
interface PaymentStrategy { void pay(Order order); }
class AlipayPayment implements PaymentStrategy { ... }
class PaymentContext { ... }  // 过度抽象

// ✅ 正确示范：直白 if-else
public void pay(Order order) {
    if (order.getPayType() == PayTypeEnum.ALIPAY) {
        alipayService.pay(order);
    }
}
// 只有当分支 ≥ 5 且未来明确会扩展时，才考虑策略模式
```

### 4.2 禁止一行流炫技

```java
// ❌ 错误示范
return orders.stream().filter(o -> o.getStatus() == 1)
    .map(o -> { o.setAmount(o.getAmount().multiply(new BigDecimal("0.9"))); return o; })
    .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
    .limit(10).collect(Collectors.toList());

// ✅ 正确示范：分步写清
List<Order> paidOrders = new ArrayList<>();
for (Order order : orders) {
    if (order.getStatus() == OrderStatusEnum.PAID.getCode()) {
        paidOrders.add(order);
    }
}
paidOrders.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
if (paidOrders.size() > 10) {
    paidOrders = paidOrders.subList(0, 10);
}
```

### 4.3 方法体量约束

| 指标 | 红线 |
|------|------|
| 单方法 | ≤ 50 行（含空行和注释） |
| Controller 方法 | ≤ 20 行 |
| 嵌套层数 | ≤ 2 层 |
| 参数个数 | ≤ 3 个（超过封装为 DTO） |

### 4.4 方法提取行业共识（Martin Fowler + Google Java Style）

| 场景 | 决策 |
|------|------|
| 逻辑被 ≥ 2 处调用 | 抽取 |
| 私有方法只有 1 处调用，且逻辑 ≤ 5 行 | **不抽**（过度拆分伤害可读性） |
| 私有方法 ≥ 10 行且有独立语义 | 抽取 |
| 第三方 API 调用 | 全部抽到 Helper |

```java
// ✅ 正确：主方法只看流程，细节下沉到子方法
public double calculateOrderTotal(Order order) {
    validateOrder(order);
    double itemTotal = sumItemPrices(order);
    double discountTotal = applyDiscount(itemTotal);
    return addShippingFee(discountTotal, order);
}
```

### 4.5 禁止无意义抽象（YAGNI）

```java
// ❌ 错误示范：整个项目只有一个实现也要写接口
public interface UserService {
    UserVO getById(Long id);
}
@Service
public class UserServiceImpl implements UserService { ... }

// ✅ 正确示范：直接实现类，将来真有第二个实现再抽接口
@Service
public class UserService {
    public UserVO getById(Long id) { ... }
}
```

### 4.6 业务语义优先

| ❌ 禁止（模糊） | ✅ 推荐（直白） |
|----------------|----------------|
| `process()` | `createOrder()` / `approveOrder()` |
| `handle()` | `deductStock()` / `refundAmount()` |
| `data` | `orderCreateDTO` / `paymentResult` |
| `flag` / `temp` | `hasPaid` / `isExpired` |
| `userInfo` / `userData` | `getBasicUser()` / `getUserDetailWithOrder()` |

---

## 5. 验收层

### 5.1 架构可读性验收清单

- [ ] 所有文件位置符合项目包结构约定
- [ ] 严格遵守三层职责边界，无跨层调用
- [ ] 类名、方法名符合命名规范，见名知意
- [ ] 核心业务方法 ≤ 50 行，逻辑分段清晰
- [ ] 没有无意义的抽象类、接口和通用基类
- [ ] 没有使用项目禁止的工具和写法
- [ ] 异常提示清晰，错误码符合统一规范
- [ ] DO 未直接暴露到 Controller 层
- [ ] 所有 SQL 使用参数化查询，无字符串拼接
- [ ] 无布尔标识参数、无超长链式调用
- [ ] 方法不返回 null 集合

### 5.2 自动化检查工具建议

| 工具 | 检查内容 |
|------|---------|
| SonarQube | 方法行数、圈复杂度、代码重复率 |
| 阿里 Java 规约插件（P3C） | 命名规范、分层约束、OOP 规约、格式 |
| Checkstyle | 代码风格统一（4 空格缩进等） |
| JUnit + JaCoCo | 单元测试覆盖率（目标 ≥ 70%） |

### 5.3 人工 Review 快速对照表（一票否决项）

| 关注点 | 一票否决项 |
|--------|-----------|
| 跨层调用 | Controller 直接调 Mapper → 打回 |
| 禁用写法 | `BeanUtils.copyProperties` → 打回 |
| 超长方法 | 单个方法 > 50 行 → 打回 |
| 无意义抽象 | 只有一个实现但写了接口 → 打回 |
| 命名模糊 | `handle()` / `process()` → 打回 |
| 吞异常 | 空 catch 块无日志 → 打回 |
| 返回 null | 方法返回 null 集合 → 打回 |

### 5.4 糟糕代码自查清单（来自 Clean Code）

- [ ] 有无命名随意、语义模糊的变量/方法？
- [ ] 有无超长函数、嵌套多层 if？
- [ ] 有无大量复制粘贴的重复代码？
- [ ] 是否靠注释弥补烂代码？（好代码自解释）
- [ ] 有无硬编码、魔法数字？
- [ ] 有无全局变量、严重耦合？

---

## 6. 附录：Spec 模板

### A. L1 极简模板（字典/配置 CRUD）

```markdown
# [模块名] Spec（L1）

## 1. 接口清单
| 序号 | 接口 | Method | URL |
|------|------|--------|-----|
| 1 | 分页 | GET | /api/dict/page |
| 2 | 新增 | POST | /api/dict |
| 3 | 修改 | PUT | /api/dict/{id} |
| 4 | 删除 | DELETE | /api/dict/{id} |

## 2. 表结构
（DDL 语句）

## 3. 返回规范
遵循项目统一 Result<T> 格式，异常码见 base-rules.md
```

### B. L2 标准模板（普通业务模块）

```markdown
# [模块名] Spec（L2）

## 1. 功能边界
- 参与者与权限：（表格）
- 接口清单：（表格）
- 明确排除项：（列表）

## 2. 业务流程
- 正常流程：（步骤描述）
- 异常处理：（矩阵表格）

## 3. 数据结构
- 表结构：（DDL + 索引说明）
- DTO/VO 字段定义

## 4. 代码分层要求
- 调用链路
- 关键校验逻辑位置
```

### C. L3 完整模板（核心复杂业务）

```markdown
# [模块名] Spec（L3）

## 1. 功能边界
（同 L2）

## 2. 业务流程
- 正常流程
- 异常处理
- **状态流转图**（ASCII 图 + 合法/非法流转表）
- **事务边界**（表格）
- **并发控制策略**
- **幂等设计**

## 3. 数据结构
（同 L2）

## 4. 代码分层要求
（同 L2）

## 5. 第三方交互
- 调用时机
- 超时 / 重试 / 降级策略

## 6. 验收标准
（验收清单 + 核心场景测试用例）
```

---

## 7. 设计原则映射表

> 每个 Spec 层级对应的核心设计原则，AI 生成代码时必须遵守。

### 7.1 原则速查

| 原则 | 一句话 | 在 Spec 中的落点 |
|------|--------|-----------------|
| **DRY** | 重复的是"业务语义"才需统一，只是"代码像"不需要 | 2.3 业务规则：避免硬揉无关逻辑 |
| **KISS** | 代码阅读次数远多于编写次数，简单直白优先 | 4 实现层：禁止炫技、禁止过度模式 |
| **YAGNI** | 不做功能预埋，但做架构预留 | 2.2 分层规则：模块封装边界 |
| **单一职责** | 函数/类只做一件事 | 3.2 类设计约束 |
| **迪米特法则** | 不链式调用别人返回的对象 | 4.2 禁止炫技：封装链式调用 |
| **Ponytail** | 优先原生、直白实现，不引入不必要封装 | 4 全部铁律 |

### 7.2 DRY 正确运用

```
错误理解：代码少了 = 好了
正确理解：同一业务语义只有一个权威来源

判断标准：
- 改A处逻辑，B处是否一定也要跟着改？→ 是：DRY，合并
- 改A处逻辑，B处不受影响？           → 否：不必DRY，各自独立
```

### 7.3 KISS 正确运用

```
一个 if-else 能解决 → 不要上策略模式（过度设计）
直接写 SQL 就行     → 不要封装三层抽象（无意义封装）
简单判断直写        → 分支 > 5 且明确会扩展时再重构
代码让人秒懂        → 比"显得高级"重要一万倍
```

### 7.4 YAGNI 正确运用

```
极端YAGNI：全量硬编码，后续改一处牵动全身
过度设计：提前做万能配置项、可插拔架构

正确姿势：
- 不做功能预埋：不提前写未上线业务逻辑
- 但做架构预留：支付逻辑封装在独立模块，未来新增渠道不改主流程
    → 这不是过度设计，是合理的封装边界
```

### 7.5 重构策略分类

> 参考 Martin Fowler + Joshua Kerievsky

| 策略 | 目标 | 何时用 |
|------|------|--------|
| **实现模式** | 直接重构为明确模式 | 已存在明显坏味道，如10个if-else→策略模式 |
| **趋向模式** | 铺垫中间结构，降低后续重构成本 | 需求可能变化但尚未明确 |
| **去除模式** | 安全移除失效的模式 | 模式因需求变更变得冗余（如单一支付方式无需工厂） |

---

## 8. 整洁代码核心标准（方法论基底）

> 来自 Clean Code，作为所有 Spec 的方法论基底：

1. **易读**：旁人几分钟看懂逻辑，不用反复猜意图
2. **单一职责**：函数/类只做一件事
3. **少重复**：消除重复逻辑（DRY，语义相同才消除）
4. **完整测试**：单元测试覆盖率高，修改不怕崩
5. **最小冗余**：无多余变量、注释、逻辑分支
6. **简洁克制**：不炫技，简单直白
7. **及时纠错**：异常处理完善，无隐藏错误

### 糟糕代码特征清单（Code Review 直接对照）

- 命名随意、语义模糊
- 超长函数、嵌套多层if
- 大量复制粘贴重复代码
- 靠注释弥补烂代码
- 无测试、硬编码、魔法数字
- 全局变量满天飞、耦合严重
- 空catch块吞异常

---

> **协作方式建议**：`base-rules.md` 放项目根目录 `specs/`（分层规则、命名规范、禁止项），各模块 Spec 只写差异化内容，避免重复。设计原则（DRY/KISS/YAGNI）已内置在实现层铁律中，按需引用即可。

---

> 两份文档分工：
> - **本文件**（Spec 标准结构）：方法论 + 模板 + 设计原则
> - **`specs/base-rules.md`**：AI 自动读取的落地红线（347行，含全部命名/格式/禁止项/可读性铁律）