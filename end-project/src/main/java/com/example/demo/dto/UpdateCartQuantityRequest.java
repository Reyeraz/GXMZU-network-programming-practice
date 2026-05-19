package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartQuantityRequest {
    @NotNull(message = "新数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer newQuantity;
}
