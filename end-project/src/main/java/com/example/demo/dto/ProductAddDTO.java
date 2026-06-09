package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商品新增 DTO（含图片上传）
 */
@Data
public class ProductAddDTO {

    /** 分类 */
    @NotBlank(message = "分类不能为空")
    private String category;

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /** 商品描述 */
    private String description;

    /** 价格 */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private Double price;

    /** 品牌 */
    private String brand;

    /** 库存 */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    /** 上架状态：0-下架 1-上架 */
    @NotNull(message = "上架状态不能为空")
    private Integer isAvailable;
}
