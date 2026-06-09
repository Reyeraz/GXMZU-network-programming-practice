package com.example.demo.mapper;

import com.example.demo.dto.OrderInvoiceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderInvoiceMapper {

    @Select("""
            SELECT
                o.order_id AS orderId,
                o.order_date AS orderDate,
                o.total_amount AS totalAmount,
                o.pay_amount AS payAmount,
                o.status,
                o.consignee,
                o.telephone,
                CONCAT(o.city, ' ', o.address) AS address,
                u.username,
                u.phone AS userPhone,
                u.email AS userEmail
            FROM orders o
            LEFT JOIN user u ON o.user_id = u.user_id
            WHERE o.order_id = #{orderId}
            """)
    OrderInvoiceDTO selectOrderInvoiceData(@Param("orderId") Long orderId);

    @Select("""
            SELECT
                product_name AS productName,
                price,
                quantity,
                amount
            FROM order_item
            WHERE order_id = #{orderId}
            """)
    java.util.List<OrderInvoiceDTO.OrderItemDTO> selectOrderItems(@Param("orderId") Long orderId);
}
