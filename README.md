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

### 第14次课 - 用户注册与登录
- **PDF**: `课件/第14次课 - 用户注册与登录.pdf` (25页)
- **完成日期**: 2026-05-13
- **产出**:
  - `end-project/src/main/java/com/example/demo/model/User.java` — 用户实体（MyBatis-Plus）
  - `end-project/src/main/java/com/example/demo/dto/UserRegisterRequest.java` — 注册请求DTO
  - `end-project/src/main/java/com/example/demo/dto/UserLoginRequest.java` — 登录请求DTO
  - `end-project/src/main/java/com/example/demo/mapper/UserMapper.java` — 用户Mapper
  - `end-project/src/main/java/com/example/demo/service/UserService.java` — 用户服务接口
  - `end-project/src/main/java/com/example/demo/service/impl/UserServiceImpl.java` — 用户服务实现（BCrypt加密、JWT生成）
  - `end-project/src/main/java/com/example/demo/controller/UserController.java` — 用户控制器（注册/登录接口）
  - `end-project/src/main/java/com/example/demo/config/SecurityConfig.java` — Spring Security配置（关闭默认安全、提供BCrypt Bean）
  - `end-project/src/main/java/com/example/demo/config/GlobalExceptionHandler.java` — 全局异常处理器
  - `end-project/src/main/java/com/example/demo/utils/JwtUtil.java` — JWT工具类
  - `end-project/pom.xml` — 添加spring-boot-starter-security和jjwt依赖
  - `front-project/src/views/Login.vue` — 登录页面
  - `front-project/src/views/Register.vue` — 注册页面
  - `front-project/src/stores/user.ts` — Pinia用户状态管理
  - `front-project/src/api/api.ts` — 添加userAPI（register/login）
  - `front-project/src/api/request.js` — 请求拦截器添加JWT Token
  - `front-project/src/types.ts` — 添加用户相关类型定义
  - `front-project/src/main.ts` — 添加登录/注册路由
  - `front-project/src/App.vue` — 导航栏添加登录/注册/退出入口
  - `课件/第14次课 - 用户注册与登录/额外文件/api-test-user.http` — API测试文件
- **课堂练习**:
  - 完成用户注册接口（BCrypt密码加密、用户名/手机号唯一性校验）
  - 完成用户登录接口（密码验证、JWT Token生成与返回）
  - 前端实现注册页面和登录页面，集成Pinia状态管理和JWT存储

### 第12次课 - Spring IoC
- **PDF**: `课件/第12次课-Spring IoC.pdf` (5页)
- **完成日期**: 2026-05-08
- **产出**:
  - `end-project/src/main/java/com/example/demo/service/GreetingService.java` — 使用 @Service 的业务服务 Bean
  - `end-project/src/main/java/com/example/demo/controller/HelloController.java` — 更新为使用 @Autowired 依赖注入
  - `课件/第12次课-Spring IoC/额外文件/api-test-ioc.http` — API 测试文件
- **课堂练习**: 
  - 练习1：删除 @Service → 观察错误，理解容器管理 Bean
  - 练习2：添加 timeGreeting() 方法和 /time-greeting 接口 → 理解单例 Bean 行为

### 第13次课 - 开发第一个商品API接口-从MySQL数据库获取商品数据
- **PDF**: `课件/第13次课-开发第一个商品API接口- 从MySQL数据库获取商品数据.pdf` (14页)
- **完成日期**: 2026-05-15
- **产出**:
  - `end-project/src/main/java/com/example/demo/model/Product.java` — 商品实体类（MyBatis-Plus注解）
  - `end-project/src/main/java/com/example/demo/model/ApiResponse.java` — 统一响应类
  - `end-project/src/main/java/com/example/demo/mapper/ProductMapper.java` — 数据访问层
  - `end-project/src/main/java/com/example/demo/service/ProductService.java` — 业务逻辑层
  - `end-project/src/main/java/com/example/demo/controller/ProductController.java` — 控制层（完整CRUD）
  - `end-project/src/main/resources/application.properties` — 数据库连接配置
  - `end-project/pom.xml` — MyBatis-Plus、MySQL驱动、Lombok依赖
- **课堂练习**:
  - 任务1：完成"获取所有商品"API接口开发
  - 任务2：实现"根据ID查询商品"API接口
  - 任务3：实现添加商品、修改商品和删除商品API接口

### 第15次课 - Spring MVC框架
- **PDF**: `课件/第15次课-Spring MVC框架.pdf` (4页)
- **完成日期**: 2026-05-15
- **课堂练习**: 理解Spring MVC核心组件（DispatcherServlet、HandlerMapping、HandlerAdapter、HttpMessageConverter、ViewResolver）及其在电商项目中的工作流程

### 第15次课 - Spring拦截器
- **PDF**: `课件/第15次课-Spring拦截器.pdf` (10页)
- **完成日期**: 2026-05-15
- **产出**:
  - `end-project/src/main/java/com/example/demo/interceptor/JwtInterceptor.java` — JWT拦截器（Token校验、userId提取）
  - `end-project/src/main/java/com/example/demo/config/WebConfig.java` — 更新为实现WebMvcConfigurer，注册拦截器，配置拦截/放行路径
- **课堂练习**: 完成JWT拦截器的实现并进行测试（未登录拦截、登录后携带Token访问、Token过期拦截）

### 第16次课 - 购物车接口开发
- **PDF**: `课件/第16次课-购物车接口开发.pdf` (28页) + `课件/购物车管理接口文档.pdf` (7页)
- **完成日期**: 2026-05-19
- **产出**:
  - `end-project/src/main/java/com/example/demo/model/Cart.java` — 购物车实体类（MyBatis-Plus）
  - `end-project/src/main/java/com/example/demo/model/Product.java` — 添加isAvailable字段
  - `end-project/src/main/java/com/example/demo/vo/CartItemVO.java` — 购物车视图对象（Cart+Product）
  - `end-project/src/main/java/com/example/demo/dto/AddToCartRequest.java` — 添加购物车请求DTO
  - `end-project/src/main/java/com/example/demo/dto/UpdateCartQuantityRequest.java` — 更新数量请求DTO
  - `end-project/src/main/java/com/example/demo/mapper/CartMapper.java` — 购物车Mapper
  - `end-project/src/main/java/com/example/demo/service/CartService.java` — 购物车服务接口
  - `end-project/src/main/java/com/example/demo/service/impl/CartServiceImpl.java` — 购物车服务实现
  - `end-project/src/main/java/com/example/demo/controller/CartController.java` — 购物车控制器
  - `课件/第16次课-购物车接口开发/额外文件/api-test-cart.http` — API测试文件
  - `课件/第16次课-购物车接口开发/额外文件/cart_table.sql` — 购物车表结构SQL
- **课堂练习**:
  - 接口1：GET /cart — 查询当前用户购物车列表（关联product表实时查询）
  - 接口2：POST /cart — 添加商品到购物车（校验商品上架、库存、重复累加）
  - 接口3：PUT /cart/{cart_id} — 修改购物车商品数量（越权校验、库存校验）
- **备注**: 剩余6个接口在第17次课完成

### 第17次课 - CartController代码审核与优化
- **PDF**: `课件/第17次课-CartController代码审核与优化.pdf` + `课件/购物车管理接口文档 (1).pdf`
- **完成日期**: 2026-05-22
- **产出**:
  - **代码优化**: 删除CartController中3个方法冗余的`userId`判空（JWT拦截器已确保非空）
  - **新增接口**（6个）:
    1. `PUT /cart/{cart_id}/selected` — 修改单个商品勾选状态（越权校验）
    2. `PUT /cart/selected/batch` — 全选/全不选（批量更新）
    3. `DELETE /cart/{cart_id}` — 删除购物车单个商品（越权校验）
    4. `DELETE /cart/selected` — 批量删除已勾选商品
    5. `GET /cart/count` — 获取购物车总件数（SUM(quantity)）
    6. `GET /cart/selected/count` — 获取已勾选商品总件数
  - `end-project/src/main/java/com/example/demo/dto/UpdateCartSelectedRequest.java` — 勾选状态请求DTO
  - `end-project/src/main/java/com/example/demo/vo/TotalCountVO.java` — 总件数VO
  - `end-project/src/main/java/com/example/demo/controller/CartController.java` — 新增6个接口、优化代码
  - `end-project/src/main/java/com/example/demo/service/CartService.java` — 新增6个方法声明
  - `end-project/src/main/java/com/example/demo/service/impl/CartServiceImpl.java` — 新增6个方法实现
  - `课件/第17次课-CartController代码审核与优化/额外文件/api-test-cart-new.http` — API测试文件
- **课堂练习**:
  - CartController代码审核：发现拦截器已校验userId，删除重复判空
  - 实现购物车完整9个REST API接口（GET/POST/PUT/DELETE）

## 项目结构

```
网络编程实践/
├── README.md
├── 作业完成记录.md
├── 课件/
│   ├── 第11次课-Maven项目管理工具.pdf
│   ├── 第11次课-服务器后端项目搭建.pdf
│   ├── 第11次课-API接口文档.pdf
│   ├── 第12次课-搭建第一个Web API接口.pdf
│   ├── 第12次课-Spring IoC.pdf
│   ├── 第13次课-开发第一个商品API接口.pdf
│   ├── 第14次课-用户注册与登录.pdf
│   ├── 第15次课-Spring MVC框架.pdf
│   ├── 第15次课-Spring拦截器.pdf
│   ├── 第16次课-购物车接口开发.pdf
│   ├── 购物车管理接口文档.pdf
│   ├── 第17次课-CartController代码审核与优化.pdf
│   ├── 第17次课-购物车接口开发-续.pdf
│   ├── 购物车管理接口文档 (1).pdf
│   ├── 第11次课-API接口文档/额外文件/
│   │   ├── product_table.sql
│   │   └── API接口文档-商品管理.md
│   ├── 第12次课-搭建第一个Web API接口/额外文件/
│   │   └── api-test-backend.http
│   ├── 第12次课-Spring IoC/额外文件/
│   │   └── api-test-ioc.http
│   ├── 第14次课-用户注册与登录/额外文件/
│   │   └── api-test-user.http
│   ├── 第16次课-购物车接口开发/额外文件/
│   │   ├── api-test-cart.http
│   │   └── cart_table.sql
│   └── 第17次课-CartController代码审核与优化/额外文件/
│       └── api-test-cart-new.http
├── front-project/                     # Vue.js 前端项目
└── end-project/                       # Spring Boot 后端项目
    ├── pom.xml
    └── src/main/java/com/example/demo/
        ├── ShoppingEndApplication.java
        ├── config/
        │   ├── WebConfig.java
        │   ├── SecurityConfig.java
        │   └── GlobalExceptionHandler.java
        ├── controller/
        │   ├── CartController.java
        │   ├── HelloController.java
        │   ├── ProductController.java
        │   └── UserController.java
        ├── service/
        │   ├── CartService.java
        │   ├── GreetingService.java
        │   ├── ProductService.java
        │   ├── UserService.java
        │   └── impl/
        │       ├── CartServiceImpl.java
        │       └── UserServiceImpl.java
        ├── mapper/
        │   ├── CartMapper.java
        │   ├── ProductMapper.java
        │   └── UserMapper.java
        ├── model/
        │   ├── Cart.java
        │   ├── Product.java
        │   ├── User.java
        │   ├── ApiResponse.java
        │   └── PageResult.java
        ├── dto/
        │   ├── AddToCartRequest.java
        │   ├── UpdateCartQuantityRequest.java
        │   ├── UpdateCartSelectedRequest.java
        │   ├── UserRegisterRequest.java
        │   └── UserLoginRequest.java
        ├── vo/
        │   ├── CartItemVO.java
        │   └── TotalCountVO.java
        ├── interceptor/
        │   └── JwtInterceptor.java
        ├── utils/
        │   └── JwtUtil.java
        └── pojo/
            └── User.java
```

## 环境信息

- **Java**: 21.0.8
- **Maven**: 3.9.9
- **Spring Boot**: 3.4.4
- **Node.js**: (front-project)
- **OS**: Windows 11
