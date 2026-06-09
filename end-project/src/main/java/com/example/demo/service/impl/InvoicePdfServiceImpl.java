package com.example.demo.service.impl;

import com.example.demo.dto.OrderInvoiceDTO;
import com.example.demo.service.InvoicePdfService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class InvoicePdfServiceImpl implements InvoicePdfService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;

    @Override
    public byte[] generateInvoicePdf(OrderInvoiceDTO invoice) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 加载中文字体（从 resources/fonts/ 目录）
            PDType0Font chineseFont = loadChineseFont(document);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = PDRectangle.A4.getHeight() - MARGIN;

                // 标题
                drawText(cs, chineseFont, 20, "订单发票", PAGE_WIDTH / 2 - 60, y);
                y -= 40;

                // 订单信息
                drawText(cs, chineseFont, 12, "订单号：" + invoice.getOrderId(), MARGIN, y);
                y -= 20;
                drawText(cs, chineseFont, 12, "下单时间：" + invoice.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), MARGIN, y);
                y -= 20;
                drawText(cs, chineseFont, 12, "收货人：" + invoice.getConsignee() + " " + invoice.getTelephone(), MARGIN, y);
                y -= 20;
                drawText(cs, chineseFont, 12, "收货地址：" + invoice.getAddress(), MARGIN, y);
                y -= 30;

                // 表格头部
                float[] colWidths = {300, 80, 80, 100};
                String[] headers = {"商品名称", "单价", "数量", "金额"};
                drawTableHeader(cs, chineseFont, MARGIN, y, colWidths, headers);
                y -= 25;

                // 表格内容
                float rowY = y;
                for (OrderInvoiceDTO.OrderItemDTO item : invoice.getItems()) {
                    if (rowY < MARGIN + 50) {
                        // 内容超出页面，新建页面
                        cs.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        try (PDPageContentStream newCs = new PDPageContentStream(document, newPage)) {
                            rowY = PDRectangle.A4.getHeight() - MARGIN;
                            drawTableHeader(newCs, chineseFont, MARGIN, rowY, colWidths, headers);
                            rowY -= 25;
                            drawTableRow(newCs, chineseFont, MARGIN, rowY, colWidths, new String[]{
                                    item.getProductName(),
                                    item.getPrice() + "元",
                                    String.valueOf(item.getQuantity()),
                                    item.getAmount() + "元"
                            });
                        }
                        rowY -= 20;
                    } else {
                        drawTableRow(cs, chineseFont, MARGIN, rowY, colWidths, new String[]{
                                item.getProductName(),
                                item.getPrice() + "元",
                                String.valueOf(item.getQuantity()),
                                item.getAmount() + "元"
                        });
                        rowY -= 20;
                    }
                }

                // 合计金额
                rowY -= 20;
                drawText(cs, chineseFont, 14, "合计：" + invoice.getPayAmount() + "元", PAGE_WIDTH - 150, rowY);
            }

            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("PDF生成异常, orderId: {}", invoice.getOrderId(), e);
            throw new RuntimeException("PDF生成失败", e);
        }
    }

    /**
     * 加载中文字体
     * 需要在 src/main/resources/fonts/ 目录下放置 SourceHanSansCN-Regular.ttf 字体文件
     * 下载地址：https://github.com/adobe-fonts/source-han-sans
     */
    private PDType0Font loadChineseFont(PDDocument document) throws IOException {
        String fontPath = "fonts/SourceHanSansCN-Regular.ttf";
        try {
            InputStream fontStream = new ClassPathResource(fontPath).getInputStream();
            return PDType0Font.load(document, fontStream);
        } catch (Exception e) {
            log.error("字体文件未找到: {}", fontPath);
            log.error("请在 src/main/resources/fonts/ 目录下放置中文字体文件");
            log.error("下载地址: https://github.com/adobe-fonts/source-han-sans");
            throw new IOException("字体文件缺失: " + fontPath + "，请在 resources/fonts/ 目录放置中文字体", e);
        }
    }

    /**
     * 绘制文本
     */
    private void drawText(PDPageContentStream cs, PDType0Font font, float fontSize,
                          String text, float x, float y) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    /**
     * 绘制表头
     */
    private void drawTableHeader(PDPageContentStream cs, PDType0Font font,
                                  float x, float y, float[] widths, String[] headers) throws IOException {
        cs.beginText();
        cs.setFont(font, 12);
        float offsetX = x;
        for (int i = 0; i < headers.length; i++) {
            cs.newLineAtOffset(offsetX, y);
            cs.showText(headers[i]);
            offsetX += widths[i];
        }
        cs.endText();
        // 绘制表头下划线
        cs.moveTo(x, y - 2);
        cs.lineTo(x + PAGE_WIDTH, y - 2);
        cs.stroke();
    }

    /**
     * 绘制表格行
     */
    private void drawTableRow(PDPageContentStream cs, PDType0Font font,
                               float x, float y, float[] widths, String[] values) throws IOException {
        cs.beginText();
        cs.setFont(font, 10);
        float offsetX = x;
        for (int i = 0; i < values.length; i++) {
            cs.newLineAtOffset(offsetX, y);
            cs.showText(values[i]);
            offsetX += widths[i];
        }
        cs.endText();
    }
}
