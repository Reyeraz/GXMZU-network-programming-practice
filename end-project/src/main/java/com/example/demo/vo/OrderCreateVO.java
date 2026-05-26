package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateVO {
    private Integer orderId;
    private Integer status;
    private BigDecimal payAmount;
    private LocalDateTime orderDate;
}
