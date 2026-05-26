package com.example.demo.controller;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.UpdateCartQuantityRequest;
import com.example.demo.dto.UpdateCartSelectedRequest;
import com.example.demo.model.ApiResponse;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
import com.example.demo.vo.TotalCountVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemVO>> getCartList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        List<CartItemVO> cartList = cartService.getCartList(userId);
        return ApiResponse.ok(cartList);
    }

    @PostMapping
    public ApiResponse<?> addToCart(HttpServletRequest request,
                                    @Valid @RequestBody AddToCartRequest addToCartRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.addToCart(userId, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
        return ApiResponse.ok("添加购物车成功");
    }

    @PutMapping("/{cart_id}")
    public ApiResponse<?> updateQuantity(HttpServletRequest request,
                                         @PathVariable("cart_id") Integer cartId,
                                         @Valid @RequestBody UpdateCartQuantityRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.updateQuantity(userId, cartId, updateRequest.getNewQuantity());
        return ApiResponse.ok("更新数量成功");
    }

    @PutMapping("/{cart_id}/selected")
    public ApiResponse<?> updateSelected(HttpServletRequest request,
                                         @PathVariable("cart_id") Integer cartId,
                                         @Valid @RequestBody UpdateCartSelectedRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.updateSelected(userId, cartId, updateRequest.getSelected());
        return ApiResponse.ok("状态修改成功");
    }

    @PutMapping("/selected/batch")
    public ApiResponse<?> batchUpdateSelected(HttpServletRequest request,
                                              @Valid @RequestBody UpdateCartSelectedRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.batchUpdateSelected(userId, updateRequest.getSelected());
        return ApiResponse.ok("批量修改成功");
    }

    @DeleteMapping("/{cart_id}")
    public ApiResponse<?> deleteCartItem(HttpServletRequest request,
                                         @PathVariable("cart_id") Integer cartId) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.deleteCartItem(userId, cartId);
        return ApiResponse.ok("删除成功");
    }

    @DeleteMapping("/selected")
    public ApiResponse<?> deleteSelected(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        cartService.deleteSelected(userId);
        return ApiResponse.ok("已清空勾选商品");
    }

    @GetMapping("/count")
    public ApiResponse<?> getCartCount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        int count = cartService.getCartCount(userId);
        return ApiResponse.ok(new TotalCountVO(count));
    }

    @GetMapping("/selected/count")
    public ApiResponse<?> getSelectedCount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        int count = cartService.getSelectedCount(userId);
        return ApiResponse.ok(count);
    }
}
