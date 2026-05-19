package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cart")
public class Cart {
    @TableId(value = "cart_id", type = IdType.AUTO)
    private Integer cartId;
    @TableField("user_id")
    private Integer userId;
    @TableField("product_id")
    private Long productId;
    private Integer quantity;
    private Integer selected;
}
