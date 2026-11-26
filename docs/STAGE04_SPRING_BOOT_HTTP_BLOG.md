# Java 入门第四阶段：从 HTTP 到 Spring Boot，完成教师管理接口（Day31～Day40）

前三个阶段，我们学习了 Java 语法、面向对象、常用类库、Maven、测试、SQL 和 JDBC，能够在控制台运行程序并操作数据库。

接下来要解决一个新的问题：如何让浏览器或其他客户端，通过 HTTP 调用我们的 Java 代码？

第四阶段以教师管理为主线，从 HTTP 协议出发，逐步学习 Spring 容器、Spring Boot、参数绑定、DTO、校验、异常处理、AOP 和 Web 测试，最终完成新增、查询、更新教师的接口闭环。

**源码地址：[https://github.com/qishuixian/javaProject](https://github.com/qishuixian/javaProject)**

本文依据项目中的 Day31～Day40 文档与示例整理。代码采用 Java 8，Spring Boot 示例使用 2.3.10.RELEASE，IoC 示例使用 Spring Framework 5.2.14.RELEASE。这些版本用于学习现有项目，不作为新生产项目的版本选型建议。

## 一、阶段目标与工程结构

| 天数 | 主题 | 需要掌握的能力 |
|---|---|---|
| Day31 | HTTP 与接口契约 | 看懂请求、响应与状态码 |
| Day32 | IoC 与依赖注入 | 理解对象由谁创建、依赖怎样传入 |
| Day33 | Spring Boot 与配置 | 启动服务，切换 profile 和端口 |
| Day34 | MVC 参数绑定 | 接收路径、查询参数与 JSON 请求体 |
| Day35 | DTO 与 Entity | 划分请求、内部数据和响应边界 |
| Day36 | 参数校验 | 拒绝缺失、空白、超长等非法输入 |
| Day37 | 统一异常处理 | 返回一致的错误协议并保留诊断信息 |
| Day38 | AOP 与代理 | 理解切面何时执行、何时被绕过 |
| Day39 | MockMvc 测试 | 自动检查状态码、JSON 和错误分支 |
| Day40 | HTTP 综合练习 | 完成教师新增、查询、更新闭环 |

本阶段相关文件分布如下：

```text
javaProject/
├── java-basics/src/Day31HttpContract.java
├── spring-ioc-lab/
│   ├── pom.xml
│   └── src/main/java/learning/day32/Day32Injection.java
├── spring-boot-lab/
│   ├── pom.xml
│   ├── src/main/java/learning/boot/
│   │   ├── Day33Application.java
│   │   ├── Day34BindingController.java
│   │   └── day35/ ... day40/
│   ├── src/main/resources/
│   ├── src/test/java/learning/boot/
│   └── requests/day40.http
└── docs/
    └── DAY31_... 至 DAY40_...
```

Day33～Day40 共用一个 Spring Boot 启动入口。类名叫 `Day33Application`，不代表它只能运行 Day33 的代码。

本阶段先用内存仓库学习 Web 分层，不直接复用上一阶段的 JDBC 仓库。这样可以分别理解 HTTP 调用链和数据库访问，后续再组合。

## 二、Day31：先理解 HTTP，再学习注解

### 1. 一次请求由什么组成？

```http
GET /teachers?id=10001 HTTP/1.1
```

可以拆成：

| 内容 | 含义 |
|---|---|
| GET | 请求方法 |
| /teachers | 资源路径 |
| id=10001 | 查询参数 |
| HTTP/1.1 | 协议版本 |

响应则包含状态码、响应头和响应体：

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{"data":{"id":10001,"name":"张老师"}}
```

`Content-Type` 描述内容的媒体类型，响应体中的 JSON 承载业务数据。

### 2. 状态码与业务码

| 状态码 | 本阶段中的典型含义 |
|---|---|
| 200 | 查询或更新成功 |
| 201 | 创建成功 |
| 400 | 参数无法转换、JSON 错误或字段校验失败 |
| 404 | 教师不存在，或请求的路由不存在 |
| 405 | 路径不支持当前请求方法 |
| 415 | 请求媒体类型不受支持 |
| 500 | 未预期的服务端错误 |

HTTP 状态码表达请求结果，JSON 中的业务码进一步区分错误原因。例如同为 400，可以是 `INVALID_BODY`，也可以是 `VALIDATION_ERROR`。

客户端应按协议判断状态和业务码，不要依赖某句中文提示是否完全一致。

### 3. 教学入口的边界

Day31 使用 JDK 自带的 `HttpServer`，观察请求方法、URI、响应头和响应体的处理方式。示例使用随机端口，启动后立即停止，不是一个供浏览器持续访问的服务。

因此，看到程序打印接口说明，不等于客户端已经真正完成请求。后续 Spring Boot 示例才提供持续运行的服务。

## 三、Day32：IoC 与依赖注入到底是什么？

假设 `TeacherService` 需要调用仓库查询教师。

如果在业务类内部直接创建实现：

```java
private final TeacherRepository repository = new MemoryTeacherRepository();
```

业务类就决定了使用哪一种仓库，替换实现时需要修改它。

### 1. 先理解构造器注入

```java
public TeacherService(TeacherRepository repository) {
    this.repository = repository;
}
```

外部负责组装：

```java
TeacherService service = new TeacherService(new MemoryTeacherRepository());
```

这已经是依赖注入：把需要的对象从外部传进来，不需要先使用 Spring 注解。

### 2. 让 Spring 管理对象

Day32 使用配置类注册 Bean，核心写法如下：

```java
@Configuration
public static class AppConfig {
    @Bean
    public TeacherRepository teacherRepository() {
        return new MemoryTeacherRepository();
    }

    @Bean
    public TeacherService teacherService(TeacherRepository repository) {
        return new TeacherService(repository);
    }
}
```

- `@Configuration`：声明配置类。
- `@Bean`：把方法返回的对象交给 Spring 管理。
- 方法参数：Spring 根据类型提供依赖对象。

三个概念可以这样理解：

| 概念 | 含义 |
|---|---|
| DI | 从外部传入依赖对象 |
| IoC | 将对象创建和组装等控制权交给外部容器 |
| Bean | 由 Spring 容器管理的对象 |

`new` 没有消失，只是对象创建集中在了组装位置。

### 3. 默认单例不等于线程安全

同一容器中的同一 Bean 定义，默认使用 singleton 作用域。多次获取通常得到同一个实例。

这不表示全 JVM 永远只有一个实例，也不保证对象线程安全。Controller、Service 可能被多个请求共享，不应随意把某次请求的姓名、ID 存入共享字段。

## 四、Day33：启动 Spring Boot 服务

### 1. 启动入口

```java
@SpringBootApplication
public class Day33Application {
    public static void main(String[] args) {
        SpringApplication.run(Day33Application.class, args);
    }
}
```

`@SpringBootApplication` 组合了配置、自动配置与组件扫描等能力。默认组件扫描从启动类所在包开始，覆盖它的子包。

本项目启动类位于 `learning.boot`，后续 `day35`～`day40` 都在其子包中，因此能够被扫描。

Web starter 配合自动配置，会启动内嵌 Web 服务器。程序启动后持续等待请求，不会像普通打印示例一样立即退出。

### 2. 基础配置与 profile

项目提供三份配置：

| 文件 | 端口 | 示例环境 |
|---|---:|---|
| application.properties | 8080 | base |
| application-local.properties | 8081 | local |
| application-test.properties | 8082 | test |

基础配置中的服务地址是 `127.0.0.1`。激活 local 后，读取公共配置，并由 local 配置覆盖同名属性。

在 IDEA 运行配置的 Program arguments 中填写：

```text
--spring.profiles.active=local --server.port=8081
```

这里的 `--server.port=8081` 是命令行属性，能够覆盖配置文件中的端口设置。

### 3. 构建与运行

在仓库根目录打开 PowerShell：

```powershell
Set-Location .\spring-boot-lab
mvn clean package
java -jar target/spring-boot-lab-1.0-SNAPSHOT.jar --spring.profiles.active=local --server.port=8081
```

本模块配置了 Spring Boot Maven 插件，打包结果可以通过 `java -jar` 启动；这与未配置入口和依赖打包的普通 Jar 不同。

也可以在 IDEA 导入本模块的 POM，选择 JDK 8，直接运行 `Day33Application.main()`。

启动后访问：[http://127.0.0.1:8081/api/lesson](http://127.0.0.1:8081/api/lesson)。访问没有配置的根路径 `/` 得到 404，并不意味着启动失败。

## 五、Day34：HTTP 参数怎样进入 Java 方法？

参数绑定负责从 HTTP 请求中读取数据，并转换为 Java 参数。

| 参数位置 | 示例 | 注解 |
|---|---|---|
| 路径 | /teachers/10001 | @PathVariable |
| 查询字符串 | /teachers?page=2 | @RequestParam |
| JSON 请求体 | {"name":"张老师","age":28} | @RequestBody |

### 1. 路径参数

```java
@GetMapping("/teachers/{id}")
public Map<String, Object> teacher(@PathVariable("id") long id) {
    // 使用已经转换为 long 的 id
}
```

以上为方法结构片段，完整返回逻辑见 `Day34BindingController`。

传入 `10001` 可以转换；传入 `abc` 无法转换为 long，会返回 400，业务方法不会正常执行。

### 2. 查询参数

```java
@RequestParam(value = "page", defaultValue = "1") int page
```

未传或传入空字符串时使用默认值，再转换为整数。

需要区别：

- `page=abc`：类型转换失败。
- `page=0`：类型转换成功，但不符合页码规则。

**绑定成功只说明数据能转换，不代表数据符合业务要求。**

### 3. JSON 请求体

```java
@RequestBody TeacherPreviewRequest request
```

Spring 使用消息转换器读取请求体，项目中的 Jackson 将 JSON 反序列化为请求对象。返回对象时，再序列化为 JSON。

```json
{"name":"张老师","age":28}
```

这里无需再包装一层 `request`。`request` 是 Java 参数名，不是 JSON 协议要求的字段。

Day34 的 preview 接口只回显参数，`saved:false` 表示没有保存。发送 POST 本身并不保证发生数据写入，最终行为取决于接口实现。

## 六、Day35：为什么分 Request、Entity 和 Response？

前端提交的信息、服务端内部数据和返回给客户端的数据，往往不是同一组字段。

| 类型 | 示例字段 | 职责 |
|---|---|---|
| TeacherCreateRequest | name、subject | 接收客户端可提交的内容 |
| TeacherEntity | id、name、subject、internalRemark | 表达内部数据 |
| TeacherResponse | id、name、subject | 定义对外响应 |

Request 和 Response 都可以是 DTO，即数据传输对象。DTO 不是 Java 关键字，只是一种职责划分。

### 1. 显式转换响应

```java
return new TeacherResponse(
        entity.getId(),
        entity.getName(),
        entity.getSubject());
```

转换时没有取 `internalRemark`，响应对象也没有这个字段，所以它不会出现在 JSON 中。

这不是强制类型转换，不能用 `(TeacherResponse) entity` 代替。

### 2. Entity 不会自动持久化

类名叫 `TeacherEntity`，并不意味着已经创建数据库表或保存数据。Day35 仍使用模拟数据，preview 返回的 id 为 null，不代表新教师已经创建。

分层后的调用关系是：

```text
Controller 接收请求
    → Service 组织业务
    → Entity 表达内部数据
    → Converter 选择响应字段
    → Response 转成 JSON
```

这种边界也限制了客户端可以修改哪些字段。后续 Day40 的 ID 由服务端生成，内部备注也由服务端管理。

## 七、Day36：用校验注解拒绝非法输入

`@RequestBody` 只负责读取和转换。要限制姓名不能为空、长度不能超标，还需要校验规则。

项目中的请求类包含：

```java
@NotBlank(message = "姓名不能为空或全是空格")
@Size(max = 20, message = "姓名长度不能超过20")
private String name;

@NotBlank(message = "科目不能为空或全是空格")
@Size(max = 30, message = "科目长度不能超过30")
private String subject;
```

Controller 参数使用：

```java
@Valid @RequestBody ValidatedTeacherRequest request
```

在本项目中，`@Valid` 使用 `javax.validation.Valid`，不是较新 Spring Boot 版本中的 Jakarta 包名。

### 1. 常用规则

| 注解 | 作用 |
|---|---|
| @NotNull | 不允许 null |
| @NotBlank | 字符串不能是 null、空字符串或纯空白 |
| @Size | 限制字符串长度或集合大小 |
| @Min / @Max | 限制数值范围 |

`@Size` 本身允许 null，因此必填字符串通常要和 `@NotBlank` 配合。

### 2. 校验不等于清洗

校验负责判断是否合法，不会因为加了 `@NotBlank` 就自动对字符串执行 `trim()`。

还需要区分字段规则与业务规则：

- 姓名为空：请求字段校验。
- 教师是否存在：业务查询。
- 当前用户是否有权修改：权限规则。

不要把所有业务判断都塞进请求 DTO。

## 八、Day37：统一异常处理与错误协议

每个 Controller 都重复写 try/catch，容易让错误格式不一致。项目使用 `@RestControllerAdvice` 集中转换异常。

当前 `Day37ExceptionAdvice` 的范围覆盖 Day37 和 Day40 Controller，并非自动接管所有历史课程接口。

### 1. 明确错误分类

| 场景 | HTTP 状态 | 业务码 |
|---|---:|---|
| 字段校验失败 | 400 | VALIDATION_ERROR |
| 请求体缺失或 JSON 无法读取 | 400 | INVALID_BODY |
| 路径参数无法转换 | 400 | INVALID_PARAMETER |
| 教师不存在 | 404 | TEACHER_NOT_FOUND |
| 未预期异常 | 500 | INTERNAL_ERROR |

统一错误响应包含 `code`、`message`、`requestId` 和 `errors`。例如校验失败时的响应结构可以是：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "requestId": "本次错误对应的标识",
  "errors": [
    {"field": "name", "message": "姓名不能为空或全是空格"}
  ]
}
```

### 2. 未知异常应保留诊断信息

对外返回稳定的提示，对内记录完整异常堆栈，并带上相同的 requestId，方便关联错误响应与日志。

本例的 requestId 在错误处理时生成，并不等于已经建立了贯穿所有服务的分布式追踪系统。

统一处理也不意味着把全部异常改成 500。参数错误、请求方法错误等，应保留对应 HTTP 语义。

## 九、Day38：AOP 的关键是调用是否经过代理

日志和方法计时可能在多个业务方法中重复。AOP 可以把这些横切逻辑集中管理。

项目使用 `@Timed` 标记需要计时的方法，切面中的核心代码如下：

```java
@Around("@annotation(learning.boot.day38.Timed)")
public Object measure(ProceedingJoinPoint point) throws Throwable {
    long start = System.nanoTime();
    boolean success = false;
    try {
        Object result = point.proceed();
        success = true;
        return result;
    } finally {
        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
        LOG.info("DAY38_TIMING method={} success={} elapsedMs={}",
                point.getSignature().toShortString(), success, elapsedMs);
    }
}
```

`proceed()` 执行后续调用，`return result` 保留业务返回值。计时放在 finally 中，使正常返回和异常退出都记录耗时，异常继续向上传播。

### 1. 为什么同类内部调用不触发切面？

Spring AOP 依靠代理拦截调用：

```text
外部调用 → 代理 → 切面 → 目标方法
```

而目标对象内部调用 `this.findName()`，没有重新经过代理：

```text
外部调用 → 代理 → selfCall() → this.findName()
```

所以方法可能执行了，但对应的内部调用没有独立触发计时。

| 实验 | 是否经过目标方法的代理拦截 | 是否增加计时日志 |
|---|---|---|
| direct：外部调用注入对象的计时方法 | 是 | 是 |
| self：同类方法内部调用计时方法 | 否 | 否 |
| cross：另一个 Bean 调用注入对象 | 是 | 是 |
| manual：手动 new 后调用 | 否 | 否 |

这些结论对应本项目的调用结构和切点配置。注解本身不会自动赋予手动创建的对象代理能力。

理解这个边界，也为后续学习 Spring 代理模式下的事务注解打下基础。

## 十、Day39：用 MockMvc 自动验证接口

人工请求可以帮助观察结果，但接口修改后，需要可重复执行的自动化检查。

```java
mvc.perform(get("/api/day37/teachers/10001"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("张老师"));
```

- `perform`：执行模拟 MVC 请求。
- `andExpect`：检查实际结果。
- `$.name`：JSON 根对象的 name 属性。

### 1. MockMvc 与 MockBean 的区别

Day39 使用：

```java
@WebMvcTest(Day37ExceptionController.class)
class Day39WebTest {
    @Autowired private MockMvc mvc;
    @MockBean private Day37TeacherService service;
}
```

`@WebMvcTest` 加载 MVC 测试切片；`MockMvc` 执行 Spring MVC 流程，但不监听真实网络端口；`@MockBean` 将 Service 替换为模拟对象。

例如：

```java
when(service.find(10001L))
    .thenReturn(new TeacherResponse(10001L, "张老师", "Java"));
```

这是预设测试行为，不是向仓库或数据库插入教师。

### 2. 六个测试关注什么？

1. 查询路由和 JSON 字段正确，响应不包含内部备注。
2. 业务异常转换为 404。
3. 空姓名返回校验错误。
4. 非法路径参数返回 400，且不调用 Service。
5. 错误 JSON 返回 INVALID_BODY。
6. 未知异常返回 500，并隐藏内部异常细节。

在 `spring-boot-lab` 目录运行：

```powershell
mvn -Dtest=Day39WebTest test
```

这些测试检查 Web 层契约，不证明真实 Service、数据库、网络监听或浏览器跨域配置正确。

## 十一、Day40：完成新增、查询、更新闭环

Day40 开始真正修改当前 Java 进程中的内存数据。

### 1. 接口约定

| 操作 | 请求 | 成功结果 |
|---|---|---|
| 新增 | POST /api/day40/teachers | 201，返回教师，Location 指向查询地址 |
| 查询 | GET /api/day40/teachers/{id} | 200，返回教师 |
| 更新 | PUT /api/day40/teachers/{id} | 200，返回更新结果 |

新增与更新都提交 name、subject。PUT 在本练习中要求完整提供这两个可更新字段，不是只提交一个字段的局部更新接口。

重复姓名允许存在；重复 POST 会生成新编号，没有实现请求幂等去重。查询或更新不存在的教师返回 404，更新不会自动创建记录。

### 2. 从 Controller 追踪到仓库

```text
HTTP 请求
    → Day40TeacherController
    → TeacherCrudService
    → MemoryTeacherRepository
    → TeacherEntity
    → TeacherConverter
    → TeacherResponse
    → JSON
```

创建接口的核心实现：

```java
@PostMapping
public ResponseEntity<TeacherResponse> create(
        @Valid @RequestBody ValidatedTeacherRequest request) {
    TeacherResponse teacher = service.create(request);
    return ResponseEntity.created(
            URI.create("/api/day40/teachers/" + teacher.getId()))
            .body(teacher);
}
```

`ResponseEntity.created(...)` 表达 201，并设置 Location。校验成功后才调用业务层，ID 由服务端生成。

### 3. PowerShell 完整调用

服务按前面的方式启动在 8081 后，另开一个 PowerShell 窗口执行：

```powershell
$base = 'http://127.0.0.1:8081/api/day40/teachers'

# 新增教师。
$createBody = @{ name = '张老师'; subject = 'Java' } | ConvertTo-Json
$teacher = Invoke-RestMethod -Method Post -Uri $base `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($createBody))
$teacher

# 使用服务端实际返回的编号，不写死为 10001。
$teacherId = $teacher.id
Invoke-RestMethod -Uri "$base/$teacherId"

# 更新教师。
$updateBody = @{ name = '李老师'; subject = '数学' } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$base/$teacherId" `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($updateBody))

# 再查询，确认修改已经保存到内存仓库。
Invoke-RestMethod -Uri "$base/$teacherId"
```

PowerShell 的反引号是续行符，后面不要添加空格。显式传递 UTF-8 字节，方便正确传输中文。

最终应查询到同一个 ID 对应的“李老师 / 数学”。响应不会包含内部备注。

### 4. 内存仓库如何保存数据？

```java
private final AtomicLong sequence = new AtomicLong(10000);
private final ConcurrentHashMap<Long, TeacherEntity> teachers = new ConcurrentHashMap<>();
```

`AtomicLong` 提供原子递增编号，`ConcurrentHashMap` 支持并发访问。

更新时使用：

```java
teachers.computeIfPresent(id, (key, old) ->
    new TeacherEntity(key, name, subject, old.getInternalRemark()));
```

只有记录存在时才替换对象；保留原来的内部备注，同时更新姓名和科目。单次映射更新是原子的，但这不等于数据库事务，也没有实现版本冲突检测。

数据与计数器会在重启后重置，多个服务进程也不共享此仓库。

### 5. 不只验证成功场景

| 实验 | 预期 |
|---|---|
| POST 合法姓名与科目 | 201，并返回 ID |
| GET 新建 ID | 返回刚刚创建的教师 |
| PUT 合法数据，再 GET | 返回更新后的字段 |
| POST 空姓名 | 400，VALIDATION_ERROR |
| PUT 已有 ID，但请求体为 {} | 400，原有数据不变 |
| PUT 不存在的 ID | 404，不新增记录 |
| GET 不存在的 ID | 404 |
| 请求额外提交 id、internalRemark | 不能接管服务端生成的 ID 和内部字段 |

`Invoke-RestMethod` 遇到 400、404 通常会抛出错误。此时应检查 HTTP 状态与错误响应，而不是只看终端出现红色文字。

## 十二、如何确认这一阶段真的学会了？

### 1. 区分三种验证层次

| 验证方式 | 实际覆盖范围 |
|---|---|
| Day39 MVC 切片测试 | Controller、绑定、校验、异常协议；Service 为模拟对象 |
| Day40 完整上下文测试 | 真实 Service 和内存仓库，经 MockMvc 执行 |
| 启动 Jar 后发送 HTTP 请求 | 真实端口、HTTP 处理及当前应用调用链 |

Day40 使用 `@SpringBootTest + @AutoConfigureMockMvc`，两个测试分别覆盖完整新增更新流程，以及不存在编号的更新失败。MockMvc 仍不监听真实端口。

当前源码中 Day39 有 6 个测试，Day40 有 2 个。在 `spring-boot-lab` 目录可运行：

```powershell
mvn clean package
```

该命令包含测试阶段，无需为了相同验证紧接着再执行一次 `mvn test`。应检查实际测试数量、Failures、Errors 与 Skipped，而不只看 BUILD SUCCESS。

本地 Day39、Day40 课程文档记录：2026-11-26 使用 JDK 8 和临时 Maven 3.9.9 完成构建，8 个测试通过；Day40 另记录了 9 次真实 HTTP 检查通过。这是已有课程验证记录，本文整理时没有重新执行构建或请求。复现结果应以自己的最新运行输出为准。

### 2. 阶段自测

- [ ] 能解释 HTTP 方法、路径、查询参数、请求体与响应状态。
- [ ] 能不用 Spring 写出构造器注入，再说明容器如何完成组装。
- [ ] 能启动 Boot，并通过 profile 和命令行切换配置。
- [ ] 能区分参数绑定、字段校验与业务检查。
- [ ] 能解释为什么 Request、Entity、Response 的字段不完全相同。
- [ ] 能返回明确的 400、404、500，并通过 requestId 对照错误日志。
- [ ] 能解释 AOP 自调用和手动 new 为什么绕过代理。
- [ ] 能说清 MockMvc、MockBean 和真实 HTTP 请求的区别。
- [ ] 能完成新增、查询、更新、再查询，并验证非法更新不破坏数据。
- [ ] 能说明内存仓库的重启丢失、进程隔离和并发更新边界。

下一阶段将进入 MyBatis、分页和数据库事务等内容，把已经建立的 Controller → Service → Repository 调用链连接到真实持久化层。到那时，需要重新验证 SQL、映射和事务，不能用本阶段的内存测试代替数据库验证。

**完整源码与每日文档：[https://github.com/qishuixian/javaProject](https://github.com/qishuixian/javaProject)**
