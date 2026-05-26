package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Integer orderId;
    private Integer userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String consignee;
    private String telephone;
    private String city;
    private String address;
    private List<OrderItemVO> items;
}
