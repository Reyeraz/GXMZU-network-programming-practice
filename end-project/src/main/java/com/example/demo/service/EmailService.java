package com.example.demo.service;

import com.example.demo.model.Orders;
import com.example.demo.vo.OrderCreateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 * 异步发送订单确认邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 异步发送订单确认邮件
     * @param toEmail 收件人邮箱
     * @param order 订单信息
     * @param orderVO 订单创建结果
     */
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Orders order, OrderCreateVO orderVO) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("your-qq-email@qq.com"); // 需要配置为实际发件人
            message.setTo(toEmail);
            message.setSubject("【电商购物平台】订单确认 - 订单号：" + orderVO.getOrderId());
            message.setText(buildOrderConfirmationContent(order, orderVO));

            mailSender.send(message);
            log.info("订单确认邮件发送成功: orderId={}, toEmail={}", orderVO.getOrderId(), toEmail);
        } catch (Exception e) {
            log.error("订单确认邮件发送失败: orderId={}, toEmail={}", orderVO.getOrderId(), toEmail, e);
            // 邮件发送失败不影响订单创建，仅记录日志
        }
    }

    private String buildOrderConfirmationContent(Orders order, OrderCreateVO orderVO) {
        StringBuilder content = new StringBuilder();
        content.append("尊敬的用户，您好！\n\n");
        content.append("您的订单已成功创建，订单信息如下：\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        content.append("订单编号：").append(orderVO.getOrderId()).append("\n");
        content.append("订单金额：￥").append(orderVO.getPayAmount()).append("\n");
        content.append("收货人：").append(order.getConsignee()).append("\n");
        content.append("联系电话：").append(order.getTelephone()).append("\n");
        content.append("收货地址：").append(order.getCity()).append(" ").append(order.getAddress()).append("\n");
        content.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        content.append("请尽快完成支付，如有疑问请联系客服。\n\n");
        content.append("感谢您的支持！\n");
        content.append("电商购物平台");
        return content.toString();
    }
}
