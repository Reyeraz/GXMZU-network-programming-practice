# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库结构

这是一个电商教学项目的单体仓库（monorepo）：

- `end-project/` — Spring Boot 3.4.4 + Java 21 后端，端口 8080
- `front-project/` — Vue 3 + Vite + Element Plus + TypeScript 前端，端口 5173
- `课件/` — 课程 PDF 和配套资源

## 构建与运行

### 后端

Maven 位于 `D:\IDE\IDEA\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3`，通过 PowerShell 执行：

```powershell
# 编译
& 'D:\IDE\IDEA\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd' -f end-project/pom.xml compile
# 测试
... mvn.cmd -f end-project/pom.xml test
# 运行单个测试类
... mvn.cmd -f end-project/pom.xml test -Dtest='OrderControllerTest,OrdersServiceImplTest'
# 打包
... mvn.cmd -f end-project/pom.xml package -DskipTests
# 启动
... mvn.cmd -f end-project/pom.xml spring-boot:run
```

### 前端

```bash
cd front-project && npm run dev     # 开发服务器
cd front-project && npm run build   # 构建
```

## 后端架构

标准三层架构，包路径 `com.example.demo`：

| 包 | 职责 |
|---|---|
| `model/` | 实体类（MyBatis-Plus `@TableName` 注解），含 `ApiResponse`/`PageResult` 通用响应 |
| `mapper/` | MyBatis-Plus `BaseMapper`，使用 `@Update` 写自定义 SQL |
| `service/` | 接口继承 `IService`，实现继承 `ServiceImpl<Mapper, Entity>`，用 `@RequiredArgsConstructor` 注入 |
| `controller/` | `@RestController`，从 `request.getAttribute("userId")` 获取当前用户 |
| `dto/` | 请求体对象，使用 `jakarta.validation` 校验注解 |
| `vo/` | 视图对象，用于组装返回数据 |
| `config/` | `WebConfig`（CORS + JWT拦截器）、`SecurityConfig`（关闭默认安全，提供 BCrypt Bean）、`GlobalExceptionHandler` |
| `interceptor/` | `JwtInterceptor` 校验 Token 并注入 `userId` |
| `utils/` | `JwtUtil` JJWT 工具类 |

**数据库**：远程 MySQL `p-c12.polars.cc:54237`，数据库 `user_db`，用户 `user`。MyBatis-Plus 驼峰映射已开启。

**认证流程**：`JwtInterceptor.preHandle()` → 从 `Authorization: Bearer <token>` 解析 `userId` → 存入 `request.setAttribute("userId", userId)` → Controller 从 request 取 userId。

**拦截器放行路径**（`WebConfig.addInterceptors`）：`/user/login`, `/user/register`, `/api/products/**`, `/api/categories/**`, `/hello`。新增公开接口需在此注册。

**统一响应** `ApiResponse<T>`：
- `ApiResponse.ok(data)` → `{"success": true, "data": ...}`
- `ApiResponse.fail(message)` → `{"success": false, "message": "..."}`
- `RuntimeException` 由 `GlobalExceptionHandler` 捕获转为 `fail` 响应

**关键约定**：
- Controller 方法中 **不判空 userId**（JWT 拦截器已保证）
- 购物车仅操作当前用户数据（`eq(Cart::getUserId, userId)`）
- 订单明细保存 `product_name` 和 `price` 作为**快照**
- 扣库存用**乐观锁**：`UPDATE ... WHERE stock >= #{quantity}`，判断返回影响行数为 0 则库存不足

## 前端架构

- Vue 3 + Pinia 状态管理 + Vue Router
- `src/api/request.js` — Axios 实例，请求拦截器注入 JWT Token
- `src/api/api.ts` — API 方法集合
- `src/stores/user.ts` — Pinia 用户状态（token 持久化 localStorage）
- Vite 代理 `/api` → `http://localhost:8080`

## 数据库表

| 表名 | 说明 |
|---|---|
| `user` | 用户表 |
| `product` | 商品表（含 `is_available`, `stock`） |
| `cart` | 购物车（`user_id` + `product_id` 唯一约束） |
| `orders` | 订单主表（status: 0待付款/1已付款/2已发货/3已完成/4已取消） |
| `order_item` | 订单明细（商品快照） |

## 测试

- **集成测试**：`@SpringBootTest` + `@Transactional`，操作真实数据库，事务自动回滚
- **REST 层测试**：`@WebMvcTest` + `@ContextConfiguration(classes = {...})` + Mockito mock Service 层
  - 必须 mock `JwtInterceptor`，模拟注入 `userId`（见 `CartControllerTest.setUp()` 模式）
  - 注入的 Controller 类、`GlobalExceptionHandler`、`SecurityConfig`、`WebConfig` 必须全部声明在 `@ContextConfiguration.classes` 中
- HTTP 测试文件 `api-test-*.http` 位于项目根目录（用于 IntelliJ/ApiFox）

## 多仓库推送

根仓库推送到 GitHub。前端和后端子目录需分别作为独立仓库推送到 Gitee：

```bash
# 前端 → Gitee
git subtree push --prefix=front-project git@gitee.com:xiazzzzz/front-project.git main
# 后端 → Gitee
git subtree push --prefix=end-project git@gitee.com:xiazzzzz/end-project.git main
```

## 作业管理

每次完成课件任务后更新 `作业完成记录.md`（按日期记录完成内容与待完成清单），并同步更新 `README.md`。
