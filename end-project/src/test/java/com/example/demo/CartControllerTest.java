package com.example.demo;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.config.WebConfig;
import com.example.demo.controller.CartController;
import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.UpdateCartQuantityRequest;
import com.example.demo.dto.UpdateCartSelectedRequest;
import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {CartController.class, GlobalExceptionHandler.class, SecurityConfig.class, WebConfig.class})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

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

    private List<CartItemVO> mockCartList() {
        List<CartItemVO> list = new ArrayList<>();
        CartItemVO item = new CartItemVO();
        item.setCartId(1);
        item.setUserId(TEST_USER);
        item.setProductId(1L);
        item.setProductName("iPhone 15");
        item.setPrice(5999.0);
        item.setQuantity(2);
        item.setSelected(1);
        item.setStock(100);
        item.setIsAvailable(1);
        list.add(item);
        return list;
    }

    // ==================== GET /cart ====================

    @Nested
    @DisplayName("GET /cart — 查询购物车列表")
    class GetCartListTests {
        @Test
        @DisplayName("200 — 正常返回购物车列表")
        void shouldReturnCartList() throws Exception {
            when(cartService.getCartList(TEST_USER)).thenReturn(mockCartList());

            mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productName").value("iPhone 15"))
                .andExpect(jsonPath("$.data[0].quantity").value(2));

            verify(cartService).getCartList(TEST_USER);
        }

        @Test
        @DisplayName("200 — 空购物车返回空数组")
        void shouldReturnEmptyList() throws Exception {
            when(cartService.getCartList(TEST_USER)).thenReturn(new ArrayList<>());

            mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // ==================== POST /cart ====================

    @Nested
    @DisplayName("POST /cart — 添加商品到购物车")
    class AddToCartTests {
        @Test
        @DisplayName("200 — 正常添加")
        void shouldAddToCart() throws Exception {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(1L);
            req.setQuantity(3);

            mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("添加购物车成功"));

            verify(cartService).addToCart(TEST_USER, 1L, 3);
        }

        @Test
        @DisplayName("参数校验失败 — productId为null")
        void shouldFailWhenProductIdNull() throws Exception {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(null);
            req.setQuantity(1);

            mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — quantity为null")
        void shouldFailWhenQuantityNull() throws Exception {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(1L);
            req.setQuantity(null);

            mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — quantity为0（@Min校验）")
        void shouldFailWhenQuantityZero() throws Exception {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(1L);
            req.setQuantity(0);

            mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("业务异常 — Service抛运行时异常")
        void shouldReturnFailWhenServiceThrows() throws Exception {
            AddToCartRequest req = new AddToCartRequest();
            req.setProductId(999L);
            req.setQuantity(1);

            doThrow(new RuntimeException("商品不存在或已下架"))
                .when(cartService).addToCart(eq(TEST_USER), eq(999L), eq(1));

            mockMvc.perform(post("/cart")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在或已下架"));
        }
    }

    // ==================== PUT /cart/{cart_id} ====================

    @Nested
    @DisplayName("PUT /cart/{cart_id} — 修改商品数量")
    class UpdateQuantityTests {
        @Test
        @DisplayName("200 — 正常修改")
        void shouldUpdateQuantity() throws Exception {
            UpdateCartQuantityRequest req = new UpdateCartQuantityRequest();
            req.setNewQuantity(5);

            mockMvc.perform(put("/cart/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("更新数量成功"));

            verify(cartService).updateQuantity(TEST_USER, 1, 5);
        }

        @Test
        @DisplayName("参数校验失败 — newQuantity为null")
        void shouldFailWhenQuantityNull() throws Exception {
            UpdateCartQuantityRequest req = new UpdateCartQuantityRequest();
            req.setNewQuantity(null);

            mockMvc.perform(put("/cart/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — newQuantity为0")
        void shouldFailWhenQuantityZero() throws Exception {
            UpdateCartQuantityRequest req = new UpdateCartQuantityRequest();
            req.setNewQuantity(0);

            mockMvc.perform(put("/cart/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("403 — 越权操作他人购物车")
        void shouldFailWhenNotOwner() throws Exception {
            UpdateCartQuantityRequest req = new UpdateCartQuantityRequest();
            req.setNewQuantity(3);

            doThrow(new RuntimeException("无权限操作该购物车项"))
                .when(cartService).updateQuantity(eq(TEST_USER), eq(999), eq(3));

            mockMvc.perform(put("/cart/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限操作该购物车项"));
        }
    }

    // ==================== PUT /cart/{cart_id}/selected ====================

    @Nested
    @DisplayName("PUT /cart/{cart_id}/selected — 修改单个勾选状态")
    class UpdateSelectedTests {
        @Test
        @DisplayName("200 — 设置为未选中")
        void shouldSetUnselected() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(0);

            mockMvc.perform(put("/cart/1/selected")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("状态修改成功"));

            verify(cartService).updateSelected(TEST_USER, 1, 0);
        }

        @Test
        @DisplayName("200 — 设置为选中")
        void shouldSetSelected() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(1);

            mockMvc.perform(put("/cart/1/selected")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

            verify(cartService).updateSelected(TEST_USER, 1, 1);
        }

        @Test
        @DisplayName("参数校验失败 — selected为null")
        void shouldFailWhenSelectedNull() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(null);

            mockMvc.perform(put("/cart/1/selected")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — selected为2（超出0-1范围）")
        void shouldFailWhenInvalidValue() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(2);

            mockMvc.perform(put("/cart/1/selected")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("参数校验失败 — selected为-1")
        void shouldFailWhenNegative() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(-1);

            mockMvc.perform(put("/cart/1/selected")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== PUT /cart/selected/batch ====================

    @Nested
    @DisplayName("PUT /cart/selected/batch — 全选/全不选")
    class BatchUpdateSelectedTests {
        @Test
        @DisplayName("200 — 全选")
        void shouldSelectAll() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(1);

            mockMvc.perform(put("/cart/selected/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("批量修改成功"));

            verify(cartService).batchUpdateSelected(TEST_USER, 1);
        }

        @Test
        @DisplayName("200 — 全不选")
        void shouldDeselectAll() throws Exception {
            UpdateCartSelectedRequest req = new UpdateCartSelectedRequest();
            req.setSelected(0);

            mockMvc.perform(put("/cart/selected/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

            verify(cartService).batchUpdateSelected(TEST_USER, 0);
        }
    }

    // ==================== DELETE /cart/{cart_id} ====================

    @Nested
    @DisplayName("DELETE /cart/{cart_id} — 删除单个商品")
    class DeleteCartItemTests {
        @Test
        @DisplayName("200 — 正常删除")
        void shouldDeleteCartItem() throws Exception {
            mockMvc.perform(delete("/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("删除成功"));

            verify(cartService).deleteCartItem(TEST_USER, 1);
        }

        @Test
        @DisplayName("403 — 越权删除他人购物车")
        void shouldFailWhenNotOwner() throws Exception {
            doThrow(new RuntimeException("无权限操作该购物车项"))
                .when(cartService).deleteCartItem(eq(TEST_USER), eq(999));

            mockMvc.perform(delete("/cart/999"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无权限操作该购物车项"));
        }
    }

    // ==================== DELETE /cart/selected ====================

    @Nested
    @DisplayName("DELETE /cart/selected — 批量删除已勾选")
    class DeleteSelectedTests {
        @Test
        @DisplayName("200 — 正常批量删除")
        void shouldDeleteSelected() throws Exception {
            mockMvc.perform(delete("/cart/selected"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("已清空勾选商品"));

            verify(cartService).deleteSelected(TEST_USER);
        }
    }

    // ==================== GET /cart/count ====================

    @Nested
    @DisplayName("GET /cart/count — 获取总件数")
    class GetCartCountTests {
        @Test
        @DisplayName("200 — 正常返回总件数")
        void shouldReturnCartCount() throws Exception {
            when(cartService.getCartCount(TEST_USER)).thenReturn(5);

            mockMvc.perform(get("/cart/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(5));

            verify(cartService).getCartCount(TEST_USER);
        }

        @Test
        @DisplayName("200 — 空购物车返回0")
        void shouldReturnZeroForEmpty() throws Exception {
            when(cartService.getCartCount(TEST_USER)).thenReturn(0);

            mockMvc.perform(get("/cart/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(0));
        }
    }

    // ==================== GET /cart/selected/count ====================

    @Nested
    @DisplayName("GET /cart/selected/count — 获取已选件数")
    class GetSelectedCountTests {
        @Test
        @DisplayName("200 — 正常返回已选件数")
        void shouldReturnSelectedCount() throws Exception {
            when(cartService.getSelectedCount(TEST_USER)).thenReturn(3);

            mockMvc.perform(get("/cart/selected/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(3));

            verify(cartService).getSelectedCount(TEST_USER);
        }

        @Test
        @DisplayName("200 — 无勾选商品返回0")
        void shouldReturnZeroWhenNoneSelected() throws Exception {
            when(cartService.getSelectedCount(TEST_USER)).thenReturn(0);

            mockMvc.perform(get("/cart/selected/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(0));
        }
    }
}
