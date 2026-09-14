# Smart Medical

Smart Medical 是一个医院预约挂号与运营管理平台，采用前后端分离的单仓库结构：

| 模块 | 路径 | 技术栈 | 使用角色 |
| --- | --- | --- | --- |
| 后端服务 | `src/` | Spring Boot 3.5.9、Java 17、Maven | 全部角色 |
| 运营管理端 | `front/admin/` | Vue 3、Vite、Ant Design Vue、Pinia | 管理员、医生、药师 |
| 患者端 | `front/user/` | Vue 3、Vite、Vant、Pinia | 患者 |
| 基础设施 | `docker-compose.yml` | MySQL、Redis、MinIO、Nginx | 本地开发与部署 |

## 核心能力

- 四角色权限隔离：`admin`、`doctor`、`pharmacist`、`user`。
- JWT + Spring Security 无状态鉴权，支持访问令牌与刷新令牌。
- AJ-Captcha 行为验证码，登录链路可校验滑块验证码。
- 科室、医生、排班模板、号源、挂号、订单、支付和就诊状态全流程管理。
- 患者档案、就诊人、病历、处方、药品、库存和发药流程。
- 用户端图文问诊接口与医生端待诊、接诊、病历编辑页面。
- MyBatis-Plus 持久化、PageHelper 分页、Druid 数据源。
- MinIO 对象存储，支持头像、病历图片和通用文件上传。
- EasyExcel 导入导出，覆盖挂号模板等批量数据场景。
- Spug 短信验证码；未配置短信模板时自动进入日志 Mock 模式。
- Docker Compose 一键启动后端、管理端、患者端、MySQL、Redis 和 MinIO。

## 技术栈

### 后端

| 组件 | 版本 / 说明 |
| --- | --- |
| JDK | 17 |
| Spring Boot | 3.5.9 |
| Maven | 使用仓库内 `mvnw` |
| Web / Security | Spring MVC、Spring Security |
| 持久化 | MyBatis-Plus、PageHelper、Druid |
| 数据库 | MySQL 8 |
| 缓存 / 会话 | Redis 7 |
| 对象存储 | MinIO |
| API 文档 | Knife4j / OpenAPI 3 |
| Excel | EasyExcel |
| 工具库 | Hutool、Lombok |
| 验证码 | AJ-Captcha |

### 前端

| 应用 | 主要依赖 |
| --- | --- |
| 管理端 | Vue 3、Vite、TypeScript、Ant Design Vue、Pinia、Axios |
| 患者端 | Vue 3、Vite、TypeScript、Vant、Pinia、Axios |

## 目录结构

```text
.
├── src/main/java/com/yirancrazy/smartmedical/
│   ├── annotation/       自定义注解，如 @Manager
│   ├── config/           Web、Security、MyBatis-Plus、MinIO、Knife4j 配置
│   ├── constant/         常量与枚举
│   ├── controller/       admin、doctor、pharmacy、user 四套 V1 Controller
│   ├── exception/        业务异常与全局异常处理
│   ├── filter/           JWT 与验证码过滤器
│   ├── manager/          跨 Service 业务编排
│   ├── mapper/           MyBatis-Plus Mapper
│   ├── pojo/             Entity、DTO、VO、Result 与 Excel 模型
│   ├── service/          业务接口与实现
│   └── utils/            JWT、Redis、MinIO、分页等工具
├── src/main/resources/
│   ├── application.yaml
│   ├── application-dev.yaml
│   ├── application-prod.yaml
│   ├── mapper/           MyBatis XML
│   └── sql/              CreateTable.sql 与 TestData.sql
├── src/test/             单元测试
├── front/admin/          运营管理端
├── front/user/           患者端
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── mvnw / mvnw.cmd
└── pom.xml
```

## 快速开始

### 前置条件

- JDK 17
- Node.js 20 与 npm
- Docker 与 Docker Compose，推荐用于完整环境启动
- 如果不用 Docker，需要自行准备 MySQL 8、Redis 7 与 MinIO

### 1. 准备环境变量

先复制环境变量样例，并填写空值或占位值：

Windows PowerShell：

```powershell
Copy-Item .env.example .env
```

Linux / macOS：

```bash
cp .env.example .env
```

至少需要确认以下内容：

- `JWT_ACCESS_SECRET_KEY`、`JWT_REFRESH_SECRET_KEY`
- `MYSQL_PASSWORD`、`MYSQL_USERNAME`、`MYSQL_DATABASE`
- `REDIS_PASSWORD`
- `MINIO_ROOT_USER`、`MINIO_ROOT_PASSWORD`、`MINIO_BUCKET_NAME`
- `MINIO_HOST`、`MINIO_PORT`、`MINIO_PUBLIC_HOST`

禁止将真实密钥、生产密码或 Token 提交到仓库。

### 2. Docker 一键启动

Windows：

```powershell
.\start-docker.bat
```

Linux / macOS：

```bash
./start-docker.sh
```

也可以手动执行：

```bash
docker-compose up -d
```

默认入口：

| 服务 | 地址 |
| --- | --- |
| 后端 API | `http://localhost:8080` |
| Knife4j API 文档 | `http://localhost:8080/swagger-ui.html` |
| 运营管理端 | `http://localhost:5173` |
| 患者端 | `http://localhost:5174` |
| MinIO 控制台 | `http://localhost:9001` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

停止服务：

```bash
docker-compose down
```

### 3. 本地启动后端

确保 `.env` 中的 MySQL、Redis 和 MinIO 地址均可访问，然后执行：

```bash
./mvnw spring-boot:run
```

默认启用 `dev` Profile。切换生产配置：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. 本地启动前端

管理端：

```bash
cd front/admin
npm ci
npm run dev
```

患者端：

```bash
cd front/user
npm ci
npm run dev
```

两个 Vite 开发服务器都会将 `/api` 请求代理到 `http://localhost:8080`。

## 配置说明

项目通过 `spring-dotenv` 加载工作目录下的 `.env`。Profile 默认值为 `dev`，可由 `SPRING_PROFILES_ACTIVE` 覆盖。

### 基础配置

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | 运行 Profile：`dev` 或 `prod` |
| `SERVER_PORT` | `8080` | 后端端口 |
| `ADMIN_PORT` | `5173` | 管理端容器映射端口 |
| `USER_PORT` | `5174` | 患者端容器映射端口 |
| `TZ` | `Asia/Shanghai` | 容器时区 |

### 数据库与缓存

| 环境变量 | 说明 |
| --- | --- |
| `MYSQL_HOST` / `MYSQL_PORT` | MySQL 地址与端口 |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | MySQL 账号与密码 |
| `MYSQL_DATABASE` | 数据库名，默认使用 `smart_medical` |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址与端口 |
| `REDIS_PASSWORD` / `REDIS_DATABASE` | Redis 密码与数据库序号 |
| `REDIS_TIMEOUT` | Redis 超时时间，默认 `2000ms` |

### 对象存储

| 环境变量 | 说明 |
| --- | --- |
| `MINIO_HOST` / `MINIO_PORT` | 后端访问 MinIO 的内部地址 |
| `MINIO_PUBLIC_HOST` | 浏览器可访问的 MinIO 地址，用于拼接文件外链 |
| `MINIO_CONSOLE_PORT` | MinIO 控制台端口 |
| `MINIO_IMAGE_TAG` | MinIO 镜像版本 |
| `MINIO_BUCKET_NAME` | 文件桶名称 |
| `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` | MinIO 访问凭证 |
| `MINIO_IMG_SIZE` / `MINIO_FILE_SIZE` | 上传大小限制，单位 MB |

### 安全与通知

| 环境变量 | 说明 |
| --- | --- |
| `JWT_ACCESS_SECRET_KEY` | JWT 访问令牌密钥，必填 |
| `JWT_REFRESH_SECRET_KEY` | JWT 刷新令牌密钥，必填 |
| `JWT_ACCESS_TOKEN_PREFIX` | Redis 访问令牌前缀 |
| `JWT_REFRESH_TOKEN_PREFIX` | Redis 刷新令牌前缀 |
| `CORS_ALLOWED_ORIGINS` | 允许来源；dev 可用 `*`，prod 必须配置具体域名且禁止 `*` |
| `COOKIE_SECURE` | Cookie 安全标记；HTTPS 生产环境应设为 `true` |
| `SMS_BASE_URL` | Spug 短信服务地址 |
| `SMS_TEMPLATE_CODE` | 短信模板编码；为空时启用日志 Mock 模式 |

## 角色与接口

后端接口统一以 `/api` 开头，并按末端角色划分权限：

| 角色 | Spring Security Role | 接口前缀 | 主要能力 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `/api/admin/v1/**` | 科室、医生、排班、账号、订单、统计、文件与 Excel |
| 医生 | `doctor` | `/api/doctor/v1/**` | 待诊、接诊、病历、处方、排班与药品查询 |
| 药师 | `pharmacist` | `/api/pharmacy/v1/**` | 处方审核、发药、库存与流水查询 |
| 患者 | `user` | `/api/user/v1/**` | 注册登录、挂号、支付、就诊人、病历、处方与问诊 |

登录、刷新令牌、注册、短信验证码、忘记密码、验证码和 Swagger 相关接口位于公开白名单；其余接口均校验 JWT 与角色。

## 架构约定

后端执行严格单向分层：

```text
Controller -> Manager -> Service -> Mapper
```

| 层 | 职责 | 约束 |
| --- | --- | --- |
| Controller | 接收请求、DTO 参数校验、调用 Manager | 不直接调用 Mapper，不编写跨 Service 编排 |
| Manager | 业务编排、事务边界、多 Service 数据聚合、DTO 与 Entity 转换 | 不直接调用 Mapper，不调用其他 Manager |
| Service | 原子业务、业务规则校验、调用自身 Mapper | 返回业务数据，不处理 HTTP 语义 |
| Mapper | 数据访问与复杂 SQL | 不写业务规则、不控制事务 |

其他约定：

- 统一响应对象为 `Result<T>`，成功使用 `Result.success(...)`，失败使用 `Result.fail(...)`。
- Controller 全部使用 `V1` 后缀，例如 `AdminDepartmentControllerV1`。
- 依赖注入使用 `@RequiredArgsConstructor` 与 `private final`，禁止字段注入。
- Entity 使用 Lombok `@Data`，枚举统一放在 `constant/`。
- 主键使用 Hutool 雪花 ID。
- 业务表统一保留 `id`、`create_time`、`update_time`、`is_deleted` 四个字段。
- 建库建表以 `src/main/resources/sql/CreateTable.sql` 为准。
- 简单 CRUD 使用 MyBatis-Plus；复杂联表才新增 XML。
- 新建 Java 类必须包含 `@Author`、`@Description`、`@Datetime`、`@Version` Javadoc。
- Controller 方法必须添加 Knife4j `@Operation(summary = "...")`。

## 构建与测试

后端：

```bash
./mvnw clean package -DskipTests
./mvnw compile
./mvnw test
./mvnw verify
```

管理端：

```bash
cd front/admin
npm ci
npm run type-check
npm run build
```

患者端：

```bash
cd front/user
npm ci
npm run type-check
npm run build
```

数据库或接口行为变更后，至少执行后端编译和测试；涉及前端契约时同时执行对应前端的类型检查与生产构建。

## Git 规范

分支命名：

```text
feat/<scope>-<description>
fix/<scope>-<description>
refactor/<scope>-<description>
perf/<scope>-<description>
chore/<scope>-<description>
release/<version>
hotfix/<scope>-<description>
```

提交信息遵循 Conventional Commits，`type` 使用英文，`scope` 与描述使用中文：

```text
feat(科室): 新增停诊与启用接口
fix(挂号): 修复同一号源重复下单问题
refactor(订单): 抽取状态流转逻辑
docs: 更新项目说明
```

提交前检查改动范围，按业务模块拆分提交，不要混入密钥或本地环境文件。

## 许可证

当前仓库为内部项目，未提供开源许可证。未经授权不得对外分发或用于商业交付。
