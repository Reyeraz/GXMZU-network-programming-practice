package com.example.demo.service;

/**
 * 发票服务接口
 */
public interface InvoiceService {

    /**
     * 生成订单发票PDF
     * @param orderId 订单ID
     * @return PDF字节数组
     */
    byte[] generateInvoicePdf(Long orderId);
}
