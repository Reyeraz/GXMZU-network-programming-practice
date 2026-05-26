package com.example.demo;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.config.WebConfig;
import com.example.demo.controller.OrderController;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                    .andExpect(status().isOk())
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
                    .andExpect(status().isOk())
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
                    .andExpect(status().isOk())
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
                    .andExpect(status().isOk())
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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("业务异常 — 购物车无选中商品")
        void shouldReturnFailWhenCartEmpty() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new RuntimeException("购物车中没有已选中的商品"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("购物车中没有已选中的商品"));
        }

        @Test
        @DisplayName("业务异常 — 库存不足")
        void shouldReturnFailWhenStockInsufficient() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new RuntimeException("商品[iPhone 15]库存不足"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("商品[iPhone 15]库存不足"));
        }

        @Test
        @DisplayName("业务异常 — 商品已下架")
        void shouldReturnFailWhenProductUnavailable() throws Exception {
            when(ordersService.createOrder(eq(TEST_USER), any(OrderCreateRequest.class)))
                    .thenThrow(new RuntimeException("商品[iPhone 15]已下架或不存在"));

            mockMvc.perform(post("/orders/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("商品[iPhone 15]已下架或不存在"));
        }
    }
}
