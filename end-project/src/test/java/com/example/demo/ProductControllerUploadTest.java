package com.example.demo;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.config.UploadProperties;
import com.example.demo.config.WebConfig;
import com.example.demo.controller.ProductController;
import com.example.demo.exception.BadRequestException;
import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductController 图片上传接口 REST 层测试
 */
@WebMvcTest
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class,
        SecurityConfig.class, WebConfig.class, UploadProperties.class})
class ProductControllerUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtInterceptor jwtInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /products/add — 新增商品（含图片上传）")
    class AddProductTests {

        @Test
        @DisplayName("200 — 正常新增商品（带图片）")
        void shouldAddProductWithImage() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", "fake image".getBytes());

            mockMvc.perform(multipart("/products/add")
                            .file(image)
                            .param("category", "手机")
                            .param("name", "iPhone 16")
                            .param("price", "7999")
                            .param("stock", "100")
                            .param("isAvailable", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(productService).addProductWithImage(any(), any());
        }

        @Test
        @DisplayName("200 — 正常新增商品（不带图片）")
        void shouldAddProductWithoutImage() throws Exception {
            mockMvc.perform(multipart("/products/add")
                            .param("category", "手机")
                            .param("name", "iPhone 16")
                            .param("price", "7999")
                            .param("stock", "100")
                            .param("isAvailable", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(productService).addProductWithImage(any(), any());
        }

        @Test
        @DisplayName("400 — 参数校验失败（缺少必填字段）")
        void shouldFailWhenMissingRequired() throws Exception {
            mockMvc.perform(multipart("/products/add")
                            .param("category", "手机"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — 参数校验失败（价格为0）")
        void shouldFailWhenPriceZero() throws Exception {
            mockMvc.perform(multipart("/products/add")
                            .param("category", "手机")
                            .param("name", "iPhone 16")
                            .param("price", "0")
                            .param("stock", "100")
                            .param("isAvailable", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("400 — 业务异常（文件类型不允许）")
        void shouldFailWhenServiceThrows() throws Exception {
            MockMultipartFile image = new MockMultipartFile(
                    "image", "test.txt", "text/plain", "not an image".getBytes());

            doThrow(new BadRequestException("不支持的文件类型"))
                    .when(productService).addProductWithImage(any(), any());

            mockMvc.perform(multipart("/products/add")
                            .file(image)
                            .param("category", "手机")
                            .param("name", "iPhone 16")
                            .param("price", "7999")
                            .param("stock", "100")
                            .param("isAvailable", "1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("不支持的文件类型"));
        }
    }
}
