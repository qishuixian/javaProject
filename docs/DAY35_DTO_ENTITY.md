# Day35：为什么要分 Request、Entity 和 Response

今天只解决一个问题：**前端提交的字段、数据库保存的字段、返回给前端的字段，为什么不直接共用一个类？**

沿用 Day33 的 Spring Boot 工程和 Day34 的 JSON 参数绑定，不需要 MySQL。今天的 Entity 只是模拟持久化实体，没有实际建表或保存。

## 1. 先看一个具体例子

前端填写教师姓名、科目；服务端内部还有编号和备注。但前端不应该决定内部备注，也不需要收到它。

| 类 | 用途 | 本课字段 |
| --- | --- | --- |
| TeacherCreateRequest | 接收前端提交的数据 | name、subject |
| TeacherEntity | 表达内部持久化数据 | id、name、subject、internalRemark |
| TeacherResponse | 返回给前端的数据 | id、name、subject |

**DTO（Data Transfer Object）就是数据传输对象。Request 和 Response 都可以是 DTO。** DTO 不是一种 Java 关键字，也不一定要把类名写成 XxxDTO。

Entity 通常对应数据库表中的记录，但类名以 Entity 结尾不会自动建表、保存数据。本课没有数据库依赖。

可以类比前端：表单提交对象、后端原始记录、页面需要的展示对象，字段不一定相同。分开以后，可以明确每个边界允许哪些字段。

## 2. 先运行看看

1. IDEA 将 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml) 添加为 Maven 项目并同步。
2. 仍运行 [Day33Application.main()](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)。模块选 **spring-boot-lab**，JRE 选 **JDK 8**。
3. Program arguments 设置为 `--spring.profiles.active=local --server.port=8081`。
4. 停止旧进程，重新启动。
5. 打开 <http://127.0.0.1:8081/api/day35/teachers/example>。

预期返回：

```json
{"id":10001,"name":"张老师","subject":"Java"}
```

代码里明明有内部备注“仅供内部教学演示”，但 JSON 没有 internalRemark。不是前端把它藏起来了，而是后端转换后的响应对象根本没有这个字段。

## 3. 按这条链看源码

```text
GET /api/day35/teachers/example
  → Day35TeacherController.example()
  → TeacherLayerService.example()
  → 创建模拟 TeacherEntity
  → TeacherConverter.toResponse(entity)
  → 返回 TeacherResponse
  → Jackson 转成 JSON
```

文件都在 `spring-boot-lab/src/main/java/learning/boot/day35/`：

- [Day35TeacherController.java](../spring-boot-lab/src/main/java/learning/boot/day35/Day35TeacherController.java)：接收 HTTP 请求。
- [TeacherLayerService.java](../spring-boot-lab/src/main/java/learning/boot/day35/TeacherLayerService.java)：组织处理过程。
- [TeacherEntity.java](../spring-boot-lab/src/main/java/learning/boot/day35/TeacherEntity.java)：内部数据模型。
- [TeacherConverter.java](../spring-boot-lab/src/main/java/learning/boot/day35/TeacherConverter.java)：显式选择响应字段。
- [TeacherResponse.java](../spring-boot-lab/src/main/java/learning/boot/day35/TeacherResponse.java)：对外返回结构。
- [TeacherCreateRequest.java](../spring-boot-lab/src/main/java/learning/boot/day35/TeacherCreateRequest.java)：POST 请求结构。

day35 是 `learning.boot` 的子包，仍在启动类的扫描范围内。文件拆开是为了看清各自职责，不是每一行代码都必须拆成一个类。

## 4. 最重要的转换语句，逐个看

```java
return new TeacherResponse(entity.getId(), entity.getName(), entity.getSubject());
```

1. `entity.getId()`：从内部对象取出编号。
2. `entity.getName()`：取出姓名。
3. `entity.getSubject()`：取出科目。
4. `new TeacherResponse(...)`：用这三个值创建一个新的响应对象。
5. `return`：把这个新对象交给调用方。

这里没有取 `entity.getInternalRemark()`，所以不会把内部备注传入响应对象。

这不是强制类型转换，不能写 `(TeacherResponse) entity` 来代替。两个类没有继承关系，字段相似也不代表对象类型相同。

```java
TeacherEntity entity = new TeacherEntity(10001L, "张老师", "Java", "仅供内部教学演示");
return TeacherConverter.toResponse(entity);
```

第一行创建内部对象；第二行调用转换方法。`toResponse` 是我们起的方法名，不是 Spring 的魔法。它是 static 方法，所以直接通过类名调用，不需要创建 Converter 对象。

本例 Entity 和 Response 用构造方法赋值、getter 读取，没有 setter。响应序列化只需要读取数据，不要求为了输出 JSON 再添加无参构造方法。Request 则使用普通 JavaBean 的 setter 接收数据；它没声明构造方法，Java 自动提供无参构造方法。

## 5. POST：请求对象也要单独设计

PowerShell 中执行：

```powershell
$body = @{ name = '李老师'; subject = '数学' } | ConvertTo-Json
Invoke-RestMethod `
  -Method Post `
  -Uri 'http://127.0.0.1:8081/api/day35/teachers/preview' `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($body))
```

预期 JSON 内容：

```json
{"id":null,"name":"李老师","subject":"数学"}
```

PowerShell 可能把 null 显示为空白。preview 是预览接口，没有保存，所以没有新编号，id 为 null；这不表示创建成功。再次 GET example 仍会得到固定的张老师示例。

对应处理过程：

```java
TeacherEntity entity = new TeacherEntity(null, request.getName(),
        request.getSubject(), "待内部审核");
return TeacherConverter.toResponse(entity);
```

前端只提供 name 和 subject；内部备注由服务端提供。实际项目的新增流程还会调用仓库保存、生成编号；今天不引入这些步骤。

## 6. 前端多传字段会怎么样

把请求改成：

```json
{"id":99999,"name":"李老师","subject":"数学","internalRemark":"客户端改写"}
```

本课 Boot/Jackson 默认配置会忽略请求类中没有的属性：id 仍然为 null，响应仍然只有 id、name、subject。它不会把额外字段偷偷写进 Entity。

这是当前配置的行为，不代表所有框架或项目都会忽略未知字段。有的项目会选择拒绝这种请求。重点是：请求 DTO 只声明允许接收的字段，转换代码只读取它们，内部字段由服务端控制。

但 DTO 分层不等于已经完成权限检查或参数校验。今天发送 `{}` 仍会得到 name、subject 为 null 的预览；姓名必填等规则留到 Day36。

## 7. 为什么不直接返回 Entity

如果 Controller 直接返回本课 TeacherEntity，由于它有公开的 `getInternalRemark()`，默认 JSON 序列化会把内部备注也输出。`private` 只限制 Java 字段访问，不等于禁止 Jackson 通过 getter 输出属性。

独立 Response 的作用：

- 明确哪些字段可以对外输出。
- 内部新增字段时，不会自动增加接口响应字段。
- 数据库字段名和前端字段名不必一一相同。

例如数据库字段叫 departmentName，响应可以叫 department，转换时手动对应即可。Response 是输出边界，不是隐藏字段的唯一技术，但最容易直观看懂。

## 8. 对照真实项目

阅读 `E:\code\java\aic-teaching-service` 中以下文件：

| 路径（相对目标项目） | 本次确认的职责与字段 |
| --- | --- |
| `aic-teaching-service-api/src/main/java/cn/vipthink/aic/teaching/api/dto/TeacherListRequest.java` | 查询请求，包含 bizLine、pageNum、pageSize |
| `aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/domain/entity/TeacherProfileEntity.java` | `@TableName("teacher_profile")` 映射实体，含 id、teacherName、departmentName |
| `aic-teaching-service-api/src/main/java/cn/vipthink/aic/teaching/api/dto/TeacherListItemResponse.java` | 列表响应，含 teacherName、department、group、team |

这里已经能看出请求、实体、响应的字段并不相同。本课只对照这些模型定义，没有据此宣称目标服务的每个字段如何完成映射，也没有启动目标服务。

## 9. 练习与验收

- [ ] 能解释 Request、Entity、Response 分别在哪里使用。
- [ ] 能逐句解释 `new TeacherResponse(...)`。
- [ ] GET 响应不含 internalRemark。
- [ ] POST 中文姓名、科目能正确返回，id 为 null。
- [ ] 多传 id、internalRemark，不能改变服务端内部字段来源。
- [ ] 只给 Entity 加一个内部字段，响应 JSON 不会自动变化。
- [ ] 如果要公开新字段，能指出需要修改 Response 和 Converter，并提供对应数据。

## 10. 本次验证记录

2026-11-26：JDK 8 配合临时目录中的 Maven 3.9.9，执行本模块 `package`，结果 `BUILD SUCCESS`。模块没有单元测试，日志显示 `No tests to run`。

实际启动 JAR，使用随机空闲端口完成 7 次 HTTP 检查：固定示例及准确响应字段、中文预览、额外字段忽略、空对象边界、预览不修改示例、Day33 与 Day34 接口回归，全部通过。检查确认响应只有 id、name、subject，没有 internalRemark。验证进程已停止；未连接数据库，也未实际操作 IDEA。

下一课 Day36：参数校验，让必填字段、长度和数值范围有明确规则。
