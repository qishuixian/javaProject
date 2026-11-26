# Day36：参数绑定之后，还要校验

Day34 学习把 JSON 转成对象；Day35 区分 Request、Entity、Response。今天解决：**对象已经创建出来，但字段值不符合要求怎么办？**

本课在现有 `spring-boot-lab` 增加 `/api/day36` 接口，复用 Day35 的预览流程，不保存数据，也不连接数据库。

## 1. 最先理解这一行

```java
public TeacherResponse preview(@Valid @RequestBody ValidatedTeacherRequest request)
```

按处理顺序理解：

1. `@RequestBody`：读取 JSON，转换成请求对象。
2. `@Valid`：要求检查该对象上的校验规则。
3. 校验通过：进入方法体，继续业务处理。
4. 校验失败：抛出校验异常，由本课处理器返回 HTTP 400 和字段错误。

`@Valid` 不会自动发明规则。规则要写在请求类字段上；只写规则却不触发校验，也不会自动拦截所有 Java 方法调用。

## 2. 先运行一个成功请求

1. IDEA 重新加载 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml)，本次增加了 `spring-boot-starter-validation`。
2. 仍运行 [Day33Application.main()](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)，模块选 **spring-boot-lab**，JRE 选 **JDK 8**。
3. Program arguments 设置为 `--spring.profiles.active=local --server.port=8081`。
4. 停止旧服务，重新启动。

在 PowerShell 中发送 POST：

```powershell
$body = @{ name = '张老师'; subject = 'Java' } | ConvertTo-Json
Invoke-RestMethod `
  -Method Post `
  -Uri 'http://127.0.0.1:8081/api/day36/teachers/preview' `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

返回内容对应 `{"id":null,"name":"张老师","subject":"Java"}`。id 为 null，因为只是预览。

今天接口都是 POST，直接在浏览器地址栏访问不能完成实验。

## 3. 必填与长度怎么写

打开 [ValidatedTeacherRequest.java](../spring-boot-lab/src/main/java/learning/boot/day36/ValidatedTeacherRequest.java)：

```java
@NotBlank(message = "姓名不能为空或全是空格")
@Size(max = 20, message = "姓名长度不能超过20")
private String name;
```

- `@NotBlank`：拒绝 null、空字符串以及普通全空白字符串。
- `@Size(max = 20)`：长度不能超过 20。
- `message`：校验失败时返回的说明。
- 两个注解合在一起：既要有内容，也不能过长。

科目同样不能为空，最大长度为 30。`@Size` 对 Java String 按 length 判断；普通中文通常计 1，部分 emoji 等字符可能占 2 个 UTF-16 单元，不等同于用户看到的字符数量。

`@Size` 本身允许 null，因此不能代替必填校验。`@NotBlank` 只用于字符序列，不能写在 Integer 页大小上。

## 4. 亲自看错误响应

下面的代码可显示 Windows PowerShell 中非 2xx 响应的状态码和原始 JSON；改 `$body` 即可重复实验：

```powershell
Add-Type -AssemblyName System.Net.Http
$client = New-Object System.Net.Http.HttpClient
$body = '{"name":"   ","subject":"Java"}'
$content = New-Object System.Net.Http.StringContent -ArgumentList @(
  $body, [System.Text.Encoding]::UTF8, 'application/json'
)
try {
  $response = $client.PostAsync(
    'http://127.0.0.1:8081/api/day36/teachers/preview', $content
  ).GetAwaiter().GetResult()
  try {
    [int]$response.StatusCode
    $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
  } finally { $response.Dispose() }
} finally { $content.Dispose(); $client.Dispose() }
```

预期 HTTP 状态为 **400**，响应：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "参数校验失败",
  "errors": [
    {"field": "name", "message": "姓名不能为空或全是空格"}
  ]
}
```

前端可以按 `field` 找到输入框，把 message 显示在下方。多个字段错误都会放入 errors；错误顺序不保证，不应依赖数组第一项永远对应某个字段。

## 5. 数值范围与默认值

打开 [TeacherPageRequest.java](../spring-boot-lab/src/main/java/learning/boot/day36/TeacherPageRequest.java)：

```java
@NotNull(message = "每页数量不能为null")
@Min(value = 1, message = "每页数量必须大于等于1")
@Max(value = 100, message = "每页数量不能超过100")
private Integer pageSize = 20;
```

- `@NotNull`：不能是 null。
- `@Min(1)`、`@Max(100)`：允许 1 到 100，包括两端。
- `= 20`：创建请求对象时设置默认值。
- Min 和 Max 允许 null，所以仍需要 NotNull。

使用上一节代码，将 URL 改为 `/api/day36/teachers/query-preview`，分别尝试：

| JSON | 结果 |
| --- | --- |
| `{}` | 200，page=1、pageSize=20，沿用字段默认值 |
| `{"page":2,"pageSize":100}` | 200，回显对应数值 |
| `{"pageSize":null}` | 400，显式 null 覆盖默认值，触发 NotNull |
| `{"pageSize":0}` | 400，低于下限 |
| `{"pageSize":101}` | 400，超过上限 |
| `{"page":0}` | 400，页码至少是 1 |
| `{"pageSize":"abc"}` | 400，转换 Integer 失败，还没到字段校验 |

这个接口只回显合法分页参数，不执行真实分页查询。

## 6. 三个容易混淆的注解

| 注解 | 要求 | 示例 |
| --- | --- | --- |
| NotNull | 不为 null；不检查空字符串或空集合 | Integer 页大小 |
| NotEmpty | 不为 null，且长度/大小不为零；字符串空格仍可能通过 | 列表至少有一个元素 |
| NotBlank | 字符串有非空白内容 | 教师姓名 |

本课使用 Boot 2.3 和 Java 8，所以导入 `javax.validation.*`。不要直接照搬 Boot 3 教程中的 `jakarta.validation.*`。

`@Valid` 是标准 Bean Validation 的级联校验标记；Spring MVC 会用它触发本课请求对象校验。Spring 的 `@Validated` 还支持校验分组及配合代理的方法校验等场景，今天不混用这些机制。

## 7. 错误是谁整理的

打开 [Day36ValidationAdvice.java](../spring-boot-lab/src/main/java/learning/boot/day36/Day36ValidationAdvice.java)：

```java
@RestControllerAdvice(assignableTypes = Day36ValidationController.class)
```

表示这里的异常处理只应用于 Day36 Controller。Day34、Day35 的历史示例行为保持原样，方便比较。

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
```

含义是：捕获请求对象校验异常，并把 HTTP 状态设为 400。代码遍历字段错误，将字段名和注解 message 整理成 errors 数组；不输出用户提交的原始值或异常堆栈。

另外处理 `HttpMessageNotReadableException`，返回 `INVALID_BODY`。例如缺少整个请求体、不完整 JSON、pageSize="abc" 都属于读取或转换失败，不应假装是某条 Min 规则失败。

今日只处理这两类输入异常。业务错误、未知异常、服务端日志等完整体系留到 Day37。

## 8. 与昨天的衔接

[Day36ValidationController.java](../spring-boot-lab/src/main/java/learning/boot/day36/Day36ValidationController.java) 在校验通过后，把 name、subject 复制到 Day35 的请求对象，再调用原有 Service，最终返回 TeacherResponse。这样可以观察“同样的预览逻辑，加校验前后有什么区别”。

这里的对象复制用于保留每日示例；真实项目不必为每一天重复创建请求类。

前端校验方便用户及时纠错，但客户端可以绕过前端发送请求，因此后端还要校验。字段规则也不等于权限检查、数据库唯一约束或完整业务校验。

## 9. 对照目标项目

查看 `E:\code\java\aic-teaching-service\aic-teaching-service-api\src\main\java\cn\vipthink\aic\teaching\api\dto\TeacherListRequest.java`：

- bizLine 使用 NotBlank 与 Pattern。
- pageNum 默认值为 1，并使用 Min(1)。
- pageSize 默认值为 20，并使用 Min(1)。

本课额外给 pageSize 添加了 NotNull 和 Max(100)，用于演示 null 与上限，并非声称原项目有完全一样的规则。本次只阅读源码，没有运行目标服务。

## 10. 今日验收

- [ ] 能解释 RequestBody 和 Valid 的区别。
- [ ] 正常姓名通过；缺失、空字符串、全空格姓名失败。
- [ ] 姓名 20 长度通过，21 长度失败。
- [ ] pageSize=1/100 通过，0/101/null 失败。
- [ ] 能解释缺失 pageSize 和显式 null 为什么不同。
- [ ] 能从 errors 找到字段名和错误原因。
- [ ] 能区分 VALIDATION_ERROR 与 INVALID_BODY。

## 11. 本次验证记录

2026-11-26：使用 JDK 8 和临时目录中的 Maven 3.9.9 执行 `package`，结果为 `BUILD SUCCESS`。本模块没有单元测试，构建日志显示 `No tests to run`。

实际启动打包后的 JAR，在随机空闲端口完成 23 次 HTTP 检查：正常中文请求、缺失/null/空串/空格姓名、姓名和科目长度边界、多字段错误、分页默认值及上下限、显式 null、类型错误、损坏 JSON、空请求体，以及 Day34/35 回归，全部符合预期。检查包含状态码、错误码、字段信息及关键响应值；验证进程已停止。未操作 IDEA，也未连接数据库。

下一课 Day37：统一异常处理，区分校验错误、业务错误与未知异常。
