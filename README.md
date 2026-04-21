# Yuan-Picture 云图

> AI 图片管理分享平台 | AI-Powered Image Management & Sharing Platform

## 项目介绍

Yuan-Picture（云图）是一个全栈 AI 图片管理分享平台，支持图片上传、管理、分类、分享以及 AI 智能扩图、以图搜图、色搜图等高级功能。

## 技术架构

### 前端

- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI 组件库**: Ant Design Vue
- **状态管理**: Pinia
- **路由**: Vue Router
- **图表**: ECharts / vue-echarts
- **图片处理**: vue-cropper

### 后端

- **框架**: Spring Boot 2.7.6
- **语言**: Java 11
- **ORM**: MyBatis Plus
- **缓存**: Caffeine + Redis
- **文档**: Knife4j

### 存储与服务

- **数据库**: MySQL 8.x
- **缓存**: Redis
- **对象存储**: 腾讯云 COS
- **AI 服务**: 阿里云通义万相
- **图像搜索**: 腾讯云图像搜索 API

## 核心功能

### 用户系统

- 用户注册 / 登录
- 基于角色的访问控制（普通用户 / 管理员）

### 图片管理

- 本地文件上传
- URL 远程图片导入
- 批量图片抓取上传
- 图片编辑（裁剪、旋转）
- 图片审核（待审核 / 已通过 / 已拒绝）

### 空间系统

- 创建个人图片空间
- 三种会员等级：
  - **普通版**: 100 张图片 / 100 MB
  - **专业版**: 1000 张图片 / 1 GB
  - **企业版**: 10000 张图片 / 10 GB
- 空间容量统计与限制

### AI 功能

- **智能扩图**: 利用 AI 扩展图片边界
- **以图搜图**: 相似图片检索
- **色搜图**: 按主色调搜索图片

### 数据分析

- 空间使用概览
- 图片分类分布统计
- 标签统计分析
- 用户贡献排行

## 项目结构

```
yuan-picture/
├── yuan-picture-backend/          # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com.yuanc.yuanpicturebackend/
│   │       ├── annotation/    # 自定义注解
│   │       ├── aop/         # AOP 切面
│   │       ├── api/          # 外部 API 封装
│   │       ├── config/       # 配置类
│   │       ├── constant/     # 常量
│   │       ├── controller/   # REST 控制器
│   │       ├── exception/   # 异常处理
│   │       ├── manager/     # 业务管理器
│   │       ├── mapper/     # MyBatis Mapper
│   │       ├── model/      # 数据模型
│   │       ├── service/    # 业务服务
│   │       └── utils/       # 工具类
│   ├── sql/
│   │   └── create_table.sql
│   └── pom.xml
│
└── yuan-picture-frontend/     # Vue 3 前端
    ├── src/
    │   ├── api/           # API 请求
    │   ├── components/     # 公共组件
    │   ├── pages/        # 页面组件
    │   │   ├── admin/    # 管理后台
    │   │   └── user/   # 用户中心
    │   ├── router/       # 路由配置
    │   ├── stores/      # Pinia 状态
    │   └── utils/       # 工具函数
    ├── package.json
    └── vite.config.ts
```

## 快速开始

### 前置要求

- JDK 11+
- Maven 3.6+
- MySQL 8.x
- Redis
- Node.js 18+

### 后端启动

```bash
# 进入后端目录
cd yuan-picture-backend

# 配置数据库和 Redis 连接
# 修改 src/main/resources/application.yml

# 启动项目
mvn spring-boot:run

# API 文档访问地址
http://localhost:8123/api/doc.html
```

### 前端启动

```bash
# 进入前端目录
cd yuan-picture-frontend

# 安装依赖
npm install

# 启���开发服务器
npm run dev

# 构建生产版本
npm run build
```

## API 接口

### 用户接口 `/api/user`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /register | 用户注册 |
| POST | /login | 用户登录 |
| POST | /logout | 用户登出 |
| GET | /get/login | 获取当前登录用户 |
| POST | /list/page/vo | 用户列表（管理员） |

### 图片接口 `/api/picture`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /upload | 上传图片（文件） |
| POST | /upload/url | 上传图片（URL） |
| POST | /upload/batch | 批量上传 |
| POST | /delete | 删除图片 |
| POST | /update | 更新图片 |
| GET | /get/vo | 获取图片详情 |
| POST | /list/page/vo | 图片列表 |
| POST | /edit | 编辑图片 |
| POST | /review | 图片审核 |
| POST | /search/picture | 以图搜图 |
| POST | /search/color | 色搜图 |
| POST | /out_painting/create_task | AI 扩图 |

### 空间接口 `/api/space`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /add | 创建空间 |
| POST | /delete | 删除空间 |
| POST | /edit | 编辑空间 |
| GET | /get/vo | 获取空间详情 |
| POST | /list/page/vo | 空间列表 |
| GET | /list/level | 获取空间等级 |

### 分析接口 `/api/space/analyze`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /analyze | 综合分析 |
| POST | /analyze/category | 分类统计 |
| POST | /analyze/tag | 标签统计 |
| POST | /analyze/size | 大小分布 |
| POST | /analyze/usage | 使用概览 |
| POST | /analyze/user | 用户贡献 |

## 配置说明

### 后端配置文件

`src/main/resources/application.yml`:

```yaml
server:
  port: 8123
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yuan_picture
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379

# 腾讯云 COS 配置
cos:
  secretId: your_secret_id
  secretKey: your_secret_key
  bucket: yuan1-1375940541
  region: ap-shanghai

# 阿里云 AI 配置
aliyun:
  accessKeyId: your_access_key_id
  accessKeySecret: your_access_key_secret
```

### 前端环境变量

创建 `.env.development` 或 `.env.production` 文件：

```
VITE_API_BASE_URL=http://localhost:8123/api
```

## 页面路由

| 路径 | 页面 | 权限 |
|------|------|------|
| / | 首页 | 公开 |
| /user/login | 登录 | 公开 |
| /user/register | 注册 | 公开 |
| /add_picture | 上传图片 | 登录 |
| /add_picture/batch | 批量上传 | 管理员 |
| /picture/:id | 图片详情 | 登录 |
| /search_picture | 图片搜索 | 登录 |
| /add_space | 创建空间 | 登录 |
| /my_space | 我的空间 | 登录 |
| /space/:id | 空间详情 | 空间成员 |
| /space_analyze | 空间分析 | 空间成员 |
| /admin/userManage | 用户管理 | 管理员 |
| /admin/pictureManage | 图片管理 | 管理员 |
| /admin/spaceManage | 空间管理 | 管理员 |

## 数据库表

### user 表

用户账户信息，包含 userAccount、userPassword、userName、userAvatar、userRole 等字段。

### picture 表

图片元数据，包含 url、thumbnailUrl、name、category、tags、picSize、picWidth、picHeight、picColor、reviewStatus、spaceId 等字段。

### space 表

空间信息，包含 spaceName、spaceLevel、maxSize、maxCount、totalSize、totalCount、userId 等字段。

## 许可证

MIT License