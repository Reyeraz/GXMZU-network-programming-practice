package com.example.demo.service;

import com.example.demo.dto.OrderInvoiceDTO;

/**
 * 发票PDF生成服务接口
 */
public interface InvoicePdfService {

    /**
     * 根据订单数据生成PDF
     * @param invoice 订单发票数据
     * @return PDF字节数组
     */
    byte[] generateInvoicePdf(OrderInvoiceDTO invoice);
}
