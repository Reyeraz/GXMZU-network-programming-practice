package com.example.demo;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.config.WebConfig;
import com.example.demo.controller.OrderController;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;
import com.example.demo.vo.OrderItemVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {OrderController.class, GlobalExceptionHandler.class, SecurityConfig.class, WebConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrdersService ordersService;

    @MockitoBean
    private JwtInterceptor jwtInterceptor;

    private static final Integer TEST_USER = 100;

    @BeforeEach
    void setUp() throws Exception {
        when(jwtInterceptor.preHandle(any(), any(), any())).thenAnswer(invocation -> {
            jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
            request.setAttribute("userId", TEST_USER);
            return true;
        });
    }

    private OrderCreateRequest validRequest() {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setConsignee("张三");
        req.setTelephone("13800138000");
        req.setCity("北京");
        req.setAddress("朝阳路100号");
        return req;
    }

    private OrderCreateVO mockResult() {
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(1);
        vo.setStatus(0);
        vo.setPayAmount(new BigDecimal("4799.00"));
        vo.setOrderDate(LocalDateTime.now());
        return vo;
    }

    @Nested
    @DisplayName("POST /orders/create — 创建订单")
    class CreateOrderTests {

        @Test
        @DisplayName("200 — 正常创建订单")
        void shouldCreateOrder() throws Exception {
            OrderCreateVO mockVO = mockResult();
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class))).thenReturn(mockVO);

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(1))
                    .andExpect(jsonPath("$.data.status").value(0))
                    .andExpect(jsonPath("$.data.payAmount").value(4799.00))
                    .andExpect(jsonPath("$.data.orderDate").exists());

            verify(ordersService).createOrder(eq(TEST_USER), any(OrderCreateRequest.class));
        }

        @Test
        @DisplayName("参数校验失败 — 收货人为空")
        void shouldFailWhenConsigneeBlank() throws Exception {
            OrderCreateRequest req = validRequest();
            req.setConsignee("");

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — 电话为空")
        void shouldFailWhenTelephoneBlank() throws Exception {
            OrderCreateRequest req = validRequest();
            req.setTelephone("");

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — 电话格式不正确")
        void shouldFailWhenTelephoneInvalid() throws Exception {
            OrderCreateRequest req = validRequest();
            req.setTelephone("12345");

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — 城市为空")
        void shouldFailWhenCityBlank() throws Exception {
            OrderCreateRequest req = validRequest();
            req.setCity("");

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — 详细地址为空")
        void shouldFailWhenAddressBlank() throws Exception {
            OrderCreateRequest req = validRequest();
            req.setAddress("");

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("业务异常 — 购物车无选中商品")
        void shouldReturnFailWhenCartEmpty() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new BadRequestException("购物车中没有已选中的商品"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("购物车中没有已选中的商品"));
        }

        @Test
        @DisplayName("业务异常 — 库存不足")
        void shouldReturnFailWhenStockInsufficient() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new BadRequestException("商品[iPhone 15]库存不足"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("商品[iPhone 15]库存不足"));
        }

        @Test
        @DisplayName("业务异常 — 商品已下架")
        void shouldReturnFailWhenProductUnavailable() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new BadRequestException("商品[iPhone 15]已下架或不存在"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("商品[iPhone 15]已下架或不存在"));
        }
    }

    // ==================== GET /orders/{order_id} ====================

    private OrderDetailVO mockOrderDetail() {
        OrderDetailVO detail = new OrderDetailVO();
        detail.setOrderId(1);
        detail.setUserId(TEST_USER);
        detail.setOrderDate(LocalDateTime.now());
        detail.setTotalAmount(new BigDecimal("7999.00"));
        detail.setPayAmount(new BigDecimal("7999.00"));
        detail.setStatus(0);
        detail.setConsignee("张三");
        detail.setTelephone("13800138000");
        detail.setCity("北京");
        detail.setAddress("朝阳路100号");

        OrderItemVO item = new OrderItemVO();
        item.setOrderItemId(1);
        item.setProductId(1L);
        item.setProductName("iPhone 15 Pro");
        item.setPrice(new BigDecimal("7999.00"));
        item.setQuantity(1);
        item.setDiscount(new BigDecimal("1.00"));
        item.setAmount(new BigDecimal("7999.00"));
        detail.setItems(List.of(item));
        return detail;
    }

    @Nested
    @DisplayName("GET /orders/{order_id} — 获取订单详情")
    class GetOrderDetailTests {

        @Test
        @DisplayName("200 — 正常返回订单详情")
        void shouldReturnOrderDetail() throws Exception {
            OrderDetailVO mockVO = mockOrderDetail();
            when(ordersService.getOrderDetail(TEST_USER, 1)).thenReturn(mockVO);

            mockMvc.perform(get("/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(1))
                    .andExpect(jsonPath("$.data.status").value(0))
                    .andExpect(jsonPath("$.data.consignee").value("张三"))
                    .andExpect(jsonPath("$.data.items[0].productName").value("iPhone 15 Pro"))
                    .andExpect(jsonPath("$.data.items[0].price").value(7999.00))
                    .andExpect(jsonPath("$.data.items[0].quantity").value(1));

            verify(ordersService).getOrderDetail(TEST_USER, 1);
        }

        @Test
        @DisplayName("业务异常 — 订单不存在")
        void shouldReturnFailWhenOrderNotFound() throws Exception {
            when(ordersService.getOrderDetail(TEST_USER, 999))
                    .thenThrow(new ResourceNotFoundException("订单不存在"));

            mockMvc.perform(get("/orders/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("订单不存在"));
        }

        @Test
        @DisplayName("业务异常 — 越权查看他人订单")
        void shouldReturnFailWhenNotOwner() throws Exception {
            when(ordersService.getOrderDetail(TEST_USER, 5))
                    .thenThrow(new ForbiddenException("无权限查看该订单"));

            mockMvc.perform(get("/orders/5"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("无权限查看该订单"));
        }
    }
}
