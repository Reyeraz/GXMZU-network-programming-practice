package com.example.demo;

import com.example.demo.config.GlobalExceptionHandler;
import com.example.demo.config.SecurityConfig;
import com.example.demo.config.UploadProperties;
import com.example.demo.config.WebConfig;
import com.example.demo.controller.InvoiceController;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.interceptor.JwtInterceptor;
import com.example.demo.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {InvoiceController.class, InvoiceService.class, GlobalExceptionHandler.class, SecurityConfig.class, WebConfig.class, UploadProperties.class})
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvoiceService invoiceService;

    @MockitoBean
    private JwtInterceptor jwtInterceptor;

    private static final Integer TEST_USER = 100;

    @BeforeEach
    void setUp() throws Exception {
        when(jwtInterceptor.preHandle(any(), any(), any())).thenAnswer(invocation -> {
            jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
            request.setAttribute("userId", TEST_USER);
            return true;
        });
    }

    @Nested
    @DisplayName("GET /invoice/download/{orderId} — 下载发票")
    class DownloadInvoiceTests {

        @Test
        @DisplayName("200 — 正常下载发票PDF")
        void shouldDownloadInvoice() throws Exception {
            byte[] pdfBytes = "%PDF-1.4 fake pdf content".getBytes();
            when(invoiceService.generateInvoicePdf(1L)).thenReturn(pdfBytes);

            mockMvc.perform(get("/invoice/download/1"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Type"))
                    .andExpect(result -> assertTrue(result.getResponse().getContentType().startsWith("application/pdf")))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"invoice_1.pdf\""))
                    .andExpect(content().bytes(pdfBytes));
        }

        @Test
        @DisplayName("404 — 订单不存在")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            when(invoiceService.generateInvoicePdf(99999L))
                    .thenThrow(new ResourceNotFoundException("订单不存在"));

            mockMvc.perform(get("/invoice/download/99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("订单不存在"));
        }

        @Test
        @DisplayName("400 — 待付款订单不能下载发票")
        void shouldReturn400WhenOrderNotPaid() throws Exception {
            when(invoiceService.generateInvoicePdf(2L))
                    .thenThrow(new BadRequestException("待付款或已取消的订单不能下载发票"));

            mockMvc.perform(get("/invoice/download/2"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("待付款或已取消的订单不能下载发票"));
        }

        @Test
        @DisplayName("400 — 已取消订单不能下载发票")
        void shouldReturn400WhenOrderCancelled() throws Exception {
            when(invoiceService.generateInvoicePdf(3L))
                    .thenThrow(new BadRequestException("待付款或已取消的订单不能下载发票"));

            mockMvc.perform(get("/invoice/download/3"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("待付款或已取消的订单不能下载发票"));
        }

    }
}
