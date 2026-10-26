# 校园头条（Campus Headlines）

基于 **SSM**（Spring 5 + SpringMVC + MyBatis-Plus）的校园新闻资讯站。当前仓库为**初始化骨架**：Maven Web 工程结构、依赖、Spring 双容器配置、日志与数据库配置模板已就绪，业务代码待后续步骤填充。

---

## 一、技术栈与版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | **21.0.2**（本机已装） | `JAVA_HOME=D:\soft\Java\jdk-21`，`--release 21` 编译。Spring 5.3.30+ 起正式支持 JDK 21 |
| Maven | 3.9.6 | 本机**未装独立 Maven**，用 IDEA 2024.1 自带的 3.9.6（见「4. 构建」） |
| Spring | 5.3.39 | 5.3 系列最终版，支持 JDK 8–21 |
| SpringMVC | 5.3.39 | 与 Spring 同版本 |
| MyBatis | 3.5.13 | 与 MyBatis-Plus 3.5.3.2 内部锁定版本一致 |
| mybatis-spring | 2.1.1 | 同上，避免版本冲突 |
| MyBatis-Plus | 3.5.3.2 | 兼容 Spring 5；分页插件在 `mybatis-plus-extension` 内 |
| MySQL | **8.0.44**（本机服务 `MySQL80` 运行中） | 驱动 `mysql-connector-j:8.0.33`，8.0/5.7 都能连 |
| Druid | 1.2.24 | 连接池，自带 SQL 监控 |
| Servlet API | 4.0.1 | `javax.servlet`（**不是** `jakarta.servlet`） |
| JSP API | 2.3.3 | 编译期 provided，运行期由 Tomcat 提供 |
| JSTL | 1.2 | 需要打进 war |
| Jackson | 2.17.3 | 含 `jackson-datatype-jsr310` 支持 `LocalDateTime` |
| AspectJ | 1.9.22.1 | 1.9.20 只支持到 Java 18，故上调 |
| 文件上传 | commons-fileupload 1.5 + commons-io 2.11.0 | 配合 `CommonsMultipartResolver` |
| 日志 | Log4j2 2.24.3 + slf4j-api 1.7.36 | 不要同时引入 logback |
| Web 容器 | **Tomcat 9.0.123**（本机已装） | Windows 服务 `Tomcat9`，监听 8080；⚠️ 必须是 9.0.x，见下方说明 |

> **为什么是 Java 21**：本机只有 JDK 21（另有 11.0.14 和 1.8.0_472 备用），`JAVA_HOME` 指向 21，所以直接以 21 为目标，不搞「用 21 编 8」的将就方案。Spring 5.3.30 起补充了对 JDK 21 的支持，5.3.39 是这条线的最终版，配合上调后的 AspectJ 1.9.22.1 / Log4j2 2.24.3 / Jackson 2.17.3 / Druid 1.2.24 在 JDK 21 上没有已知坑。
>
> 要退回更低版本（例如课程要求 JDK 8）：只改 `pom.xml` 里 `<maven.compiler.release>` 一处即可，Spring 5.3.39 本身覆盖 8–21，不必换依赖。

> ⚠️ **Tomcat 10 / 11 不能直接用**：Tomcat 10 起 servlet API 从 `javax.*` 改为 `jakarta.*`，部署本项目会直接抛 `ClassNotFoundException: javax.servlet.Filter`。
>
> 本机已装的是 **9.0.123**，正确；但 `C:\Users\11270\Downloads\apache-tomcat-10.1.59-windows-x64` 那份是 **10.1.59**，属于不能用的一类，别误用。
>
> 若坚持用 Tomcat 10.1，则整套要迁到 Jakarta：Spring 6.x + MyBatis-Plus 3.5.9+ + `jakarta.servlet-api`，属于另一条技术路线，不在本项目范围内。

---

## 二、目录结构

```
Campus-Headlines/
├── pom.xml
├── README.md
├── .gitignore
├── docs/
│   └── database-design.md       # 数据库设计：E-R 图、数据字典、状态流转
└── src/
    ├── main/
    │   ├── java/com/campus/headlines/
    │   │   ├── controller/      # 控制层：接收请求、返回视图/JSON
    │   │   ├── service/         # 业务接口
    │   │   │   └── impl/        # 业务实现（@Service / @Transactional）
    │   │   ├── mapper/          # MyBatis-Plus Mapper 接口（6 个，已建）
    │   │   ├── entity/          # 数据库实体（6 个，已建）
    │   │   ├── aspect/          # AOP 切面（LogAspect：日志/耗时/异常）
    │   │   ├── config/          # JavaConfig（ObjectMapper、跨域等）
    │   │   ├── interceptor/     # 拦截器（LoginInterceptor 已建，未启用）
    │   │   ├── util/            # 工具类（Result、加密、上传…）
    │   │   ├── dto/             # 入参对象
    │   │   └── vo/              # 出参对象
    │   ├── resources/
    │   │   ├── spring/
    │   │   │   ├── applicationContext.xml   # 根容器：Service/Mapper/数据源/事务
    │   │   │   └── spring-mvc.xml           # MVC 容器：Controller/视图/上传/拦截器
    │   │   ├── properties/
    │   │   │   ├── jdbc.properties          # 数据库连接模板（需自己填）
│   │   │   └── upload.properties        # 上传目录、大小与扩展名白名单
    │   │   ├── mapper/                      # MyBatis XML（ArticleMapper.xml 动态 SQL）
    │   │   ├── sql/
    │   │   │   └── schema.sql               # 建表脚本（6 张表 + 外键 + 初始化栏目）
    │   │   └── log4j2.xml                   # 日志配置
    │   └── webapp/
    │       ├── index.jsp                    # 部署自检页
    │       ├── WEB-INF/
    │       │   ├── web.xml                  # ContextLoaderListener + DispatcherServlet
    │       │   └── views/                   # JSP 视图（前缀 /WEB-INF/views/）
    │       ├── static/{css,js,img}/         # 静态资源（映射 /static/**）
    │       └── upload/                      # 上传文件回显目录（映射 /upload/**）
    └── test/java/com/campus/headlines/
```

---

## 三、数据库设计

建表脚本：[`src/main/resources/sql/schema.sql`](src/main/resources/sql/schema.sql)（6 张表、7 个外键）

| 表 | 说明 |
| --- | --- |
| `user` | 用户，`role` 区分学生(0)与管理员(1) |
| `category` | 栏目分类 |
| `material` | 素材（图片 / 视频） |
| `article` | 资讯，`status` 走审核流转 |
| `article_material` | 资讯与素材的多对多关联 |
| `favorite` | 收藏，`(user_id, article_id)` 唯一 |

导入方式（PowerShell，需先有 `campus_headlines` 库）：

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
Get-Content .\src\main\resources\sql\schema.sql -Raw -Encoding UTF8 |
    & $mysql -u root -p --default-character-set=utf8mb4 campus_headlines
```

完整的 E-R 关系、Mermaid 图、资讯状态流转图与数据字典见
[`docs/database-design.md`](docs/database-design.md)。

---

## 三、本地运行步骤

### 1. 准备环境

本机现状（2026-10 实测确认）：

| 项 | 状态 |
| --- | --- |
| JDK 21.0.2 | ✅ 已装，`JAVA_HOME=D:\soft\Java\jdk-21` |
| MySQL 8.0.44 | ✅ 已装，服务 `MySQL80` 运行中，`campus_headlines` 库已建 |
| Maven | ⚠️ 未装独立版本，用 IDEA 2024.1 自带的 3.9.6 |
| Tomcat 9.0.123 | ✅ **已装并运行**，Windows 服务名 `Tomcat9`，端口 8080 |
| IDEA | ✅ 2024.1（`D:\Program Files\JetBrains\IntelliJ IDEA 2024.1`） |

Tomcat 的安装信息：

| 项 | 值 |
| --- | --- |
| 安装目录 | `D:\Program Files\Apache Software Foundation\Tomcat 9.0` |
| 安装包 | `C:\Users\11270\Downloads\apache-tomcat-9.0.123.exe`（Windows 服务版安装器） |
| 服务名 / 账户 | `Tomcat9` / `NT Authority\LocalService` |
| 启动类型 | **手动（Manual）** —— 开机不会自启，需 `net start Tomcat9` |
| 访问地址 | <http://localhost:8080/> |

> ⚠️ 安装目录的 ACL 只授予服务账户与管理员，普通用户会话读不了、也拷不进文件。
> **部署 war 必须用管理员权限的 PowerShell**（见「5. 部署到 Tomcat」）。
>
> 另外 `C:\Users\11270\Downloads\apache-tomcat-10.1.59-windows-x64` 是 **Tomcat 10.1**，
> Jakarta 命名空间，跑不了本项目，别用；留着或删掉都行。

所以环境已齐全，只剩可选地装一个独立 Maven（不装就用 IDEA 自带的，见「4. 构建」）。

### 2. 建库

> 本机 `campus_headlines` 库**已创建**（utf8mb4 / utf8mb4_0900_ai_ci），6 张表也已导入。
> 换机器或重建时才需要执行下面的语句。

```sql
CREATE DATABASE IF NOT EXISTS campus_headlines
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;   -- MySQL 8 默认；5.7 用 utf8mb4_general_ci
```

建表脚本导入：

```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
Get-Content .\src\main\resources\sql\schema.sql -Raw -Encoding UTF8 |
    & $mysql -u root -p --default-character-set=utf8mb4 campus_headlines
```

### 3. 配置数据库连接（密码不要进仓库）

配置拆成两个文件，**模板入库、真实密码不入库**：

| 文件 | 是否入库 | 用途 |
| --- | --- | --- |
| `properties/jdbc.properties` | ✅ 入库 | 通用模板，密码是占位符 |
| `properties/jdbc-local.properties` | ❌ 已 gitignore | 本地真实账号密码，覆盖模板（**本机已配好**） |

`applicationContext.xml` 里两个都会加载，后者覆盖前者，且后者不存在时也不报错：

```xml
<context:property-placeholder
    location="classpath:properties/jdbc.properties,classpath:properties/jdbc-local.properties"
    ignore-resource-not-found="true" ignore-unresolvable="true"/>
```

新建 `src/main/resources/properties/jdbc-local.properties`，只写需要覆盖的项：

```properties
jdbc.url=jdbc:mysql://localhost:3306/campus_headlines?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的密码
```

> ⚠️ 仓库是公开的，**不要把真实密码写进 `jdbc.properties` 再提交**。改了本地文件后 `git status` 若看见它，说明 gitignore 没生效，别 `git add`。
> 本机这个文件已经存在且已连通，**不要删除**，删了应用会退回模板占位符、连不上库。

`serverTimezone` 必须和 MySQL 时区一致（本机 `@@time_zone = SYSTEM`，即东八区，用 `Asia/Shanghai` 正确），否则查询时间会差 8 小时。

### 4. 构建

本机没装独立 Maven，直接借用 IDEA 2024.1 自带的 3.9.6（PowerShell）：

```powershell
$env:JAVA_HOME = "D:\soft\Java\jdk-21"
$mvn = "D:\Program Files\JetBrains\IntelliJ IDEA 2024.1\plugins\maven\lib\maven3\bin\mvn.cmd"
cd D:\Users\11270\Documents\deepseek-harness\default-workspace\Campus-Headlines
& $mvn clean package -DskipTests
```

装好独立 Maven 之后就是常规的：

```bash
mvn clean package -DskipTests
```

产物：`target/campus-headlines.war`

> 依赖下载慢就配阿里云镜像，`~/.m2/settings.xml` 内容见文末「常见问题」。

### 5. 部署到 Tomcat

本机的 Tomcat 9.0.123 是**安装成 Windows 服务**的，安装目录在 `D:\Program Files\...`，
ACL 只给服务账户和管理员，所以下面两种方式都要注意权限。

**方式 A：拷 war 包（需管理员 PowerShell）**

```powershell
# 以管理员身份打开 PowerShell，否则 Copy-Item 会 Access denied
Copy-Item "D:\Users\11270\Documents\deepseek-harness\default-workspace\Campus-Headlines\target\campus-headlines.war" `
          "D:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\" -Force
```

Tomcat 服务默认开启热部署，几秒后自动解压；没反应就重启服务：

```powershell
Restart-Service Tomcat9     # 或 net stop Tomcat9 && net start Tomcat9
```

**方式 B：IDEA 配置（开发期推荐，免拷包、改代码热更新）**

1. `File → Project Structure → Modules`，确认 Web Facet 的 Web Resource Directory 指向 `src/main/webapp`
2. `Run → Edit Configurations → + → Tomcat Server → Local`，Tomcat Home 选
   `D:\Program Files\Apache Software Foundation\Tomcat 9.0`
3. `Deployment` 页签 `+ → Artifact`，选 `campus-headlines:war exploded`
4. Application context 设为 `/campus-headlines`，端口与已运行的 `Tomcat9` 服务错开（例如 8081），
   或先 `Stop-Service Tomcat9` 释放 8080
5. 启动后访问 <http://localhost:8080/campus-headlines/>

> 若在 IDEA 里启动报端口占用，说明 `Tomcat9` 服务正在占着 8080，先停服务。

### 6. 验证

打开 <http://localhost:8080/campus-headlines/>，能看到 "校园头条 环境就绪" 页面即部署成功。

再看控制台/日志：
- 出现 `Root WebApplicationContext: initialization completed` → 根容器 OK
- 出现 `Initializing Spring DispatcherServlet 'dispatcher'` → MVC 容器 OK
- 无 `ClassNotFoundException` / `BeanCreationException` → 配置无误
- 若报 `Access denied for user` → `jdbc-local.properties` 缺失或密码不对

查看服务日志（服务方式运行时日志在安装目录，需管理员权限）：

```powershell
Get-Content "D:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\catalina.$(Get-Date -f yyyy-MM-dd).log" -Tail 50
```

---

## 四、配置要点说明

### 两个 Spring 容器

| 容器 | 配置文件 | 加载者 | 扫描范围 |
| --- | --- | --- | --- |
| 根容器 | `applicationContext.xml` | `ContextLoaderListener` | `com.campus.headlines.service` |
| MVC 容器 | `spring-mvc.xml` | `DispatcherServlet` | `com.campus.headlines.controller` |

子容器能拿到父容器的 Bean，反之不行。所以 **Controller 可以注入 Service，Service 不能注入 Controller**。如果把 Controller 也放进根容器扫描，会导致事务/拦截器异常，别这么干。

### 为什么 SqlSessionFactory 用 MybatisSqlSessionFactoryBean

MyBatis-Plus 的 `BaseMapper`、分页插件、逻辑删除都依赖它。若换成原生 `org.mybatis.spring.SqlSessionFactoryBean`，MP 的功能会静默失效（不报错，只是不生效）。

### 分页

已注册 `PaginationInnerInterceptor`（`DbType.MYSQL`），Service 里直接：

```java
Page<News> page = new Page<>(pageNum, pageSize);
newsMapper.selectPage(page, wrapper);
```

`maxLimit=500` 用于防止前端传超大 size。

### 文件上传

`multipartResolver` 已配置：单文件 10MB、单次请求 50MB、编码 UTF-8。Controller 里用 `MultipartFile` 接收：

```java
@PostMapping("/upload")
@ResponseBody
public String upload(MultipartFile file) { ... }
```

超过限制会抛 `MaxUploadSizeExceededException`，建议用 `@ControllerAdvice` 统一处理成友好提示。

### 静态资源

`DispatcherServlet` 映射了 `/`，所以 `/static/**` 必须在 `spring-mvc.xml` 里显式映射，否则 CSS/JS 会 404。新增静态目录时记得同步加 `<mvc:resources>`。

### 用户与登录（已实现）

**接口一览**（全部返回 `Result`：`{code, msg, data}`，HTTP 恒 200，靠 `code` 判断）

| 方法 | 路径 | 入参 | 说明 |
| --- | --- | --- | --- |
| POST | `/user/register` | JSON `{username, password, nickname}` | 注册，角色固定学生(0)、状态正常(1) |
| POST | `/user/login` | JSON `{username, password}` | 登录，成功后写 Session（key `loginUser`） |
| GET | `/user/info` | — | 当前登录用户，取 Session |
| POST | `/user/logout` | — | 销毁 Session |
| GET | `/login` `/register` | — | 登录页 / 注册页（JSP） |

**密码为什么用 BCrypt 而不是 MD5 + 盐**

| | MD5 + 盐 | BCrypt（本项目） |
| --- | --- | --- |
| 计算速度 | 极快（GPU 每秒上亿次） | 慢且可调（强度 10，单次约 50-100ms） |
| 盐管理 | 要多存一个 salt 字段，还要自己保证随机 | 盐自动生成并写进密文，**无需额外字段** |
| 抗暴力破解 | 密文泄露后可高速爆破 | 每次校验都有固定成本，爆破代价极高 |
| 现成实现 | 需自己拼装 | `BCryptPasswordEncoder`，`matches()` 直接比对 |

结论：MD5 属于“加密快、破解也快”的历史方案，课程作业里用它可以跑通，但报告里很难讲清安全性。BCrypt 只依赖 `spring-security-crypto` 一个 jar（**不引入 Spring Security 的过滤器链**，对现有 SSM 零侵入），`UserServiceImpl` 里只用到 `encode()` 与 `matches()` 两个方法。

库里存的就是 60 位密文，例如 `$2a$10$...`，长度和 `password varchar(100)` 匹配。

**登录拦截策略**

`LoginInterceptor` 已在 `spring-mvc.xml` 注册，采用「白名单列出要保护的路径」：

| 受保护 | `/article/publish`、`/article/draft`、`/article/mine`、`/material/**`、`/favorite/**`、`/admin/**` |
| --- | --- |
| 放行 | `/user/login`、`/user/register`、`/login`、`/register`、`/article/list`、`/article/detail/**`、`/static/**`、`/upload/**` |

未登录时的两种响应：

- **AJAX**（带 `X-Requested-With: XMLHttpRequest`，或 `Accept: application/json`）→ `{"code":401,"msg":"请先登录"}`
- **页面请求** → 302 重定向到 `/login`

Session 超时在 `web.xml` 设 30 分钟。

> ⚠️ `<mvc:interceptors>` 里至少要有一个 `<mvc:interceptor>` 子元素，空标签会让 Spring 启动直接抛
> `cvc-complex-type.2.4.b: … 'mvc:interceptors' 的内容不完整`，整个应用起不来。

### 素材上传与管理（已实现）

**接口一览**

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| POST | `/material/upload` | `multipart/form-data`，字段名 `file` | 落盘 + 写 `material` 表 |
| GET | `/material/list` | — | 当前用户的素材，按上传时间倒序 |
| DELETE | `/material/{id}` | — | 删除（校验归属 + 是否被引用） |
| GET | `/material` | — | 素材管理页 |

**存储结构**

```
D:/campus-upload/202610/afacfbbbd66d47b38658e4713a4931e0.png   ← 磁盘（upload.base-path）
                             ↕
/upload/202610/afacfbbbd66d47b38658e4713a4931e0.png            ← 对外地址（upload.url-prefix）
```

磁盘目录刻意放在 **webapp 之外**，war 重新部署不会清空用户文件；访问由
`<mvc:resources mapping="/upload/**" location="file:${upload.base-path}/"/>` 提供。
路径与大小限制都在 [`properties/upload.properties`](src/main/resources/properties/upload.properties)：

```properties
upload.base-path=D:/campus-upload
upload.url-prefix=/upload
upload.max-image-size=10485760     # 图片 10MB
upload.max-video-size=52428800     # 视频 50MB
upload.image-ext=jpg,jpeg,png,gif,webp
upload.video-ext=mp4,webm,ogg
```

**安全设计（前端 accept 只是体验，真正的防线在服务端）**

| 风险 | 处理 |
| --- | --- |
| 上传可执行文件（.jsp/.sh） | 扩展名白名单，不在名单内直接拒绝 |
| 中文名、空格、`../` 路径穿越 | **落盘文件名一律用 UUID**，原始名只入库展示 |
| 超大文件打爆磁盘 | 双重校验：`MultipartResolver`（请求级）+ `FileUploadUtil`（按类型：图片 10MB / 视频 50MB） |
| 越权删除他人素材 | `delete(id, userId)` 校验 `material.user_id` |
| 删除被引用的素材导致资讯缺图 | 删除前查 `article_material`，被引用则拒绝并提示引用数 |

> ⚠️ **上传大小上限已调整**：`MultipartResolver` 从 10MB/50MB 放宽到 **单文件 50MB / 单请求 100MB**，
> 否则 mp4 基本传不上去。原来 10MB 的限制改由 `FileUploadUtil` 按图片类型执行，视频单独 50MB。

**删除策略：物理删除（不是逻辑删除）**

素材是用户自己的中间产物、可重新上传；磁盘只增不减会很快吃满，而逻辑删除要加状态字段、
文件还照样占空间。为防误删，删除前会检查是否被资讯引用，被引用时提示
「该素材已被 N 篇资讯引用，请先在资讯中移除后再删除」。

**发布页的素材关联**

[`article/publish.jsp`](src/main/webapp/WEB-INF/views/article/publish.jsp) 里有两个选择器：
封面图（单选）和内嵌素材（多选），数据都来自 `/material/list`。提交时
`POST /article/publish`（提交审核）或 `/article/draft`（存草稿），
`ArticleServiceImpl` 会把 `materialIds` **先删后插**写入 `article_material`；
封面选中的素材也会自动计入关联表，避免“封面图不在素材列表里”的孤儿引用。

### 资讯发布与审核（已实现）

**状态机（整个项目的核心，只允许这四条边）**

```
  [0 草稿] ──submitForReview──> [1 待审核] ──audit(pass=1)──> [2 已通过]（写 publish_time，前台可见）
                                      └──── audit(pass=0)──> [3 已驳回]（写 reject_reason）
  [3 已驳回] ──saveDraft（编辑保存）──> [0 草稿] ──submitForReview──> [1 待审核]
```

| 状态 | 谁可以看到 | 谁能改 |
| --- | --- | --- |
| 0 草稿 | 仅作者 | 作者（编辑） |
| 1 待审核 | 仅作者 + 管理员 | 都不能改（等审核） |
| 2 已通过 | 所有人 | 都不能直接改 |
| 3 已驳回 | 仅作者 | 作者（编辑后回到草稿） |

**接口**

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/article/draft` | 保存草稿（新建或更新），`@Transactional` 同时写 article 与 article_material |
| POST | `/article/submit/{id}` | 提交审核，0 → 1 |
| POST | `/admin/article/audit` | 审核，body `{articleId, pass, rejectReason}` |
| GET | `/article/list` | 公开列表，参数 `categoryId` / `keyword` / `page` / `size` |
| GET | `/article/detail/{id}` | 详情页（服务端渲染），浏览量 +1 |
| GET | `/article/my`、`/article/my/list` | 我的文章（页面 / 数据） |
| GET | `/admin/article/pending` | 待审核列表 |

**页面**

| 路径 | JSP |
| --- | --- |
| `/` | `views/index.jsp` 首页卡片列表 + 栏目筛选 + 搜索 |
| `/article/detail/{id}` | `views/article/detail.jsp` 正文 + 内嵌素材 + 收藏 |
| `/article/publish`（可带 `?id=`） | `views/article/publish.jsp` 新建/编辑，封面与内嵌素材选择 |
| `/article/my` | `views/article/my.jsp` 状态标签 + 驳回原因 + 编辑/提交 |
| `/admin/article/page` | `views/admin/audit.jsp` 通过 / 驳回 |

**三个关键实现点**

1. **防重复审核靠条件更新，不是先查后改**

   ```sql
   UPDATE article SET status = 2, publish_time = NOW()
    WHERE id = ? AND status = 1        -- 只在“待审核”时生效
   ```

   两个人同时点“通过”时，第二次影响 0 行，于是拿到「该资讯不在待审核状态，可能已被处理」。
   如果写成“先 select 判断 status==1 再 update”，并发下会重复改状态、重复写发布时间。

2. **学生无法把自己写成“已通过”**

   `saveDraft` 只接受 `id == null` 或状态为 0/3 的稿件，且强制 `status = 0`；
   `submitForReview` 只把 0 改成 1；只有 `audit`（校验调用者 role=1）能写 2。
   请求体里根本没有 `status` 字段，前端传什么都没用。

3. **浏览量原子自增**

   ```java
   update ... setSql("view_count = view_count + 1")
   ```

   而不是“查出来 +1 再写回”，避免并发下互相覆盖、计数丢失。

**权限是双保险**：`AdminInterceptor` 拦 `/admin/**` 做粗筛（非管理员 403），
`audit()` 里再查一次数据库的 `role`——Session 里的角色是登录时的快照，
用户被降权后 Session 不会立刻失效，服务层才是最后防线。

### MyBatis 动态 SQL 与 Spring AOP

#### 1. 手写动态 SQL（`resources/mapper/ArticleMapper.xml`）

接口方法：

```java
List<Article> searchArticles(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword,
                             @Param("status") Integer status);
```

XML 用 `<where>` + `<if>`，三个条件都可缺省：

```xml
<select id="searchArticles" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM article
    <where>
        <if test="categoryId != null">
            AND category_id = #{categoryId}
        </if>
        <if test="keyword != null and keyword != ''">
            AND (title LIKE CONCAT('%', #{keyword}, '%')
                 OR content LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
    ORDER BY is_top DESC, publish_time DESC, id DESC
</select>
```

**`<where>` 的价值**在实测日志里能直接看到——它自动处理了「第一个 AND」和「全空」两种情况：

| 传入参数 | 实际生成的 WHERE |
| --- | --- |
| `keyword=图书馆, status=2` | `WHERE (title LIKE ... OR content LIKE ...) AND status = ?` |
| `categoryId=2, status=2` | `WHERE category_id = ? AND status = ?` |
| 全空（接口强制 status=2） | `WHERE status = ?` |

如果手写字符串拼接，前两种要分别判断“是不是第一个条件”，第三种还要判断要不要加 `WHERE` 关键字；
用 `<where>` 后只写 `AND`，由框架决定去掉哪一个。

> 安全说明：该接口对非管理员会把 `status` 强制成 2（已通过），否则它就成了读取草稿/待审核内容的越权入口。

#### 2. 日志切面（`aspect/LogAspect.java`）

```java
@Pointcut("execution(* com.campus.headlines.service..*.*(..))")
public void serviceMethods() {}
```

三条通知：`@Before` 记方法名与参数、`@AfterReturning` 记耗时与返回值、`@AfterThrowing` 记异常类型与消息。
业务代码里**没有一行日志语句**，加日志不用改 20 个类。

实测日志（真实运行输出）：

```
[AOP-前] 调用 ArticleServiceImpl.listPublished() 入参=[null, null, 1, 10]
[AOP-后] 完成 ArticleServiceImpl.listPublished() 耗时=308ms 返回=PageVO{page=1, size=10, total=2, records=2}
[AOP-前] 调用 ArticleServiceImpl.searchArticles() 入参=[1, null, 2]
[AOP-后] 完成 ArticleServiceImpl.searchArticles() 耗时=8ms 返回=ArrayList(size=1)
[AOP-异常] ArticleServiceImpl.getDetail() 耗时=7ms 异常=BusinessException 消息=资讯不存在或未通过审核
```

几个不显眼但重要的实现细节：

| 细节 | 原因 |
| --- | --- |
| 耗时用 `Deque<Long>` 而不是单个 `Long` | service 之间会互相调用（ArticleService 调 CategoryService/UserService），单值 ThreadLocal 会被内层覆盖，栈才能正确配对 |
| 参数与返回值截断到 200 字符 | 否则一条日志可能刷几十行，反而看不清 |
| DTO 只打类名 `RegisterDTO{…}` | 注册/登录 DTO 含密码，日志是最容易泄漏凭据的地方 |
| `MultipartFile` 只打文件名与大小 | 避免把文件内容写进日志 |
| `PageVO` 重写 `toString()` | 不然日志里只有 `PageVO@3d00673e`，看不出分页信息 |

#### 3. `<aop:aspectj-autoproxy/>` 必须显式声明

```xml
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

**`<tx:annotation-driven/>` 不会让 `@Aspect` 生效**——它只注册事务相关的 Advisor 与代理创建器，
和 AspectJ 自动代理是两个独立机制。两行都写时，Spring 会把事务 Advisor 与切面 Advisor 织入同一个代理对象，
不会出现“代理套代理”。

`proxy-target-class="true"` 与 `tx:annotation-driven` 保持一致（CGLIB）：
若用默认的 JDK 动态代理，Controller 里注入的类型必须写成接口（`ArticleService`），
写成实现类（`ArticleServiceImpl`）会注入失败。

切面所在的 `com.campus.headlines.aspect` 包必须纳入**根容器**的 component-scan，
因为 service 的代理由根容器创建。

### 两个必踩的坑（已处理）

1. **`@Resource` 在 JDK 11+ 编译不过**：`javax.annotation` 包已从 JDK 移除，必须显式引入
   `javax.annotation:javax.annotation-api:1.3.2`，否则报“程序包 javax.annotation 不存在”，
   更隐蔽的是运行期注入会静默失效。
2. **`LocalDateTime` 默认被序列化成数组**：不注册 `JavaTimeModule` 时 JSON 里是
   `[2026,10,26,19,28,41]`。已通过 `config/JacksonConfig` 统一成 `yyyy-MM-dd HH:mm:ss`，
   并在 `spring-mvc.xml` 的 JSON 转换器里引用该 `ObjectMapper`。

---

## 五、常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 启动报 `ClassNotFoundException: javax.servlet.Filter` | 用了 Tomcat 10+。换 Tomcat 9，或整体升级到 Spring 6 + jakarta |
| 拷 war 到 webapps 报 `Access denied` / 拒绝访问 | 安装目录 ACL 只给服务账户与管理员，用**管理员** PowerShell |
| `localhost:8080` 打不开 | 服务是**手动**启动类型，执行 `net start Tomcat9` |
| 中文乱码 | 检查 `web.xml` 的 `CharacterEncodingFilter`、响应 `StringHttpMessageConverter` 是否为 UTF-8，以及 MySQL 库/表是否为 `utf8mb4` |
| 时间差 8 小时 | `jdbc.url` 的 `serverTimezone` 与 MySQL 时区不一致 |
| 启动报 `Access denied for user` | `jdbc-local.properties` 缺失或账号密码不对，或 MySQL 未启动 |
| 启动报 `Public Key Retrieval is not allowed` | URL 缺 `allowPublicKeyRetrieval=true`（MySQL 8 + `caching_sha2_password`） |
| 访问页面 404 | war 名 / Application context 与访问路径不一致，注意 `/campus-headlines/` 前缀 |
| CSS/JS 404 | 未配置 `<mvc:resources>`，或路径没带 `/static/` 前缀 |
| JSP 里 `${}` 不解析 | 视图解析器未用 `JstlView`，或页面缺 JSTL taglib 声明 |
| `mvn package` 下载依赖超时 | 在 `~/.m2/settings.xml` 配阿里云镜像 |

阿里云镜像配置（`~/.m2/settings.xml`）：

```xml
<mirrors>
    <mirror>
        <id>aliyun</id>
        <mirrorOf>central</mirrorOf>
        <name>aliyun maven</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

---

## 六、开发进度

- [x] 建表：user / category / material / article / article_material / favorite（`resources/sql/schema.sql`）
- [x] 数据设计文档：E-R 图、状态流转、数据字典（`docs/database-design.md`）
- [x] entity（6 个）+ mapper（6 个，继承 BaseMapper）
- [x] Service 层与事务（User/Category/Material/Article，`@Transactional`）
- [x] 登录/注册（Session + BCrypt），启用 `LoginInterceptor`
- [x] 素材上传（图片/视频，落 `material` 表 + `/upload/**` 回显）
- [x] 资讯发布（草稿/提交审核，封面与内嵌素材关联 article_material）
- [x] 管理员审核（待审核 → 通过/驳回，`@Transactional` + 条件更新）
- [x] 首页列表分页 + 详情页 + 浏览量原子自增
- [x] 栏目筛选、收藏/取消收藏
- [x] JSP 视图与静态资源样式（首页/详情/发布/我的/审核）
- [x] 统一返回体 `Result` + 全局异常处理 `@RestControllerAdvice`
- [x] MyBatis 手写动态 SQL（ArticleMapper.xml 的 <where> + <if>）
- [x] Spring AOP 日志切面（@Aspect + <aop:aspectj-autoproxy/>）
