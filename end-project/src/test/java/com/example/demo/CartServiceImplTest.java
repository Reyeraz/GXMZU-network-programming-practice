package com.example.demo;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.UpdateCartQuantityRequest;
import com.example.demo.dto.UpdateCartSelectedRequest;
import com.example.demo.model.Cart;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
import com.example.demo.vo.TotalCountVO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
class CartServiceImplTest {

    @Autowired
    private CartService cartService;

    private static final Integer TEST_USER_A = 100;
    private static final Integer TEST_USER_B = 101;
    private static final Long PRODUCT_IPHONE = 1L;
    private static final Long PRODUCT_HUAWEI = 3L;
    private Integer cartIdA1;
    private Integer cartIdA2;

    @Test
    @DisplayName("01_addToCart — 正常添加新商品")
    void t01_addToCart_shouldSucceed() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        assertEquals(1, list.size());
        assertEquals(PRODUCT_IPHONE, list.get(0).getProductId());
        assertEquals(3, list.get(0).getQuantity());
        assertEquals(1, list.get(0).getSelected());
        cartIdA1 = list.get(0).getCartId();
    }

    @Test
    @DisplayName("02_addToCart — 已存在商品累加数量")
    void t02_addToCart_shouldAccumulateWhenExists() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        assertEquals(1, list.size());
        assertEquals(5, list.get(0).getQuantity());
    }

    @Test
    @DisplayName("03_addToCart — 商品不存在抛异常")
    void t03_addToCart_shouldThrowWhenProductNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.addToCart(TEST_USER_A, 99999L, 1));
        assertTrue(ex.getMessage().contains("商品不存在") || ex.getMessage().contains("下架"));
    }

    @Test
    @DisplayName("04_addToCart — 购买数量为0抛异常（Service层校验）")
    void t04_addToCart_shouldThrowWhenQuantityZero() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 0));
        assertEquals("购买数量必须大于0", ex.getMessage());
    }

    @Test
    @DisplayName("05_addToCart — 库存不足抛异常")
    void t05_addToCart_shouldThrowWhenStockInsufficient() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 999999));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("06_addToCart — 添加多个不同商品")
    void t06_addToCart_multipleProducts() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("07_getCartList — 空购物车返回空列表")
    void t07_getCartList_shouldReturnEmptyForNewUser() {
        List<CartItemVO> list = cartService.getCartList(99999);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    @DisplayName("08_getCartList — 关联商品信息完整")
    void t08_getCartList_shouldReturnProductInfo() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        CartItemVO item = list.get(0);
        assertNotNull(item.getProductName());
        assertTrue(item.getPrice() > 0);
        assertTrue(item.getStock() >= 0);
        assertNotNull(item.getIsAvailable());
    }

    @Test
    @DisplayName("09_updateQuantity — 正常修改数量")
    void t09_updateQuantity_shouldSucceed() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 2);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        Integer cartId = list.get(0).getCartId();

        cartService.updateQuantity(TEST_USER_A, cartId, 5);
        list = cartService.getCartList(TEST_USER_A);
        assertEquals(5, list.get(0).getQuantity());
    }

    @Test
    @DisplayName("10_updateQuantity — 购物车记录不存在")
    void t10_updateQuantity_shouldThrowWhenNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.updateQuantity(TEST_USER_A, 99999, 3));
        assertEquals("购物车记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("11_updateQuantity — 越权操作抛异常")
    void t11_updateQuantity_shouldThrowWhenNotOwner() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 2);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.updateQuantity(TEST_USER_B, cartId, 5));
        assertEquals("无权限操作该购物车项", ex.getMessage());
    }

    @Test
    @DisplayName("12_updateQuantity — 超过库存抛异常")
    void t12_updateQuantity_shouldThrowWhenExceedStock() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.updateQuantity(TEST_USER_A, cartId, 999999));
        assertTrue(ex.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("13_updateSelected — 正常修改勾选状态为0")
    void t13_updateSelected_shouldSetToZero() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        cartService.updateSelected(TEST_USER_A, cartId, 0);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        assertEquals(0, list.get(0).getSelected());
    }

    @Test
    @DisplayName("14_updateSelected — 正常修改勾选状态为1")
    void t14_updateSelected_shouldSetToOne() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();
        cartService.updateSelected(TEST_USER_A, cartId, 0);
        cartService.updateSelected(TEST_USER_A, cartId, 1);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        assertEquals(1, list.get(0).getSelected());
    }

    @Test
    @DisplayName("15_updateSelected — 购物车记录不存在抛异常")
    void t15_updateSelected_shouldThrowWhenNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.updateSelected(TEST_USER_A, 99999, 1));
        assertEquals("购物车记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("16_updateSelected — 越权操作抛异常")
    void t16_updateSelected_shouldThrowWhenNotOwner() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.updateSelected(TEST_USER_B, cartId, 0));
        assertEquals("无权限操作该购物车项", ex.getMessage());
    }

    @Test
    @DisplayName("17_batchUpdateSelected — 全选所有商品")
    void t17_batchUpdateSelected_shouldSelectAll() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);
        // 先把它们全设为0
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        for (CartItemVO item : list) {
            cartService.updateSelected(TEST_USER_A, item.getCartId(), 0);
        }

        cartService.batchUpdateSelected(TEST_USER_A, 1);
        list = cartService.getCartList(TEST_USER_A);
        for (CartItemVO item : list) {
            assertEquals(1, item.getSelected());
        }
    }

    @Test
    @DisplayName("18_batchUpdateSelected — 全不选所有商品")
    void t18_batchUpdateSelected_shouldDeselectAll() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);

        cartService.batchUpdateSelected(TEST_USER_A, 0);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        for (CartItemVO item : list) {
            assertEquals(0, item.getSelected());
        }
    }

    @Test
    @DisplayName("19_deleteCartItem — 正常删除")
    void t19_deleteCartItem_shouldSucceed() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        cartService.deleteCartItem(TEST_USER_A, cartId);
        assertTrue(cartService.getCartList(TEST_USER_A).isEmpty());
    }

    @Test
    @DisplayName("20_deleteCartItem — 记录不存在抛异常")
    void t20_deleteCartItem_shouldThrowWhenNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.deleteCartItem(TEST_USER_A, 99999));
        assertEquals("购物车记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("21_deleteCartItem — 越权操作抛异常")
    void t21_deleteCartItem_shouldThrowWhenNotOwner() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        Integer cartId = cartService.getCartList(TEST_USER_A).get(0).getCartId();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            cartService.deleteCartItem(TEST_USER_B, cartId));
        assertEquals("无权限操作该购物车项", ex.getMessage());
    }

    @Test
    @DisplayName("22_deleteSelected — 批量删除已勾选商品")
    void t22_deleteSelected_shouldDeleteOnlySelected() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        // 查询按cart_id倒序，后添加的在前 → 华为(index 0), iPhone(index 1)
        // 取消勾选iPhone (index 1)，保留华为勾选状态
        cartService.updateSelected(TEST_USER_A, list.get(1).getCartId(), 0);

        cartService.deleteSelected(TEST_USER_A);
        list = cartService.getCartList(TEST_USER_A);
        // 删除了已勾选的华为，未勾选的iPhone保留
        assertEquals(1, list.size());
        assertEquals(PRODUCT_IPHONE, list.get(0).getProductId());
    }

    @Test
    @DisplayName("23_getCartCount — 获取总件数")
    void t23_getCartCount_shouldSumAllQuantities() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);
        // 总计 = 3 + 2 = 5
        assertEquals(5, cartService.getCartCount(TEST_USER_A));
    }

    @Test
    @DisplayName("24_getCartCount — 空购物车返回0")
    void t24_getCartCount_shouldReturnZeroForEmpty() {
        assertEquals(0, cartService.getCartCount(99999));
    }

    @Test
    @DisplayName("25_getSelectedCount — 获取已勾选件数")
    void t25_getSelectedCount_shouldReturnSelectedSum() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 2);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        // 查询按cart_id倒序 → 华为(index 0), iPhone(index 1)
        // 取消勾选iPhone (index 1)，已选 = 华为 = 2件
        cartService.updateSelected(TEST_USER_A, list.get(1).getCartId(), 0);
        assertEquals(2, cartService.getSelectedCount(TEST_USER_A));
    }

    @Test
    @DisplayName("26_getSelectedCount — 无勾选商品返回0")
    void t26_getSelectedCount_shouldReturnZeroWhenNoneSelected() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        List<CartItemVO> list = cartService.getCartList(TEST_USER_A);
        cartService.updateSelected(TEST_USER_A, list.get(0).getCartId(), 0);
        assertEquals(0, cartService.getSelectedCount(TEST_USER_A));
    }

    @Test
    @DisplayName("27_getSelectedCount — 空购物车返回0")
    void t27_getSelectedCount_shouldReturnZeroForEmptyCart() {
        assertEquals(0, cartService.getSelectedCount(99999));
    }

    @Test
    @DisplayName("28_综合流程：添加→全选→获取已选件数→删除已勾选→验证清空")
    void t28_integrationFlow() {
        // Step 1: 添加多个商品
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 3);
        assertEquals(5, cartService.getCartCount(TEST_USER_A));

        // Step 2: 全选
        cartService.batchUpdateSelected(TEST_USER_A, 1);
        assertEquals(5, cartService.getSelectedCount(TEST_USER_A));

        // Step 3: 批量删除已勾选
        cartService.deleteSelected(TEST_USER_A);
        assertTrue(cartService.getCartList(TEST_USER_A).isEmpty());
        assertEquals(0, cartService.getCartCount(TEST_USER_A));
        assertEquals(0, cartService.getSelectedCount(TEST_USER_A));
    }

    @Test
    @DisplayName("29_多用户隔离：用户A购物车不影响用户B")
    void t29_userIsolation() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 1);
        cartService.addToCart(TEST_USER_B, PRODUCT_HUAWEI, 2);

        List<CartItemVO> listA = cartService.getCartList(TEST_USER_A);
        List<CartItemVO> listB = cartService.getCartList(TEST_USER_B);

        assertEquals(1, listA.size());
        assertEquals(PRODUCT_IPHONE, listA.get(0).getProductId());
        assertEquals(1, listB.size());
        assertEquals(PRODUCT_HUAWEI, listB.get(0).getProductId());
    }

    @Test
    @DisplayName("30_多次添加同商品后总件数正确")
    void t30_multipleAdditions_countShouldBeCorrect() {
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 2);
        cartService.addToCart(TEST_USER_A, PRODUCT_IPHONE, 3);
        cartService.addToCart(TEST_USER_A, PRODUCT_HUAWEI, 1);
        // iPhone: 2+3=5, 华为: 1, 总计=6
        assertEquals(6, cartService.getCartCount(TEST_USER_A));
    }
}
