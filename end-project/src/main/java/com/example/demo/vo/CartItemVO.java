package com.example.demo.vo;

import lombok.Data;

@Data
public class CartItemVO {
    private Integer cartId;
    private Integer userId;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
    private Integer isAvailable;
}
