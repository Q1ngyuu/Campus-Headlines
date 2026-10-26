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
| Web 容器 | **Tomcat 9.0.x** | ⚠️ 必须是 9.0.x，见下方说明 |

> **为什么是 Java 21**：本机只有 JDK 21（另有 11.0.14 和 1.8.0_472 备用），`JAVA_HOME` 指向 21，所以直接以 21 为目标，不搞「用 21 编 8」的将就方案。Spring 5.3.30 起补充了对 JDK 21 的支持，5.3.39 是这条线的最终版，配合上调后的 AspectJ 1.9.22.1 / Log4j2 2.24.3 / Jackson 2.17.3 / Druid 1.2.24 在 JDK 21 上没有已知坑。
>
> 要退回更低版本（例如课程要求 JDK 8）：只改 `pom.xml` 里 `<maven.compiler.release>` 一处即可，Spring 5.3.39 本身覆盖 8–21，不必换依赖。

> ⚠️ **Tomcat 10 / 11 不能直接用**：Tomcat 10 起 servlet API 从 `javax.*` 改为 `jakarta.*`，部署本项目会直接抛 `ClassNotFoundException: javax.servlet.Filter`。本机 `C:\Users\11270\Downloads\apache-tomcat-10.1.59-windows-x64` 是 **10.1.59**，属于这一类，**请另下 Tomcat 9.0.x**（建议 9.0.10x 最新版，支持 JDK 21）。
>
> 若坚持用 Tomcat 10.1，则整套要迁到 Jakarta：Spring 6.x + MyBatis-Plus 3.5.9+ + `jakarta.servlet-api`，属于另一条技术路线，不在本项目范围内。

---

## 二、目录结构

```
Campus-Headlines/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/campus/headlines/
    │   │   ├── controller/      # 控制层：接收请求、返回视图/JSON
    │   │   ├── service/         # 业务接口
    │   │   │   └── impl/        # 业务实现（@Service / @Transactional）
    │   │   ├── mapper/          # MyBatis-Plus Mapper 接口
    │   │   ├── entity/          # 数据库实体
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
    │   │   │   └── jdbc.properties          # 数据库连接模板（需自己填）
    │   │   ├── mapper/                      # MyBatis XML（classpath:mapper/**/*.xml）
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

## 三、本地运行步骤

### 1. 准备环境

本机现状（2026-10 体检）：

| 项 | 状态 |
| --- | --- |
| JDK 21.0.2 | ✅ 已装，`JAVA_HOME=D:\soft\Java\jdk-21` |
| MySQL 8.0.44 | ✅ 已装，服务 `MySQL80` 正在运行 |
| Maven | ⚠️ 未装独立版本，用 IDEA 2024.1 自带的 3.9.6 |
| Tomcat 9 | ❌ 未装（只有 10.1.59，**不能用**） |
| IDEA | ✅ 2024.1（`D:\Program Files\JetBrains\IntelliJ IDEA 2024.1`） |

所以只剩两步要补：**下载 Tomcat 9.0.x**（解压即用），以及可选地装独立 Maven。

Tomcat 9 下载地址：<https://dlcdn.apache.org/tomcat/tomcat-9/>（选最新 9.0.x 的 `windows-x64.zip`）。
拿到后解压到例如 `D:\soft\apache-tomcat-9.0.xx`，解压即用，无需安装、无需配环境变量。

### 2. 建库

```sql
CREATE DATABASE IF NOT EXISTS campus_headlines
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;
```

> 业务表在后续步骤中创建；现在库是空的也能正常启动（MyBatis 启动时不校验表是否存在）。

### 3. 配置数据库连接

编辑 `src/main/resources/properties/jdbc.properties`，把 `jdbc.username` / `jdbc.password` 改成自己的：

```properties
jdbc.url=jdbc:mysql://localhost:3306/campus_headlines?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
jdbc.username=root
jdbc.password=你的密码
```

`serverTimezone` 必须和 MySQL 实际时区一致，否则查询时间会差 8 小时。

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

**方式 A：拷 war 包（最快）**

```bash
cp target/campus-headlines.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh      # Windows: startup.bat
```

**方式 B：IDEA 配置**

1. `File → Project Structure → Modules`，确认 Web Facet 的 Web Resource Directory 指向 `src/main/webapp`
2. `Run → Edit Configurations → + → Tomcat Server → Local`，选择本机 Tomcat 9
3. `Deployment` 页签 `+ → Artifact`，选 `campus-headlines:war exploded`
4. Application context 设为 `/campus-headlines`
5. 启动后访问 <http://localhost:8080/campus-headlines/>

### 6. 验证

打开 <http://localhost:8080/campus-headlines/>，能看到 "校园头条 环境就绪" 页面即部署成功。

再看控制台日志：
- 出现 `Root WebApplicationContext: initialization completed` → 根容器 OK
- 出现 `Initializing Spring DispatcherServlet 'dispatcher'` → MVC 容器 OK
- 无 `ClassNotFoundException` / `BeanCreationException` → 配置无误

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

### 登录拦截器

`LoginInterceptor` 已创建但**尚未启用**（`spring-mvc.xml` 中整段注释）。等登录功能完成后，放开 XML 里的 `<mvc:interceptors>` 注释，并把 `preHandle` 中的校验逻辑解除注释即可。

> ⚠️ 别只注释掉内部的 `<mvc:interceptor>` 而留下空的 `<mvc:interceptors></mvc:interceptors>`：该标签的 XSD 要求至少一个子元素，空标签会让 Spring 在启动时直接抛
> `cvc-complex-type.2.4.b: … 'mvc:interceptors' 的内容不完整`，整个应用起不来。

---

## 五、常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 启动报 `ClassNotFoundException: javax.servlet.Filter` | 用了 Tomcat 10+。换 Tomcat 9，或整体升级到 Spring 6 + jakarta |
| 中文乱码 | 检查 `web.xml` 的 `CharacterEncodingFilter`、响应 `StringHttpMessageConverter` 是否为 UTF-8，以及 MySQL 库/表是否为 `utf8mb4` |
| 时间差 8 小时 | `jdbc.url` 的 `serverTimezone` 与 MySQL 时区不一致 |
| 启动报 `Access denied for user` | `jdbc.properties` 账号密码不对，或 MySQL 未启动 |
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

## 六、后续开发计划

- [ ] 建表：用户、新闻、分类、评论、点赞（SQL 脚本放 `src/main/resources/sql/`）
- [ ] entity + mapper + mapper.xml
- [ ] Service 层与事务
- [ ] 登录/注册（Session + 密码加密），启用 `LoginInterceptor`
- [ ] 新闻列表分页、详情、发布、编辑、删除
- [ ] 图片上传（`/upload/**` 回显）
- [ ] JSP 视图与静态资源样式
- [ ] 统一返回体 `Result` + 全局异常处理 `@ControllerAdvice`
