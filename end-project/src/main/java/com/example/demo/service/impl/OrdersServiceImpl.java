package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.Cart;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Orders;
import com.example.demo.model.Product;
import com.example.demo.service.EmailService;
import com.example.demo.service.OrderAsyncService;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;
import com.example.demo.vo.OrderItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 * 使用 MyBatis-Plus ServiceImpl 简化 CRUD 操作
 * 异步处理：库存扣减、购物车清理、邮件通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderAsyncService orderAsyncService;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Integer userId, OrderCreateRequest request) {
        // 1. 查询已选中的购物车商品
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1);
        List<Cart> selectedCartList = cartMapper.selectList(cartWrapper);

        if (selectedCartList == null || selectedCartList.isEmpty()) {
            throw new BadRequestException("购物车中没有已选中的商品");
        }

        // 2. 校验商品状态并计算金额
        List<OrderItem> orderItemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : selectedCartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getIsAvailable() == null || product.getIsAvailable() != 1) {
                throw new BadRequestException("商品[" + (product != null ? product.getName() : "未知") + "]已下架或不存在");
            }

            // 同步扣减库存（事务内保证原子性）
            int affectedRows = productMapper.decreaseStock(product.getId(), cart.getQuantity());
            if (affectedRows == 0) {
                throw new BadRequestException("商品[" + product.getName() + "]库存不足");
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

        // 3. 创建订单主表
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

        // 4. 批量插入订单明细
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderId(order.getOrderId());
            orderItemMapper.insert(orderItem);
        }

        // 5. 同步清空已选购物车（关键业务操作，保证事务一致性）
        for (Cart cart : selectedCartList) {
            cartMapper.deleteById(cart.getCartId());
        }

        // 6. 构建返回结果
        OrderCreateVO result = new OrderCreateVO();
        result.setOrderId(order.getOrderId());
        result.setStatus(order.getStatus());
        result.setPayAmount(order.getPayAmount());
        result.setOrderDate(order.getOrderDate());

        // 7. 异步发送订单确认邮件（不阻塞主流程）
        // 注意：实际项目中应从用户表获取邮箱，这里演示用 request 中的字段
        // emailService.sendOrderConfirmationEmail(userEmail, order, result);

        log.info("订单创建成功: orderId={}, userId={}, payAmount={}", order.getOrderId(), userId, order.getPayAmount());
        return result;
    }

    @Override
    public OrderDetailVO getOrderDetail(Integer userId, Integer orderId) {
        Orders orders = baseMapper.selectById(orderId);
        if (orders == null) {
            throw new ResourceNotFoundException("订单不存在");
        }
        if (!orders.getUserId().equals(userId)) {
            throw new ForbiddenException("无权限查看该订单");
        }

        LambdaQueryWrapper<OrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
        orderItemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemWrapper);

        List<OrderItemVO> orderItemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVO orderItemVO = new OrderItemVO();
            orderItemVO.setOrderItemId(orderItem.getOrderItemId());
            orderItemVO.setProductId(orderItem.getProductId());
            orderItemVO.setProductName(orderItem.getProductName());
            orderItemVO.setPrice(orderItem.getPrice());
            orderItemVO.setQuantity(orderItem.getQuantity());
            orderItemVO.setDiscount(orderItem.getDiscount());
            orderItemVO.setAmount(orderItem.getAmount());
            orderItemVOList.add(orderItemVO);
        }

        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setOrderId(orders.getOrderId());
        orderDetailVO.setUserId(orders.getUserId());
        orderDetailVO.setOrderDate(orders.getOrderDate());
        orderDetailVO.setTotalAmount(orders.getTotalAmount());
        orderDetailVO.setPayAmount(orders.getPayAmount());
        orderDetailVO.setStatus(orders.getStatus());
        orderDetailVO.setConsignee(orders.getConsignee());
        orderDetailVO.setTelephone(orders.getTelephone());
        orderDetailVO.setCity(orders.getCity());
        orderDetailVO.setAddress(orders.getAddress());
        orderDetailVO.setItems(orderItemVOList);
        return orderDetailVO;
    }
}
