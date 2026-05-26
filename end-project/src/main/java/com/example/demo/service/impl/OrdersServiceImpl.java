package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Orders;
import com.example.demo.model.Product;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Integer userId, OrderCreateRequest request) {
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1);
        List<Cart> selectedCartList = cartMapper.selectList(cartWrapper);

        if (selectedCartList == null || selectedCartList.isEmpty()) {
            throw new RuntimeException("购物车中没有已选中的商品");
        }

        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : selectedCartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getIsAvailable() == null || product.getIsAvailable() != 1) {
                throw new RuntimeException("商品[" + (product != null ? product.getName() : "未知") + "]已下架或不存在");
            }

            int affectedRows = productMapper.decreaseStock(product.getId(), cart.getQuantity());
            if (affectedRows == 0) {
                throw new RuntimeException("商品[" + product.getName() + "]库存不足");
            }

            BigDecimal productPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal discount = new BigDecimal("1.00");
            BigDecimal amount = productPrice.multiply(new BigDecimal(cart.getQuantity())).multiply(discount);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(productPrice);
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setDiscount(discount);
            orderItem.setAmount(amount);
            orderItemList.add(orderItem);

            totalAmount = totalAmount.add(amount);
        }

        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setConsignee(request.getConsignee());
        order.setTelephone(request.getTelephone());
        order.setCity(request.getCity());
        order.setAddress(request.getAddress());
        baseMapper.insert(order);

        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getOrderId());
            orderItemMapper.insert(orderItem);
        }

        for (Cart cart : selectedCartList) {
            cartMapper.deleteById(cart.getCartId());
        }

        OrderCreateVO result = new OrderCreateVO();
        result.setOrderId(order.getOrderId());
        result.setStatus(order.getStatus());
        result.setPayAmount(order.getPayAmount());
        result.setOrderDate(order.getOrderDate());
        return result;
    }
}
