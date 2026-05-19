package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.Cart;
import com.example.demo.vo.CartItemVO;

import java.util.List;

public interface CartService extends IService<Cart> {
    List<CartItemVO> getCartList(Integer userId);
    void addToCart(Integer userId, Long productId, Integer quantity);
    void updateQuantity(Integer userId, Integer cartId, Integer newQuantity);
}
