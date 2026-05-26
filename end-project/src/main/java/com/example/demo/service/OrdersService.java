package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.model.Orders;
import com.example.demo.vo.OrderCreateVO;
import com.example.demo.vo.OrderDetailVO;

public interface OrdersService extends IService<Orders> {

    OrderCreateVO createOrder(Integer userId, OrderCreateRequest request);

    OrderDetailVO getOrderDetail(Integer userId, Integer orderId);
}
