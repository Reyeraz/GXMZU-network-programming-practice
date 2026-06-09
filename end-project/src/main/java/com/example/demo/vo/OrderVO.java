package com.example.demo.vo;

import com.example.demo.model.OrderItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long orderId;
    private String orderNo;
    private Integer userId;
    private Double totalAmount;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItem> items;
}
