# Day34：把 HTTP 参数交给 Java 方法

今天只解决一个问题：前端传来的参数，怎样变成 Java 方法里的变量？

沿用 Day33 的 `spring-boot-lab`，不需要数据库。本课接口回显收到的参数，不实现真实查询、分页或保存。这样可以先看清参数从哪里来。

## 1. 先记住这张表

| 参数位置 | 请求例子 | Java 注解 |
| --- | --- | --- |
| URL 路径中 | `/api/day34/teachers/10001` | `@PathVariable("id")` |
| URL 问号后 | `/api/day34/teachers?keyword=Zhang&page=2` | `@RequestParam` |
| 请求体中 | POST，body 是 `{"name":"张老师","age":28}` | `@RequestBody` |

参数绑定，就是 Spring 找到这些数据，转换成所需的 Java 类型，再调用你的方法。GET/POST 是 HTTP 方法；参数放在哪里是另一个问题。本课按常用方式演示，但不是说 POST 只能接收 body：POST 也能有查询参数。

## 2. 启动步骤

1. 在 IDEA 将 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml) 添加为 Maven 项目，等待同步。
2. 仍然运行 [Day33Application.main()](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)，不是直接运行 Controller。
3. 运行配置的模块选 **spring-boot-lab**，JRE 选 **JDK 8**。
4. 为避免昨天残留的端口参数影响本课，将 **Program arguments（程序实参）** 设置为 `--spring.profiles.active=local --server.port=8081`。
5. 停止旧进程，再运行，看到 `Started Day33Application`。

类名里的 Day33 只表示入口最初创建于 Day33。它会扫描 `learning.boot` 下新增的 Controller，所以今天不必再创建一个启动类。Day33 的 `/api/lesson` 仍可使用。

代码入口：[Day34BindingController.java](../spring-boot-lab/src/main/java/learning/boot/Day34BindingController.java)。

## 3. 路径参数：从 URL 中取出编号

浏览器打开 <http://127.0.0.1:8081/api/day34/teachers/10001>：

```json
{"source":"path","teacherId":10001}
```

对应代码：

```java
@GetMapping("/teachers/{id}")
public Map<String, Object> teacher(@PathVariable("id") long id) {
    // ...
}
```

逐句解释：

1. 类上的 `@RequestMapping("/api/day34")` 是公共前缀。
2. 方法上的 `/teachers/{id}` 拼到前缀后面，组成完整路径。
3. `{id}` 是占位符，匹配 URL 这一段，例如 `10001`。
4. `@PathVariable("id")` 表示从这个占位符取值。
5. `long id` 表示把 URL 中的文本转换为 long，再交给参数 id。

把地址末尾改为 `20002`，返回的 teacherId 也会变。这只证明绑定成功，不代表数据库存在这个教师。

把末尾改为 `abc`，它无法转换为 long，返回 **400 Bad Request**，不会正常进入方法体。

## 4. 查询参数：从问号后取值

打开 <http://127.0.0.1:8081/api/day34/teachers?keyword=Zhang&page=2>：

```json
{"source":"query","keyword":"Zhang","page":2}
```

```java
@RequestParam(value = "keyword", defaultValue = "") String keyword,
@RequestParam(value = "page", defaultValue = "1") int page
```

- `value = "page"`：读取查询参数里的 page。
- `defaultValue = "1"`：没传或传空字符串时使用 1，随后转为 int。
- `int page`：进入方法后，它是数字，不再是 URL 中的文本。
- 没传 keyword 时，得到空字符串。

访问 <http://127.0.0.1:8081/api/day34/teachers>，会得到 `keyword: ""`、`page: 1`。

默认情况下，没有 defaultValue 的 `@RequestParam("page")` 是必填的。需要可选且保留缺失状态时，可写 `@RequestParam(value = "page", required = false) Integer page`，再自行处理 null。

两种 400 要区分：

| 请求 | 为什么失败 |
| --- | --- |
| `?page=abc` | Spring 无法把 abc 转为 int |
| `?page=0` | 已转换成数字，但我们在方法中检查了 `page < 1` 并拒绝 |

**类型转换成功不代表业务数据合法。** 本课用 `ResponseStatusException` 手动返回 400；完整校验和统一错误格式后续再学。

## 5. 请求体：把 JSON 转成 Java 对象

浏览器地址栏只能方便地发送 GET，不能直接完成下面的 POST 实验。启动服务后，在 PowerShell 粘贴：

```powershell
$body = @{ name = '张老师'; age = 28 } | ConvertTo-Json
Invoke-RestMethod `
  -Method Post `
  -Uri 'http://127.0.0.1:8081/api/day34/teachers/preview' `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

这里反引号是 PowerShell 的续行符，后面不要有空格。显式转 UTF-8 字节避免中文传输编码问题。

返回内容对应：

```json
{"source":"body","name":"张老师","age":28,"saved":false}
```

`saved: false` 表示只预览收到的值，没有保存数据。

前端可在同源页面中这样发送；若跨域，还需另行配置 CORS，本课没有开启：

```javascript
fetch('/api/day34/teachers/preview', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ name: '张老师', age: 28 })
}).then(response => response.json()).then(console.log);
```

对应 Java：

```java
public Map<String, Object> preview(@RequestBody TeacherPreviewRequest request)
```

拆开来看：

1. `@RequestBody`：从 HTTP 请求体读取数据。
2. `TeacherPreviewRequest`：希望转换成的 Java 类型。
3. `request`：转换成功后保存这个对象的参数名，可以换名字。
4. `request.getName()`：读取对象中的 name。

`Content-Type: application/json` 告诉服务端如何理解请求体。Spring 使用 Web starter 带来的 Jackson，将 JSON 转成 Java 对象，这叫反序列化。返回 Map 转成 JSON 则叫序列化。

## 6. 为什么要写 getter、setter 和无参构造方法

打开 [TeacherPreviewRequest.java](../spring-boot-lab/src/main/java/learning/boot/TeacherPreviewRequest.java)：

```java
private String name;
private Integer age;

public TeacherPreviewRequest() { }
public void setName(String name) { this.name = name; }
public String getName() { return name; }
```

本例可理解为：Jackson 创建对象，再按属性填入值；业务代码通过 getter 读出值。这里用普通 JavaBean 写法，方便观察，不依赖 Lombok。Jackson 也支持其他构造方式，不是所有请求类都必须采用这一写法。

字段名要对应：JSON 的 `name` 对应 Java 的 name 属性，JSON 的 age 对应 age 属性。request 是方法参数名，不是 JSON 外层的键，不需要传 `{"request": {...}}`。

age 用 `Integer` 是为了区分“没传”和“传了 0”：缺失时是 null，0 则是一个明确的数字。若使用 int，缺失时通常是默认值 0。

## 7. 失败实验：接收与校验不是一回事

| 操作 | 本课预期 |
| --- | --- |
| POST 正常 JSON | 200，回显 name、age |
| POST `{}` | 200，name 和 age 是 null；尚未配置字段必填校验 |
| POST `{"name":"张老师","age":"abc"}` | 400，age 无法转换为 Integer |
| POST 不完整 JSON，例如 `{` | 400，JSON 解析失败 |
| POST 缺少整个请求体 | 400，`@RequestBody` 默认要求请求体 |
| POST JSON 文本但 Content-Type 是 `text/plain` | 415，不支持这种媒体类型来转换本例对象 |
| 浏览器 GET `/api/day34/teachers/preview` | 命中 GET `/teachers/{id}`，preview 转不成 long，因此为 400 |

注意最后一行：路由不仅看 URL，也看 HTTP 方法；路径占位符还可能匹配文字。这就是为什么调用 POST 接口要按规定使用 POST。

Boot 默认错误响应可能不包含具体异常 message，今天判断 HTTP 状态码即可，不依赖错误文案。`@RequestBody` 负责读取和转换，不会自动验证 name 非空或 age 合理。

## 8. 对照目标项目

阅读 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\controller\TeacherManagementController.java`：

- `listTeachers`：`@PostMapping("/teaching-web/management/teachers/query")` 配合 `@RequestBody TeacherListRequest request`，接收 JSON 查询条件。
- `listFilterOptions`：使用 `@RequestParam("bizLine")` 和 `@RequestParam("type")` 接收参数；type 还会转为枚举。
- `@Validated`、`@NotBlank` 等属于校验，不要与参数读取注解混为一谈。

本课路径参数来自自己的教学示例，不把目标项目中注释掉的路径接口当作正在生效的接口。今天只阅读目标项目，不向其发送业务请求。

## 9. 练习与验收

- [ ] 能指出三种参数分别放在哪里。
- [ ] 将路径编号从 10001 改为 20002，观察回显。
- [ ] 不传 page 时得到 1，传 page=2 时得到 2。
- [ ] 用 PowerShell POST 中文姓名，观察正确返回。
- [ ] 能解释为什么 `{}` 可以成功，而 age="abc" 返回 400。
- [ ] 在请求类新增一个 String subject，补 getter/setter，并在回显中加入它，重新启动后验证。

## 10. 本次验证记录

2026-11-26：使用 JDK 8 和临时 Maven 3.9.9 执行 `package`，结果为 `BUILD SUCCESS`；本模块没有单元测试，Maven 显示 `No tests to run`。

实际启动打包后的 JAR，以随机空闲端口执行 14 次 HTTP 检查：路径绑定、查询参数、默认值、中文 JSON、空对象、类型转换失败、非法页码、不完整 JSON、缺失请求体、错误媒体类型、GET/POST 路由区别，以及 Day33 接口回归，全部符合预期。验证后已停止进程。未实际操作 IDEA；文档使用固定 8081 端口便于练习。

下一课 Day35：DTO/Entity 分层，进一步区分请求对象、数据库实体和响应对象。
