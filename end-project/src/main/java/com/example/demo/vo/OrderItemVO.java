package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Integer orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal amount;
}
