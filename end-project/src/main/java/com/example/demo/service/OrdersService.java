package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.model.Orders;
import com.example.demo.vo.OrderCreateVO;

public interface OrdersService extends IService<Orders> {

    OrderCreateVO createOrder(Integer userId, OrderCreateRequest request);
}
