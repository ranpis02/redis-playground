# 🚀 黑马点评（Redis Playground）

> **黑马点评 —— 一个基于 Spring Boot 3 + Redis 的「探店点评」后端实战项目，深度整合 Redis 核心能力，涵盖缓存加速、分布式锁、秒杀系统、Feed 流、全局 ID 生成等高频业务场景。**

---

## 📖 项目概述

本项目源于黑马程序员品达架构课程，以「**大众点评**」风格的探店点评平台为业务背景，通过**真实业务需求驱动**的方式，系统性地演示了 Redis 在微服务后端开发中的**八大核心应用场景**。每个场景都有完整的工程化实现，而非零散的 Demo 代码。

无论是想学习 Redis 实战技巧，还是需要一个可扩展的点评后端骨架，本项目都值得研究。

---

## 🧩 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.15 | 核心框架 |
| Java | 17 | 运行环境 |
| Maven | 3.6+ | 构建工具 |
| MySQL | 8.4+ | 持久化数据库 |
| Redis | 7.2+ | 缓存/分布式组件 |
| MyBatis-Plus | 最新 | ORM 框架 |
| Redisson | 4.5.0 | 分布式锁客户端 |
| Knife4j | 最新 | API 文档 |
| Lombok | — | 代码简化 |
| Hutool | — | 工具库 |

---

## 🎯 Redis 八大实战场景

### 1. 🪙 全局唯一 ID 生成（RedisIdWorker）

基于 **Redis 自增计数器** + **时间戳偏移** 实现分布式全局 ID，类似雪花算法但无需依赖机器时钟：

```
高32位（时间戳） | 低32位（自增序列）
```

> 📁 `com.review.utils.RedisIdWorker`

### 2. 🔐 短信登录 & Session 管理

- 验证码存入 Redis（5 分钟 TTL）
- 登录成功后生成 token，用户信息存入 Redis（7 天 TTL）
- `RefreshTokenInterceptor` 拦截器自动续签

> 📁 `com.review.service.impl.UserServiceImpl` + `com.review.Interceptor.*`

### 3. 🏪 商户缓存（缓存穿透 / 击穿防护）

多层缓存策略组合：

| 策略 | 说明 |
|------|------|
| **缓存穿透防护** | 缓存未命中时写入空值（短 TTL），防止恶意请求穿透到 DB |
| **缓存击穿防护** | **逻辑过期**方案 + **互斥锁**方案，二选一均可 |
| **缓存雪崩预防** | 缓存 TTL 添加随机偏移量，避免大量缓存同时过期 |

> 📁 `com.review.cache.CacheClient` — 通用缓存工具类，支持泛型回调

### 4. 🔒 分布式锁

#### 4.1 自定义 Redis 锁（SimpleRedisLock）

基于 `SET NX PX` + Lua 脚本实现，支持：
- **原子加锁**：`SET key value NX PX ttl`
- **原子解锁**：Lua 脚本校验线程标识，防止误删

> 📁 `com.review.utils.lock.SimpleRedisLock` + `resources/scripts/lock.lua` + `unlock.lua`

#### 4.2 Redisson 分布式锁

集成 `RedissonClient`，利用 `RLock` 实现更高级的分布式互斥控制。

> 📁 `com.review.config.RedissonConfig`

### 5. ⚡ 秒杀系统（Redis Stream + Lua）

高性能秒杀方案，核心逻辑由 **Lua 脚本** 在 Redis 端原子执行：

1. **库存校验 & 扣减** — Redis 原子 DECR
2. **重复下单校验** — SET 去重标记
3. **订单入队** — XADD 写入 Redis Stream
4. **异步消费** — 后台线程拉取 Stream 数据，写入 MySQL

整个秒杀流程仅需 **一次 Redis 网络往返**，性能极高。

> 📁 `resources/scripts/stream-seckill.lua` + `com.review.service.impl.VoucherOrderServiceImpl`

### 6. 👍 点赞排行榜（Sorted Set）

- 使用 **Set** 存储点赞用户，保证唯一性
- 使用 **Sorted Set** 按点赞时间排序，实现「点赞排行榜」
- 使用 **ZSet** 实现共同关注等社交功能

> 📁 `com.review.service.impl.BlogServiceImpl`

### 7. 📰 关注 &  Feed 流推送

- **关注/取关** — 实时操作
- **Feed 流** — 基于 **Sorted Set** 的推模式，按时间线滚动查询
- **共同关注** — Set 交集运算

> 📁 `com.review.service.impl.FollowServiceImpl`

### 8. 📋 店铺分类缓存

全量缓存店铺分类列表，利用 Redis 的 `List` / `Set` 结构存储，减少数据库压力。

> 📁 `com.review.service.impl.ShopTypeServiceImpl`

---

## 📂 项目结构

```
黑马点评（redis-playground）/
├── src/main/java/com/review/
│   ├── ReviewApplication.java          # 启动类
│   ├── config/                          # 配置类
│   │   ├── RedisConfig.java             # RedisTemplate 配置
│   │   ├── RedisScriptConfig.java       # Lua 脚本 Bean 注册
│   │   ├── RedissonConfig.java          # Redisson 客户端配置
│   │   ├── Knife4jConfig.java           # API 文档配置
│   │   └── MvcConfig.java               # Spring MVC 拦截器注册
│   ├── cache/
│   │   └── CacheClient.java             # 通用缓存客户端（穿透/击穿防护）
│   ├── controller/                      # REST 控制器
│   │   ├── UserController.java          # 用户登录/注册
│   │   ├── ShopController.java          # 商户查询
│   │   ├── ShopTypeController.java      # 商户分类
│   │   ├── BlogController.java          # 探店笔记
│   │   ├── BlogCommentsController.java  # 笔记评论
│   │   ├── FollowController.java        # 关注/取关
│   │   ├── UploadController.java        # 文件上传
│   │   ├── VoucherController.java       # 优惠券管理
│   │   └── VoucherOrderController.java  # 秒杀下单
│   ├── service/                         # 业务逻辑层
│   │   └── impl/                        # 实现类
│   ├── mapper/                          # MyBatis-Plus 数据访问
│   ├── model/
│   │   ├── entity/                      # 数据实体
│   │   ├── dto/                         # 数据传输对象
│   │   └── enums/                       # 枚举
│   ├── Interceptor/                     # 登录拦截器
│   │   ├── LoginInterceptor.java        # 登录校验拦截器
│   │   └── RefreshTokenInterceptor.java # Token 自动续签拦截器
│   └── utils/
│       ├── RedisConstants.java          # Redis Key 前缀 & TTL 常量
│       ├── RedisIdWorker.java           # 全局 ID 生成器
│       ├── CacheClient.java             # 缓存工具类
│       ├── lock/
│       │   ├── RedisLock.java           # 分布式锁接口
│       │   └── SimpleRedisLock.java     # 基于 SETNX + Lua 实现
│       ├── R.java                       # 统一响应结果
│       ├── UserHolder.java              # 当前线程用户持有者
│       └── SystemConstants.java         # 系统常量
├── src/main/resources/
│   ├── application.yaml                 # 应用配置
│   ├── db/review.sql                    # 数据库初始化脚本
│   └── scripts/
│       ├── lock.lua                     # 加锁脚本
│       ├── unlock.lua                   # 解锁脚本
│       └── stream-seckill.lua           # 秒杀 Lua 脚本
├── pom.xml
└── README.md
```

---

## 🚦 快速开始

### 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.2+（建议搭配 Redis Stack）

### 1. 初始化数据库

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS review DEFAULT CHARSET utf8mb4;

-- 导入表结构
mysql -u root -p review < src/main/resources/db/review.sql
```

### 2. 修改配置

编辑 `src/main/resources/application.yaml`，按需修改：

```yaml
spring:
  datasource:
    username: root         # 你的 MySQL 用户名
    password: root         # 你的 MySQL 密码
  data:
    redis:
      host: 127.0.0.1     # Redis 地址
      port: 6379           # Redis 端口
```

### 3. 构建 & 启动

```bash
# 编译打包
mvn clean install -DskipTests

# 启动应用
mvn spring-boot:run
```

应用默认启动在 `http://localhost:8081`。

### 4. 访问 API 文档

启动后打开：**[http://localhost:8081/doc.html](http://localhost:8081/doc.html)**（Knife4j 增强文档）

---

## ⚙️ 配置详解

### Redis 连接池

Spring Boot 3 中 Lettuce 连接池配置路径为 `spring.data.redis.lettuce.pool`：

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 10     # 最大连接数
          max-idle: 8        # 最大空闲连接
          min-idle: 2        # 最小空闲连接
          max-wait: 3s       # 获取连接最大等待时间
```

### Lua 脚本

所有 Lua 脚本位于 `resources/scripts/`，通过 `RedisScriptConfig` 注册为 Bean：

| 脚本 | 用途 |
|------|------|
| `lock.lua` | 原子加锁（SET NX PX） |
| `unlock.lua` | 原子解锁（校验线程标识后 DEL） |
| `stream-seckill.lua` | 秒杀全流程（库存扣减 + 去重 + 消息入队） |

---

## 🔍 关键实现一览

### 缓存穿透防护（CacheClient）

```java
// 查询时自动处理缓存穿透
Shop shop = cacheClient.queryWithBlankCache(
    CACHE_SHOP_KEY_PREFIX, id,
    dbId -> shopMapper.selectById(dbId),  // DB 回查逻辑
    Shop.class, CACHE_SHOP_TTL, TimeUnit.SECONDS
);
```

### 缓存击穿防护 — 逻辑过期方案

数据缓存包含**逻辑过期时间**，查询时判断是否过期；若过期则获取互斥锁，异步重建缓存。

### Redis 分布式 ID

```java
@Autowired
private RedisIdWorker redisIdWorker;

long orderId = redisIdWorker.nextId("order");  // 生成全局唯一订单 ID
```

### 秒杀 Lua 脚本

秒杀核心逻辑全部在 Redis 端用 Lua 完成，**一次网络 IO** 完成全部验证 + 扣减 + 入队，避免竞态条件。

---

## 🧪 测试

```bash
mvn test
```

---

## 📌 Redis 版本建议

推荐使用 **Redis 7.2.x 或 7.4.x**，与 Spring Boot 3 默认集成的 Lettuce 客户端兼容性最佳。本项目中使用的 Stream、Set、Sorted Set 等数据结构在 7.x 版本中均有良好支持。

---

## 🤝 贡献

欢迎提交 Issue 或 Pull Request！如果你有新的 Redis 实战场景想要加入，请确保：

1. 场景有清晰的业务背景
2. 提供完整的 Controller → Service → Redis 实现
3. 附必要的注释说明

---

## 📄 许可证

本项目基于 MIT 许可证开源，详见 [LICENSE](LICENSE) 文件。

