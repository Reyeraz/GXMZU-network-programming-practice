# 网络编程实践 - 课件任务完成记录

## 已完成任务

### 第11次课 - Maven项目管理工具
- **PDF**: `课件/第11次课-Maven项目管理工具.pdf` (5页)
- **完成日期**: 2026-05-08
- **产出**:
  - `end-project/pom.xml` — Maven 项目配置，含 Spring Boot、Lombok 依赖
  - `end-project/src/main/java/com/example/demo/pojo/User.java` — 使用 @Data 注解的 POJO
  - `end-project/src/main/java/com/example/demo/ShoppingEndApplication.java` — 使用 @Slf4j 的主类
- **课堂练习**: 添加 Lombok 依赖 → 创建 User POJO → 使用 @Data/@Slf4j 注解 → 运行验证

### 第11次课 - 服务器后端项目搭建
- **PDF**: `课件/第11次课-服务器后端项目搭建.pdf` (7页)
- **完成日期**: 2026-05-08
- **产出**:
  - `end-project/` — 完整 Spring Boot 3.4.4 + Java 21 项目结构
  - `end-project/src/main/resources/application.properties` — 端口 8080
  - `end-project/src/main/java/com/example/demo/config/WebConfig.java` — CORS 配置
- **课堂练习**: 创建 Spring Boot 项目 → 运行验证 → 浏览器访问 localhost:8080

### 第11次课 - API接口文档
- **PDF**: `课件/第11次课-API接口文档.pdf` (6页)
- **完成日期**: 2026-05-08
- **产出**:
  - `课件/第11次课-API接口文档/额外文件/product_table.sql` — 商品表结构与初始数据
  - `课件/第11次课-API接口文档/额外文件/API接口文档-商品管理.md` — 完整 CRUD 接口文档
- **课堂练习**: 导出 product 表 SQL → 生成商品管理 API 接口文档

### 第12次课 - 搭建第一个Web API接口
- **PDF**: `课件/第12次课-搭建第一个Web API接口.pdf` (3页)
- **完成日期**: 2026-05-08
- **产出**:
  - `end-project/src/main/java/com/example/demo/controller/HelloController.java` — Hello 控制器
  - `end-project/src/main/java/com/example/demo/controller/ProductController.java` — 商品 CRUD API
  - `end-project/src/main/java/com/example/demo/model/Product.java` — 商品实体
  - `end-project/src/main/java/com/example/demo/model/ApiResponse.java` — 统一响应封装
  - `end-project/src/main/java/com/example/demo/model/PageResult.java` — 分页结果封装
  - `课件/第12次课-搭建第一个Web API接口/额外文件/api-test-backend.http` — 后端测试文件
- **课堂练习**: 创建 HelloController → 浏览器/ApiFox 测试 → 理解 @RestController 和 @GetMapping
- **拓展**: 实现完整商品管理 CRUD REST API（8 个接口）

## 项目结构

```
网络编程实践/
├── README.md                          # 本文件
├── 课件/                              # 课件 PDF 目录
│   ├── 第11次课-Maven项目管理工具.pdf
│   ├── 第11次课-服务器后端项目搭建.pdf
│   ├── 第11次课-API接口文档.pdf
│   ├── 第12次课-搭建第一个Web API接口.pdf
│   ├── 第11次课-API接口文档/额外文件/
│   │   ├── product_table.sql
│   │   └── API接口文档-商品管理.md
│   ├── 第11次课-Maven项目管理工具/额外文件/    # (空目录)
│   ├── 第11次课-服务器后端项目搭建/额外文件/   # (空目录)
│   └── 第12次课-搭建第一个Web API接口/额外文件/
│       └── api-test-backend.http
├── front-project/                     # Vue.js 前端项目（第7-10次课）
└── end-project/                       # Spring Boot 后端项目（第11-12次课）
    ├── pom.xml
    └── src/main/java/com/example/demo/
        ├── ShoppingEndApplication.java
        ├── config/WebConfig.java
        ├── controller/
        │   ├── HelloController.java
        │   └── ProductController.java
        ├── model/
        │   ├── Product.java
        │   ├── ApiResponse.java
        │   └── PageResult.java
        └── pojo/User.java
```

## 环境信息

- **Java**: 21.0.8
- **Maven**: 3.9.9
- **Spring Boot**: 3.4.4
- **Node.js**: (front-project)
- **OS**: Windows 11
