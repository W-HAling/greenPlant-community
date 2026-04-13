
# 办公室绿植领养系统 - 产品规划文档（PRD）
**版本**: v1.2 
**日期**: 2026-03-23 
**状态**: 需求确认，待开发 
---
## 一、产品概述
### 1.1 产品定位
企业内部绿植共享领养 + 社区化养护平台，通过数字化手段解决办公室绿植管理难题，同时增强员工参与感和归属感。
### 1.2 核心价值主张
| 利益相关方 | 痛点 | 解决方案 | 价值 |
| ------------ | -------------------------- | ----------------------- | ------------------------------ |
| **企业行政** | 绿植无人养护，经常枯萎死亡 | 员工领养制 + 数字化追踪 | 降低绿植损耗成本，提升办公环境 |
| **企业员工** | 想养绿植但条件受限 | 办公室领养 + 养护指导 | 满足养植爱好，缓解工作压力 |
| **领养员工** | 担心养护责任不明确 | 二维码绑定 + 记录可追溯 | 明确责任，获得养护成就感 |
### 1.3 目标用户画像
**主要用户 - 企业员工（领养人）**
- 年龄：22-35岁
- 特征：办公室白领，对绿植有兴趣但家里养不了
- 需求：方便领养、养护指导、分享交流
**次要用户 - 行政/后勤管理员**
- 职责：绿植资产管理、领养审批、养护监督
- 需求：高效管理、数据可视化、责任追溯
### 1.4 业务规则与全局约束
*(注：本节为系统开发的硬性约束，AI Coding 必须严格遵守)*
#### 1.4.1 统一响应与错误码规范
**统一响应格式：**
```json
{
  "code": 0,
  "message": "success",
  "data": { ... }
}
```
**统一错误码：**
| 错误码 | 说明 | HTTP 状态码 |
| ------ | ---------------- | ----------- |
| 0 | 成功 | 200 |
| 10001 | 参数校验失败 | 400 |
| 10002 | 未登录/Token过期 | 401 |
| 10003 | 无权限 | 403 |
| 20001 | 绿植不存在 | 404 |
| 20002 | 绿植状态不可领养 | 409 |
| 20003 | 超出领养数量限制 | 409 |
| 20004 | 重复申请 | 409 |
| 30001 | 帖子不存在 | 404 |
| 99999 | 系统内部错误 | 500 |
#### 1.4.2 领养与审批规则
- **领养数量限制**：单个用户最大可同时领养 5 棵绿植。该数值需支持通过系统配置表动态调整，不写死在代码中。
- **审批规则**：强制人工审批，提交至管理员处理。若超时未处理则自动驳回，默认超时时间为 24 小时。该超时时间同样支持通过系统配置表动态调整。
- **超时实现机制**：使用 Redis Key 过期事件监听。提交申请时写入 Redis Key（TTL=配置的超时小时数），过期时触发自动驳回逻辑。
#### 1.4.3 状态流转规则
- **申请前提**：绿植状态必须为 `available` 时才可发起领养申请。
- **放弃领养**：用户和管理员均有权操作"放弃领养"（解除绑定）。用户主动放弃时，无需审批，直接解除绑定，但必须发送消息通知对应管理员。
- **删除保护**：需要删除绿植（变更为 `removed` 软删除状态）时，系统必须校验该绿植是否存在 `active` 状态的领养记录，若有则必须先解除领养记录才能删除。
#### 1.4.4 归还与移交规则
- **移交操作**：仅管理员有权进行绿植移交（更换领养人）。
- **放弃操作**：用户无权移交，只能选择"放弃领养"。
- **记录追溯**：发生移交或放弃时，原领养人的领养记录状态更新（如改为 `returned`），但**记录本身继续保留**，确保全生命周期可追溯。
#### 1.4.5 养护提醒规则
- 需支持为每一棵绿植单独配置养护计划及提醒频率（如：每 3 天提醒一次浇水）。
- 提醒通过消息通知表下发，类型为 `care_remind`。
#### 1.4.6 社区管理规则
- **发帖机制**：社区帖子发布不需要后台审核，不需要做敏感词过滤。
- **隐藏权限**：管理员有权在后台将违规帖子状态设为 `hidden`（隐藏）。
- **删帖权限**：用户有权删除自己发布的帖子（状态设为 `deleted`）。
#### 1.4.7 并发控制规则
- 针对领养申请等可能产生并发竞争的场景，必须使用 **Redisson 的 `RLock`**，锁 Key 格式为 `lock:plant:adopt:{plantId}`，等待时间 0 秒，持有时间 10 秒。
#### 1.4.8 消息通知规则
- 异步消息通过 **RabbitMQ** 进行解耦投递，消费端手动确认（acknowledge-mode: manual）。
- 消息发送失败支持重试（retry.enabled: true）。
---
## 二、技术架构
### 2.1 技术栈与精确依赖版本
| 层级 | 技术方案 | 精确版本/说明 |
| ------------------ | ------------------- | --------------------------------------------- |
| **用户端** | UniApp + Vue3 | 跨平台 H5，支持微信/钉钉内嵌 |
| **管理端** | Vue3 + Element Plus | 响应式 Web 管理后台 |
| **后端服务** | Spring Boot | **3.2.5** (Java 17) |
| **ORM框架** | MyBatis-Plus | **3.5.6**（不使用原生 MyBatis） |
| **数据库** | MySQL Connector | **8.0.33** |
| **缓存与分布式锁** | Redis + Redisson | spring-boot-starter-data-redis + Redisson |
| **消息队列** | RabbitMQ | spring-boot-starter-amqp |
| **对象存储** | MinIO | **8.5.9** |
| **二维码** | ZXing | **core 3.5.3** + **javase 3.5.3** |
| **认证授权** | JWT (jjwt) | **0.12.5** |
| **代码简化** | Lombok | **1.18.32** |
| **对象映射** | MapStruct | **1.5.5.Final**（DTO 转换，禁止手写 set/get） |
| **部署** | Docker + Nginx | 容器化部署，负载均衡 |
### 2.2 关键技术配置决策
- **ORM 使用**：使用 MyBatis-Plus，**不写 XML**，统一使用注解 + `LambdaQueryWrapper`。
- **认证方式**：JWT 存放于 Header 的 `Authorization` 字段，格式为 `Bearer {token}`。
- **Token 策略**：有效期 7 天；过期后需重新登录（**不做 refresh token 机制**）。
- **文件上传**：单文件最大 **20MB**，仅允许上传后缀为 `jpg`、`png`、`webp` 的图片。
- **分页规范**：统一使用 MyBatis-Plus 的 `Page<T>` 对象，前端传参字段名为 `page` 和 `size`。分页响应体中列表字段名统一为 `records`。
- **主键策略**：使用数据库自增（**不使用雪花算法**）。
- **删除策略**：全局不使用逻辑删除插件，直接物理删除；**唯独绿植表**使用 `removed` 状态表示软删除。
- **MinIO 规范**：桶名称固定为 `plant-images`，上传后返回给前端的 URL 为完整的 HTTP 链接。
- **枚举持久化与序列化规范 (AI 强制遵循)**：所有枚举类必须放在 `common/enums` 包下，实现 MyBatis-Plus 的 `IEnum<String>` 接口。存入数据库的字段加 `@EnumValue` 注解，返回给前端的 JSON 字段加 `@JsonValue` 注解。确保数据库存 `available`，前端 API 返回 `"available"` 而不是大写或数字索引。
  ```java
  // 示例结构（AI 必须照此生成所有枚举）
  public enum PlantStatusEnum implements IEnum<String> {
      AVAILABLE("available", "可领养"),
      ADOPTED("adopted", "已领养");
      
      @EnumValue // 标记存入DB的值
      private final String value;
      
      @JsonValue // 标记返回给前端的JSON值
      private final String desc;
      // ...
  }
  ```
### 2.3 系统架构图
```text
┌─────────────────────────────────────────────────────────────────┐
│                         客户端层                                │
├─────────────────┬──────────────────┬────────────────────────────┤
│    UniApp H5    │    管理端 Web     │        微信小程序           │
│    (员工使用)    │    (管理员使用)    │        (可选扩展)           │
└────────┬────────┴────────┬─────────┴─────────────┬──────────────┘
         │                 │                       │
         └─────────────────┼───────────────────────┘
                           │ HTTPS/JSON
┌──────────────────────────┼──────────────────────────────────────┐
│ 网关层                   │ Nginx                                │
│                          │ 负载均衡、限流、鉴权、路由            │
└──────────────────────────┼──────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│ 服务层            Spring Boot 3.2.5                             │
│                                                               │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│ │ 用户服务 │ │ 绿植服务 │ │ 领养服务 │ │ 养护服务 │           │
│ │   Auth   │ │  Plant   │ │ Adoption │ │   Care   │           │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│ │ 社区服务 │ │ 消息服务 │ │ 二维码服务│ │ 通知服务 │           │
│ │ Community│ │  Notify  │ │    QR    │ │  Config  │           │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│ 中间件层                                                      │
│ ┌──────────┐ ┌───────────────┐ ┌──────────────────┐           │
│ │  Redis   │ │   RabbitMQ    │ │      MinIO       │           │
│ │ 缓存/锁  │ │ 异步消息队列   │ │    对象存储       │           │
│ │ Key过期  │ │ 延时/事件驱动  │ │  plant-images    │           │
│ └──────────┘ └───────────────┘ └──────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────┼──────────────────────────────────────┐
│ 数据层                     MySQL 8.0                            │
└─────────────────────────────────────────────────────────────────┘
```
### 2.4 数据库设计（完整表结构）
```sql
-- ============================================================
-- 1. 部门表（sys_user 的 dept_id 引用此表）
-- ============================================================
CREATE TABLE sys_department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '部门名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0为顶级',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent (parent_id)
) COMMENT='部门表';
-- ============================================================
-- 2. 系统配置表（支持动态配置领养上限、超时时间等）
-- ============================================================
CREATE TABLE sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_value VARCHAR(500) NOT NULL COMMENT '配置值',
  config_desc VARCHAR(255) COMMENT '配置描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_config_key (config_key)
) COMMENT='系统配置表';
-- 初始种子数据
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
('adoption_max_limit', '5', '单个用户最大同时领养数量'),
('adoption_timeout_hours', '24', '领养申请超时自动驳回小时数');
-- ============================================================
-- 3. 用户表
-- ============================================================
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  real_name VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  dept_id BIGINT COMMENT '部门ID',
  role ENUM('ADMIN','USER') DEFAULT 'USER' COMMENT '角色',
  avatar_url VARCHAR(500) COMMENT '头像URL',
  status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_phone (phone),
  INDEX idx_dept (dept_id)
) COMMENT='用户表';
-- ============================================================
-- 4. 绿植表（新增养护提醒字段）
-- ============================================================
CREATE TABLE plant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '绿植名称',
  species VARCHAR(100) COMMENT '品种',
  category ENUM('foliage','succulent','flower','fern','other') COMMENT '类别',
  location VARCHAR(200) COMMENT '摆放位置',
  description TEXT COMMENT '描述',
  care_manual TEXT COMMENT '养护手册（Markdown格式）',
  image_urls JSON COMMENT '图片URL数组',
  status ENUM('available','adopted','dead','removed') DEFAULT 'available' COMMENT '状态',
  qr_code VARCHAR(200) COMMENT '二维码标识（格式：PLANT-XXXXXXXX）',
  adopter_id BIGINT COMMENT '当前领养人ID',
  adopt_time DATETIME COMMENT '领养时间',
  -- 养护提醒配置
  remind_cycle_days INT DEFAULT NULL COMMENT '养护提醒周期（天），NULL表示不提醒',
  last_remind_time DATETIME DEFAULT NULL COMMENT '上次提醒时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_status (status),
  INDEX idx_qr_code (qr_code),
  INDEX idx_adopter (adopter_id)
) COMMENT='绿植表';
-- ============================================================
-- 5. 领养记录表（扩展状态枚举）
-- ============================================================
CREATE TABLE adoption_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plant_id BIGINT NOT NULL COMMENT '绿植ID',
  adopter_id BIGINT NOT NULL COMMENT '领养人ID',
  status ENUM('pending','active','returned','transferred','rejected','timeout_rejected') DEFAULT 'pending' COMMENT '状态：
                  pending-待审批,
                  active-领养中,
                  returned-用户放弃/归还,
                  transferred-管理员移交,
                  rejected-管理员拒绝,
                  timeout_rejected-超时自动驳回',
  reason VARCHAR(500) COMMENT '申请理由',
  reject_reason VARCHAR(500) COMMENT '拒绝/驳回原因',
  approved_by BIGINT COMMENT '审批人ID（管理员）',
  adopt_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  approve_time DATETIME COMMENT '审批时间',
  return_time DATETIME COMMENT '归还/移交时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_plant (plant_id),
  INDEX idx_adopter (adopter_id),
  INDEX idx_status (status),
  INDEX idx_plant_status (plant_id, status)
) COMMENT='领养记录表';
-- ============================================================
-- 6. 养护记录表
-- ============================================================
CREATE TABLE care_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plant_id BIGINT NOT NULL COMMENT '绿植ID',
  adopter_id BIGINT NOT NULL COMMENT '养护人ID',
  adoption_id BIGINT COMMENT '关联领养记录ID',
  care_type ENUM('water','fertilize','prune','repot','pest','other') COMMENT '养护类型',
  care_date DATE COMMENT '养护日期',
  content TEXT COMMENT '养护内容',
  image_urls JSON COMMENT '图片URL数组',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_plant (plant_id),
  INDEX idx_adopter (adopter_id),
  INDEX idx_date (care_date)
) COMMENT='养护记录表';
-- ============================================================
-- 7. 社区帖子表
-- ============================================================
CREATE TABLE community_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_id BIGINT NOT NULL COMMENT '作者ID',
  plant_id BIGINT COMMENT '关联绿植ID（可选）',
  content TEXT NOT NULL COMMENT '帖子内容',
  image_urls JSON COMMENT '图片URL数组',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  comment_count INT DEFAULT 0 COMMENT '评论数',
  status ENUM('normal','hidden','deleted') DEFAULT 'normal' COMMENT '状态：normal-正常, hidden-管理员隐藏, deleted-用户删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_author (author_id),
  INDEX idx_plant (plant_id),
  INDEX idx_status (status),
  INDEX idx_time (create_time)
) COMMENT='社区帖子表';
-- ============================================================
-- 8. 帖子评论表（新增）
-- ============================================================
CREATE TABLE community_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  author_id BIGINT NOT NULL COMMENT '评论人ID',
  content VARCHAR(1000) NOT NULL COMMENT '评论内容',
  status ENUM('normal','deleted') DEFAULT 'normal' COMMENT '状态',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_post (post_id),
  INDEX idx_author (author_id)
) COMMENT='帖子评论表';
-- ============================================================
-- 9. 帖子点赞记录表（新增，用于防重复点赞）
-- ============================================================
CREATE TABLE post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '点赞用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_user (user_id)
) COMMENT='帖子点赞记录表';
-- ============================================================
-- 10. 消息通知表（扩展类型枚举）
-- ============================================================
CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '接收人ID',
  type ENUM('adoption_approved','adoption_rejected','adoption_timeout',
                   'care_remind','system','post_like','post_comment',
                   'user_abandon','transfer') COMMENT '类型：
                   adoption_approved-领养通过,
                   adoption_rejected-领养被拒绝,
                   adoption_timeout-领养超时驳回,
                   care_remind-养护提醒,
                   system-系统通知,
                   post_like-帖子被点赞,
                   post_comment-帖子被评论,
                   user_abandon-用户放弃领养(通知管理员),
                   transfer-管理员移交(通知双方)',
  title VARCHAR(200) COMMENT '标题',
  content TEXT COMMENT '内容',
  related_id BIGINT COMMENT '关联业务ID',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_read (is_read),
  INDEX idx_time (create_time)
) COMMENT='消息通知表';
```
**表关系说明：**
```text
sys_department ←──── sys_user (N:1)
                      │
                      ├──→ adoption_record (1:N 作为领养人)
                      ├──→ care_log (1:N 作为养护人)
                      ├──→ community_post (1:N 作为作者)
                      ├──→ community_comment (1:N 作为评论人)
                      ├──→ post_like (1:N 作为点赞人)
                      └──→ notification (1:N 作为接收人)
plant ──→ adoption_record (1:N)
plant ──→ care_log (1:N)
plant ──→ community_post (1:N, 可选关联)
community_post ──→ community_comment (1:N)
community_post ──→ post_like (1:N)
adoption_record ←──── care_log (N:1)
```
---
## 三、后端工程结构目录
```text
plant-adoption-server/
├── pom.xml
├── Dockerfile
├── src/
│   ├── main/
│   │   ├── java/com/plant/adoption/
│   │   │   ├── PlantAdoptionApplication.java
│   │   │   │
│   │   │   ├── common/                          # 【公共模块】
│   │   │   │   ├── result/
│   │   │   │   │   ├── Result.java               #   统一响应体 {code, message, data}
│   │   │   │   │   └── PageResult.java           #   分页响应包装
│   │   │   │   ├── exception/
│   │   │   │   │   ├── BusinessException.java    #   自定义业务异常
│   │   │   │   │   ├── ErrorCode.java            #   错误码枚举
│   │   │   │   │   └── GlobalExceptionHandler.java  # 全局异常拦截(@RestControllerAdvice)
│   │   │   │   ├── constant/
│   │   │   │   │   ├── RedisKeyConstant.java     #   Redis Key 常量（锁Key、过期Key等）
│   │   │   │   │   ├── MqConstant.java           #   RabbitMQ 队列/交换机常量
│   │   │   │   │   └── CommonConstant.java       #   通用常量
│   │   │   │   ├── enums/
│   │   │   │   │   ├── PlantStatusEnum.java      #   绿植状态枚举
│   │   │   │   │   ├── AdoptionStatusEnum.java   #   领养状态枚举
│   │   │   │   │   ├── CareTypeEnum.java         #   养护类型枚举
│   │   │   │   │   ├── PostStatusEnum.java       #   帖子状态枚举
│   │   │   │   │   └── NotificationTypeEnum.java #   通知类型枚举
│   │   │   │   └── util/
│   │   │   │       ├── JwtUtil.java              #   JWT 工具类
│   │   │   │       └── QrCodeUtil.java           #   二维码生成/解析工具类
│   │   │   │
│   │   │   ├── config/                            # 【配置类】
│   │   │   │   ├── RedisConfig.java              #   Redis 配置 + Redisson
│   │   │   │   ├── RedissonConfig.java           #   Redisson 分布式锁配置
│   │   │   │   ├── RabbitMqConfig.java           #   RabbitMQ 交换机、队列绑定
│   │   │   │   ├── MinioConfig.java              #   MinIO 客户端配置
│   │   │   │   ├── WebMvcConfig.java             #   CORS 跨域、拦截器注册
│   │   │   │   ├── MybatisPlusConfig.java        #   分页插件等配置
│   │   │   │   └── RedisKeyExpirationConfig.java #   Redis Key过期事件监听配置
│   │   │   │
│   │   │   ├── security/                          # 【安全模块】
│   │   │   │   ├── JwtAuthFilter.java            #   JWT 认证过滤器(OncePerRequestFilter)
│   │   │   │   └── RequireRole.java              #   自定义注解：角色权限校验
│   │   │   │
│   │   │   ├── modules/                           # 【业务模块（按功能划分子包）】
│   │   │   │   │
│   │   │   │   ├── auth/                          # 认证模块
│   │   │   │   │   ├── controller/AuthController.java
│   │   │   │   │   ├── service/AuthService.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── LoginRequest.java
│   │   │   │   │       └── LoginResponse.java
│   │   │   │   │
│   │   │   │   ├── user/                          # 用户模块
│   │   │   │   │   ├── controller/UserController.java
│   │   │   │   │   ├── service/UserService.java
│   │   │   │   │   ├── service/impl/UserServiceImpl.java
│   │   │   │   │   ├── mapper/UserMapper.java     #   extends BaseMapper<SysUser>
│   │   │   │   │   ├── entity/SysUser.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── UserVO.java            #   返回给前端的用户视图
│   │   │   │   │       └── ProfileUpdateRequest.java
│   │   │   │   │
│   │   │   │   ├── plant/                         # 绿植模块
│   │   │   │   │   ├── controller/PlantController.java
│   │   │   │   │   ├── controller/AdminPlantController.java
│   │   │   │   │   ├── service/PlantService.java  #   extends IService<Plant>
│   │   │   │   │   ├── service/impl/PlantServiceImpl.java  # extends ServiceImpl<PlantMapper, Plant>
│   │   │   │   │   ├── mapper/PlantMapper.java    #   extends BaseMapper<Plant>
│   │   │   │   │   ├── entity/Plant.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── PlantVO.java
│   │   │   │   │       ├── PlantDetailVO.java
│   │   │   │   │       ├── PlantCreateRequest.java
│   │   │   │   │       └── PlantQueryRequest.java
│   │   │   │   │
│   │   │   │   ├── adoption/                      # 领养模块
│   │   │   │   │   ├── controller/AdoptionController.java
│   │   │   │   │   ├── controller/AdminAdoptionController.java
│   │   │   │   │   ├── service/AdoptionService.java
│   │   │   │   │   ├── service/impl/AdoptionServiceImpl.java
│   │   │   │   │   ├── mapper/AdoptionRecordMapper.java
│   │   │   │   │   ├── entity/AdoptionRecord.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── AdoptionVO.java
│   │   │   │   │       ├── AdoptionApplyRequest.java
│   │   │   │   │       ├── AdoptionApproveRequest.java
│   │   │   │   │       └── AdoptionTransferRequest.java
│   │   │   │   │
│   │   │   │   ├── care/                          # 养护记录模块
│   │   │   │   │   ├── controller/CareLogController.java
│   │   │   │   │   ├── service/CareLogService.java
│   │   │   │   │   ├── service/impl/CareLogServiceImpl.java
│   │   │   │   │   ├── mapper/CareLogMapper.java
│   │   │   │   │   ├── entity/CareLog.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── CareLogVO.java
│   │   │   │   │       └── CareLogCreateRequest.java
│   │   │   │   │
│   │   │   │   ├── community/                     # 社区模块
│   │   │   │   │   ├── controller/PostController.java
│   │   │   │   │   ├── controller/CommentController.java
│   │   │   │   │   ├── controller/AdminCommunityController.java
│   │   │   │   │   ├── service/PostService.java
│   │   │   │   │   ├── service/CommentService.java
│   │   │   │   │   ├── service/LikeService.java
│   │   │   │   │   ├── service/impl/PostServiceImpl.java
│   │   │   │   │   ├── service/impl/CommentServiceImpl.java
│   │   │   │   │   ├── service/impl/LikeServiceImpl.java
│   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── PostMapper.java
│   │   │   │   │   │   ├── CommentMapper.java
│   │   │   │   │   │   └── PostLikeMapper.java
│   │   │   │   │   ├── entity/
│   │   │   │   │   │   ├── CommunityPost.java
│   │   │   │   │   │   ├── CommunityComment.java
│   │   │   │   │   │   └── PostLike.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── PostVO.java
│   │   │   │   │       ├── PostCreateRequest.java
│   │   │   │   │       ├── CommentVO.java
│   │   │   │   │       └── CommentCreateRequest.java
│   │   │   │   │
│   │   │   │   ├── notification/                  # 消息通知模块
│   │   │   │   │   ├── controller/NotificationController.java
│   │   │   │   │   ├── service/NotificationService.java
│   │   │   │   │   ├── service/impl/NotificationServiceImpl.java
│   │   │   │   │   ├── mapper/NotificationMapper.java
│   │   │   │   │   ├── entity/Notification.java
│   │   │   │   │   └── dto/
│   │   │   │   │       ├── NotificationVO.java
│   │   │   │   │       └── UnreadCountVO.java
│   │   │   │   │
│   │   │   │   ├── upload/                        # 文件上传模块
│   │   │   │   │   └── controller/UploadController.java
│   │   │   │   │
│   │   │   │   └── system/                        # 系统配置模块
│   │   │   │       ├── controller/SysConfigController.java
│   │   │   │       ├── service/SysConfigService.java
│   │   │   │       ├── service/impl/SysConfigServiceImpl.java
│   │   │   │       ├── mapper/SysConfigMapper.java
│   │   │   │       ├── entity/SysConfig.java
│   │   │   │       └── dto/
│   │   │   │           ├── ConfigVO.java
│   │   │   │           └── ConfigUpdateRequest.java
│   │   │   │
│   │   │   └── mq/                                # 【RabbitMQ 消息模块】
│   │   │       ├── producer/
│   │   │       │   └── NotificationProducer.java  #   通知消息生产者
│   │   │       ├── consumer/
│   │   │       │   └── NotificationConsumer.java  #   通知消息消费者（手动ACK）
│   │   │       └── message/
│   │   │           └── NotificationMessage.java   #   消息体定义
│   │   │
│   │   └── resources/
│   │       ├── application.yml                    # 公共配置
│   │       ├── application-dev.yml                # 开发环境配置（见下方）
│   │       ├── application-prod.yml               # 生产环境配置
│   │       └── db/
│   │           └── init.sql                       # 数据库初始化脚本（含种子数据）
│   │
│   └── test/                                      # 测试目录（结构镜像 main）
│       └── java/com/plant/adoption/
│
└── docs/                                          # 项目文档
    ├── sql/                                       # SQL 脚本归档
    └── api/                                       # API 文档归档
```
**分层约定：**
```text
Controller 层：负责参数接收、调用 Service、返回 Result<T>
  ├── 不可包含业务逻辑
  └── 通过 @RequireRole 注解或 @PreAuthorize 控制接口权限
Service 层：负责业务逻辑编排
  ├── 接口：XxxService extends IService<XxxEntity>
  └── 实现类：XxxServiceImpl extends ServiceImpl<XxxMapper, XxxEntity> implements XxxService
Mapper 层：负责数据访问，不可包含业务逻辑
  └── XxxMapper extends BaseMapper<XxxEntity>（不写 XML）
Entity 层：严格对应数据库表字段，使用 @TableName、@TableField 等注解
DTO 层：分为 Request（入参）和 VO（出参），禁止直接将 Entity 返回给前端
  └── Entity ↔ DTO 转换统一使用 MapStruct 自动生成的 Mapper
```
---
## 四、核心底层组件实现细节
### 4.1 Redisson 分布式锁规范
```java
// ======== 使用规范 ========
// 场景：领养申请时，防止同一绿植被并发申请
// Key 格式：lock:plant:adopt:{plantId}
// 等待时间：0 秒（拿不到锁立即失败）
// 持有时间：10 秒（防死锁自动释放）
// 示例代码：
RLock lock = redissonClient.getLock("lock:plant:adopt:" + plantId);
boolean acquired = lock.tryLock(0, 10, TimeUnit.SECONDS);
if (!acquired) {
    throw new BusinessException(99999, "操作太频繁，请稍后再试");
}
try {
    // 业务逻辑：校验 → 创建申请 → 设置 Redis 过期 Key
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```
### 4.2 领养超时自动驳回机制（Redis Key 过期事件监听）
```java
// ======== 实现方案 ========
// 1. 提交领养申请时，写入 Redis Key + TTL
//    Key 格式：adoption:timeout:{adoptionId}
//    TTL = 从 sys_config 读取 adoption_timeout_hours（默认24小时），转成秒
//
// 2. RedisKeyExpirationConfig 配置：
//    - 开启 Redis 的 notify-keyspace-events Ex（配置在 application-dev.yml 的 spring.data.redis 或 RedisConfig 中）
//    - 使用 RedisMessageListenerContainer 监听 __keyevent@{database}__:expired 频道
//
// 3. KeyExpirationListener 处理逻辑：
//    a. 接收到 Key 过期事件
//    b. 解析出 adoptionId
//    c. 查询 adoption_record，校验 status 是否仍为 pending
//    d. 若仍为 pending → 更新为 timeout_rejected
//    e. 若 plant.adopter_id 仍指向该记录 → 清空 adopter_id，plant.status 改回 available
//    f. 通过 RabbitMQ 发送通知消息（类型：adoption_timeout）
// ======== Redis 配置要求 ========
// application-dev.yml 中需要确保 Redis 开启了 Key 过期通知：
// spring.data.redis.notify-keyspace-events: Ex
//（注意：Redis 服务端也需要配置 notify-keyspace-events Ex）
```
### 4.3 MinIO 文件上传规范
```java
// ======== 规范 ========
// 桶名称：plant-images（启动时自动检测并创建）
// 允许后缀：jpg, png, webp
// 单文件大小上限：20MB
// 文件命名策略：{yyyy/MM/dd}/{UUID}.{ext}（按日期分目录，避免单目录文件过多）
// 返回值：完整的 HTTP URL（如 http://192.168.1.248:9000/plant-images/2026/03/23/xxx.jpg）
// 预览策略：桶设置为公开读，前端可直接用 URL 显示图片
// ======== 上传接口 ========
// POST /api/upload/image
// Content-Type: multipart/form-data
// 参数：file (必填, 单文件)
// 响应：Result<String> → data 中为完整的图片 HTTP URL
// ======== 批量上传接口 ========
// POST /api/upload/images
// Content-Type: multipart/form-data
// 参数：files (必填, 多文件，最多 9 张)
// 响应：Result<List<String>> → data 中为完整的图片 HTTP URL 列表
```
### 4.4 RabbitMQ 消息队列规范与容错机制
```java
// ======== 交换机与队列定义 ========
// 交换机名称：plant.notification.exchange（Topic 模式）
// 队列名称：plant.notification.queue
// 路由键规则：
//   notification.adoption.*      → 领养相关通知（approved / rejected / timeout）
//   notification.care.remind      → 养护提醒
//   notification.community.*      → 社区相关通知（like / comment）
//   notification.system.*         → 系统通知（abandon / transfer）
// ======== 消费者规范 ========
// acknowledge-mode: manual（手动ACK）
// 成功处理 → channel.basicAck()
// 处理失败 → channel.basicNack()，重试进入死信队列
// 最大重试次数：3 次（通过配置 retry.enabled=true）
// ======== 消息体结构（NotificationMessage） ========
public class NotificationMessage {
    private Long userId;          // 接收人ID
    private String type;          // 通知类型（对应 NotificationTypeEnum）
    private String title;         // 通知标题
    private String content;       // 通知内容
    private Long relatedId;       // 关联业务ID
}
// ======== 消息队列容错与死信规范 (AI 强制遵循) ========
// 1. 死信队列设计：
//    - 正常队列 plant.notification.queue 必须配置死信交换机 plant.notification.dlx.exchange
//    - 死信队列 plant.notification.dlx.queue 绑定死信交换机
// 2. 消费者 Ack/Nack 规范（强制代码结构）：
@RabbitListener(queues = "plant.notification.queue")
public void handleNotification(NotificationMessage msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
    try {
        // 业务逻辑：写入 notification 表
        channel.basicAck(tag, false); // 成功确认
    } catch (Exception e) {
        try {
            // 异常捕获：拒绝消息，且不重新放回队列（直接进死信队列，防止死循环）
            channel.basicNack(tag, false, false); 
            log.error("通知消费失败，已转入死信队列: {}", msg, e);
        } catch (IOException ioException) {
            log.error("Nack操作失败", ioException);
        }
    }
}
```
### 4.5 JWT 认证规范
```java
// ======== 规范 ========
// Header 格式：Authorization: Bearer {token}
// Payload 内容：userId, role, username（不存敏感信息）
// 签名算法：HS256
// 密钥：从 application-dev.yml 读取（plant.jwt.secret）
// 有效期：7 天（可配置 plant.jwt.expiration-days）
// 不做 refresh token，过期需重新登录
// ======== 拦截器逻辑 ========
// JwtAuthFilter (extends OncePerRequestFilter):
//   1. 从 Header 中提取 token
//   2. 验证签名和有效期
//   3. 将 userId 和 role 存入 ThreadLocal 或 SecurityContext
//   4. 放行路径：/api/auth/login、/api/plants（GET列表）、/api/plants/{id}（GET详情）、/api/plants/qr/**
//   5. 其余路径需验证 token，失败返回错误码 10002
```
### 4.6 环境配置文件
**application.yml（公共配置）：**
```yaml
server:
  port: 18080
spring:
  application:
    name: plant-adoption-server
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 100MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    default-property-inclusion: non_null
# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: null   # 不使用逻辑删除
  type-enums-package: com.plant.adoption.common.enums
# 自定义业务配置（对应 sys_config 表，作为默认兜底值）
plant:
  jwt:
    secret: plant-adoption-jwt-secret-key-2026
    expiration-days: 7
  adoption:
    max-limit: 5
    timeout-hours: 24
# MinIO 配置
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: plant-images
```
**application-dev.yml（开发环境配置）：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.248:3306/plant_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: a!5682724@
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      database: 17
      host: 192.168.1.241
      port: 31552
      password: a!5682724@
  rabbitmq:
    host: 192.168.1.252
    username: admin
    password: a!5682724@
    port: 5672
    publisher-confirms: true
    publisher-returns: true
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual
        concurrency: 1
        max-concurrency: 1
        retry:
          enabled: true
```
---
## 五、接口设计（完整 RESTful API）
> 所有接口响应体严格遵循：`{ "code": 0, "message": "success", "data": { ... } }`
> 分页响应 `data` 结构：`{ "records": [...], "total": long, "size": int, "current": int, "pages": int }`
### 5.1 认证模块
```yaml
# 用户登录（短信验证码）
POST /api/auth/login
Request:
  phone: string          # 手机号
  code: string           # 短信验证码
Response:
  code: 0
  data:
    token: string        # JWT Token
    user:
      id: long
      username: string
      realName: string
      avatarUrl: string | null
      role: "USER" | "ADMIN"
# 退出登录
POST /api/auth/logout
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  message: "退出成功"
```
### 5.2 用户模块
```yaml
# 获取当前登录用户信息
GET /api/user/me
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  data:
    id: long
    username: string
    realName: string
    phone: string
    email: string | null
    deptId: long | null
    deptName: string | null
    avatarUrl: string | null
    role: "USER" | "ADMIN"
    adoptionCount: int   # 当前活跃领养数量（status=active）
    maxAdoptionLimit: int # 系统配置的最大领养数
# 更新个人信息
PUT /api/user/profile
Headers:
  Authorization: Bearer {token}
Request:
  realName: string
  avatarUrl: string | null
Response:
  code: 0
# 获取用户列表（仅管理员）
GET /api/admin/users
Headers:
  Authorization: Bearer {token}
Parameters:
  keyword: string        # 姓名/手机号模糊搜索
  role: string           # ADMIN | USER | 空=全部
  status: int            # 1=正常 0=禁用
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        username: string
        realName: string
        phone: string
        deptName: string | null
        role: string
        status: int
        createTime: string
    total: long
# 管理员禁用/启用用户
PUT /api/admin/users/{id}/status
Headers:
  Authorization: Bearer {token}
Request:
  status: int            # 1=启用 0=禁用
Response:
  code: 0
```
### 5.3 绿植模块
```yaml
# 获取绿植列表（所有用户可访问）
GET /api/plants
Parameters:
  status: string         # available | adopted | all（默认 all）
  category: string       # foliage | succulent | flower | fern | other | 空=全部
  keyword: string        # 名称/品种模糊搜索
  page: int              # 当前页（默认1）
  size: int              # 每页条数（默认10）
Response:
  code: 0
  data:
    records:
      - id: long
        name: string
        species: string | null
        category: string
        location: string
        coverImage: string | null       # 取 imageUrls[0] 作为封面
        status: string
        adopter:
          id: long
          realName: string
          avatarUrl: string | null
        adoptTime: string | null
    total: long
# 获取绿植详情
GET /api/plants/{id}
Response:
  code: 0
  data:
    id: long
    name: string
    species: string | null
    category: string
    location: string
    description: string | null
    careManual: string | null           # Markdown 格式
    imageUrls: string[]                 # 完整图片列表
    status: string
    qrCode: string | null
    remindCycleDays: int | null
    adopter:
      id: long
      realName: string
      avatarUrl: string | null
    adoptTime: string | null
    createTime: string
# 扫码查询绿植
GET /api/plants/qr/{qrCode}
Response:
  code: 0
  data:
    # 同绿植详情结构
# ---- 以下为管理员接口 ----
# 添加绿植
POST /api/admin/plants
Headers:
  Authorization: Bearer {token}
Request:
  name: string
  species: string | null
  category: string
  location: string
  description: string | null
  careManual: string | null
  imageUrls: string[]                   # 已上传至 MinIO 的完整 URL 列表
  remindCycleDays: int | null           # 养护提醒周期（天），null=不提醒
Response:
  code: 0
  data:
    id: long
    qrCode: string                      # 自动生成的二维码标识
# 更新绿植
PUT /api/admin/plants/{id}
Headers:
  Authorization: Bearer {token}
Request: (同添加，所有字段均可选)
Response:
  code: 0
# 删除绿植（软删除）
DELETE /api/admin/plants/{id}
Headers:
  Authorization: Bearer {token}
Response:
  code: 0                               # 若存在 active 领养记录，返回 code: 20002
# 批量生成二维码（获取指定绿植的二维码图片URL列表）
GET /api/admin/plants/qr-codes
Headers:
  Authorization: Bearer {token}
Parameters:
  ids: long[]                           # 绿植ID列表
Response:
  code: 0
  data:
    - plantId: long
      plantName: string
      location: string
      qrCode: string                    # 二维码标识
      qrImageUrl: string                # 二维码图片的完整 HTTP URL
```
### 5.4 领养模块
```yaml
# 申请领养（需登录 + Redisson 分布式锁）
POST /api/adoptions
Headers:
  Authorization: Bearer {token}
Request:
  plantId: long
  reason: string | null                 # 申请理由（可选）
Response:
  code: 0                               # 20002=状态不可领养, 20003=超出限制, 20004=重复申请
  data:
    id: long
    status: "pending"
# 获取我的领养列表
GET /api/adoptions/my
Headers:
  Authorization: Bearer {token}
Parameters:
  status: string         # pending | active | returned | transferred | rejected | timeout_rejected | 空=全部
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        plant:
          id: long
          name: string
          species: string | null
          category: string
          location: string
          coverImage: string | null
        status: string
        reason: string | null
        rejectReason: string | null
        adoptTime: string
        approveTime: string | null
        returnTime: string | null
    total: long
# 获取领养详情
GET /api/adoptions/{id}
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  data:
    id: long
    plant: { ... }
    adopter:
      id: long
      realName: string
      avatarUrl: string | null
    status: string
    reason: string | null
    rejectReason: string | null
    approvedBy:
      id: long
      realName: string           # 审批人（拒绝时可见）
    adoptTime: string
    approveTime: string | null
    returnTime: string | null
# 用户放弃领养（无需审批，直接生效）
POST /api/adoptions/{id}/abandon
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  message: "已放弃领养，绿植已回归可领养状态"
# ---- 以下为管理员接口 ----
# 获取全部领养申请（管理员审批列表）
GET /api/admin/adoptions
Headers:
  Authorization: Bearer {token}
Parameters:
  status: string         # pending | active | returned | transferred | rejected | timeout_rejected | 空=全部
  keyword: string        # 搜索领养人姓名/绿植名称
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        plant:
          id: long
          name: string
          species: string | null
          location: string
        adopter:
          id: long
          realName: string
          deptName: string | null
        status: string
        reason: string | null
        adoptTime: string
        approveTime: string | null
    total: long
# 审批通过
POST /api/admin/adoptions/{id}/approve
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  # 后端逻辑：
  # 1. 更新 adoption_record.status = active
  # 2. 更新 plant.adopter_id = adopterId, plant.status = adopted, plant.adopt_time = now
  # 3. 删除 Redis Key adoption:timeout:{id}（取消超时监听）
  # 4. 通过 RabbitMQ 发送 adoption_approved 通知
# 审批拒绝
POST /api/admin/adoptions/{id}/reject
Headers:
  Authorization: Bearer {token}
Request:
  reason: string                        # 拒绝原因（必填）
Response:
  code: 0
  # 后端逻辑：
  # 1. 更新 adoption_record.status = rejected, reject_reason = reason
  # 2. 删除 Redis Key adoption:timeout:{id}
  # 3. 通过 RabbitMQ 发送 adoption_rejected 通知
# 管理员强制移交（更换领养人）
POST /api/admin/adoptions/{id}/transfer
Headers:
  Authorization: Bearer {token}
Request:
  targetUserId: long                    # 新领养人ID
  reason: string                        # 移交原因
Response:
  code: 0
  # 后端逻辑：
  # 1. 原记录 status → transferred, return_time = now, return_reason = reason
  # 2. 创建新的 adoption_record (status=active, adopter_id=targetUserId)
  # 3. 更新 plant.adopter_id = targetUserId
  # 4. 通过 RabbitMQ 发送 transfer 通知（通知新旧双方）
```
### 5.5 养护记录模块
```yaml
# 添加养护记录
POST /api/care-logs
Headers:
  Authorization: Bearer {token}
Request:
  plantId: long                         # 必须是自己领养的绿植
  careType: "water" | "fertilize" | "prune" | "repot" | "pest" | "other"
  careDate: string                      # yyyy-MM-dd
  content: string                       # 养护内容描述
  imageUrls: string[]                   # 可选，图片URL列表
Response:
  code: 0
  data:
    id: long
# 获取指定绿植的养护记录（按时间倒序）
GET /api/care-logs/plant/{plantId}
Parameters:
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        careType: string
        careTypeName: string            # 中文：浇水/施肥/修剪...
        careDate: string
        content: string
        imageUrls: string[]
        adopter:
          id: long
          realName: string
          avatarUrl: string | null
        createTime: string
    total: long
# 获取我的所有养护记录
GET /api/care-logs/my
Headers:
  Authorization: Bearer {token}
Parameters:
  page: int
  size: int
Response:
  code: 0
  data:
    records: [ ... ]                    # 同上结构
    total: long
```
### 5.6 社区模块
```yaml
# 获取帖子列表（支持分页、按时间倒序）
GET /api/posts
Parameters:
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        author:
          id: long
          realName: string
          avatarUrl: string | null
        plant:                            # 关联绿植（可选，无则为null）
          id: long
          name: string
          coverImage: string | null
        content: string
        imageUrls: string[]
        likeCount: int
        commentCount: int
        isLiked: boolean                  # 当前用户是否已点赞（登录时返回）
        createTime: string
    total: long
# 获取帖子详情
GET /api/posts/{id}
Response:
  code: 0
  data:
    id: long
    author:
      id: long
      realName: string
      avatarUrl: string | null
    plant:
      id: long
      name: string
      coverImage: string | null
    content: string
    imageUrls: string[]
    likeCount: int
    commentCount: int
    isLiked: boolean
    comments:                            # 帖子详情直接返回评论列表
      - id: long
        author:
          id: long
          realName: string
          avatarUrl: string | null
        content: string
        createTime: string
    createTime: string
# 发布帖子（无需审核，直接发布）
POST /api/posts
Headers:
  Authorization: Bearer {token}
Request:
  content: string                        # 必填，最大500字
  plantId: long | null                  # 可选，关联绿植
  imageUrls: string[]                    # 可选，最多9张
Response:
  code: 0
  data:
    id: long
# 删除帖子（仅作者本人，逻辑删除 status→deleted）
DELETE /api/posts/{id}
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
# 点赞 / 取消点赞（幂等操作）
POST /api/posts/{id}/like
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  data:
    liked: boolean                       # true=已点赞, false=已取消点赞
# 发表评论
POST /api/posts/{postId}/comments
Headers:
  Authorization: Bearer {token}
Request:
  content: string                        # 必填，最大500字
Response:
  code: 0
  data:
    id: long
# 删除评论（仅评论作者本人）
DELETE /api/posts/{postId}/comments/{commentId}
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
# ---- 以下为管理员接口 ----
# 管理员隐藏帖子（status→hidden）
POST /api/admin/posts/{id}/hide
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
# 管理员恢复帖子（status→normal）
POST /api/admin/posts/{id}/show
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
# 管理员获取帖子列表（含已隐藏和已删除）
GET /api/admin/posts
Headers:
  Authorization: Bearer {token}
Parameters:
  status: string         # normal | hidden | deleted | 空=全部
  keyword: string
  page: int
  size: int
Response:
  code: 0
  data:
    records: [ ... ]                    # 同帖子列表结构
    total: long
```
### 5.7 消息通知模块
```yaml
# 获取我的通知列表（分页，按时间倒序）
GET /api/notifications
Headers:
  Authorization: Bearer {token}
Parameters:
  page: int
  size: int
Response:
  code: 0
  data:
    records:
      - id: long
        type: string                     # 通知类型枚举值
        typeName: string                 # 中文类型名
        title: string
        content: string
        relatedId: long | null
        isRead: boolean
        createTime: string
    total: long
# 获取未读消息数量
GET /api/notifications/unread-count
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  data:
    count: int
# 标记单条通知为已读
PUT /api/notifications/{id}/read
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
# 标记所有通知为已读
PUT /api/notifications/read-all
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
```
### 5.8 文件上传模块
```yaml
# 单图片上传
POST /api/upload/image
Headers:
  Authorization: Bearer {token}
Content-Type: multipart/form-data
Body: file (必填)
Response:
  code: 0
  data: "http://192.168.1.248:9000/plant-images/2026/03/23/xxxxx.jpg"
  # 错误码：10001=文件为空或后缀不支持, 99999=上传失败
# 批量图片上传（最多9张）
POST /api/upload/images
Headers:
  Authorization: Bearer {token}
Content-Type: multipart/form-data
Body: files (必填, 多文件)
Response:
  code: 0
  data:
    - "http://192.168.1.248:9000/plant-images/2026/03/23/xxxxx1.jpg"
    - "http://192.168.1.248:9000/plant-images/2026/03/23/xxxxx2.jpg"
```
### 5.9 系统配置模块（仅管理员）
```yaml
# 获取所有系统配置
GET /api/admin/configs
Headers:
  Authorization: Bearer {token}
Response:
  code: 0
  data:
    - id: long
      configKey: string
      configValue: string
      configDesc: string | null
# 更新系统配置（按 key 更新）
PUT /api/admin/configs
Headers:
  Authorization: Bearer {token}
Request:
  configs:
    - configKey: "adoption_max_limit"
      configValue: "5"
    - configKey: "adoption_timeout_hours"
      configValue: "24"
Response:
  code: 0
  # 后端逻辑：批量更新 + 刷新 Redis 缓存
```
### 5.10 接口权限矩阵
| 接口路径前缀 | 未登录 | 普通用户 (USER) | 管理员 (ADMIN) |
| ---------------------------------- | ------ | --------------- | -------------- |
| `POST /api/auth/login` | ✅ | ✅ | ✅ |
| `GET /api/plants` | ✅ | ✅ | ✅ |
| `GET /api/plants/{id}` | ✅ | ✅ | ✅ |
| `GET /api/plants/qr/**` | ✅ | ✅ | ✅ |
| `GET /api/posts` | ✅ | ✅ | ✅ |
| `GET /api/posts/{id}` | ✅ | ✅ | ✅ |
| `POST /api/adoptions` | ❌ | ✅ | ✅ |
| `GET /api/adoptions/my` | ❌ | ✅ | ✅ |
| `POST /api/adoptions/{id}/abandon` | ❌ | ✅（仅本人） | ✅ |
| `POST /api/care-logs` | ❌ | ✅ | ✅ |
| `POST /api/posts` | ❌ | ✅ | ✅ |
| `DELETE /api/posts/{id}` | ❌ | ✅（仅本人） | ✅ |
| `POST /api/posts/{id}/like` | ❌ | ✅ | ✅ |
| `GET /api/notifications/**` | ❌ | ✅ | ✅ |
| `POST /api/upload/**` | ❌ | ✅ | ✅ |
| `POST /api/admin/**` | ❌ | ❌ | ✅ |
| `GET /api/admin/**` | ❌ | ❌ | ✅ |
| `PUT /api/admin/**` | ❌ | ❌ | ✅ |
| `DELETE /api/admin/**` | ❌ | ❌ | ✅ |
---
## 六、前端工程结构与规范约束
### 6.1 用户端（UniApp + Vue3）规范
- **UI 框架**：强制使用 **uView Plus 2.x**。禁止 AI 自己手写原生 `<button>`、`<input>` 或原生 CSS 弹窗，必须使用 `<u-button>`、`<u-input>`、`<u-modal>` 等组件。
- **网络请求封装**：基于 `uni.request` 封装 `src/utils/request.js`。
  - 请求头自动带 `Authorization: Bearer {token}`。
  - **统一拦截**：判断 `res.data.code === 10002` 时，清除本地 Storage 并 `uni.reLaunch('/pages/login/index')`。
  - 返回值只透传 `response.data.data` 给业务页面。
- **状态管理**：使用 Pinia (`src/stores/user.js`, `plant.js`)。
### 6.2 管理端（Vue3 + Element Plus）规范
- **网络请求**：使用 Axios 封装 `src/utils/request.js`（同理拦截 10002 跳转 `/login`）。
- **布局结构**：不使用第三方 Layout 模板，使用 `<el-container>` + `<el-aside>` + `<el-header>` 自建经典后台布局。
- **组件使用**：
  - 表格必须用 `<el-table>` 配合 `<el-pagination>`。
  - 表单必须用 `<el-form>` + `<el-form-item>` 并绑定 `rules` 进行前端校验。
  - 上传图片必须使用已有的公共组件 `<ImageUpload.vue>`。
### 6.3 核心页面 UI 原型文字描述 (防 AI 乱写 UI)
- **用户端-绿植列表**：顶部绿色背景白字搜索框(`placeholder="搜索绿植名称/品种"`)。下方 `u-tabs` 横向滑动(全部/观叶/多肉...)。列表为 `u-waterfall` 瀑布流两列。卡片包含：圆角图片、标题(最多一行省略)、灰色品种位置、右上角状态 Tag(`available`绿, `adopted`灰)。
- **管理端-领养审批**：页面顶部为 `<el-tabs model-value="pending">` (待审批/已通过/已拒绝/超时)。表格列包含：绿植名、申请理由、申请人及部门、申请时间。操作列为 `<el-button type="success" size="small">通过</el-button>` 和 `<el-button type="danger" size="small">拒绝</el-button>`。拒绝时弹出 `<el-dialog>` 内含 `<el-input type="textarea">` 填写原因。
---
## 七、功能模块详细设计
### 7.1 用户端（UniApp H5）
#### 页面结构
```text
pages/
├── index/               # 首页
│   └── index.vue        # 绿植列表、搜索、筛选
├── plant/
│   ├── detail.vue       # 绿植详情
│   └── adopt.vue        # 领养申请（提交后显示等待审批状态）
├── my/
│   ├── index.vue        # 我的页面
│   ├── my-plants.vue    # 我的领养（含放弃领养操作）
│   ├── care-log.vue     # 养护记录
│   └── settings.vue     # 设置
├── community/
│   ├── index.vue        # 社区首页
│   ├── post.vue         # 发布帖子
│   └── detail.vue       # 帖子详情（含点赞、评论）
├── scan/
│   └── index.vue        # 扫码页面
└── notice/
    └── index.vue        # 消息通知（含未读数角标）
```
#### 核心功能流程
**1. 领养与放弃流程**
```text
用户浏览绿植列表 → 查看详情 → 点击"申请领养"
  ↓ (后端校验：Redisson分布式锁 → 状态是否available → 是否超出动态上限 → 是否重复申请)
  ↓ (写入 Redis Key adoption:timeout:{id}，TTL = 配置的超时小时数)
填写申请理由（可选）→ 提交申请 → 状态为 pending
  ↓ (Redis Key 过期 → KeyExpirationListener 触发 → 检查是否仍为pending → 自动驳回)
  ↓ 或 (管理员审批通过/拒绝)
收到通知 → 领养成功 或 申请被拒
  ↓ (领养成功后)
我的领养 → 选择绿植 → 点击"放弃领养"
  ↓ (无需审批，直接生效)
  ↓ (plant.status → available, adoption_record.status → returned)
  ↓ (RabbitMQ 发送 user_abandon 通知给管理员)
绿植回归可领养状态
```
**2. 扫码流程**
```text
打开扫码 → 扫描绿植二维码（内容为 qrCode 标识）
  ↓ GET /api/plants/qr/{qrCode}
判断状态：
  ├─ available → 显示信息 + "申请领养"按钮
  └─ adopted → 显示信息 + 当前领养人 + 养护记录时间线
```
**3. 养护记录流程**
```text
我的领养 → 选择绿植 → 点击"添加养护"
  ↓ 选择养护类型（浇水/施肥/修剪/换盆/除虫/其他）
  ↓ 选择日期 → 填写内容 → 上传图片（可选）→ 提交
```
**4. 社区分享流程**
```text
发布帖子 → 选择关联绿植（可选，弹出底部选择器）
  ↓ 填写内容（textarea, 最多500字）→ 上传图片（最多9张）→ 发布
  ↓ 直接成功（无审核）
其他用户可点赞（切换）、评论；作者可删帖
```
### 7.2 管理端（Vue3 + Element Plus）
#### 页面结构
```text
src/
├── views/
│   ├── dashboard/          # 仪表盘
│   │   └── index.vue       # 统计卡片（绿植总数/可领养/已领养/待审批）
│   ├── plant/              # 绿植管理
│   │   ├── list.vue        # 列表（含搜索、筛选、新增按钮）
│   │   ├── add.vue         # 添加/编辑表单（含养护提醒周期配置）
│   │   └── detail.vue      # 详情（含养护记录时间线、领养历史）
│   ├── adoption/           # 领养管理
│   │   ├── applications.vue # 审批列表（el-tabs: 待审批/已通过/已拒绝/超时驳回）
│   │   └── records.vue     # 全部记录查询（含移交历史）
│   ├── user/               # 用户管理
│   │   └── list.vue        # 用户列表（搜索、禁用/启用操作）
│   ├── community/          # 社区管理
│   │   └── posts.vue       # 帖子列表（含隐藏/恢复操作按钮）
│   └── system/             # 系统设置
│       └── settings.vue    # 配置管理（领养上限、超时时间等动态配置）
├── components/             # 公共组件
│   ├── ImageUpload.vue     # 图片上传组件（调用 /api/upload）
│   └── QrCodePrint.vue     # 二维码打印组件（A4排版）
└── router/index.ts
```
---
## 八、部署架构
```text
┌─────────────────────────────────────────────────────────┐
│                     Docker Compose                       │
│                                                         │
│  ┌──────────┐  ┌──────────────┐  ┌───────────────────┐ │
│  │  Nginx   │  │  Spring Boot │  │      MinIO        │ │
│  │  :80/443 │──│    :18080    │──│     :9000         │ │
│  │          │  │              │  │   (plant-images)  │ │
│  └──────────┘  └──────┬───────┘  └───────────────────┘ │
│                       │                                 │
│           ┌───────────┼───────────┐                     │
│           ▼           ▼           ▼                     │
│     ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│     │  MySQL   │ │  Redis   │ │ RabbitMQ │             │
│     │  :3306   │ │  :6379   │ │  :5672   │             │
│     └──────────┘ └──────────┘ └──────────┘             │
└─────────────────────────────────────────────────────────┘
```
---
> **版本变更记录**
> 
> | 版本 | 日期 | 变更内容 |
> | ---- | ---------- | ------------------------------------------------------------ |
> | v1.0 | 2026-03-23 | 初始版本 |
> | v1.1 | 2026-03-23 | 新增：业务规则细化、统一错误码、精确依赖版本；补充：sys_config表、community_comment表、post_like表、sys_department表；扩展：adoption_record状态枚举、notification类型枚举；完善：全部 API 契约补全；新增：后端工程结构、核心组件实现细节（Redisson锁、Redis过期监听、RabbitMQ消息、MinIO上传）、application-dev.yml |
> | v1.2 | 2026-03-23 | 新增：前端工程结构与规范约束（强制UI库、请求封装、拦截器逻辑）；新增：MyBatis-Plus枚举持久化与序列化规范（@EnumValue与@JsonValue强约束）；新增：RabbitMQ消息队列容错与死信机制规范（强制Ack/Nack代码结构） |
