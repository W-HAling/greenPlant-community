# 办公室绿植领养系统 - 产品规划文档（PRD）

**版本**: v1.2

**日期**: 2026-04-10

**状态**: 需求细化完成，新增漂流瓶模块，养护计划细化，待开发

---

# 一、产品概述

## 1.1 产品定位

企业内部绿植共享领养 + 社区化养护 + 漂流瓶互动平台，通过数字化手段解决办公室绿植管理难题，增强员工参与感、归属感，同时新增轻量化互动场景，丰富员工办公闲暇体验；通过精细化养护计划，降低绿植养护难度，减少绿植损耗。

## 1.2 核心价值主张

|**利益相关方**|**痛点**|**解决方案**|**价值**|
|---|---|---|---|
|**企业行政**|绿植无人养护，经常枯萎死亡；养护标准不统一，损耗率高|员工领养制 + 数字化追踪 + 精细化养护计划|降低绿植损耗成本，提升办公环境，减少行政维护工作量|
|**企业员工**|想养绿植但条件受限；不懂养护知识，不知道何时浇水/施肥；办公闲暇缺乏轻量化互动|办公室领养 + 精细化养护指导 + 养护提醒 + 漂流瓶互动|满足养植爱好，缓解工作压力，掌握养护技巧，增加办公趣味性|
|**领养员工**|担心养护责任不明确；养护心得缺乏倾诉渠道；无清晰养护节奏，易遗漏操作|二维码绑定 + 记录可追溯 + 精细化养护计划 + 漂流瓶分享|明确责任，获得养护成就感，实现轻量化情感互动，降低养护难度|
## 1.3 目标用户画像

**主要用户 - 企业员工（领养人）**

- 年龄：22-35岁

- 特征：办公室白领，对绿植有兴趣但家里养不了，喜欢轻量化、无压力的互动方式；多数缺乏专业养护知识，需要明确的养护指引

- 需求：方便领养、精细化养护指导、明确养护节奏、养护提醒、分享交流、轻松互动

**次要用户 - 行政/后勤管理员**

- 职责：绿植资产管理、领养审批、养护监督、漂流瓶内容管理（可选）、养护计划模板维护

- 需求：高效管理、数据可视化、责任追溯、低成本维护互动秩序、统一养护标准

---

# 二、技术架构

## 2.1 技术栈选型

|**层级**|**技术方案**|**说明**|
|---|---|---|
|用户端|UniApp + Vue3|跨平台 H5，支持微信/钉钉内嵌，适配漂流瓶轻量化交互、养护计划展示与操作|
|管理端|Vue3 + Element Plus|响应式 Web 管理后台，新增漂流瓶管理、养护计划模板管理功能|
|后端服务|Spring Boot 3.x + Java 17|企业级稳定架构，新增漂流瓶相关接口、精细化养护计划相关接口与业务逻辑|
|数据库|MySQL 8.0 + Redis 7.x|主从架构，读写分离；Redis 新增漂流瓶缓存、随机匹配逻辑，同时缓存养护提醒任务|
|对象存储|MinIO|私有化部署，图片/文件存储（含漂流瓶图片、养护记录图片）|
|二维码|ZXing|生成与解析二维码|
|部署|Docker + Nginx|容器化部署，负载均衡|
## 2.2 系统架构图

```Plain Text
┌─────────────────────────────────────────────────────────────┐
│                         客户端层                              │
├─────────────────┬─────────────────┬─────────────────────────┤
│   UniApp H5     │   管理端 Web     │      微信小程序          │
│   (员工使用)     │   (管理员使用)   │      (可选扩展)          │
│   含漂流瓶模块   │   含漂流瓶管理   │      含漂流瓶模块        │
│   含养护计划模块 │   含养护模板管理 │      含养护计划模块      │
└────────┬────────┴────────┬────────┴──────────┬──────────────┘
         │                 │                   │
         └─────────────────┼───────────────────┘
                           │ HTTPS/JSON
┌──────────────────────────┼─────────────────────────────────┐
│                     网关层 │ Nginx + Gateway                  │
│                     负载均衡 │ 限流、鉴权、路由                │
└──────────────────────────┼─────────────────────────────────┘
                           │
┌──────────────────────────┼─────────────────────────────────┐
│                     服务层 │ Spring Boot                     │
│  ┌─────────────┐  ┌─────┴──────┐  ┌─────────────┐          │
│  │ 用户服务     │  │ 绿植服务    │  │ 领养服务     │          │
│  │ User Service│  │Plant Service│  │Adoption Svc │          │
│  └─────────────┘  └────────────┘  └─────────────┘          │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────┐          │
│  │ 社区服务     │  │ 消息服务    │  │ 二维码服务   │          │
│  │Social Svc   │  │Notify Svc  │  │ QR Service  │          │
│  └─────────────┘  └────────────┘  └─────────────┘          │
│  ┌─────────────┐  ┌────────────┐                            │
│  │ 漂流瓶服务   │  │养护计划服务 │                            │
│  │Bottle Svc   │  │CarePlan Svc│                            │
│  └─────────────┘  └────────────┘                            │
└─────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────┼─────────────────────────────────┐
│                     数据层                                  │
│  ┌─────────────┐  ┌────────────┐  ┌─────────────┐          │
│  │   MySQL     │  │   Redis    │  │    MinIO    │          │
│  │  主从架构    │  │  缓存/会话  │  │  对象存储    │          │
│  │ 含漂流瓶表   │  │ 漂流瓶缓存   │  │ 漂流瓶图片   │          │
│  │ 含养护计划表 │  │ 养护提醒缓存 │  │ 养护记录图片 │          │
│  └─────────────┘  └────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## 2.3 数据库设计（核心表，新增/修改养护计划相关表）

```sql
-- 用户表
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
    INDEX idx_dept (dept_id),
    INDEX idx_phone (phone)
) COMMENT='用户表';
-- 绿植表（修改：新增养护计划模板ID关联）
CREATE TABLE plant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '绿植名称',
    species VARCHAR(100) COMMENT '品种',
    category ENUM('foliage','succulent','flower','fern','other') COMMENT '类别',
    location VARCHAR(200) COMMENT '摆放位置',
    description TEXT COMMENT '描述',
    care_manual TEXT COMMENT '养护手册（Markdown，精细化）',
    care_plan_template_id BIGINT COMMENT '关联养护计划模板ID',
    care_remind_frequency INT DEFAULT 7 COMMENT '基础养护提醒频率（天），可被模板覆盖',
    image_urls JSON COMMENT '图片URL数组',
    status ENUM('available','adopted','dead','removed') DEFAULT 'available' COMMENT '状态',
    qr_code VARCHAR(200) COMMENT '二维码标识',
    adopter_id BIGINT COMMENT '当前领养人ID',
    adopt_time DATETIME COMMENT '领养时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_qr (qr_code),
    INDEX idx_adopter (adopter_id),
    INDEX idx_care_plan_template (care_plan_template_id)
) COMMENT='绿植表';
-- 领养记录表
CREATE TABLE adoption_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id BIGINT NOT NULL COMMENT '绿植ID',
    adopter_id BIGINT NOT NULL COMMENT '领养人ID',
    status ENUM('active','returned','transferred') DEFAULT 'active' COMMENT '状态',
    adopt_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领养时间',
    return_time DATETIME COMMENT '归还/移交时间',
    return_reason VARCHAR(500) COMMENT '归还原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plant (plant_id),
    INDEX idx_adopter (adopter_id),
    INDEX idx_status (status)
) COMMENT='领养记录表';
-- 养护记录表（修改：细化养护字段，关联养护计划项）
CREATE TABLE care_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id BIGINT NOT NULL COMMENT '绿植ID',
    adopter_id BIGINT NOT NULL COMMENT '养护人ID',
    adoption_id BIGINT COMMENT '关联领养记录',
    care_plan_item_id BIGINT COMMENT '关联养护计划项ID',
    care_type ENUM('water','fertilize','prune','repot','pest','clean','ventilate','other') COMMENT '养护类型，新增清洁、通风',
    care_detail VARCHAR(500) COMMENT '养护细节（如：浇水50ml、修剪枯叶3片）',
    care_date DATE COMMENT '养护日期',
    care_time VARCHAR(20) COMMENT '养护具体时间（如：上午10:30）',
    content TEXT COMMENT '养护备注',
    image_urls JSON COMMENT '图片',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plant (plant_id),
    INDEX idx_adopter (adopter_id),
    INDEX idx_date (care_date),
    INDEX idx_care_plan_item (care_plan_item_id)
) COMMENT='养护记录表';
-- 社区帖子表
CREATE TABLE community_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    author_id BIGINT NOT NULL COMMENT '作者ID',
    plant_id BIGINT COMMENT '关联绿植ID',
    content TEXT COMMENT '内容',
    image_urls JSON COMMENT '图片',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    status ENUM('normal','hidden','deleted') DEFAULT 'normal' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_author (author_id),
    INDEX idx_plant (plant_id),
    INDEX idx_status (status),
    INDEX idx_time (create_time)
) COMMENT='社区帖子表';
-- 消息通知表（修改：新增养护计划提醒类型）
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '接收人ID',
    type ENUM('adoption_approved','adoption_rejected','care_remind','care_plan_remind','system','post_like','post_comment','bottle_pick','bottle_reply') COMMENT '类型，新增养护计划提醒',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容',
    related_id BIGINT COMMENT '关联业务ID（养护计划项ID/漂流瓶ID等）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read),
    INDEX idx_time (create_time)
) COMMENT='消息通知表';
-- 系统配置表
CREATE TABLE sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) NOT NULL COMMENT '配置值',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_key (config_key)
) COMMENT='系统配置表（领养上限、审批超时等）';
-- 新增：漂流瓶表
CREATE TABLE drift_bottle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT COMMENT '接收者ID（未被拾取时为null）',
    content TEXT NOT NULL COMMENT '漂流瓶内容（限500字）',
    image_urls JSON COMMENT '漂流瓶图片（最多3张）',
    status ENUM('floating','picked','replied','deleted') DEFAULT 'floating' COMMENT '状态：漂浮中/已拾取/已回复/已删除',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    pick_time DATETIME COMMENT '拾取时间',
    reply_content TEXT COMMENT '回复内容',
    reply_time DATETIME COMMENT '回复时间',
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_status (status),
    INDEX idx_send_time (send_time)
) COMMENT='漂流瓶表';
-- 新增：漂流瓶操作记录表（可选，用于追溯）
CREATE TABLE drift_bottle_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bottle_id BIGINT NOT NULL COMMENT '漂流瓶ID',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operation ENUM('send','pick','reply','delete') COMMENT '操作类型',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    remark VARCHAR(200) COMMENT '备注',
    INDEX idx_bottle (bottle_id),
    INDEX idx_operator (operator_id),
    INDEX idx_operation_time (operation_time)
) COMMENT='漂流瓶操作记录表';
-- 新增：养护计划模板表（管理员维护，按绿植品种/类别配置）
CREATE TABLE care_plan_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称（如：绿萝养护模板、多肉养护模板）',
    plant_category ENUM('foliage','succulent','flower','fern','other') COMMENT '适配绿植类别，可指定具体品种',
    plant_species VARCHAR(100) COMMENT '适配绿植品种（为空则适配该类别所有品种）',
    description TEXT COMMENT '模板说明',
    create_by BIGINT NOT NULL COMMENT '创建人ID（管理员）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    INDEX idx_plant_category (plant_category),
    INDEX idx_plant_species (plant_species),
    INDEX idx_status (status)
) COMMENT='养护计划模板表';
-- 新增：养护计划项表（模板下的具体养护任务）
CREATE TABLE care_plan_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL COMMENT '关联养护计划模板ID',
    care_type ENUM('water','fertilize','prune','repot','pest','clean','ventilate','other') COMMENT '养护类型',
    cycle_type ENUM('daily','weekly','monthly','quarterly','seasonal') COMMENT '周期类型',
    cycle_value INT NOT NULL COMMENT '周期值（如：weekly+2=每2周，monthly+1=每月1次）',
    cycle_fixed_day INT COMMENT '固定日期（如：monthly+15=每月15日，daily为空）',
    care_detail VARCHAR(500) NOT NULL COMMENT '养护操作细节（如：浇水50-80ml，沿盆边缓慢浇灌，避免浇到叶片）',
    care_note TEXT COMMENT '注意事项（如：夏季浇水需在早晚，避免正午高温）',
    remind_advance INT DEFAULT 1 COMMENT '提前提醒时间（小时），0表示当天提醒',
    sort INT DEFAULT 0 COMMENT '排序，按养护优先级排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template (template_id),
    INDEX idx_care_type (care_type),
    INDEX idx_cycle (cycle_type, cycle_value)
) COMMENT='养护计划项表';
-- 新增：绿植养护计划关联表（绿植与模板绑定，支持个性化调整）
CREATE TABLE plant_care_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plant_id BIGINT NOT NULL COMMENT '绿植ID',
    template_id BIGINT NOT NULL COMMENT '关联养护计划模板ID',
    adopter_id BIGINT NOT NULL COMMENT '领养人ID（养护计划归当前领养人所有，移交/归还后重新关联）',
    adjust_note TEXT COMMENT '个性化调整说明（如：该绿萝喜阴，浇水周期延长1天）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_plant_adopter (plant_id, adopter_id), -- 同一绿植同一领养人唯一绑定
    INDEX idx_plant (plant_id),
    INDEX idx_template (template_id),
    INDEX idx_adopter (adopter_id)
) COMMENT='绿植养护计划关联表';
-- 新增：养护计划执行记录表（跟踪养护计划项的执行情况）
CREATE TABLE care_plan_execute_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_item_id BIGINT NOT NULL COMMENT '关联养护计划项ID',
    plant_id BIGINT NOT NULL COMMENT '绿植ID',
    adopter_id BIGINT NOT NULL COMMENT '领养人ID',
    plan_execute_date DATE NOT NULL COMMENT '计划执行日期',
    actual_execute_date DATE COMMENT '实际执行日期',
    actual_execute_time VARCHAR(20) COMMENT '实际执行时间',
    execute_status ENUM('pending','executed','overdue','canceled') DEFAULT 'pending' COMMENT '执行状态：待执行/已执行/逾期/已取消',
    care_log_id BIGINT COMMENT '关联养护记录表ID（执行后关联）',
    remind_status ENUM('unreminded','reminded','read') DEFAULT 'unreminded' COMMENT '提醒状态：未提醒/已提醒/已读取',
    remind_time DATETIME COMMENT '提醒时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan_item (plan_item_id),
    INDEX idx_plant (plant_id),
    INDEX idx_adopter (adopter_id),
    INDEX idx_execute_status (execute_status),
    INDEX idx_plan_execute_date (plan_execute_date)
) COMMENT='养护计划执行记录表';
```

## 2.4 精确依赖版本

|**依赖**|**版本**|**说明**|
|---|---|---|
|Spring Boot|3.2.5|后端核心框架，支撑漂流瓶服务、养护计划服务开发|
|MyBatis-Plus|3.5.6|ORM框架，替代原生MyBatis，快速开发漂流瓶、养护计划相关CRUD|
|MySQL Connector|8.0.33|MySQL数据库连接|
|Spring Data Redis|spring-boot-starter-data-redis|Redis客户端，用于漂流瓶随机匹配、缓存未拾取漂流瓶，缓存养护提醒任务|
|MinIO|8.5.9|对象存储客户端，存储漂流瓶图片、养护记录图片|
|ZXing core|3.5.3|二维码核心库|
|ZXing javase|3.5.3|二维码JavaSE适配|
|JJWT|0.12.5|JWT认证|
|Lombok|1.18.32|简化代码|
|MapStruct|1.5.5.Final|DTO自动转换，适配漂流瓶、养护计划数据传输|
## 2.5 关键配置决策

1. **ORM框架**：使用MyBatis-Plus，无XML配置，采用注解+LambdaQueryWrapper开发

2. **认证方式**：JWT存放在Header的`Authorization`字段，格式为`Bearer {token}`

3. **Token策略**：有效期7天，过期后重新登录，不做refresh token

4. **文件上传**：单文件最大20MB，仅支持`jpg/png/webp`格式（漂流瓶图片、养护记录图片同此规则）

5. **分页方案**：统一使用MyBatis-Plus的Page对象，前端传`page/size`参数

6. **ID策略**：数据库自增ID，不使用雪花算法

7. **删除策略**：仅绿植使用`removed`状态软删除，其余业务表（含漂流瓶表、养护计划相关表）均物理删除

8. **并发控制**：通过Redis分布式锁防止领养、移交等并发竞争；漂流瓶拾取采用Redis原子操作，避免重复拾取；养护计划执行记录采用乐观锁，防止重复执行

9. **漂流瓶专属配置**：单个用户每日可发送漂流瓶上限3个（可通过sys_config动态配置）；未拾取漂流瓶缓存时间24小时，超时自动入库；漂流瓶随机匹配优先匹配同部门未拾取瓶子，无则全局匹配

10. **养护计划专属配置（新增）**：
          

    - 养护提醒触发时间：每日固定时间（如上午9:00）推送当日待执行养护计划提醒

    - 逾期提醒规则：养护计划项逾期1天、3天各推送1次逾期提醒，逾期7天推送最后1次提醒后，标记为“逾期未执行”，同步通知管理员

    - 模板默认配置：每个绿植类别至少配置1个默认养护模板，新增绿植时自动关联对应类别默认模板，管理员可手动修改

    - 个性化调整限制：用户可调整养护计划项的周期、提醒时间，不可修改养护类型和核心操作细节（管理员可无限制调整）

---

# 三、功能模块详细设计

## 3.1 用户端（UniApp H5）

### 3.1.1 页面结构（新增/修改养护计划相关页面）

```Plain Text
pages/
├── index/                    # 首页
│   └── index.vue            # 绿植列表、搜索、筛选，新增养护提醒入口
├── plant/
│   ├── detail.vue           # 绿植详情，新增养护计划标签页
│   ├── adopt.vue            # 领养申请
│   └── care-plan.vue        # 新增：绿植养护计划详情（查看/调整）
├── my/
│   ├── index.vue            # 我的页面，新增今日养护任务入口
│   ├── my-plants.vue        # 我的领养，显示各绿植养护计划状态
│   ├── care-log.vue         # 养护记录，关联养护计划项，显示执行情况
│   ├── my-bottles.vue       # 新增：我的漂流瓶（发送/接收/回复记录）
│   ├── care-tasks.vue       # 新增：我的养护任务（今日/待执行/逾期）
│   └── settings.vue         # 设置
├── community/
│   ├── index.vue            # 社区首页
│   ├── post.vue             # 发布帖子
│   └── detail.vue           # 帖子详情
├── drift-bottle/            # 新增：漂流瓶模块
│   ├── index.vue            # 漂流瓶首页（扔瓶子、捡瓶子入口）
│   ├── throw.vue            # 扔漂流瓶（编辑内容、上传图片）
│   ├── pick.vue             # 捡漂流瓶（随机获取、查看内容）
│   └── chat.vue             # 漂流瓶回复（与拾取者/发送者对话）
├── scan/
│   └── index.vue            # 扫码页面，扫码后可查看养护计划、提交养护记录
└── notice/
    └── index.vue            # 消息通知，区分养护提醒、养护计划提醒
```

### 3.1.2 核心功能流程（新增/修改养护计划相关流程）

#### 1. 领养流程（修改：新增养护计划绑定）

```Plain Text
用户浏览绿植列表 → 查看详情（含养护计划预览） → 点击"申请领养"
    ↓
填写申请理由（可选）→ 提交申请
    ↓
等待管理员审批 → 收到通知 → 领养成功
    ↓
系统自动为该绿植绑定对应养护模板，生成个性化养护计划 → 推送养护计划提醒
```

#### 2. 扫码流程（修改：新增养护计划相关操作）

```Plain Text
打开扫码 → 扫描绿植二维码
    ↓
判断状态：
    ├─ 未领养 → 显示信息 + 申请领养按钮 + 养护计划预览
    └─ 已领养 → 显示信息 + 养护记录 + 当前领养人 + 养护计划入口 + 今日养护任务
        ↓
可点击养护计划入口查看详情，可直接提交养护记录（关联对应计划项）
```

#### 3. 养护记录流程（修改：关联养护计划，细化操作）

```Plain Text
我的养护任务/绿植详情/扫码 → 选择待执行养护计划项
    ↓
系统自动填充养护类型、操作细节提示（如：浇水50-80ml）
    ↓
填写实际养护细节（如：浇水60ml）、养护时间 → 上传图片（可选）→ 填写备注（可选）
    ↓
提交 → 系统更新养护计划执行记录为"已执行"，关联养护记录ID
    ↓
可选择同步到社区分享（自动携带养护计划项信息）
```

#### 4. 社区分享流程（不变）

```Plain Text
发布帖子 → 选择关联绿植（可选）
    ↓
填写内容 + 上传图片 → 发布
    ↓
其他用户可点赞、评论
```

#### 5. 漂流瓶核心流程（不变）

**5.1 扔漂流瓶流程**

```Plain Text
进入漂流瓶首页 → 点击"扔瓶子"
    ↓
编辑内容（限500字）→ 上传图片（可选，最多3张）
    ↓
校验：每日发送数量未超上限 → 提交
    ↓
处理：生成漂流瓶（status=floating）→ 缓存至Redis（优先同部门）
    ↓
反馈：提示"漂流瓶已出发～"，跳转至我的漂流瓶列表
```

**5.2 捡漂流瓶流程**

```Plain Text
进入漂流瓶首页 → 点击"捡瓶子"
    ↓
校验：当前用户无未回复的漂流瓶 → 从Redis随机获取漂流瓶
    ↓
判断：
    ├─ 有未拾取瓶子 → 显示瓶子内容、图片，提供"回复"和"忽略"按钮
    └─ 无未拾取瓶子 → 提示"暂无漂流瓶，换个时间再来吧～"
    ↓
点击"回复" → 编辑回复内容 → 提交
    ↓
处理：更新漂流瓶状态为replied，填写receiver_id、reply_content、reply_time → 推送通知给发送者
    ↓
点击"忽略" → 漂流瓶重新放回Redis，可被其他用户拾取
```

**5.3 我的漂流瓶流程**

```Plain Text
进入我的漂流瓶页面 → 切换标签（发送的/收到的/已回复的）
    ↓
查看对应漂流瓶列表 → 点击某条漂流瓶
    ↓
查看详情（内容、图片、回复）→ 可删除自己发送/收到的漂流瓶
    ↓
删除操作：更新漂流瓶状态为deleted，物理删除（或软删除），记录操作日志
```

#### 6. 养护计划核心流程（新增）

**6.1 养护计划查看与个性化调整流程**

```Plain Text
我的领养 → 选择绿植 → 进入养护计划详情页
    ↓
查看养护计划模板信息、所有养护计划项（按周期排序）
    ↓
点击"调整计划" → 可调整项：周期值、固定日期、提前提醒时间
    ↓
填写调整说明 → 提交
    ↓
系统更新绿植养护计划关联表，同步更新后续执行计划 → 提示调整成功
    （注：管理员可调整所有项，普通用户仅可调整上述可调整项）
```

**6.2 养护任务查看与执行流程**

```Plain Text
我的养护任务页面 → 切换标签（今日任务/待执行/逾期/已完成）
    ↓
今日任务标签：显示当日所有待执行养护计划项，按优先级排序，标注提醒时间
    ↓
点击某任务 → 进入执行页面，系统自动填充养护细节提示
    ↓
填写实际执行信息（细节、时间）→ 上传图片 → 提交
    ↓
执行成功：更新执行记录为"已执行"，关联养护记录，同步到养护记录列表
    跳过任务：标记为"已取消"，填写跳过原因，后续仍按周期生成任务
    逾期任务：显示逾期天数，执行流程同上，执行后标记为"已执行（逾期）"
```

**6.3 养护提醒流程**

```Plain Text
系统每日固定时间（如9:00） → 查询当日待执行养护计划项
    ↓
按提前提醒时间推送提醒通知（如提前1小时，即8:00推送）
    ↓
用户查看通知 → 点击通知跳转至对应养护任务执行页面
    ↓
用户未读取提醒：1小时后再次推送1次，当日不再重复推送
    ↓
任务逾期：逾期1天、3天、7天各推送1次逾期提醒，逾期7天后停止推送
```

## 3.2 管理端（Vue3 Web）

### 3.2.1 页面结构（新增/修改养护计划相关页面）

```Plain Text
src/
├── views/
│   ├── dashboard/           # 仪表盘，新增养护计划执行数据统计
│   │   └── index.vue
│   ├── plant/               # 绿植管理
│   │   ├── list.vue
│   │   ├── add.vue          # 修改：新增养护计划模板选择
│   │   └── detail.vue       # 修改：新增养护计划标签页
│   ├── adoption/            # 领养管理
│   │   ├── applications.vue
│   │   └── records.vue
│   ├── user/                # 用户管理
│   │   └── list.vue
│   ├── community/           # 社区管理
│   │   └── posts.vue
│   ├── drift-bottle/        # 新增：漂流瓶管理
│   │   └── list.vue         # 漂流瓶列表、筛选、删除
│   ├── care-plan/           # 新增：养护计划管理
│   │   ├── template-list.vue # 养护计划模板列表、新增、编辑、删除
│   │   ├── template-add.vue  # 新增养护计划模板
│   │   ├── template-edit.vue # 编辑养护计划模板
│   │   └── execute-log.vue   # 养护计划执行记录查询、统计
│   ├── system/              # 系统设置
│   │   └── config.vue       # 配置管理（含漂流瓶、养护计划相关配置）
│   └── notice/              # 消息管理
└── utils/                   # 工具类
```

### 3.2.2 核心功能（新增/修改养护计划相关功能）

#### 1. 绿植管理（修改：关联养护计划模板）

- 添加/编辑绿植信息（名称、品种、位置、图片、养护手册、提醒频率）

- 上传图片到 MinIO

- 生成二维码（可打印）

- 绿植状态管理（健康、需关注、已枯萎）

- 关联养护计划模板：新增绿植时，根据类别/品种自动匹配默认模板，可手动修改关联模板

#### 2. 领养管理（不变）

- 领养申请审批（通过/拒绝）

- 领养记录查询

- 绿植移交（更换领养人）：移交后，自动为新领养人重新关联该绿植的养护计划，保留历史执行记录

#### 3. 二维码管理（不变）

- 批量生成二维码

- 打印模板（A4 纸张排版）

- 二维码与绿植绑定

#### 4. 系统配置（修改：新增养护计划相关配置）

- 动态配置用户领养上限、审批超时时间等参数

- 动态配置单个用户每日发送漂流瓶上限（默认3个）

- 配置漂流瓶缓存时间（默认24小时）

- 配置养护提醒固定时间（默认上午9:00）

- 配置逾期提醒规则（逾期1天、3天、7天推送）

#### 5. 漂流瓶管理（不变）

- 漂流瓶列表查询：支持按状态（漂浮中/已拾取/已回复/已删除）、发送时间、发送人筛选

- 漂流瓶详情查看：查看内容、图片、回复记录、操作日志

- 违规漂流瓶删除：管理员可删除违规内容，删除后同步更新漂流瓶状态为deleted，通知发送者和接收者（若有）

- 数据统计：查看漂流瓶总发送数、总拾取数、每日发送/拾取趋势

#### 6. 养护计划管理（新增）

- **养护计划模板管理**：
         

    - 新增模板：填写模板名称、适配类别/品种、说明，添加多个养护计划项（设置类型、周期、操作细节、注意事项等）

    - 编辑模板：可修改模板基本信息、新增/删除/编辑养护计划项，修改后同步更新所有关联该模板的绿植养护计划（用户个性化调整部分保留）

    - 删除模板：仅可删除未被绿植关联的模板，已关联模板需先解除关联才能删除

    - 模板启用/禁用：禁用后，新绿植不再关联该模板，已关联的绿植养护计划仍可正常执行

- **养护计划执行记录管理**：
          

    - 执行记录查询：支持按绿植、领养人、执行状态、执行日期筛选，查看详细执行信息（关联养护记录、提醒状态等）

    - 数据统计：查看养护计划执行率、逾期率、各养护类型执行次数，按部门/用户/绿植类别统计

    - 异常处理：可手动标记逾期任务为“已执行”“已取消”，填写处理说明；可查看长期逾期（超过7天）的任务，提醒对应领养人

- **绿植养护计划关联管理**：
          

    - 查看所有绿植的养护计划关联情况，可手动修改关联模板

    - 查看用户对养护计划的个性化调整记录，可重置为模板默认配置

## 3.3 核心业务规则（新增/修改养护计划相关规则）

1. **领养限制规则（不变）**

    - 默认单个用户最大可领养**5盆**绿植

    - 领养上限支持通过系统配置**动态调整**

    - 提交领养申请时校验当前用户已领养数量

2. **审批规则（不变）**

    - 领养申请**必须人工审批**，提交后推送通知至管理员

    - 审批超时时间默认**24小时**，超时自动驳回申请

    - 超时时间支持通过系统配置**动态调整**

3. **绿植状态流转规则（不变）**

    - 仅绿植状态为`available`时可发起领养申请

    - 放弃领养：用户/管理员均可操作；用户放弃无需审批，自动通知管理员，同时解除该用户与绿植的养护计划关联

    - 删除绿植：必须先解除领养记录，才可将绿植状态置为`removed`（软删除），同时删除该绿植的养护计划关联和执行记录

4. **归还/移交规则（修改：补充养护计划关联）**

    - 移交操作仅管理员可执行，用户仅可执行放弃领养

    - 移交/归还后，原领养人的领养记录**永久保留**，支持全流程追溯；原领养人的养护计划关联自动解除，养护执行记录保留

    - 移交后，系统自动为新领养人关联该绿植的养护计划（沿用原模板，无个性化调整），生成新的执行计划

5. **养护提醒规则（修改：精细化）**

    - 养护提醒分为“日常养护提醒”和“养护计划提醒”，日常提醒基于原频率，计划提醒基于养护计划项的周期

    - 每盆绿植的养护计划项独立配置提醒时间（提前提醒小时数），默认提前1小时

    - 系统每日固定时间（可配置）推送当日待执行养护计划提醒，未读取提醒1小时后再推送1次

    - 逾期提醒：养护计划项逾期1天、3天、7天各推送1次，逾期7天后停止推送，标记为“逾期未执行”，同步通知管理员

6. **社区内容规则（不变）**

    - 社区帖子**无需审核、无敏感词过滤**

    - 管理员可对帖子执行隐藏操作，用户可自行删帖

7. **并发竞争规则（不变，补充养护计划相关）**

    - 领养申请、绿植移交等核心操作，通过**Redis分布式锁**防止并发冲突

    - 漂流瓶拾取采用Redis原子操作（如RPOPLPUSH），避免同一漂流瓶被多个用户同时拾取

    - 养护计划执行记录采用乐观锁（版本号），防止同一计划项被重复执行、重复修改

8. **漂流瓶专属规则（不变）**

    - 发送限制：单个用户每日可发送漂流瓶上限默认3个，支持系统配置动态调整；内容限500字，图片最多3张（同文件上传规则）

    - 拾取规则：用户每次只能拾取1个漂流瓶，拾取后需回复或忽略才能继续拾取；不可拾取自己发送的漂流瓶

    - 匹配规则：优先匹配同部门未拾取的漂流瓶，无则全局随机匹配；未拾取漂流瓶缓存24小时，超时自动入库，可继续被拾取

    - 回复规则：拾取者可对漂流瓶进行1次回复，回复后漂流瓶状态变为replied，发送者可查看回复，不可二次回复

9. **养护计划专属规则（新增）**
   - **模板关联规则**：新增绿植时，系统根据绿植类别自动匹配对应类别默认养护计划模板；若无默认模板，需管理员手动关联后，绿植才可被领养。

   - **个性化调整规则**：普通用户仅可调整养护计划项的`cycle_value`（周期值）、`cycle_fixed_day`（固定日期）、`remind_advance`（提前提醒时间），调整后需填写调整说明，且调整后周期值不得超出模板设定的上下限（如模板设定浇水周期为5-10天，用户仅可在此范围内调整）；管理员无调整限制，可修改所有字段。

   - **执行记录规则**：养护计划执行记录仅可由系统自动生成（按周期），不可手动新增；执行状态仅可由“待执行”→“已执行”/“逾期”/“已取消”，不可反向修改；逾期超过7天的记录，系统自动锁定，仅管理员可手动干预。

   - **模板修改同步规则**：管理员修改养护计划模板后，已关联该模板的绿植养护计划中“未执行”的计划项自动同步更新，已执行/逾期的计划项保留历史配置；用户个性化调整的部分，若模板修改了核心操作细节，用户调整项中与核心细节冲突的部分自动失效，恢复为模板默认值，并通知用户。

   - **养护计划解绑规则**：仅当绿植状态为`available`（未领养）时，可解除与养护计划模板的关联；已领养绿植需先归还/移交，才能修改关联模板。

2. **数据统计规则（补充）**
- 系统每日凌晨自动统计前一日数据：包括领养申请数、领养成功数、养护计划执行率、漂流瓶发送/拾取/回复数、绿植损耗率等。
   
- 管理员可按周/月/季度导出统计报表，报表格式支持Excel/PDF，包含数据趋势图、部门维度对比、用户维度排名。

---

# 四、接口设计

## 4.1 接口规范

### 4.1.1 通用规范

- **请求方式**：RESTful风格，GET（查询）、POST（新增）、PUT（修改）、DELETE（删除）

- **请求头**：必传`Authorization: Bearer {token}`，可选`Content-Type: application/json`（默认）

- **响应格式**：统一JSON格式，包含`code`（状态码）、`msg`（提示信息）、`data`（业务数据）、`timestamp`（时间戳）

  ```JSON
  {
    "code": 200,
    "msg": "操作成功",
    "data": {},
    "timestamp": 1712764800000
  }
  ```

- **状态码定义**：

| 状态码 | 含义             |
| ------ | ---------------- |
| 200    | 成功             |
| 400    | 参数错误         |
| 401    | 未登录/Token过期 |
| 403    | 无权限           |
| 404    | 资源不存在       |
| 500    | 服务器内部错误   |

### 4.1.2 分页参数规范

- 前端传参：`page`（页码，默认1）、`size`（每页条数，默认10）

- 后端响应`data`格式：

  ```JSON
  {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
  ```

## 4.2 核心接口（新增漂流瓶、养护计划相关）

### 4.2.1 漂流瓶接口

| 接口路径                   | 请求方式 | 接口描述       | 请求参数                                           | 响应数据                               |
| -------------------------- | -------- | -------------- | -------------------------------------------------- | -------------------------------------- |
| `/api/drift-bottle/throw`  | POST     | 发送漂流瓶     | `content`（内容）、`imageUrls`（图片URL数组）      | 漂流瓶ID、发送时间                     |
| `/api/drift-bottle/pick`   | GET      | 拾取漂流瓶     | -                                                  | 漂流瓶详情（ID、内容、图片、发送时间） |
| `/api/drift-bottle/reply`  | PUT      | 回复漂流瓶     | `bottleId`（漂流瓶ID）、`replyContent`（回复内容） | 回复时间、漂流瓶状态                   |
| `/api/drift-bottle/my`     | GET      | 我的漂流瓶列表 | `type`（send/receive/reply）、`page`、`size`       | 分页漂流瓶列表                         |
| `/api/drift-bottle/delete` | DELETE   | 删除漂流瓶     | `bottleId`（漂流瓶ID）                             | 操作结果                               |
| `/api/drift-bottle/stats`  | GET      | 漂流瓶数据统计 | `startDate`、`endDate`                             | 发送数、拾取数、回复数、趋势数据       |

### 4.2.2 养护计划接口

| 接口路径                         | 请求方式 | 接口描述                   | 请求参数                                                     | 响应数据                       |
| -------------------------------- | -------- | -------------------------- | ------------------------------------------------------------ | ------------------------------ |
| `/api/care-plan/template/list`   | GET      | 养护计划模板列表           | `plantCategory`（绿植类别）、`status`（状态）、`page`、`size` | 分页模板列表                   |
| `/api/care-plan/template/add`    | POST     | 新增养护计划模板           | `templateName`、`plantCategory`、`plantSpecies`、`description`、`items`（计划项数组） | 模板ID                         |
| `/api/care-plan/template/edit`   | PUT      | 编辑养护计划模板           | `id`、`templateName`、`description`、`items`                 | 操作结果                       |
| `/api/care-plan/template/delete` | DELETE   | 删除养护计划模板           | `id`                                                         | 操作结果                       |
| `/api/care-plan/plant/bind`      | PUT      | 绿植绑定养护模板           | `plantId`、`templateId`、`adjustNote`（调整说明）            | 绑定结果                       |
| `/api/care-plan/plant/detail`    | GET      | 绿植养护计划详情           | `plantId`                                                    | 模板信息、计划项列表、调整记录 |
| `/api/care-plan/plant/adjust`    | PUT      | 个性化调整养护计划         | `plantId`、`items`（调整后的计划项）、`adjustNote`           | 调整结果                       |
| `/api/care-plan/execute/list`    | GET      | 养护计划执行记录列表       | `plantId`、`adopterId`、`executeStatus`、`page`、`size`      | 分页执行记录列表               |
| `/api/care-plan/execute/update`  | PUT      | 手动更新执行状态（管理员） | `executeId`、`executeStatus`、`handleNote`（处理说明）       | 操作结果                       |
| `/api/care-plan/tasks/my`        | GET      | 我的养护任务列表           | `type`（today/pending/overdue/finished）、`page`、`size`     | 分页任务列表                   |
| `/api/care-plan/tasks/execute`   | POST     | 执行养护任务               | `executeId`、`careDetail`（养护细节）、`careTime`、`imageUrls` | 养护记录ID、执行状态           |

### 4.2.3 通用接口（修改补充）

| 接口路径                 | 请求方式 | 接口描述     | 新增/修改说明                            |
| ------------------------ | -------- | ------------ | ---------------------------------------- |
| `/api/plant/detail`      | GET      | 绿植详情     | 新增返回养护计划模板ID、养护计划入口标识 |
| `/api/plant/add`         | POST     | 新增绿植     | 新增`carePlanTemplateId`参数             |
| `/api/adoption/adopt`    | POST     | 提交领养申请 | 新增领养成功后自动绑定养护计划的逻辑     |
| `/api/adoption/transfer` | PUT      | 绿植移交     | 新增移交后重新绑定养护计划的逻辑         |
| `/api/notification/list` | GET      | 消息通知列表 | 新增`type=care_plan_remind`筛选条件      |
| `/api/system/config`     | GET/PUT  | 系统配置     | 新增漂流瓶、养护计划相关配置项           |

---

# 五、非功能需求

## 5.1 性能需求

### 5.1.1 响应时间

- 普通查询接口（如绿植列表、我的养护任务）：响应时间≤500ms

- 复杂查询接口（如养护计划执行统计、漂流瓶数据趋势）：响应时间≤1000ms

- 写入接口（如发送漂流瓶、提交养护记录）：响应时间≤800ms

- 扫码接口：响应时间≤300ms（保障用户体验）

### 5.1.2 并发能力

- 支持同时在线用户数：≥500人

- 峰值QPS：≥200（核心接口：领养申请、漂流瓶拾取、养护任务执行）

- Redis缓存命中率：≥90%（漂流瓶缓存、养护提醒缓存）

### 5.1.3 数据存储

- 单表数据量上限：MySQL单表≤100万条（超过后分表，如养护记录表、漂流瓶表）

- 图片存储：单张图片≤20MB，总存储容量≥100GB

- 数据备份：每日凌晨全量备份，保留30天备份数据

## 5.2 安全需求

### 5.2.1 接口安全

- 所有接口需Token鉴权，无Token请求直接返回401

- 敏感接口（如领养移交、管理员删除操作）需二次校验用户角色

- 接口防刷：单IP/单用户1分钟内请求次数≤60次，超出限流（返回429）

- 数据脱敏：用户手机号、邮箱返回时脱敏（如138****1234）

### 5.2.2 数据安全

- 图片存储：MinIO存储的图片URL需带签名，有效期1小时，防止外链访问

- 数据库加密：用户密码采用BCrypt加密存储，无明文存储

- 操作日志：管理员核心操作（如删除漂流瓶、修改养护模板）记录操作日志，包含操作人、操作时间、操作内容、IP地址

### 5.2.3 前端安全

- XSS防护：前端输入内容过滤特殊字符，后端接口参数校验

- CSRF防护：接口请求添加Referer校验，仅允许企业内网域名访问

## 5.3 兼容性需求

### 5.3.1 终端兼容

- 用户端（UniApp H5）：兼容Chrome、Edge、Firefox最新版，支持微信/钉钉内置浏览器

- 管理端（Vue3 Web）：兼容Chrome、Edge最新版，分辨率≥1366×768

- 扫码功能：兼容主流扫码工具（微信、支付宝、系统自带扫码）

### 5.3.2 网络兼容

- 支持弱网环境（带宽≥1Mbps），核心功能（如养护提醒查看、漂流瓶拾取）可正常使用

- 图片上传支持断点续传，弱网下可重新上传

## 5.4 可维护性需求

### 5.4.1 代码规范

- 后端：遵循Alibaba Java开发手册，代码注释覆盖率≥80%

- 前端：遵循Vue官方规范，组件化开发，注释覆盖率≥70%

- 数据库：表、字段命名统一（下划线命名），注释完整

### 5.4.2 监控告警

- 接口监控：核心接口响应时间、错误率监控，超过阈值（响应时间>2000ms/错误率>5%）触发告警

- 服务器监控：CPU使用率≥80%、内存使用率≥85%、磁盘使用率≥90%触发告警

- 业务监控：养护计划逾期率≥30%、漂流瓶发送失败率≥10%触发告警

- 告警方式：邮件+企业微信通知管理员

---

# 六、测试计划

## 6.1 测试类型

### 6.1.1 功能测试

- 单元测试：后端核心服务（漂流瓶服务、养护计划服务）单元测试覆盖率≥70%

- 接口测试：所有接口全覆盖，验证参数校验、响应格式、业务逻辑

- 端到端测试：核心流程（领养→养护计划绑定→养护任务执行→漂流瓶互动）全流程验证

- 边界测试：如每日漂流瓶发送上限、养护计划逾期提醒规则、并发领养冲突等

### 6.1.2 性能测试

- 压力测试：核心接口在峰值QPS下的响应时间、服务器资源占用

- 并发测试：模拟500用户同时在线，执行核心操作，验证数据一致性

- 稳定性测试：系统连续运行72小时，验证无内存泄漏、数据异常

### 6.1.3 安全测试

- 渗透测试：接口鉴权绕过、SQL注入、XSS攻击等测试

- 数据安全测试：密码加密、图片URL签名、数据脱敏验证

### 6.1.4 兼容性测试

- 终端兼容性：不同浏览器、不同设备（PC/手机）的功能验证

- 网络兼容性：弱网、断网场景下的功能容错性（如断网后重新提交养护记录）

## 6.2 测试环境

- 开发环境：本地开发+测试服务器（单机）

- 测试环境：模拟生产配置（Docker容器化，1主1从MySQL，Redis集群）

- 预生产环境：与生产环境配置一致，数据为测试数据

## 6.3 上线前验收标准

- 功能测试通过率100%

- 性能测试满足响应时间、并发能力要求

- 安全测试无高危漏洞

- 核心流程（领养、养护计划执行、漂流瓶互动）全流程验证通过

- 管理员、普通用户角色功能验证通过

---

# 七、上线与运维计划

## 7.1 上线计划

### 7.1.1 上线阶段

1. **灰度上线**：先上线给企业行政部门+少量试点员工（约50人），运行3天，收集反馈

2. **全量上线**：灰度无重大问题后，全企业员工上线

3. **上线时间**：选择非工作日（如周六），避免影响日常办公

### 7.1.2 上线前准备

- 数据初始化：导入现有绿植数据，配置默认养护计划模板

- 管理员培训：讲解养护计划管理、漂流瓶管理、系统配置操作

- 用户手册：编写员工端/管理员端操作手册，发布至企业内网

- 回滚方案：准备上线失败回滚脚本，可快速回滚至上线前版本

## 7.2 运维计划

### 7.2.1 日常运维

- 每日检查：服务器状态、接口运行状态、数据备份情况

- 每周优化：慢查询SQL优化、Redis缓存策略调整

- 每月统计：系统使用数据（领养率、养护计划执行率、漂流瓶活跃度），输出运营报告

### 7.2.2 故障处理

- 故障分级：

  - 一级故障（核心功能不可用）：响应时间≤30分钟，解决时间≤2小时

  - 二级故障（非核心功能不可用）：响应时间≤1小时，解决时间≤4小时

  - 三级故障（功能异常但不影响使用）：响应时间≤2小时，解决时间≤8小时

- 故障复盘：所有故障处理完成后，输出复盘报告，优化避免重复问题

### 7.2.3 版本迭代

- 迭代周期：每月1次小版本迭代（功能优化、BUG修复），每季度1次大版本迭代（新功能上线）

- 需求收集：通过企业内网问卷、管理员反馈收集用户需求，纳入迭代计划

---

# 八、风险与应对措施

| 风险类型 | 具体风险                              | 应对措施                                                     |
| -------- | ------------------------------------- | ------------------------------------------------------------ |
| 技术风险 | Redis缓存击穿，导致漂流瓶拾取失败     | 1. 增加Redis布隆过滤器；2. 缓存空值，设置过期时间；3. 降级策略：缓存失效时直接查询数据库 |
| 技术风险 | 养护计划提醒推送失败，用户未收到通知  | 1. 消息推送记录日志，失败自动重试3次；2. 每日统计未推送成功的提醒，管理员手动补发；3. 提供“我的养护任务”页面，用户可主动查看 |
| 业务风险 | 员工参与度低，领养率/养护计划执行率低 | 1. 增加激励机制（如养护达标员工获得小礼品）；2. 管理员定期提醒未执行养护任务的员工；3. 社区分享养护成果，增加互动性 |
| 业务风险 | 漂流瓶内容违规，引发投诉              | 1. 管理员实时监控漂流瓶内容，及时删除违规内容；2. 增加用户举报功能，违规用户限制发送漂流瓶权限；3. 系统配置敏感词库，发送时自动过滤 |
| 运维风险 | 数据库单表数据量过大，查询性能下降    | 1. 提前规划分表策略（按时间分表）；2. 定期归档历史数据至冷备库；3. 优化查询SQL，增加索引 |
| 上线风险 | 上线后核心功能异常，影响办公          | 1. 灰度上线，先验证再全量；2. 准备回滚方案，快速恢复；3. 上线后安排运维人员值守24小时 |

---

# 九、附录

## 9.1 名词解释

| 名词             | 定义                                                         |
| ---------------- | ------------------------------------------------------------ |
| 养护计划模板     | 管理员维护的、按绿植类别/品种配置的标准化养护任务集合        |
| 养护计划项       | 模板下的具体养护任务（如每周浇水1次、每月施肥1次）           |
| 养护计划执行记录 | 跟踪养护计划项执行情况的记录（待执行/已执行/逾期/已取消）    |
| 漂流瓶           | 员工轻量化互动工具，支持发送/拾取/回复，内容限500字、图片≤3张 |
| 领养人           | 申请并获批领养绿植的企业员工，承担该绿植的养护责任           |

## 9.2 参考文档

- 《Spring Boot 3.x 官方文档》

- 《UniApp 跨平台开发指南》

- 《MySQL 8.0 性能优化指南》

- 《Redis 7.x 缓存设计最佳实践》

## 9.3 版本更新记录

| 版本 | 更新时间   | 更新内容                                               |
| ---- | ---------- | ------------------------------------------------------ |
| v1.0 | 2026-03-15 | 完成基础产品规划，包含领养、养护核心功能               |
| v1.1 | 2026-03-30 | 新增漂流瓶模块，补充社区互动功能                       |
| v1.2 | 2026-04-10 | 细化养护计划模块，完善数据库设计、接口规范、非功能需求 |

---

**文档结束**  

*注：本产品规划文档为办公室绿植领养系统v1.2版本，后续将根据开发进度、用户反馈持续迭代更新。*