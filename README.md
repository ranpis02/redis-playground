# Redis Playground

这是一个基于 Spring Boot 的 Redis 实验项目。

## 项目结构

```
redis-playground/
├── src/
│   ├── main/
│   │   ├── java/com/review/
│   │   │   ├── Application.java          # 应用主类
│   │   │   └── controller/
│   │   │       └── HelloController.java  # REST 控制器示例
│   │   └── resources/
│   │       └── application.properties    # 应用配置文件
│   └── test/
│       └── java/                          # 测试代码
├── pom.xml                               # Maven 构建配置
└── README.md                             # 项目说明文档
```

## 前置要求

- Java 17 或以上版本
- Maven 3.6 或以上版本
- Redis 服务器（可选，用于测试 Redis 集成）

## 快速开始

### 1. 构建项目

```bash
mvn clean install
```

### 2. 运行应用

```bash
mvn spring-boot:run
```

或者：

```bash
java -jar target/redis-playground-1.0.0.jar
```

### 3. 测试 API

应用启动后，你可以访问：

- `http://localhost:8080/api/hello` - 测试基本功能
- `http://localhost:8080/api/status` - 检查应用状态

### 主要依赖

- **Spring Boot 3.5.15** - 核心框架
- **Spring Data Redis** - Redis 集成
- **commons-pool2** - Lettuce 连接池支持
- **Redisson** - 分布式锁与高级 Redis 能力
- **Lombok** - 代码简化工具
- **DevTools** - 热重启支持

### Redis 版本建议

建议使用 **Redis 7.2.x 或 7.4.x**。对 Spring Boot 3 来说，这两个版本都比较稳，和 `spring-boot-starter-data-redis` 默认使用的 Lettuce 客户端兼容性也最好。

如果你后面要和 MySQL 8.4.9 一起用，这两个服务端版本之间没有直接冲突；Redis 版本主要看你的部署环境和是否需要新特性，不受 MySQL 版本限制。

### 连接池配置

Spring Boot 3 里 Lettuce 连接池要配在 `spring.data.redis.lettuce.pool` 下，不是旧的 `spring.redis.lettuce.pool`。当前示例已经在 `application.yaml` 里补了 `max-active`、`max-idle`、`min-idle` 和 `max-wait`。

### Redisson 分布式锁

项目里已经加了 `RedissonClient` 配置和一个 `DistributedLockService` 示例，可以直接用 `RLock` 做分布式互斥控制。

## 配置说明

编辑 `src/main/resources/application.properties` 来配置：

- 服务器端口（默认 8080）
- Redis 连接参数（主机、端口、密码等）
- 日志级别

## 开发指南

### 添加新的 REST 端点

在 `src/main/java/com/example/controller/` 目录下创建新的控制器类：

```java
@RestController
@RequestMapping("/api/myfeature")
public class MyFeatureController {
    // 你的端点实现
}
```

### 使用 Redis

注入 `RedisTemplate` 使用 Redis：

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void saveData(String key, Object value) {
    redisTemplate.opsForValue().set(key, value);
}
```

## 故障排除

- **连接 Redis 失败**: 确保 Redis 服务器正在运行，检查 `application.properties` 中的连接配置
- **端口被占用**: 修改 `application.properties` 中的 `server.port` 值
- **编译错误**: 确保 Java 版本 >= 17

## 许可证

查看 LICENSE 文件了解许可证信息。

## 更多资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Data Redis 文档](https://spring.io/projects/spring-data-redis)
- [Redis 官方网站](https://redis.io/)

