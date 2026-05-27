package com.example.demo;

import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.Orders;
import com.example.demo.service.CartService;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
class OrdersServiceImplTest {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    private static boolean tablesCreated = false;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void ensureTables() throws Exception {
        if (!tablesCreated) {
            try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID'," +
                    "user_id INT NOT NULL COMMENT '用户ID'," +
                    "order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间'," +
                    "total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额'," +
                    "pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额'," +
                    "status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态'," +
                    "consignee VARCHAR(50) NOT NULL COMMENT '收货人'," +
                    "telephone VARCHAR(20) NOT NULL COMMENT '联系电话'," +
                    "city VARCHAR(50) NOT NULL COMMENT '城市'," +
                    "address VARCHAR(200) NOT NULL COMMENT '详细地址'," +
                    "INDEX idx_user_id (user_id)," +
                    "INDEX idx_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表'");
                stmt.execute("CREATE TABLE IF NOT EXISTS order_item (" +
                    "order_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID'," +
                    "order_id INT NOT NULL COMMENT '订单ID'," +
                    "product_id BIGINT NOT NULL COMMENT '商品ID'," +
                    "product_name VARCHAR(100) NOT NULL COMMENT '商品名称（快照）'," +
                    "price DECIMAL(10,2) NOT NULL COMMENT '商品价格（快照）'," +
                    "quantity INT NOT NULL COMMENT '购买数量'," +
                    "discount DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '折扣'," +
                    "amount DECIMAL(10,2) NOT NULL COMMENT '小计金额'," +
                    "INDEX idx_order_id (order_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表'");
            }
            tablesCreated = true;
        }
    }

    private static final Integer TEST_USER = 200;
    private static final Long PRODUCT_IPHONE = 1L;
    private static final Long PRODUCT_HUAWEI = 3L;

    private OrderCreateRequest validRequest() {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setConsignee("张三");
        req.setTelephone("13800138000");
        req.setCity("北京");
        req.setAddress("朝阳路100号");
        return req;
    }

    @Test
    @DisplayName("01_createOrder — 购物车为空抛异常")
    void t01_createOrder_shouldThrowWhenCartEmpty() {
        OrderCreateRequest req = validRequest();
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ordersService.createOrder(TEST_USER, req));
        assertTrue(ex.getMessage().contains("购物车中没有已选中的商品"));
    }

    @Test
    @DisplayName("02_createOrder — 正常创建订单")
    void t02_createOrder_shouldSucceed() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER, PRODUCT_HUAWEI, 1);

        OrderCreateVO result = ordersService.createOrder(TEST_USER, validRequest());

        assertNotNull(result.getOrderId());
        assertEquals(0, result.getStatus());
        assertTrue(result.getPayAmount().doubleValue() > 0);
        assertNotNull(result.getOrderDate());

        int count = cartService.getCartCount(TEST_USER);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("03_createOrder — 只结算已勾选商品，未勾选商品保留")
    void t03_createOrder_shouldOnlyCheckoutSelected() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER, PRODUCT_HUAWEI, 3);

        var cartList = cartService.getCartList(TEST_USER);
        cartService.updateSelected(TEST_USER, cartList.get(0).getCartId(), 0);

        OrderCreateVO result = ordersService.createOrder(TEST_USER, validRequest());

        assertNotNull(result.getOrderId());
        int count = cartService.getCartCount(TEST_USER);
        assertEquals(3, count);
        var remaining = cartService.getCartList(TEST_USER);
        assertEquals(1, remaining.size());
        assertEquals(PRODUCT_HUAWEI, remaining.get(0).getProductId());
    }

    @Test
    @DisplayName("04_createOrder — 下单后库存扣减")
    void t04_createOrder_shouldDecreaseStock() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 2);

        var cartList = cartService.getCartList(TEST_USER);
        int originalStock = cartList.get(0).getStock();

        ordersService.createOrder(TEST_USER, validRequest());

        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 1);
        var cartList2 = cartService.getCartList(TEST_USER);
        assertEquals(1, cartList2.get(0).getQuantity());
    }

    @Test
    @DisplayName("05_createOrder — 库存不足抛异常，数据回滚")
    void t05_createOrder_shouldRollbackWhenStockInsufficient() {
        Cart cart = new Cart();
        cart.setUserId(TEST_USER);
        cart.setProductId(PRODUCT_IPHONE);
        cart.setQuantity(99999);
        cart.setSelected(1);
        cartMapper.insert(cart);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                ordersService.createOrder(TEST_USER, validRequest()));
        assertTrue(ex.getMessage().contains("库存不足"));

        int count = cartService.getCartCount(TEST_USER);
        assertEquals(99999, count);
    }

    @Test
    @DisplayName("06_createOrder — 订单主表记录完整")
    void t06_createOrder_shouldHaveCompleteOrderRecord() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 1);

        OrderCreateRequest req = validRequest();
        OrderCreateVO result = ordersService.createOrder(TEST_USER, req);

        Orders order = ordersService.getById(result.getOrderId());
        assertNotNull(order);
        assertEquals(TEST_USER, order.getUserId());
        assertEquals(0, order.getStatus());
        assertEquals(req.getConsignee(), order.getConsignee());
        assertEquals(req.getTelephone(), order.getTelephone());
        assertEquals(req.getCity(), order.getCity());
        assertEquals(req.getAddress(), order.getAddress());
        assertTrue(order.getTotalAmount().doubleValue() > 0);
    }

    @Test
    @DisplayName("07_createOrder — 多用户订单隔离")
    void t07_createOrder_userIsolation() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 1);
        cartService.addToCart(201, PRODUCT_HUAWEI, 1);

        OrderCreateVO resultA = ordersService.createOrder(TEST_USER, validRequest());
        Orders orderA = ordersService.getById(resultA.getOrderId());
        assertEquals(TEST_USER, orderA.getUserId());

        int countB = cartService.getCartCount(201);
        assertEquals(1, countB);
    }

    @Test
    @DisplayName("08_createOrder — 订单金额计算正确")
    void t08_createOrder_shouldCalculateAmountCorrectly() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 2);

        var cartList = cartService.getCartList(TEST_USER);
        double unitPrice = cartList.get(0).getPrice();

        OrderCreateVO result = ordersService.createOrder(TEST_USER, validRequest());
        Orders order = ordersService.getById(result.getOrderId());

        assertEquals(unitPrice * 2, order.getTotalAmount().doubleValue(), 0.01);
        assertEquals(unitPrice * 2, order.getPayAmount().doubleValue(), 0.01);
    }

    // ==================== GET /orders/{order_id} ====================

    @Test
    @DisplayName("09_getOrderDetail — 正常查询订单详情")
    void t09_getOrderDetail_shouldReturnFullDetail() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER, PRODUCT_HUAWEI, 1);
        OrderCreateVO created = ordersService.createOrder(TEST_USER, validRequest());

        OrderDetailVO detail = ordersService.getOrderDetail(TEST_USER, created.getOrderId());

        assertNotNull(detail);
        assertEquals(created.getOrderId(), detail.getOrderId());
        assertEquals(TEST_USER, detail.getUserId());
        assertEquals(0, detail.getStatus());
        assertNotNull(detail.getConsignee());
        assertNotNull(detail.getItems());
        assertEquals(2, detail.getItems().size());
        // 验证订单项包含商品快照信息
        for (var item : detail.getItems()) {
            assertNotNull(item.getProductName());
            assertTrue(item.getPrice().doubleValue() > 0);
            assertTrue(item.getQuantity() > 0);
            assertTrue(item.getAmount().doubleValue() > 0);
        }
    }

    @Test
    @DisplayName("10_getOrderDetail — 订单不存在抛异常")
    void t10_getOrderDetail_shouldThrowWhenNotFound() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                ordersService.getOrderDetail(TEST_USER, 99999));
        assertEquals("订单不存在", ex.getMessage());
    }

    @Test
    @DisplayName("11_getOrderDetail — 越权查看他人订单抛异常")
    void t11_getOrderDetail_shouldThrowWhenNotOwner() {
        cartService.addToCart(TEST_USER, PRODUCT_IPHONE, 1);
        OrderCreateVO created = ordersService.createOrder(TEST_USER, validRequest());

        ForbiddenException ex = assertThrows(ForbiddenException.class, () ->
                ordersService.getOrderDetail(999, created.getOrderId()));
        assertEquals("无权限查看该订单", ex.getMessage());
    }
}
