package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`orders`")
public class Order {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private Integer userId;
    @TableField("total_amount")
    private Double totalAmount;
    /**
     * 订单状态：0待付款 1已付款 2已发货 3已完成 4已取消
     */
    private Integer status;
    @TableField("receiver_name")
    private String receiverName;
    @TableField("receiver_phone")
    private String receiverPhone;
    @TableField("receiver_address")
    private String receiverAddress;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
