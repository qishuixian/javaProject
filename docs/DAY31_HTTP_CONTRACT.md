# Day 31：HTTP 与接口契约

## 今日目标

理解一次 HTTP 接口调用由什么组成：请求方法、路径、查询参数、状态码、响应头和响应体。入口使用 JDK 自带的 `HttpServer`，不新增 Spring 依赖；示例启动后立即停止，保证不会留下后台进程。

入口：[Day31HttpContract.java](../java-basics/src/Day31HttpContract.java)。在 IDEA 点击 main 左侧绿色三角运行。

## 1. 一次请求的结构

```text
GET /teachers?id=10001 HTTP/1.1
```

| 部分 | 含义 |
|---|---|
| GET | 请求方法，读取资源 |
| /teachers | 路径，表示教师资源 |
| ?id=10001 | 查询参数，键是 id，值是 10001 |
| HTTP/1.1 | 协议版本 |

响应包含状态码、响应头和响应体：

```http
HTTP/1.1 200 OK
Content-Type: application/json; charset=UTF-8

{"data":{"id":10001,"name":"张老师"}}
```

## 2. 状态码与业务码

本课约定：

| 请求 | 状态码 | 含义 |
|---|---:|---|
| `GET /teachers?id=10001` | 200 | 查询成功 |
| `GET /teachers` | 400 | 缺少或非法请求参数 |
| `GET /teachers?id=99999` | 404 | 教师不存在 |
| POST /teachers | 405 | 本入口只允许 GET |

状态码表达 HTTP 层结果，JSON 中的 code/message 表达更细的业务信息。不能所有情况都返回 200 再让调用方自己猜错误，也不能只依赖字符串 message 判断业务。

## 3. 查询参数与路径参数

本课使用：

```text
/teachers?id=10001
```

id 是 query parameter。真实 Spring Controller 可能使用 `@RequestParam` 或 `@PathVariable`：

```text
GET /teachers/10001
```

二者是不同接口契约，客户端、文档和测试要统一。参数解析后还要做类型、范围、权限和业务存在性校验；字符串“10001”不应直接当作已验证的 long。

## 4. JSON 响应结构

```json
{"data":{"id":10001,"name":"张老师"}}
```

data 是成功数据容器，错误响应使用 code 和 message。本例手写固定 JSON 只为观察 HTTP 结构，不是 Day19 JSON 解析器的替代品，也不支持真实请求体。

## 5. 逐段读懂代码

- `HttpServer.create` 创建本地 HTTP 服务。
- `createContext("/teachers", handler)` 把 `/teachers` 路径交给处理器。
- `exchange.getRequestMethod()` 读取请求方法。
- `exchange.getRequestURI()` 读取路径和查询参数。
- `sendResponseHeaders(status, bytes.length)` 发送状态码和响应长度。
- `getResponseBody()` 写入响应字节并关闭输出流。

本例端口使用 0，由操作系统分配未占用端口，避免固定端口冲突；启动后打印实际端口但没有外部客户端连接。若想手动 curl，需要改为固定端口并在收到请求后再 stop，属于练习扩展。

## 6. 与 aic-teaching-service 对照

目标项目的 `TeacherManagementController` 使用 `@RestController`、`@PostMapping`、`@GetMapping`、请求 DTO 和统一 `Result` 响应。Spring Boot 会替你处理服务器生命周期、参数绑定、JSON 序列化和异常转换；今天先手动看到这些边界。

完整调用链通常是：

```text
HTTP 请求 → Controller 参数绑定 → Service 业务校验
→ Mapper/Repository 查询 → DTO/Result 响应 → HTTP 响应
```

本课的 Handler 直接返回固定数据，故意跳过数据库和权限，不代表完整后端实现。

## 7. 运行预期

```text
HTTP 服务已启动：<随机端口>
GET /teachers?id=10001 → 200
GET /teachers → 400
HTTP 服务已停止
```

如果需要真正观察状态码，可把 `server.stop(0)` 暂时移动到输入等待之后，用浏览器或 curl 访问，再恢复自动停止。不要在共享端口上留下学习服务。

## 8. 今日练习与验收

- [ ] 能区分方法、路径、查询参数和请求体。
- [ ] 能解释 200、400、404、405 的基本含义。
- [ ] 能区分 HTTP 状态码和 JSON 业务码。
- [ ] 能说明 GET 查询与 POST 写入的契约差异。
- [ ] 能读懂 Handler 如何生成 JSON 响应。
- [ ] 能把 Controller → Service → Repository 调用链与目标项目对应起来。

下一课：IoC 与依赖注入，把 Repository/Service 从手动 new 改为可替换组件。
