package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.PageResult;
import com.example.demo.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductMapper productMapper;

    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> getAllProducts() {
        return productMapper.selectList(null);
    }

    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }

    public List<Product> searchProducts(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getName, keyword)
            .or().like(Product::getDescription, keyword)
            .or().like(Product::getCategory, keyword)
            .or().like(Product::getBrand, keyword);
        return productMapper.selectList(wrapper);
    }

    public PageResult<Product> getProductsByPage(int page, int pageSize, String category, String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                .or().like(Product::getDescription, keyword)
                .or().like(Product::getBrand, keyword));
        }

        long total = productMapper.selectCount(wrapper);
        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + "," + pageSize);
        List<Product> records = productMapper.selectList(wrapper);

        return new PageResult<>(records, total, page, pageSize);
    }

    public List<String> getCategories() {
        return productMapper.selectList(null).stream()
            .map(Product::getCategory)
            .distinct()
            .toList();
    }

    public Product createProduct(Product product) {
        productMapper.insert(product);
        return product;
    }

    public Product updateProduct(Long id, Product updated) {
        Product existing = productMapper.selectById(id);
        if (existing == null) return null;
        updated.setId(id);
        productMapper.updateById(updated);
        return updated;
    }

    public boolean deleteProduct(Long id) {
        return productMapper.deleteById(id) > 0;
    }
}
