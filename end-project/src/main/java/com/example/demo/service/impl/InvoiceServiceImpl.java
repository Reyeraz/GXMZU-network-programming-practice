package com.example.demo.service.impl;

import com.example.demo.dto.OrderInvoiceDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.OrderInvoiceMapper;
import com.example.demo.service.InvoicePdfService;
import com.example.demo.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final OrderInvoiceMapper orderInvoiceMapper;
    private final InvoicePdfService invoicePdfService;

    @Override
    public byte[] generateInvoicePdf(Long orderId) {
        // 1. 查询订单基本信息
        OrderInvoiceDTO invoice = orderInvoiceMapper.selectOrderInvoiceData(orderId);
        if (invoice == null) {
            log.warn("订单不存在, orderId: {}", orderId);
            throw new ResourceNotFoundException("订单不存在");
        }

        // 2. 校验订单状态：已付款(1)或已完成(3)才能下载发票
        // 状态：0待付款 1已付款 2已发货 3已完成 4已取消
        if (invoice.getStatus() == 0 || invoice.getStatus() == 4) {
            log.warn("订单状态不允许下载发票, orderId: {}, status: {}", orderId, invoice.getStatus());
            throw new BadRequestException("待付款或已取消的订单不能下载发票");
        }

        // 3. 查询订单商品明细
        invoice.setItems(orderInvoiceMapper.selectOrderItems(orderId));

        // 4. 生成PDF
        try {
            return invoicePdfService.generateInvoicePdf(invoice);
        } catch (Exception e) {
            log.error("PDF生成失败, orderId: {}, 错误: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("发票生成失败: " + e.getMessage());
        }
    }
}
