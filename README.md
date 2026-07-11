<div align="center">

<img src="res/logo.png" alt="Smart Medical Logo" width="120" />

# Smart Medical · 智慧医院预约挂号系统

> 面向医院场景的后端服务：科室 / 排班 / 医生 / 号源管理 + 患者挂号 / 支付 / 就诊人全流程。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![MinIO](https://img.shields.io/badge/MinIO-Object%20Storage-C72E29?style=flat-square&logo=minio&logoColor=white)](https://min.io/)
[![Knife4j](https://img.shields.io/badge/Knife4j-API%20Docs-1AB6FF?style=flat-square)](https://doc.xiaominfo.com/)
[![License](https://img.shields.io/badge/license-Internal-blue?style=flat-square)](./LICENSE)

</div>

---

## 📖 项目简介

**Smart Medical** 是一套医院预约挂号与后台管理系统的**后端服务**，基于 Spring Boot 3.5.9 + Java 17 构建。

系统围绕三类角色提供完整的业务流程：

| 角色 | 主要能力 |
| --- | --- |
| 🛡️ **Admin（管理员）** | 科室 / 排班 / 医生 / 订单管理 |
| 🩺 **Doctor（医生）** | 出诊安排 / 诊室管理 |
| 🧑 **User（患者）** | 挂号 / 支付 / 就诊人维护 |

> 当前仓库为**后端**实现，前端项目另见对应仓库。

---

## ✨ 核心特性

- 🔐 **多端鉴权**：基于 JWT + Spring Security，区分 `admin` / `user` / `doctor` 角色。
- 🏥 **完整挂号链路**：科室 → 排班规则 → 号源 → 订单 → 支付 → 就诊。
- 📅 **排班规则引擎**：支持按规则类型、状态灵活配置出诊安排。
- 👨‍⚕️ **就诊人管理**：支持一户多就诊人、默认就诊人切换。
- 📑 **Excel 导入导出**：基于 EasyExcel 批量维护科室、医生、号源等基础数据。
- 🖼️ **对象存储**：使用 MinIO 管理头像 / 病历 / 通用图片资源。
- 📘 **API 文档**：集成 Knife4j，调试体验优于原生 Swagger。
- 🧩 **Manager 编排层**：通过 `@Manager` 注解 + Spring Bean 组合，避免 Service 间循环依赖。

---

## 🧱 技术栈

### 运行环境

| 组件 | 版本 |
| --- | --- |
| JDK | 17 |
| Spring Boot | 3.5.9 |
| Maven | 3.9+（项目自带 `mvnw`） |
| MySQL | 8.x |
| MinIO | 最新稳定版 |

### 关键依赖

| 类别 | 组件 |
| --- | --- |
| 持久层 | MyBatis-Plus（`BaseMapper`）、分页插件 PageHelper |
| 安全 | Spring Security + JWT |
| API 文档 | Knife4j（OpenAPI 3） |
| 工具库 | Hutool（雪花 ID、加密、类型转换等） |
| Excel | EasyExcel |
| 对象存储 | MinIO Java SDK |
| 日志 | SLF4J + Logback（Spring Boot 默认） |

---

## 📂 目录结构

```
smart-medical/
├── src/main/java/com/yirancrazy/smartmedical/
│   ├── SmartMedicalApplication.java   # Spring Boot 启动类
│   ├── annotation/                    # 自定义注解（@Manager 等）
│   ├── config/                        # Web / Swagger / MybatisPlus / MinIO / Security / CORS
│   ├── constant/                      # 常量 + 枚举（OrderStatus、RegistrationStatus、Role…）
│   ├── controller/                    # 三套 controller：admin / doctor / user（均 V1 版本化）
│   ├── filter/                        # JWT / 手机号+密码 过滤器
│   ├── manager/                       # 业务编排层（@Manager 注解，可注入多个 Service）
│   ├── mapper/                        # MyBatis-Plus Mapper
│   ├── pojo/                          # 实体 + dto + vo + result + excel
│   ├── service/ (+ impl/)             # IService 风格的业务接口与实现
│   └── utils/                         # 工具类
├── src/main/resources/
│   ├── application.yaml               # 主配置（JWT、MinIO、Knife4j）
│   ├── application-dev.yaml / -prod.yaml
│   ├── mapper/                        # MyBatis XML（复杂联表）
│   ├── sql/CreateTable.sql            # 数据库 DDL（**数据表唯一来源**）
│   └── static/ templates/             # 静态资源
├── docs/                              # 项目文档 / 变更记录
├── res/                               # 设计资源（logo / icon）
├── pom.xml
└── README.md
```

---

## 🚀 快速开始

### 前置条件

- JDK 17+
- Maven 3.9+（可直接使用仓库自带的 `./mvnw`）
- 已启动 **MySQL 8** 与 **MinIO**，并准备好 bucket
- 推荐 IDE：IntelliJ IDEA 2024+

### 1. 克隆 & 初始化数据库

```bash
git clone <your-org>/smart-medical.git
cd smart-medical

# 创建数据库并导入 DDL
mysql -uroot -p < src/main/resources/sql/CreateTable.sql
```

### 2. 修改配置

编辑 [src/main/resources/application-dev.yaml](src/main/resources/application-dev.yaml)，
按本机环境调整 MySQL / MinIO / JWT 密钥等字段。
**生产环境必须通过环境变量覆盖密钥**，详见下方 [⚙️ 配置说明](#-配置说明)。

### 3. 启动

```bash
# 默认使用 dev profile
./mvnw spring-boot:run
```

启动成功后：

| 入口 | 地址 |
| --- | --- |
| 应用服务 | `http://localhost:8080` |
| Knife4j API 文档 | `http://localhost:8080/swagger-ui.html` |

### 4. 打包

```bash
# 跳过单测
./mvnw clean package -DskipTests

# 产物
target/smart-medical-<version>.jar
```

---

## ⚙️ 配置说明

### Profile

| Profile | 入口配置 | 用途 |
| --- | --- | --- |
| `dev` | `application-dev.yaml` | 本地开发，默认启用 |
| `prod` | `application-prod.yaml` | 生产部署 |

切换：

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 关键配置项

> ⚠️ **生产环境请勿将密钥 / Token 写入 yaml，必须通过环境变量覆盖。**

| 配置 | 默认值 | 覆盖方式 |
| --- | --- | --- |
| JWT secret | `application.yaml` 中的占位符 | `SM_JWT_SECRET` |
| MinIO endpoint | `http://localdev:9000` | `SM_MINIO_ENDPOINT` |
| MinIO access / secret key | 见 `application.yaml` | `SM_MINIO_ACCESS_KEY` / `SM_MINIO_SECRET_KEY` |
| MySQL JDBC | `jdbc:mysql://localdev:3306/smart_medical` | `SM_DB_URL` |
| Knife4j | 启用 | 视生产策略而定 |

---

## 🔑 角色与权限

所有接口均经过 [src/main/java/com/yirancrazy/smartmedical/config/SecurityConfig.java](src/main/java/com/yirancrazy/smartmedical/config/SecurityConfig.java) 控制，
鉴权链由 [filter/JwtAuthenticationFilter.java](src/main/java/com/yirancrazy/smartmedical/filter/JwtAuthenticationFilter.java) 注入。

| 角色 | Token 标识 | 主要接口前缀 |
| --- | --- | --- |
| `ADMIN` | `role=admin` | `/api/admin/v1/**` |
| `DOCTOR` | `role=doctor` | `/api/doctor/v1/**` |
| `USER` | `role=user` | `/api/user/v1/**` |

修改权限前请确认接口所属角色与 JWT 过滤器链顺序。

---

## 📐 架构与编码约定

层级（自上而下，**严禁反向依赖**）：

```
Controller  →  Manager  →  Service  →  Mapper
```

- **Manager**：业务编排层，可注入多个 Service；通过自定义注解 [annotation/Manager.java](src/main/java/com/yirancrazy/smartmedical/annotation/Manager.java) 注册为 Spring Bean。
- **Service**：单领域业务逻辑，事务边界控制在此层。
- **Mapper**：基于 MyBatis-Plus `BaseMapper`，**只写数据访问**；仅复杂联表使用 XML（`src/main/resources/mapper/`）。
- **Controller**：参数校验 + 鉴权 + 返回 `Result<T>`；不直接调用多个 Service。

其它约定：

- 统一返回 [pojo/Result.java](src/main/java/com/yirancrazy/smartmedical/pojo/Result.java)，构造用 `Result.success(data)` / `Result.fail(msg)`。
- 主键统一使用 Hutool 雪花 ID（见 [utils/IdGenerator.java](src/main/java/com/yirancrazy/smartmedical/utils/IdGenerator.java)）。
- 表结构以 [src/main/resources/sql/CreateTable.sql](src/main/resources/sql/CreateTable.sql) 为真值源；DDL 与实体不同步是常见 bug 来源。
- 新增 Java 类请按 [CLAUDE.md → Java 类生成格式](CLAUDE.md) 生成（含 `@Author / @Description / @Datetime / @Version` 四件套 Javadoc）。

完整规范见 [CLAUDE.md](CLAUDE.md)。

---

## 🧪 测试

```bash
./mvnw test                       # 跑全部单测
./mvnw test -Dtest=ClassName      # 指定类
./mvnw verify                     # 含集成校验
```

数据库相关修改后强烈建议跑一次完整测试。

---

## 🛠️ 常用脚本

| 命令 | 用途 |
| --- | --- |
| `./mvnw spring-boot:run` | 本地启动（默认 dev） |
| `./mvnw spring-boot:run -Dspring-boot.run.profiles=prod` | 指定 profile 启动 |
| `./mvnw clean package -DskipTests` | 打包 |
| `./mvnw compile` | 编译（大量生成后验证用） |
| `./mvnw test` | 单测 |

---

## 🗺️ 路线图 / TODO

- [ ] 就诊人模块补完（增 / 改 / 查 / 删除由前端接入）
- [ ] 支付回调统一封装（`PaymentRecord` 状态机）
- [ ] Redis 缓存接入（科室 / 排班热点数据）
- [ ] Docker Compose 一键启动 MySQL + MinIO + 服务
- [ ] OpenAPI 自动生成客户端 SDK

更多变更见 [docs/CHANGE.md](docs/CHANGE.md)。

---

## 🤝 贡献指南

1. **小步提交**：按模块拆分，例如 `feat(科室): 新增停诊与启用接口`。
2. 提交规范遵循 [约定式提交](https://www.conventionalcommits.org/zh-hans/)：`<type>(<scope>): <中文 description>`。
3. 修改业务代码前先 `Read` 对应 Manager / Service / Mapper，理解现有事务边界。
4. 大量生成后至少跑一次 `./mvnw compile`，数据库相关改动跑 `./mvnw test`。
5. 提交前请用 `git status` + `git diff --stat` 复核改动范围。

细则见 [CLAUDE.md → Git 提交规范](CLAUDE.md)。

---

## 📄 许可证

当前仓库为**内部项目**，默认不对外授权。
如需开源请补充 [LICENSE](LICENSE) 文件后再行调整。

---

## 🙏 鸣谢

- [Spring Boot](https://spring.io/projects/spring-boot) · [MyBatis-Plus](https://baomidou.com/)
- [Hutool](https://hutool.cn/) · [EasyExcel](https://easyexcel.opensphere.alibaba.com/)
- [Knife4j](https://doc.xiaominfo.com/) · [MinIO](https://min.io/)
- 设计与图标：[res/](res/) 目录

---

<div align="center">

Made with ❤️ by **YiRanCrazy**

</div>
