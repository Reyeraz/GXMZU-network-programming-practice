# 实验报告：基于 SpringBoot3 + MyBatis Plus 实现电商数据库订单模块的 CRUD 接口

---

## 一、实现思路

### 1.1 需求分析

订单模块是电商系统的核心模块，需要实现以下功能：
- **创建订单**：从购物车选中商品 → 校验库存 → 扣减库存 → 生成订单 → 清空购物车
- **查询订单详情**：根据订单ID查询订单主表和明细，包含权限校验

### 1.2 技术选型

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.4 | Web 框架 |
| MyBatis-Plus | 3.5.10 | ORM 框架，简化 CRUD |
| MySQL | 8.x | 数据库 |
| Lombok | - | 简化 Java 代码 |
| Jakarta Validation | - | 参数校验 |

### 1.3 数据库设计

采用**主表+明细表**的经典订单模型：
- `orders` 订单主表：存储订单基本信息（用户、金额、状态、收货地址）
- `order_item` 订单明细表：存储订单商品信息（商品快照、数量、金额）

**关键设计决策**：
1. **商品快照**：订单明细保存 `product_name` 和 `price`，防止商品信息变更后产生纠纷
2. **乐观锁扣库存**：`UPDATE product SET stock = stock - #{quantity} WHERE stock >= #{quantity}`，通过影响行数判断是否成功
3. **事务保护**：`@Transactional` 保证库存扣减、订单生成、购物车清空的原子性

### 1.4 分层架构

```
Controller 层 → 接收请求，参数校验，调用 Service
     ↓
Service 层 → 业务逻辑（事务控制、库存校验、订单生成）
     ↓
Mapper 层 → 数据访问（MyBatis-Plus BaseMapper）
     ↓
Model 层 → 实体类（@TableName 映射数据库表）
```

### 1.5 接口设计

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建订单 | POST | `/orders/create` | 从购物车选中商品创建订单 |
| 查询订单详情 | GET | `/orders/{order_id}` | 查询订单主表+明细 |

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

**表字段说明**：

| 表名 | 字段 | 类型 | 说明 |
|------|------|------|------|
| orders | order_id | INT | 主键，自增 |
| orders | user_id | INT | 外键，关联用户表 |
| orders | order_date | DATETIME | 下单时间，默认当前时间 |
| orders | total_amount | DECIMAL(10,2) | 订单总金额 |
| orders | pay_amount | DECIMAL(10,2) | 实际支付金额 |
| orders | status | TINYINT | 订单状态：0待付款/1已付款/2已发货/3已完成/4已取消 |
| orders | consignee | VARCHAR(50) | 收货人姓名 |
| orders | telephone | VARCHAR(20) | 联系电话 |
| orders | city | VARCHAR(50) | 城市 |
| orders | address | VARCHAR(200) | 详细地址 |
| order_item | order_item_id | INT | 主键，自增 |
| order_item | order_id | INT | 外键，关联订单主表 |
| order_item | product_id | BIGINT | 外键，关联商品表 |
| order_item | product_name | VARCHAR(100) | 商品名称快照 |
| order_item | price | DECIMAL(10,2) | 商品价格快照 |
| order_item | quantity | INT | 购买数量 |
| order_item | discount | DECIMAL(10,2) | 折扣，默认1.00 |
| order_item | amount | DECIMAL(10,2) | 小计金额 = price × quantity × discount |

---

## 三、接口代码

### 3.1 实体类

#### Orders.java（订单主表实体）
```java
package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    @TableField("user_id")
    private Integer userId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String consignee;

    private String telephone;

    private String city;

    private String address;
}
```

#### OrderItem.java（订单明细实体）
```java
package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(value = "order_item_id", type = IdType.AUTO)
    private Integer orderItemId;

    @TableField("order_id")
    private Integer orderId;

    @TableField("product_id")
    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal discount;

    private BigDecimal amount;
}
```

### 3.2 DTO（数据传输对象）

#### OrderCreateRequest.java（创建订单请求）
```java
package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotBlank(message = "收货人不能为空")
    private String consignee;

    @NotBlank(message = "电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话格式不正确")
    private String telephone;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "详细地址不能为空")
    private String address;
}
```

### 3.3 VO（视图对象）

#### OrderCreateVO.java（订单创建响应）
```java
package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateVO {
    private Integer orderId;
    private Integer status;
    private BigDecimal payAmount;
    private LocalDateTime orderDate;
}
```

#### OrderDetailVO.java（订单详情响应）
```java
package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Integer orderId;
    private Integer userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String consignee;
    private String telephone;
    private String city;
    private String address;
    private List<OrderItemVO> items;
}
```

#### OrderItemVO.java（订单项视图）
```java
package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Integer orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal amount;
}
```

### 3.4 Mapper 层

#### OrdersMapper.java
```java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
```

#### OrderItemMapper.java
```java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
```

#### ProductMapper.java（新增扣库存方法）
```java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 乐观锁扣减库存
     * @param productId 商品ID
     * @param quantity 购买数量
     * @return 影响行数（0表示库存不足）
     */
    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
```

### 3.5 Service 层

#### OrdersService.java（接口）
```java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.model.Orders;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;

public interface OrdersService extends IService<Orders> {

    OrderCreateVO createOrder(Integer userId, OrderCreateRequest request);

    OrderDetailVO getOrderDetail(Integer userId, Integer orderId);
}
```

#### OrdersServiceImpl.java（实现）
```java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Orders;
import com.example.demo.model.Product;
import com.example.demo.service.OrderAsyncService;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;
import com.example.demo.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 * 使用 MyBatis-Plus ServiceImpl 简化 CRUD 操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderAsyncService orderAsyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Integer userId, OrderCreateRequest request) {
        // 1. 查询已选中的购物车商品
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1);
        List<Cart> selectedCartList = cartMapper.selectList(cartWrapper);

        if (selectedCartList == null || selectedCartList.isEmpty()) {
            throw new BadRequestException("购物车中没有已选中的商品");
        }

        // 2. 校验商品状态并计算金额
        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : selectedCartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getIsAvailable() == null || product.getIsAvailable() != 1) {
                throw new BadRequestException("商品[" + (product != null ? product.getName() : "未知") + "]已下架或不存在");
            }

            // 同步扣减库存（事务内保证原子性）
            int affectedRows = productMapper.decreaseStock(product.getId(), cart.getQuantity());
            if (affectedRows == 0) {
                throw new BadRequestException("商品[" + product.getName() + "]库存不足");
            }

            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal discount = new BigDecimal("1.00");
            BigDecimal amount = productPrice.multiply(new BigDecimal(cart.getQuantity())).multiply(discount);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(productPrice);
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setDiscount(discount);
            orderItem.setAmount(amount);
            orderItemList.add(orderItem);

            totalAmount = totalAmount.add(amount);
        }

        // 3. 创建订单主表
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setConsignee(request.getConsignee());
        order.setTelephone(request.getTelephone());
        order.setCity(request.getCity());
        order.setAddress(request.getAddress());
        baseMapper.insert(order);

        // 4. 批量插入订单明细
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getOrderId());
            orderItemMapper.insert(orderItem);
        }

        // 5. 同步清空已选购物车
        for (Cart cart : selectedCartList) {
            cartMapper.deleteById(cart.getCartId());
        }

        // 6. 构建返回结果
        OrderCreateVO result = new OrderCreateVO();
        result.setOrderId(order.getOrderId());
        result.setStatus(order.getStatus());
        result.setPayAmount(order.getPayAmount());
        result.setOrderDate(order.getOrderDate());

        log.info("订单创建成功: orderId={}, userId={}, payAmount={}", order.getOrderId(), userId, order.getPayAmount());
        return result;
    }

    @Override
    public OrderDetailVO getOrderDetail(Integer userId, Integer orderId) {
        Orders orders = baseMapper.selectById(orderId);
        if (orders == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        if (!orders.getUserId().equals(userId)) {
            throw new ForbiddenException("无权限查看该订单");
        }

        LambdaQueryWrapper<OrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
        orderItemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemWrapper);

        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = new OrderItemVO();
            orderItemVO.setOrderItemId(orderItem.getOrderItemId());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setPrice(orderItem.getPrice());
            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setDiscount(orderItem.getDiscount());
            orderItemVO.setAmount(orderItem.getAmount());
            orderItemVOList.add(orderItemVO);
        }

        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setOrderId(orders.getOrderId());
        orderDetailVO.setUserId(orders.getUserId());
        orderDetailVO.setOrderDate(orders.getOrderDate());
        orderDetailVO.setTotalAmount(orders.getTotalAmount());
        orderDetailVO.setPayAmount(orders.getPayAmount());
        orderDetailVO.setStatus(orders.getStatus());
        orderDetailVO.setConsignee(orders.getConsignee());
        orderDetailVO.setTelephone(orders.getTelephone());
        orderDetailVO.setCity(orders.getCity());
        orderDetailVO.setAddress(orders.getAddress());
        orderDetailVO.setItems(orderItemVOList);
        return orderDetailVO;
    }
}
```

### 3.6 Controller 层

#### OrderController.java
```java
package com.example.demo.controller;

import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.model.ApiResponse;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrdersService ordersService;

    /**
     * 创建订单
     * POST /orders/create
     */
    @PostMapping("/create")
    public ApiResponse<OrderCreateVO> createOrder(HttpServletRequest request,
                                                   @Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        OrderCreateVO orderCreateVO = ordersService.createOrder(userId, orderCreateRequest);
        return ApiResponse.ok(orderCreateVO);
    }

    /**
     * 查询订单详情
     * GET /orders/{order_id}
     */
    @GetMapping("/{order_id}")
    public ApiResponse<OrderDetailVO> getOrderDetail(HttpServletRequest request,
                                                      @PathVariable("order_id") Integer orderId) {
        Integer userId = (Integer) request.getAttribute("userId");
        OrderDetailVO orderDetailVO = ordersService.getOrderDetail(userId, orderId);
        return ApiResponse.ok(orderDetailVO);
    }
}
```

### 3.7 自定义异常

```java
// BusinessException.java — 基类
package com.example.demo.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// BadRequestException.java — 400
public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(400, message);
    }
}

// ForbiddenException.java — 403
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}

// ResourceNotFoundException.java — 404
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
```

---

## 四、测试报告

### 4.1 单元测试（JUnit 5）

测试类：`OrdersServiceImplTest.java`

| 序号 | 测试方法 | 测试场景 | 预期结果 | 实际结果 |
|------|----------|----------|----------|----------|
| 01 | t01_createOrder_shouldThrowWhenCartEmpty | 购物车为空创建订单 | 抛出 BadRequestException | ✅ 通过 |
| 02 | t02_createOrder_shouldSucceed | 正常创建订单 | 返回订单ID、状态、金额 | ✅ 通过 |
| 03 | t03_createOrder_shouldOnlyCheckoutSelected | 只结算已勾选商品 | 未勾选商品保留 | ✅ 通过 |
| 04 | t04_createOrder_shouldDecreaseStock | 下单后库存扣减 | 库存减少 | ✅ 通过 |
| 05 | t05_createOrder_shouldRollbackWhenStockInsufficient | 库存不足抛异常 | 事务回滚 | ✅ 通过 |
| 06 | t06_createOrder_shouldHaveCompleteOrderRecord | 订单主表记录完整 | 所有字段正确 | ✅ 通过 |
| 07 | t07_createOrder_userIsolation | 多用户订单隔离 | 用户数据独立 | ✅ 通过 |
| 08 | t08_createOrder_shouldCalculateAmountCorrectly | 订单金额计算正确 | 金额 = 单价 × 数量 | ✅ 通过 |
| 09 | t09_getOrderDetail_shouldReturnFullDetail | 正常查询订单详情 | 返回完整订单信息 | ✅ 通过 |
| 10 | t10_getOrderDetail_shouldThrowWhenNotFound | 订单不存在 | 抛出 ResourceNotFoundException | ✅ 通过 |
| 11 | t11_getOrderDetail_shouldThrowWhenNotOwner | 越权查看他人订单 | 抛出 ForbiddenException | ✅ 通过 |

**测试结果**：11 项测试全部通过 ✅

### 4.2 REST 层测试（MockMvc）

测试类：`OrderControllerTest.java`

| 序号 | 测试场景 | HTTP 状态码 | 响应内容 | 结果 |
|------|----------|-------------|----------|------|
| 1 | 正常创建订单 | 200 | success=true, data.orderId | ✅ |
| 2 | 参数校验失败（空字段） | 400 | success=false | ✅ |
| 3 | 业务异常（购物车为空） | 400 | success=false | ✅ |
| 4 | 正常查询订单详情 | 200 | success=true, data.items | ✅ |
| 5 | 订单不存在 | 404 | success=false | ✅ |
| 6 | 越权查看 | 403 | success=false | ✅ |

**测试结果**：12 项测试全部通过 ✅

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
