package com.example.demo.controller;

import com.example.demo.dto.OrderCreateRequest;
import com.example.demo.model.ApiResponse;
import com.example.demo.service.OrdersService;
import com.example.demo.vo.OrderCreateVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrdersService ordersService;

    @PostMapping("/create")
    public ApiResponse<OrderCreateVO> createOrder(HttpServletRequest request,
                                                   @Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        OrderCreateVO orderCreateVO = ordersService.createOrder(userId, orderCreateRequest);
        return ApiResponse.ok(orderCreateVO);
    }
}
