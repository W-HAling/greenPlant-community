# 办公室绿植领养系统

一个企业内部绿植领养与社区养护平台，支持员工领养办公室绿植、记录养护日志、分享养护经验。

## 项目结构

```
greenPlant-community/
├── plant-adoption-server/     # 后端服务 (Spring Boot 3.2.5)
├── plant-adoption-h5/         # H5移动端 (UniApp + Vue3)
├── plant-adoption-admin/      # 管理后台 (Vue3 + Element Plus)
└── 办公室绿植领养系统 - 产品规划文档（PRD）.md
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.6
- MySQL 8.0.33
- Redis (Redisson 分布式锁)
- RabbitMQ
- MinIO (对象存储)
- JWT 认证

### 前端H5
- UniApp
- Vue 3
- uView Plus 2.x
- Pinia
- TypeScript

### 管理后台
- Vue 3
- Element Plus
- Vue Router
- Pinia
- TypeScript
- Vite
- ECharts

## 快速开始

### 环境要求
- Node.js >= 18
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 1. 数据库初始化

```bash
# 执行数据库初始化脚本
mysql -u root -p < plant-adoption-server/src/main/resources/db/init.sql
```

### 2. 启动后端服务

```bash
cd plant-adoption-server
mvn clean install -DskipTests
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 3. 启动H5前端

```bash
cd plant-adoption-h5
pnpm install
pnpm dev:h5
```

H5前端将在 http://localhost:3000 启动

### 4. 启动管理后台

```bash
cd plant-adoption-admin
pnpm install
pnpm dev
```

管理后台将在 http://localhost:3001 启动

## 默认账号

### 管理员账号
- 手机号: 13800138000
- 验证码: 123456 (模拟环境)

## 功能模块

### 用户端 (H5)
- 手机验证码登录/注册
- 绿植列表浏览与搜索
- 绿植详情查看
- 领养申请与管理
- 养护日志记录
- 社区帖子发布与互动
- 消息通知

### 管理后台
- 仪表盘数据统计
- 用户管理
- 绿植管理
- 领养审批
- 养护记录查看
- 社区内容管理
- 部门管理
- 系统配置

## API文档

启动后端服务后，访问: http://localhost:8080/api/doc.html

## 项目配置

### 后端配置文件
- `application.yml` - 主配置
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

### 数据库配置
修改 `application-dev.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/plant_db
    username: root
    password: your_password
```

### Redis配置
修改 `application-dev.yml` 中的Redis连接信息：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 17
```

## 许可证

MIT License
