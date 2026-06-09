package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderInvoiceDTO {
    // 订单基本信息
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;

    // 收货信息
    private String consignee;
    private String telephone;
    private String address; // city + address

    // 用户信息
    private String username;
    private String userPhone;
    private String userEmail;

    // 订单商品列表
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal amount; // price * quantity
    }
}
