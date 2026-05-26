package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer orderId;

    @TableField("user_id")
    private Integer userId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String consignee;

    private String telephone;

    private String city;

    private String address;
}
