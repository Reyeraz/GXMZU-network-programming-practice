package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.Product;
import com.example.demo.service.CartService;
import com.example.demo.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final ProductMapper productMapper;

    @Override
    public List<CartItemVO> getCartList(Integer userId) {
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCartId);
        List<Cart> cartList = baseMapper.selectList(cartWrapper);
        if (cartList == null || cartList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> productIds = cartList.stream()
                .map(Cart::getProductId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getId, productIds);
        List<Product> productList = productMapper.selectList(productWrapper);
        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<CartItemVO> cartItemVOList = new ArrayList<>();
        for (Cart cart : cartList) {
            CartItemVO vo = new CartItemVO();
            vo.setCartId(cart.getCartId());
            vo.setUserId(cart.getUserId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());
            vo.setSelected(cart.getSelected());

            Product product = productMap.get(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setPrice(product.getPrice());
                vo.setStock(product.getStock());
                vo.setIsAvailable(product.getIsAvailable());
            } else {
                vo.setProductName("商品已下架");
                vo.setPrice(0.0);
                vo.setStock(0);
                vo.setIsAvailable(0);
            }
            cartItemVOList.add(vo);
        }
        return cartItemVOList;
    }

    @Override
    public void addToCart(Integer userId, Long productId, Integer quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsAvailable() == null || product.getIsAvailable() == 0) {
            throw new RuntimeException("商品不存在或已下架");
        }
        if (quantity < 1) {
            throw new RuntimeException("购买数量必须大于0");
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("库存不足，当前仅剩" + product.getStock() + "件");
        }

        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId);
        Cart cart = baseMapper.selectOne(cartWrapper);
        if (cart != null) {
            cart.setQuantity(cart.getQuantity() + quantity);
            baseMapper.updateById(cart);
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(productId);
            newCart.setQuantity(quantity);
            newCart.setSelected(1);
            baseMapper.insert(newCart);
        }
    }

    @Override
    public void updateQuantity(Integer userId, Integer cartId, Integer newQuantity) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该购物车项");
        }

        Product product = productMapper.selectById(cart.getProductId());
        if (product == null || product.getIsAvailable() == null || product.getIsAvailable() != 1) {
            throw new RuntimeException("商品已失效，请删除后重新添加");
        }
        if (newQuantity > product.getStock()) {
            throw new RuntimeException("库存不足，当前仅剩" + product.getStock() + "件");
        }

        cart.setQuantity(newQuantity);
        baseMapper.updateById(cart);
    }

    @Override
    public void updateSelected(Integer userId, Integer cartId, Integer selected) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该购物车项");
        }
        cart.setSelected(selected);
        baseMapper.updateById(cart);
    }

    @Override
    public void batchUpdateSelected(Integer userId, Integer selected) {
        LambdaUpdateWrapper<Cart> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Cart::getUserId, userId)
                .set(Cart::getSelected, selected);
        baseMapper.update(updateWrapper);
    }

    @Override
    public void deleteCartItem(Integer userId, Integer cartId) {
        Cart cart = baseMapper.selectById(cartId);
        if (cart == null) {
            throw new RuntimeException("购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new RuntimeException("无权限操作该购物车项");
        }
        baseMapper.deleteById(cartId);
    }

    @Override
    public void deleteSelected(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1);
        baseMapper.delete(queryWrapper);
    }

    @Override
    public int getCartCount(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .select(Cart::getQuantity);
        List<Cart> cartList = baseMapper.selectList(queryWrapper);
        return cartList.stream().mapToInt(Cart::getQuantity).sum();
    }

    @Override
    public int getSelectedCount(Integer userId) {
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1)
                .select(Cart::getQuantity);
        List<Cart> cartList = baseMapper.selectList(queryWrapper);
        return cartList.stream().mapToInt(Cart::getQuantity).sum();
    }
}
