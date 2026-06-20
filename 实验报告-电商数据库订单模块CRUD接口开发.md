# 实验报告：基于 SpringBoot3 + MyBatis Plus 实现电商数据库订单模块的 CRUD 接口

---

## 一、实现思路
 需求分析
订单模块是电商系统的核心模块，需要实现以下功能：
- **创建订单**：从购物车选中商品 → 校验库存 → 扣减库存 → 生成订单 → 清空购物车
- **查询订单详情**：根据订单ID查询订单主表和明细，包含权限校验

---

## 二、表结构 SQL

```sql
-- 订单主表
CREATE TABLE IF NOT EXISTS orders (
    order_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id     INT          NOT NULL COMMENT '用户ID',
    order_date  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    pay_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待付款 1-已付款 2-已发货 3-已完成 4-已取消',
    consignee   VARCHAR(50)  NOT NULL COMMENT '收货人',
    telephone   VARCHAR(20)  NOT NULL COMMENT '联系电话',
    city        VARCHAR(50)  NOT NULL COMMENT '城市',
    address     VARCHAR(200) NOT NULL COMMENT '详细地址',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id      INT          NOT NULL COMMENT '订单ID',
    product_id    BIGINT       NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    price         DECIMAL(10,2) NOT NULL COMMENT '商品价格（快照）',
    quantity      INT          NOT NULL COMMENT '购买数量',
    discount      DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '折扣',
    amount        DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
```

---

## 三、接口代码

---

## 四、测试报告

### 4.1 单元测试（JUnit 5）

### 4.2 REST 层测试（MockMvc）

### 4.3 测试运行命令

```bash
# 运行订单服务测试
mvn test -Dtest="OrdersServiceImplTest"

# 运行订单控制器测试
mvn test -Dtest="OrderControllerTest"

# 运行所有测试
mvn test
```

### 4.4 测试截图

```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 五、AI 提问内容

### 问题 1：订单模块的数据库表如何设计？

**AI 回答**：
订单模块通常采用主表+明细表的设计模式：
- `orders` 主表：存储订单的基本信息（用户、金额、状态、收货地址）
- `order_item` 明细表：存储订单中的商品信息（商品快照、数量、金额）

关键设计点：
1. 使用外键 `order_id` 关联主表和明细表
2. 明细表保存商品快照（`product_name`、`price`），防止商品信息变更后产生纠纷
3. 添加适当的索引提高查询性能

### 问题 2：如何实现乐观锁扣库存？

**AI 回答**：
使用 SQL 的 WHERE 条件实现乐观锁：

```sql
UPDATE product SET stock = stock - #{quantity} 
WHERE id = #{productId} AND stock >= #{quantity}
```

通过检查返回的影响行数（`affectedRows`）来判断是否成功：
- `affectedRows == 1`：扣减成功
- `affectedRows == 0`：库存不足，抛出异常

这种方式避免了使用版本号字段，简化了实现。

### 问题 3：为什么使用 @Transactional 注解？

**AI 回答**：
`@Transactional(rollbackFor = Exception.class)` 用于保证事务的原子性：

创建订单涉及多个数据库操作：
1. 扣减库存（UPDATE product）
2. 创建订单主表（INSERT orders）
3. 创建订单明细（INSERT order_item）
4. 清空购物车（DELETE cart）

如果其中任何一步失败，所有操作都需要回滚，否则会导致数据不一致（如库存扣了但订单没创建）。

### 问题 4：如何处理订单状态管理？

**AI 回答**：
订单状态使用 `status` 字段管理，定义如下：
- 0：待付款
- 1：已付款
- 2：已发货
- 3：已完成
- 4：已取消

状态流转：
```
待付款(0) → 已付款(1) → 已发货(2) → 已完成(3)
    ↓
  已取消(4)
```

每个状态变更操作需要校验前置状态的合法性。

### 问题 5：MyBatis-Plus 的 ServiceImpl 有什么优势？

**AI 回答**：
`ServiceImpl<Mapper, Entity>` 提供了以下优势：

1. **内置 CRUD 方法**：无需手写 `save()`、`remove()`、`update()`、`getById()`、`list()` 等
2. **链式查询**：支持 `LambdaQueryWrapper` 构建复杂查询条件
3. **批量操作**：支持 `saveBatch()`、`removeByIds()` 等批量方法
4. **分页支持**：配合分页插件实现自动分页
5. **代码简化**：通过 `baseMapper` 直接调用 Mapper 方法

---

## 六、项目代码仓库

### 6.1 仓库地址

- **GitHub**：https://github.com/Reyeraz/GXMZU-network-programming-practice
- **Gitee（后端）**：https://gitee.com/xiazzzzz/end-project

### 6.2 代码结构

```
end-project/
├── pom.xml
├── sql/
│   └── orders.sql                    # 订单表结构
├── src/main/java/com/example/demo/
│   ├── controller/
│   │   └── OrderController.java      # 订单控制器
│   ├── service/
│   │   ├── OrdersService.java        # 订单服务接口
│   │   └── impl/
│   │       └── OrdersServiceImpl.java # 订单服务实现
│   ├── mapper/
│   │   ├── OrdersMapper.java         # 订单主表 Mapper
│   │   ├── OrderItemMapper.java      # 订单明细 Mapper
│   │   └── ProductMapper.java        # 商品 Mapper（含扣库存）
│   ├── model/
│   │   ├── Orders.java               # 订单主表实体
│   │   └── OrderItem.java            # 订单明细实体
│   ├── dto/
│   │   └── OrderCreateRequest.java   # 创建订单请求 DTO
│   ├── vo/
│   │   ├── OrderCreateVO.java        # 订单创建响应 VO
│   │   ├── OrderDetailVO.java        # 订单详情 VO
│   │   └── OrderItemVO.java          # 订单项 VO
│   └── exception/
│       ├── BusinessException.java     # 业务异常基类
│       ├── BadRequestException.java   # 400 异常
│       ├── ForbiddenException.java    # 403 异常
│       └── ResourceNotFoundException.java # 404 异常
└── src/test/java/com/example/demo/
    ├── OrdersServiceImplTest.java     # 订单服务测试（11项）
    └── OrderControllerTest.java       # 订单控制器测试（12项）
```

### 6.3 提交记录

```
feat: 完成订单模块CRUD接口开发
- 创建订单（POST /orders/create）
- 查询订单详情（GET /orders/{order_id}）
- 乐观锁扣库存
- @Transactional 事务保护
- 自定义异常体系
- 23项单元测试全部通过
```

---

## 七、总结

### 7.1 完成的功能

1. ✅ 创建订单接口（POST /orders/create）
2. ✅ 查询订单详情接口（GET /orders/{order_id}）
3. ✅ 乐观锁库存扣减
4. ✅ 事务保护（原子性）
5. ✅ 自定义异常体系（400/403/404）
6. ✅ 参数校验（@Valid）
7. ✅ 权限校验（越权防护）
8. ✅ 单元测试（23项全部通过）

### 7.2 技术要点

| 技术点 | 实现方式 |
|--------|----------|
| ORM 框架 | MyBatis-Plus ServiceImpl + BaseMapper |
| 事务控制 | @Transactional(rollbackFor = Exception.class) |
| 库存扣减 | 乐观锁 SQL WHERE 条件 |
| 参数校验 | Jakarta Validation (@NotBlank, @Pattern) |
| 异常处理 | 自定义异常 + GlobalExceptionHandler |
| 权限控制 | JWT 拦截器 + userId 校验 |

### 7.3 后续扩展

- [ ] 订单列表查询（分页）
- [ ] 订单状态变更（付款/发货/收货/取消）
- [ ] 订单取消时恢复库存
- [ ] 订单超时自动取消
- [ ] 发票下载功能（PDF）

---

**实验完成日期**：2026-06-09
**实验人**：reyeraz
**指导教师**：___________
