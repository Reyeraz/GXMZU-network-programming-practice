#!/bin/bash
# 订单模块接口测试 - cURL 格式
# 请先通过登录接口获取Token，然后替换下方的token值

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInVzZXJJZCI6NCwiaWF0IjoxNzgwOTgzMDEwLCJleHAiOjE3ODE1ODc4MTB9.YzwLsbrlzQ0tN1jpIH3ymelVLHaya10njanr_YhyMbo"
BASE_URL="http://localhost:8080"

# ==================== 1. 创建订单 ====================

# 创建订单 - 正常请求
curl -X POST "${BASE_URL}/orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "consignee": "张三",
    "telephone": "13800138000",
    "city": "北京",
    "address": "朝阳路100号"
  }'

# 创建订单 - 另一个地址
curl -X POST "${BASE_URL}/orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "consignee": "李四",
    "telephone": "13900139000",
    "city": "上海",
    "address": "浦东新区陆家嘴100号"
  }'

# 创建订单 - 缺少收货人（应返回错误）
curl -X POST "${BASE_URL}/orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "consignee": "",
    "telephone": "13800138000",
    "city": "北京",
    "address": "朝阳路100号"
  }'

# 创建订单 - 电话格式错误（应返回错误）
curl -X POST "${BASE_URL}/orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "consignee": "王五",
    "telephone": "12345",
    "city": "北京",
    "address": "朝阳路100号"
  }'

# 创建订单 - 缺少城市（应返回错误）
curl -X POST "${BASE_URL}/orders/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{
    "consignee": "赵六",
    "telephone": "13800138000",
    "city": "",
    "address": "朝阳路100号"
  }'

# ==================== 2. 获取订单详情 ====================

# 获取订单详情 - 订单ID为1
curl -X GET "${BASE_URL}/orders/1" \
  -H "Authorization: Bearer ${TOKEN}"

# 获取订单详情 - 订单ID为2
curl -X GET "${BASE_URL}/orders/2" \
  -H "Authorization: Bearer ${TOKEN}"

# 获取订单详情 - 不存在的订单ID（应返回错误或空）
curl -X GET "${BASE_URL}/orders/9999" \
  -H "Authorization: Bearer ${TOKEN}"

# ==================== 3. 下载订单发票 ====================

# 下载订单发票 - 订单ID为1
curl -X GET "${BASE_URL}/invoice/download/1" \
  -H "Authorization: Bearer ${TOKEN}" \
  --output invoice_1.pdf

# 下载订单发票 - 订单ID为2
curl -X GET "${BASE_URL}/invoice/download/2" \
  -H "Authorization: Bearer ${TOKEN}" \
  --output invoice_2.pdf
