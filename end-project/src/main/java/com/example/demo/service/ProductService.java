package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.ProductAddDTO;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.model.PageResult;
import com.example.demo.model.Product;
import com.example.demo.utils.ImageUploadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品服务
 * 使用 MyBatis-Plus ServiceImpl 简化 CRUD 操作
 * 使用分页插件实现物理分页
 */
@Slf4j
@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final ImageUploadUtils imageUploadUtils;

    public ProductService(ImageUploadUtils imageUploadUtils) {
        this.imageUploadUtils = imageUploadUtils;
    }

    /**
     * 获取所有商品
     */
    public List<Product> getAllProducts() {
        return baseMapper.selectList(null);
    }

    /**
     * 根据ID获取商品
     */
    public Product getProductById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 搜索商品
     */
    public List<Product> searchProducts(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getName, keyword)
            .or().like(Product::getDescription, keyword)
            .or().like(Product::getCategory, keyword)
            .or().like(Product::getBrand, keyword);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询商品（使用 MyBatis-Plus 分页插件）
     */
    public PageResult<Product> getProductsByPage(int page, int pageSize, String category, String keyword) {
        // 创建分页对象
        Page<Product> pageParam = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                .or().like(Product::getDescription, keyword)
                .or().like(Product::getBrand, keyword));
        }
        wrapper.orderByDesc(Product::getId);

        // 执行分页查询
        Page<Product> pageResult = baseMapper.selectPage(pageParam, wrapper);

        // 转换为自定义 PageResult
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, pageSize);
    }

    /**
     * 获取所有分类
     */
    public List<String> getCategories() {
        return baseMapper.selectList(null).stream()
            .map(Product::getCategory)
            .distinct()
            .toList();
    }

    /**
     * 新增商品
     */
    public Product createProduct(Product product) {
        baseMapper.insert(product);
        return product;
    }

    /**
     * 更新商品
     */
    public Product updateProduct(Long id, Product updated) {
        Product existing = baseMapper.selectById(id);
        if (existing == null) return null;
        updated.setId(id);
        baseMapper.updateById(updated);
        return updated;
    }

    /**
     * 删除商品
     */
    public boolean deleteProduct(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    /**
     * 新增商品（支持图片上传）
     * @param dto   商品信息
     * @param image 图片文件（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addProductWithImage(ProductAddDTO dto, MultipartFile image) {
        // 1. 转换 DTO 为实体
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);

        // 2. 处理图片上传
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadUtils.saveFile(image);
            product.setImage(imageUrl);
        } else {
            log.info("未上传图片");
        }

        // 3. 保存商品
        this.save(product);
        log.info("商品新增成功，ID: {}, 名称: {}, 图片URL: {}",
                product.getId(), product.getName(), product.getImage());
    }
}
