package com.example.demo.controller;

import com.example.demo.dto.AddToCartRequest;
import com.example.demo.dto.UpdateCartQuantityRequest;
import com.example.demo.model.ApiResponse;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
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
        if (userId == null) {
            return ApiResponse.fail("未登录，请先登录");
        }
        List<CartItemVO> cartList = cartService.getCartList(userId);
        return ApiResponse.ok(cartList);
    }

    @PostMapping
    public ApiResponse<?> addToCart(HttpServletRequest request,
                                    @Valid @RequestBody AddToCartRequest addToCartRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.fail("未登录，请先登录");
        }
        cartService.addToCart(userId, addToCartRequest.getProductId(), addToCartRequest.getQuantity());
        return ApiResponse.ok("添加购物车成功");
    }

    @PutMapping("/{cart_id}")
    public ApiResponse<?> updateQuantity(HttpServletRequest request,
                                         @PathVariable("cart_id") Integer cartId,
                                         @Valid @RequestBody UpdateCartQuantityRequest updateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.fail("未登录，请先登录");
        }
        cartService.updateQuantity(userId, cartId, updateRequest.getNewQuantity());
        return ApiResponse.ok("更新数量成功");
    }
}
