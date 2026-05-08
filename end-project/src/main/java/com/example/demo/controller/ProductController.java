package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.PageResult;
import com.example.demo.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @PostConstruct
    public void init() {
        products.addAll(List.of(
            new Product(nextId(), "iPhone 15 Pro", "A17 Pro芯片，钛金属设计，支持USB-C接口，5G网络", 7999.0, "https://picsum.photos/id/1/300/300", "手机", "Apple", 50),
            new Product(nextId(), "Samsung Galaxy S24 Ultra", "骁龙8 Gen 3处理器，200MP主摄，S Pen内置，5G网络", 8999.0, "https://picsum.photos/id/2/300/300", "手机", "Samsung", 30),
            new Product(nextId(), "MacBook Pro 14英寸", "M3 Pro芯片，Liquid Retina XDR显示屏，MagSafe充电", 15999.0, "https://picsum.photos/id/3/300/300", "笔记本电脑", "Apple", 20),
            new Product(nextId(), "Dell XPS 13 Plus", "13.4英寸4K+触摸屏，第13代Intel酷睿i7处理器，16GB内存", 11999.0, "https://picsum.photos/id/4/300/300", "笔记本电脑", "Dell", 15),
            new Product(nextId(), "iPad Pro 12.9英寸", "M2芯片，Liquid Retina XDR显示屏，Apple Pencil支持", 8999.0, "https://picsum.photos/id/5/300/300", "平板电脑", "Apple", 25),
            new Product(nextId(), "Surface Pro 9", "第12代Intel酷睿处理器，13英寸PixelSense Flow显示屏，触控笔支持", 7999.0, "https://picsum.photos/id/6/300/300", "平板电脑", "Microsoft", 18),
            new Product(nextId(), "AirPods Pro 2", "主动降噪，自适应通透模式，个性化空间音频", 1899.0, "https://picsum.photos/id/7/300/300", "耳机", "Apple", 40),
            new Product(nextId(), "Sony WH-1000XM5", "业界领先的降噪技术，30小时电池续航，舒适佩戴", 2999.0, "https://picsum.photos/id/8/300/300", "耳机", "Sony", 22)
        ));
    }

    private Long nextId() {
        return idCounter.incrementAndGet();
    }

    // 2.1 获取所有商品
    @GetMapping("/products")
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponse.ok(new ArrayList<>(products));
    }

    // 2.2 根据ID获取商品
    @GetMapping("/products/{id}")
    public ApiResponse<Product> getProductById(@PathVariable Long id) {
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .map(ApiResponse::ok)
            .orElse(ApiResponse.fail("商品不存在"));
    }

    // 2.3 搜索商品
    @GetMapping("/products/search")
    public ApiResponse<List<Product>> searchProducts(@RequestParam String keyword) {
        String kw = keyword.toLowerCase();
        List<Product> results = products.stream()
            .filter(p -> p.getName().toLowerCase().contains(kw)
                || p.getDescription().toLowerCase().contains(kw)
                || p.getCategory().toLowerCase().contains(kw)
                || p.getBrand().toLowerCase().contains(kw))
            .collect(Collectors.toList());
        return ApiResponse.ok(results);
    }

    // 2.4 分页查询商品
    @GetMapping("/products/page")
    public ApiResponse<PageResult<Product>> getProductsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        List<Product> filtered = new ArrayList<>(products);

        if (category != null && !category.isEmpty()) {
            filtered = filtered.stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());
        }
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.toLowerCase();
            filtered = filtered.stream()
                .filter(p -> p.getName().toLowerCase().contains(kw)
                    || p.getDescription().toLowerCase().contains(kw)
                    || p.getBrand().toLowerCase().contains(kw))
                .collect(Collectors.toList());
        }

        long total = filtered.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());

        List<Product> items = start < filtered.size()
            ? filtered.subList(start, end)
            : Collections.emptyList();

        return ApiResponse.ok(new PageResult<>(items, total, page, pageSize));
    }

    // 2.5 获取所有分类
    @GetMapping("/categories")
    public ApiResponse<List<String>> getCategories() {
        List<String> categories = products.stream()
            .map(Product::getCategory)
            .distinct()
            .collect(Collectors.toList());
        return ApiResponse.ok(categories);
    }

    // 3.3 插入新商品 (POST - Create)
    @PostMapping("/products")
    public ApiResponse<Product> createProduct(@RequestBody Product product) {
        product.setId(nextId());
        products.add(product);
        return ApiResponse.ok(product);
    }

    // 3.4 更新商品信息 (PUT - Update)
    @PutMapping("/products/{id}")
    public ApiResponse<Product> updateProduct(@PathVariable Long id, @RequestBody Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                updated.setId(id);
                products.set(i, updated);
                return ApiResponse.ok(updated);
            }
        }
        return ApiResponse.fail("商品不存在");
    }

    // 3.5 删除商品 (DELETE)
    @DeleteMapping("/products/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        if (removed) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.fail("商品不存在");
    }
}
