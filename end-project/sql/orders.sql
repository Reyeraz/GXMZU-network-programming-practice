-- 订单主表
CREATE TABLE IF NOT EXISTS orders (
    order_id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    user_id     INT          NOT NULL COMMENT '用户ID',
    order_date  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    pay_amount   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待付款 1-已付款 2-已发货 3-已完成 4-已取消',
    consignee   VARCHAR(50)  NOT NULL COMMENT '收货人',
    telephone   VARCHAR(20)  NOT NULL COMMENT '联系电话',
    city        VARCHAR(50)  NOT NULL COMMENT '城市',
    address     VARCHAR(200) NOT NULL COMMENT '详细地址',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id      INT          NOT NULL COMMENT '订单ID',
    product_id    BIGINT       NOT NULL COMMENT '商品ID',
    product_name  VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    price         DECIMAL(10,2) NOT NULL COMMENT '商品价格（快照）',
    quantity      INT          NOT NULL COMMENT '购买数量',
    discount      DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '折扣',
    amount        DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
