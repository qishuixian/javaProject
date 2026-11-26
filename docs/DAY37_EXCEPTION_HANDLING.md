# Day37：统一异常处理

今天解决：**接口失败以后，前端收到什么，后端怎样找到原因？**

Day36 已经处理字段校验错误。今天在 `/api/day37` 中统一错误响应结构，并区分参数错误、业务错误和未知异常。不连接数据库。

## 1. 先分清三种失败

| 场景 | HTTP 状态 | 本课业务码 | 应如何理解 |
| --- | --- | --- | --- |
| 姓名为空 | 400 | VALIDATION_ERROR | 请求数据不符合规则 |
| 教师编号不存在 | 404 | TEACHER_NOT_FOUND | 请求格式正确，但业务资源不存在 |
| 程序发生未预期异常 | 500 | INTERNAL_ERROR | 服务端需要排查的问题 |

HTTP 状态在响应状态行里，code 在 JSON 中，它们不是同一个东西。不能把失败全部包装成 HTTP 200，否则客户端、监控和网关容易误判成功。

业务失败不一定都返回 404：冲突可能是 409，未登录通常是 401。应依据接口语义选择。

## 2. 启动并观察

1. IDEA 同步 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml)。
2. 运行 [Day33Application.main()](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)，模块 **spring-boot-lab**，JRE **JDK 8**。
3. Program arguments 设置为 `--spring.profiles.active=local --server.port=8081`。
4. 停止旧进程后重新启动。

依次用浏览器访问：

- <http://127.0.0.1:8081/api/day37/teachers/10001>：200，返回张老师。
- <http://127.0.0.1:8081/api/day37/teachers/99999>：404，教师不存在。
- <http://127.0.0.1:8081/api/day37/teachers/abc>：400，编号无法转换为 long。
- <http://127.0.0.1:8081/api/day37/teachers/simulate-error>：500，模拟未知异常。

在浏览器开发者工具的 Network 面板查看状态码。`simulate-error` 是固定路径，比 `/{id}` 更具体，因此会命中模拟异常方法，而不是把 simulate-error 转为 long。此接口仅用于本地教学，不应发布到生产环境。

## 3. 业务异常怎样抛出来

[Day37TeacherService.java](../spring-boot-lab/src/main/java/learning/boot/day37/Day37TeacherService.java)：

```java
if (id != 10001L) {
    throw new TeacherNotFoundException();
}
```

逐句理解：

1. 示例中只存在编号 10001。
2. `new TeacherNotFoundException()` 创建异常对象。
3. `throw` 抛出它，当前方法不会继续执行后面的成功返回。
4. Controller 没有捕获，异常交给 Spring MVC 的异常处理流程。

[TeacherNotFoundException.java](../spring-boot-lab/src/main/java/learning/boot/day37/TeacherNotFoundException.java)：

```java
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException() {
        super("教师不存在");
    }
}
```

`extends` 表示继承；`super(...)` 调用父类构造方法，设置异常说明。RuntimeException 属于非受检异常，不要求调用方必须写 try/catch。它不会自动决定 HTTP 状态，由处理器完成转换。

## 4. 为什么 Controller 不写一堆 try/catch

[Day37ExceptionController.java](../spring-boot-lab/src/main/java/learning/boot/day37/Day37ExceptionController.java) 负责接收请求并调用 Service；[Day37ExceptionAdvice.java](../spring-boot-lab/src/main/java/learning/boot/day37/Day37ExceptionAdvice.java) 集中整理异常。

```java
@ExceptionHandler(TeacherNotFoundException.class)
public ResponseEntity<Object> business(TeacherNotFoundException ex) {
    // 返回 404 和统一 JSON
}
```

- `@ExceptionHandler(...)`：声明这个方法负责哪类异常。
- `TeacherNotFoundException.class`：表示异常类型。
- `ex`：实际抛出的那个异常对象。
- `ResponseEntity`：同时指定响应状态、响应头和响应内容。

不要在 Service 中把异常吞掉后返回 null，否则统一处理器就收不到它。也不要为了记录同一次错误，在每一层都捕获、打印、重新抛出，造成重复日志。

## 5. 统一的是错误结构

业务失败示例：

```json
{
  "code": "TEACHER_NOT_FOUND",
  "message": "教师不存在",
  "requestId": "每次错误生成的标识",
  "errors": []
}
```

[ApiError.java](../spring-boot-lab/src/main/java/learning/boot/day37/ApiError.java) 定义四个响应属性。字段校验错误会在 errors 中放入 field、message；其他错误为一个空数组。成功响应继续使用 TeacherResponse，今天不统一成功响应包装。

requestId 用来把返回值和日志对应起来。本课在错误处理时生成 UUID，它只是本次错误的关联标识；不是完整链路追踪，也不为成功请求生成标识。生产项目通常在更早的 Filter 中生成或接收追踪信息。

## 6. 验证参数校验错误

PowerShell 示例可同时显示状态码和 JSON：

```powershell
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$content = New-Object System.Net.Http.StringContent -ArgumentList @(
  '{"name":"","subject":"Java"}', [System.Text.Encoding]::UTF8, 'application/json'
)
try {
  $response = $client.PostAsync(
    'http://127.0.0.1:8081/api/day37/teachers/preview', $content
  ).GetAwaiter().GetResult()
  try {
    [int]$response.StatusCode
    $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
  } finally { $response.Dispose() }
} finally { $content.Dispose(); $client.Dispose() }
```

返回 400，code 为 VALIDATION_ERROR，errors 包含 name 的错误说明。这里复用 Day36 的请求类及 @Valid。

将 body 改为 `{`，会得到 400、INVALID_BODY；改为正常姓名和科目则返回 200。校验失败和 JSON 无法读取是不同处理分支。

## 7. 未知异常：返回通用提示，日志保留堆栈

模拟方法抛出：

```java
throw new IllegalStateException("DAY37_INTERNAL_DEMO_FAILURE");
```

前端只收到：

```json
{
  "code": "INTERNAL_ERROR",
  "message": "服务暂时不可用，请稍后重试",
  "requestId": "本次错误标识",
  "errors": []
}
```

在 IDEA 控制台搜索返回的 requestId，会看到 ERROR 日志，以及 IllegalStateException 和调用栈。堆栈包含哪个类、哪个方法、哪一行触发问题，是排错的重要证据。

```java
LOG.error("requestId=" + requestId + " code=" + code, unexpected);
```

最后传入异常对象，日志框架才会记录完整堆栈。只写 `unexpected.getMessage()` 会丢失调用链。内部异常说明、Java 类名、堆栈都不应直接放进前端响应。

`@ExceptionHandler(Exception.class)` 是兜底分支，不是所有错误都要变成 500。更具体的校验和业务异常处理优先匹配。

## 8. 框架异常为什么需要单独考虑

本课 Advice 继承 Spring 的 `ResponseEntityExceptionHandler`。这是已有的 MVC 异常处理基类，能处理许多常见 HTTP 异常。

- 重写 `handleMethodArgumentNotValid`：整理校验字段错误。
- 重写 `handleHttpMessageNotReadable`：整理 JSON 读取错误。
- 重写 `handleExceptionInternal`：保留框架原有状态和响应头，同时转换错误体。路径参数转换失败返回 400，不误报 500。

`@Override` 表示重写父类方法。本课是 Boot 2.3 / Spring 5 的方法签名，不能直接照搬新版 Spring 的不同参数类型。

Advice 使用 `assignableTypes = Day37ExceptionController.class`，只覆盖 Day37，便于保留历史课程的输出。有些异常在找到 Controller 之前就发生，例如完全不存在的路由，可能仍走 Boot 默认错误响应；这里没有声称所有 URL、Filter 或异步线程异常都受此 Advice 管理。

## 9. 对照真实项目

阅读 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\config\GlobalExceptionHandler.java`：

- 使用 RestControllerAdvice 与 ExceptionHandler。
- BusinessException 根据业务码选择 401、403、409、503 等状态。
- 参数类异常返回 400，未知异常返回 500。
- error 方法从 MDC 获取 requestId，缺失时生成 UUID，并写入响应头。
- `LOGGER.error("error", exception)` 保留堆栈。

本课把关联标识放在响应体，并采用独立的 ApiError；这是便于学习的设计，不是对原项目响应协议的复制。本次只阅读原项目，没有启动其服务。

## 10. 验收

- [ ] 能区分 400、404、500 的含义。
- [ ] 能解释 throw、extends RuntimeException、super。
- [ ] 能从异常类型找到对应处理方法。
- [ ] 校验失败包含字段名和说明。
- [ ] 未知异常响应不包含内部错误文本或堆栈。
- [ ] 能用 requestId 在日志里找到该次异常与堆栈。
- [ ] 能解释为什么统一异常处理不等于所有错误都返回 500。

## 11. 本次验证记录

2026-11-26：JDK 8 配合临时 Maven 3.9.9 执行 `package`，结果为 `BUILD SUCCESS`；模块没有单元测试，日志显示 `No tests to run`。

实际启动 JAR，随机空闲端口完成 11 次 HTTP 检查，覆盖成功查询、业务 404、路径转换 400、字段校验、损坏 JSON、空请求体、正常中文预览、重复模拟 500、Day36 和 Day35 回归，均符合预期。另检查了 500 响应不含内部异常文本，且响应 requestId 能在服务端日志中找到，日志保留异常类型与 Service 调用栈。验证进程已停止；未实际操作 IDEA，未连接数据库。

下一课 Day38：AOP 与代理，观察耗时切面以及同类调用、跨 Bean 调用的区别。
