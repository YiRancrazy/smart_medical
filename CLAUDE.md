# Smart Medical

医院预约挂号与后台管理系统后端（Spring Boot 3.5.9 + Java 17 + Maven）。
面向四类角色：`admin`（系统管理员，科室 / 排班 / 医生 / 订单管理）、`doctor`（医生，出诊 / 诊室）、`user`（患者，挂号 / 支付 / 就诊人）、`pharmacist`（药师，药品管理）。

## Develop ##
- 改代码前先用 Glob/Grep 定位相关文件，只读必要的几个，不要一次性铺开读。
- 跨模块改动或根因不明的 bug，**先看目录结构 + 调一遍调用链**再动手，别只盯单个文件。
- 方案设计类任务（新功能、重构），先理清模块边界再写。
- .dev 是开发目录，内部存放开发文档、api文档等

## Build & Run

- 运行：`./mvnw spring-boot:run`（默认 profile = `dev`）
- 打包：`./mvnw clean package -DskipTests`
- 单测：`./mvnw test`
- 切换 profile：`./mvnw spring-boot:run -Dspring-boot.run.profiles=prod`
- API 文档：启动后访问 `http://localhost:8080/swagger-ui.html`（Knife4j）
- MinIO：`http://localdev:9000`，bucket `imagehost`（见 `application.yaml`）
- MySQL：`jdbc:mysql://localdev:3306/smart_medical`（DDL 见 `src/main/resources/sql/CreateTable.sql`）

## Layout

```
src/main/java/com/yirancrazy/smartmedical/
├── SmartMedicalApplication.java   # Spring Boot 启动类
├── annotation/                    # 自定义注解（@Manager 等）
├── config/                        # Web / Swagger / MybatisPlus / MinIO / Security / CORS
├── constant/                      # 常量 + 枚举（OrderStatus、RegistrationStatus、Role…）
├── controller/                    # 三套 controller：admin / doctor / user（均 V1 版本化）
├── filter/                        # JWT / 手机号+密码 过滤器
├── manager/                       # 业务编排层（@Manager 注解，可注入多个 Service）
├── mapper/                        # MyBatis-Plus Mapper
├── pojo/                          # 实体 + dto + vo + result + excel
├── service/ (+ impl/)             # IService 风格的业务接口与实现
└── utils/                         # 工具类
src/main/resources/
├── application.yaml               # 主配置（JWT、MinIO、Knife4j）
├── application-dev.yaml / -prod.yaml
├── mapper/                        # MyBatis XML
├── sql/CreateTable.sql            # 数据库 DDL
└── static/ templates/             # 静态资源（上传占位图等）
```

## Naming & Conventions

- 包名小写、类名大驼峰；controller 全部带 `V1` 版本后缀（如 `AdminDepartmentControllerV1`）。
- 统一返回 `Result<T>`（`code=200 成功`、`code=500 失败`，见 `pojo/Result.java`）；构造用 `Result.success(data)` / `Result.fail(msg)`。
- 所有 POJO 用 Lombok `@Data`；可选构造用 `@RequiredArgsConstructor` 注入依赖，禁止字段注入。
- Manager 层负责跨 Service 编排，自定义注解 `@com.yirancrazy.smartmedical.annotation.Manager`（注册为 Spring Bean）；不要在 Controller 直接调用多个 Service，优先走 Manager。
- 枚举放 `constant/`，字段命名 `(code, alias, message)`；Lombok `@Getter` + `@AllArgsConstructor`。
- 表主键用雪花 ID（`IdUtil.getSnowflakeNextId()`），见 `DepartmentManager`。
- 中文注释、Javadoc 标注 `@Author / @Description / @Datetime / @Version`，新建类时保持一致。
- API 用 `@Operation(summary = "...")` 标注，说明面向端（`管理员端 - ` / `用户端 - ` 前缀）。
- 请求 / 响应 DTO 放在 `pojo/dto/<role>/{request,response,result}/`；注意 `pojo/dto/user/response/` 子包历史命名（包含 admin / user 两端的响应，迁移前勿改路径）。
- **数据库表 4 标准字段**：所有业务表（含日志表、状态流水表）都必须包含以下 4 列 —— `id`（雪花或自增，详见 §Layout 引用）、`create_time DATETIME DEFAULT CURRENT_TIMESTAMP`、`update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`、`is_deleted TINYINT(1) DEFAULT 0`；Entity 上对应字段加 MyBatis-Plus `@TableField(fill=...)` 与 `@TableLogic` 注解。即使语义上 append-only（如状态日志）也保留这 4 列，便于 DAO 层统一处理。**豁免**：仅当表为高频热点更新或纯 append-only 流水（参考 `drug_inventory`、`inventory_transaction`）时，可在 DDL 注释里**显式说明豁免原因**并省略非必要字段，但 `id` 永不豁免。新表必须在 `CreateTable.sql` 中显式列出这 4 列（即便计划豁免也要保留并注明）。

## Java 类生成格式

新建 Java 类（Controller / Service / Manager / Mapper / Util / Config / 枚举 / POJO 等）时，**严格按以下模板**生成文件，确保包名、import、Javadoc、类声明顺序与现有代码一致。

### 模板

```java
package com.yirancrazy.smartmedical.<子包>;

import <依赖类>;
// import 按字典序排列；同组内连续无空行，组间空一行
// 顺序：java.* / javax.* → 第三方（com.*、org.*、cn.* 等）→ 本项目（com.yirancrazy.smartmedical.*）

/**
 * <类作用简述>
 * @Author: YiRanCrazy@gmail.com
 * @Description: <类作用描述>
 * @Datetime: <当前时间，格式 yyyy-MM-dd HH:mm>
 * @Version: 1.0
 */

<public | final> class <类名> <extends | implements ...> {

    // 1) static 常量（public → protected → package → private）

    // 2) 成员变量（public → protected → package → private）

    // 3) 构造方法（工具类用 private 构造 + final 类）

    // 4) static 工厂 / 工具方法

    // 5) 公有方法

    // 6) private / protected 辅助方法（@Override 紧跟被重写方法之后）
}
```

### 强制规则

- **包名**：`com.yirancrazy.smartmedical.<子包>`，子包对应目录。
- **import**：
  - 默认**不带通配符**（`import xx.xx.Yyy;`），避免 `import com.yirancrazy.smartmedical.*;`。
  - 同包类不需要 import。
  - 未使用的 import 必须删除。
- **类级 Javadoc（必须有）**：
  - `@Author: YiRanCrazy@gmail.com`
  - `@Description: <中文简述>`
  - `@Datetime: yyyy-MM-dd HH:mm`（按生成时刻）
  - `@Version: 1.0`
  - 第一行为空行（`<类作用简述>` 可省略，Javadoc 必须存在）。
- **方法级 Javadoc**：
  - 公有方法、非 trivial 的 `protected` 方法必须写；写明**意图**与**副作用**（如"插入 / 抛出 XxxException"）。
  - 格式：`/** <中文一行说明> */`；多参数 / 多返回时按需加 `@param` / `@return`。
- **类修饰符**：
  - 工具类：`public final class XxxUtil {`，必须含 `private XxxUtil() {}`。
  - 枚举：`public enum XxxEnum {`，字段 `(code, alias, message)`，配合 Lombok `@Getter` + `@AllArgsConstructor`。
  - 接口：`public interface XxxService {`，实现类放 `impl/` 包。
  - 抽象类：仅在确有公共骨架时使用。
- **方法顺序**：构造 → 静态 → public → protected → private；`@Override` 紧贴被重写方法之后。
- **Lombok**：POJO 用 `@Data`；注入用 `@RequiredArgsConstructor` + `private final` 字段，**禁止**字段注入（`@Autowired` 字段）。
- **日志**：业务类加 `@Slf4j`，禁止 `System.out.println` / `printStackTrace`。
- **空行**：类内各代码块（常量 / 字段 / 构造 / 方法组）之间留一个空行，提升可读性。

### 实战样例

```java
package com.yirancrazy.smartmedical.utils;

import cn.hutool.core.util.IdUtil;

/**
 * 雪花 ID 生成工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 雪花 ID 生成工具
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

public final class IdGenerator {

    private IdGenerator() {
    }

    /**
     * 生成下一个雪花 ID（Long）
     */
    public static Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成下一个雪花 ID（String）
     */
    public static String nextIdStr() {
        return String.valueOf(IdUtil.getSnowflakeNextId());
    }
}
```

## Things to Avoid

- **不要**修改 `application*.yaml` 中的密钥、MinIO 凭据、JWT secret 入库；改动必须先确认是否走环境变量覆盖。
- **不要**直接编辑 `src/main/resources/sql/CreateTable.sql` 的表结构，除非同步更新对应 `Mapper.java` 与 DTO；DDL 是真相源。
- **不要**在 `Manager` 里调 `Controller`、不要在 `Mapper` 里写业务；层级：`Controller → Manager → Service → Mapper`。
- **不要**新增不带 `@Operation` 的 controller 方法——Knife4j 文档依赖它。
- **不要**用 `System.out.println`；用 `@Slf4j`（类上注解，`log.info/warn/error`）。
- **不要**手写 MyBatis XML 来做简单 CRUD；用 MyBatis-Plus `BaseMapper`；只有复杂联表才写 XML（放 `src/main/resources/mapper/`）。
- 新增三方依赖前确认是否已有等价工具（项目已用：Hutool、PageHelper、EasyExcel、Knife4j），避免重复。
- 安全：`SecurityConfig.java` 控制鉴权规则，改动前先确认接口所属角色（admin / user / doctor）与 JWT filter 链顺序。

## Repo-Specific Claude Behavior

- **每条命令必须用 RTK 封装**：所有通过 Bash 工具执行的命令（git / mvn / find / cat / grep / ls / npm / docker 等）必须写成 `rtk <原命令>` 的形式，例如 `rtk git status`、`rtk ./mvnw test`、`rtk ls src`。RTK（Rust Token Killer）是 token 优化的 CLI 代理，可节省 60-90% 输出 token；不能绕开直接调用底层命令。Meta 命令（`rtk gain`、`rtk gain --history`、`rtk discover`）按 RTK 文档直接使用即可。调试或特殊场景需绕过 RTK 时使用 `rtk proxy <cmd>`，但这是例外，不应作为常规写法。
- 默认使用 `./mvnw` 而不是全局 `mvn`，与项目锁定版本一致。
- 修改业务代码前先 `Read` 对应 Manager / Service / Mapper，理解现有事务边界再动。
- 报错时优先读 `src/main/resources/sql/CreateTable.sql` + 对应 XML，确认表 / 字段是否仍然存在；很多 `unknown column` 是 DDL 与实体不同步。
- 涉及前端对接的 API，保持路径与 `AdminXxxControllerV1` / `UserXxxControllerV1` 中 `@Operation` 描述一致，方便前端查 Knife4j。
- 不要 git commit / push，除非用户明确说"提交"或"推送"；提交时遵循下方"Git 提交规范"。
- 大量生成后用 `verify`：跑一次 `rtk ./mvnw compile` 确认无编译错误；改动数据库相关时跑 `rtk ./mvnw test`。

### Vibe Coding 规范

本项目默认走「**轻流程 / 重手感**」:最小规划、最快迭代、AI 出代码为主。以下 5 个 skill 在对应场景**自动调用**,无需用户点名。

| Skill          | 触发时机                                            | 预期产出 / 调用方式                                       |
| -------------- | ----------------------------------------------- | ---------------------------------------------- |
| grill-me  | 写代码 / 新增类 / 新接口 / 改语义 前                          | 1 轮 AskUserQuestion 对齐意图, 当我现式调用brainstorming时不启用这个skill                |
| ponytail       | 每次给出代码方案时(默认 on)                                | 走 ponytail 思路:能少则少、复用优先、不加无意义依赖               |
| verify         | 改动涉及运行行为 / API / 配置 / DDL 后,准备声明完成前           | 跑 `mvn compile` + 必要时启动应用 + curl/接口验证           |
| simplify       | 单次功能 / PR 收尾、提交前                                | 对刚改文件跑 simplify 思路:复用 / 简化 / 删除冗余             |
| caveman        | 全程对话输出                                          | 用 lite / full / ultra 级别压缩文本,与现有 RTK 互补      |

以下场景**不走 vibe coding**,切换到正式流程(含 writing-plans / TDD / 至少一人复核):

- 改动 `CreateTable.sql` 或任何 DDL
- 改动 `SecurityConfig.java` / JWT 过滤器链
- 改动跨 ≥ 2 个 Manager / Service 的编排
- `release/*` 或 `hotfix/*` 分支

> 与 RTK 的关系:RTK 压缩 shell 输出(命令结果),caveman 压缩 Claude 文本(回复内容),二者互补不冲突。

## Git 分支规范

仓库采用「双长期分支 + 主题分支」模型：`main` 为受保护的发布分支（PR 目标），`master` 为日常集成与默认工作分支，所有变更通过分支 + 合并回 `master`。

### 长期分支

| 分支 | 作用 | 受保护 | 备注 |
|---|---|---|---|
| `main` | 发布版本，对应线上 | 是 | 仅接收 `master` 通过 PR 合入；禁止直推；发版后打 tag（如 `v1.2.0`）|
| `master` | 默认工作分支 / 集成 | 否 | 克隆下来默认在此；所有主题分支从这里切出 |

- 发版流程：`master` 累积稳定 → 提 PR `master → main` → 通过后打 tag → 部署。
- 紧急修复：从 `main` 切 `hotfix/xxx`，合并后**同时回灌** `master`。

### 主题分支命名

格式：`<type>/<scope>-<short-desc>`，type / scope 与下方「Git 提交规范」保持一致，**全小写**，词间用 `-`。

| 前缀 | 用途 | 示例 |
|---|---|---|
| `feat/` | 新功能 | `feat/科室-新增停诊接口` |
| `fix/` | 修 bug | `fix/挂号-重复下单` |
| `refactor/` | 重构 | `refactor/订单-抽状态机` |
| `perf/` | 性能优化 | `perf/科室-加Redis缓存` |
| `chore/` | 杂项 | `chore/升级-SpringBoot-3.5.9` |
| `hotfix/` | 线上紧急修复（从 `main` 切）| `hotfix/支付-回调超时` |
| `release/` | 发版预热（从 `master` 切）| `release/v1.2.0` |

- `scope` 复用提交规范里的中文模块名（科室 / 排班 / 医生 / 订单 / 挂号 / 支付 / 就诊人 / 药品 / 管理员 / 用户 / 鉴权 / 文档 / 依赖）。
- 分支名**短而具体**，禁止带作者名 / 时间戳 / 临时标记（如 `tmp` / `test1`）。
- 已合并分支本地 `git branch -d <branch>` 删除；远程残枝 `git push origin --delete <branch>` 清理。

### 工作流

```bash
# 1. 切分支：始终从最新 master 出发
rtk git checkout master && rtk git pull
rtk git checkout -b feat/科室-新增停诊接口

# 2. 长寿命分支定期 rebase master，避免 merge 噪音
rtk git fetch origin && rtk git rebase origin/master

# 3. 合回 master：保留分支历史，禁止 fast-forward / squash
rtk git checkout master
rtk git merge --no-ff feat/科室-新增停诊接口
```

- **禁止** `git push --force` 到 `master` / `main`；force-push 仅限**自己**的主题分支。
- PR 描述里写「关联 issue / 改动点 / 影响面」，便于 review。
- 涉及数据库 DDL / 配置文件 / 安全规则的改动，PR 必须至少一人复核。

### 与 Claude 协作

- 动手前先 `rtk git status` + `rtk git branch --show-current`，确认在主题分支上；不要在 `master` / `main` 留未提交改动。
- 不要直推 `master` / `main`；若用户要求推送，先用 PR 流程。
- 改完分支先 `rtk git fetch` 再决定 rebase / merge，避免误覆盖他人提交。

## Git 提交规范

遵循 [约定式提交（Conventional Commits）](https://www.conventionalcommits.org/zh-hans/)。`type` 用英文，`scope` 与 `description` **必须使用中文**。

### 格式

```
<type>(<scope>): <description>

[body]

[footer]
```

### type（仅允许下表）

| type | 说明 | 触发示例 |
|---|---|---|
| `feat` | 新功能 | 新增 controller / 接口 |
| `fix` | 修复 bug | 修下单 500、字段映射错 |
| `docs` | 文档变更 | 仅修改 `CLAUDE.md` / README |
| `style` | 格式调整（无逻辑变化）| 调 import、格式化 |
| `refactor` | 重构（非新功能、非修 bug）| 抽公共 Manager 方法 |
| `perf` | 性能优化 | 加缓存、改分页 |
| `test` | 测试相关 | 新增 / 调整单测 |
| `build` | 构建系统或外部依赖 | `pom.xml` 升级 |
| `ci` | CI 配置 | GitHub Actions |
| `chore` | 杂项（构建 / 工具 / 依赖）| 调 `.gitignore` |
| `revert` | 回滚 | `revert: feat(科室): ...` |

### scope（可选；本项目强烈建议填）

按模块名或业务域：`科室`、`排班`、`医生`、`订单`、`挂号`、`支付`、`就诊人`、`药品`、`管理员`、`用户`、`鉴权`、`文档`、`依赖`。

### description 规则

- **中文**，陈述句，描述**做了什么**而非"做了什么修改"。
- ≤ 50 字；**不加句号**；动词开头：`新增` / `修复` / `调整` / `重构` / `移除` / `升级` / `回滚`。
- 首字母不大写（英文词也小写，除非专有名词）。

### body / footer（可选）

- `body`：72 字符内换行，说明 *为什么*，不要重复 `description`。
- `BREAKING CHANGE: <说明>` 标注不兼容改动；亦可在 type 后加 `!`：`feat(api)!: 重构科室返回结构`。
- 关联 issue：`Refs #123` / `Closes #456`。

### 示例

```text
feat(科室): 新增停诊与启用接口
fix(挂号): 修复同一号源重复下单问题
refactor(订单): 抽取状态流转到 OrderStatusMachine
perf(科室): 列表查询加 Redis 缓存(5min)
docs: 补充 CLAUDE.md 提交规范
build: 升级 Spring Boot 至 3.5.9
feat(api)!: 统一 Result.code 语义
BREAKING CHANGE: code=200 改为 0 表示成功，500 改为业务异常
```

### 与 Claude 协作

- 单次任务可能产生多条 commit，按逻辑边界**小步提交**，而非一坨提交。
- 提交前先 `rtk git status` + `rtk git diff --stat` 让用户复核改动范围。
- 改动跨越多个 scope 时，优先拆成多条；只有同一次提交确实无法分离，再并列：`feat(科室,排班): ...`。

## Useful Pointers

- 自定义 `@Manager` 注解：[annotation/Manager.java](src/main/java/com/yirancrazy/smartmedical/annotation/Manager.java)
- 统一返回对象：[pojo/Result.java](src/main/java/com/yirancrazy/smartmedical/pojo/Result.java)
- 全局配置：[src/main/resources/application.yaml](src/main/resources/application.yaml)
- 数据库 DDL：[src/main/resources/sql/CreateTable.sql](src/main/resources/sql/CreateTable.sql)
- 安全 / JWT：[config/SecurityConfig.java](src/main/java/com/yirancrazy/smartmedical/config/SecurityConfig.java)
- 过滤链：[filter/JwtAuthenticationFilter.java](src/main/java/com/yirancrazy/smartmedical/filter/JwtAuthenticationFilter.java)


## 项目内容
- 测试系统管理员账户：13996001338 密码：123456
- 测试医生账户：15264835030 密码：123123
- 测试用户账户：13996001338 密码：123456

### 后端项目
- 项目测试端口：8080

### 前端端口