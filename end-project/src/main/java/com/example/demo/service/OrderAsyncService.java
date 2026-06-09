package com.example.demo.service;

import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单异步任务服务
 * 处理订单创建后的异步操作：扣减库存、清空购物车
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAsyncService {

    private final ProductMapper productMapper;
    private final CartMapper cartMapper;

    /**
     * 异步扣减库存
     * @param productId 商品ID
     * @param quantity 购买数量
     */
    @Async
    public void decreaseStockAsync(Long productId, Integer quantity) {
        try {
            int affectedRows = productMapper.decreaseStock(productId, quantity);
            if (affectedRows == 0) {
                log.warn("异步扣减库存失败: productId={}, quantity={}, 原因: 库存不足", productId, quantity);
            } else {
                log.info("异步扣减库存成功: productId={}, quantity={}", productId, quantity);
            }
        } catch (Exception e) {
            log.error("异步扣减库存异常: productId={}, quantity={}", productId, quantity, e);
        }
    }

    /**
     * 异步批量扣减库存
     * @param cartList 购物车列表
     */
    @Async
    public void decreaseStockBatchAsync(List<Cart> cartList) {
        try {
            for (Cart cart : cartList) {
                int affectedRows = productMapper.decreaseStock(cart.getProductId(), cart.getQuantity());
                if (affectedRows == 0) {
                    log.warn("异步批量扣减库存失败: productId={}, quantity={}, 原因: 库存不足",
                            cart.getProductId(), cart.getQuantity());
                }
            }
            log.info("异步批量扣减库存完成: count={}", cartList.size());
        } catch (Exception e) {
            log.error("异步批量扣减库存异常", e);
        }
    }

    /**
     * 异步清空购物车中已选商品
     * @param cartList 购物车列表
     */
    @Async
    public void clearSelectedCartAsync(List<Cart> cartList) {
        try {
            for (Cart cart : cartList) {
                cartMapper.deleteById(cart.getCartId());
            }
            log.info("异步清空已选购物车完成: count={}", cartList.size());
        } catch (Exception e) {
            log.error("异步清空已选购物车异常", e);
        }
    }
}
