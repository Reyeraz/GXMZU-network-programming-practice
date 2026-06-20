package com.example.demo.controller;

import com.example.demo.dto.ProductAddDTO;
import com.example.demo.model.ApiResponse;
import com.example.demo.model.PageResult;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 2.1 获取所有商品
    @GetMapping("/products")
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponse.ok(productService.getAllProducts());
    }

    // 2.2 根据ID获取商品
    @GetMapping("/products/{id}")
    public ApiResponse<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ApiResponse.ok(product);
        }
        return ApiResponse.fail("商品不存在");
    }

    // 2.3 搜索商品
    @GetMapping("/products/search")
    public ApiResponse<List<Product>> searchProducts(@RequestParam String keyword) {
        return ApiResponse.ok(productService.searchProducts(keyword));
    }

    // 2.4 分页查询商品
    @GetMapping("/products/page")
    public ApiResponse<PageResult<Product>> getProductsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(productService.getProductsByPage(page, pageSize, category, keyword));
    }

    // 2.5 获取所有分类
    @GetMapping("/categories")
    public ApiResponse<List<String>> getCategories() {
        return ApiResponse.ok(productService.getCategories());
    }

    // POST - 新增商品
    @PostMapping("/products")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        return ApiResponse.ok(productService.createProduct(product));
    }

    // POST - 新增商品（含图片上传）
    @PostMapping("/products/add")
    public ApiResponse<Void> addProduct(@Valid @ModelAttribute ProductAddDTO dto,
                                        @RequestParam(value = "image", required = false) MultipartFile image) {
        productService.addProductWithImage(dto, image);
        return ApiResponse.ok(null);
    }

    // PUT - 更新商品
    @PutMapping("/products/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product updated) {
        Product result = productService.updateProduct(id, updated);
        if (result != null) {
            return ApiResponse.ok(result);
        }
        return ApiResponse.fail("商品不存在");
    }

    // DELETE - 删除商品
    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.fail("商品不存在");
    }
}
