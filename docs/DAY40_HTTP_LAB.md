# Day40：教师新增、查询、更新闭环

今天把 Day31～39 串起来：HTTP、Boot、参数绑定、DTO、校验、异常处理和测试。与之前的 preview 不同，新增与更新会真正修改**当前 Java 进程的内存数据**。

## 1. 接口约定

| 操作 | 请求 | 成功状态 |
| --- | --- | --- |
| 新增教师 | POST `/api/day40/teachers` | 201，响应含 id，Location 指向查询地址 |
| 查询教师 | GET `/api/day40/teachers/{id}` | 200 |
| 更新教师 | PUT `/api/day40/teachers/{id}` | 200 |

新增和更新的 JSON 都包含 name、subject，使用 Day36 的必填、长度规则。PUT 要提供这两个字段，不是只改一个字段的 PATCH。不存在的编号返回 404，更新不会偷偷创建记录；非法输入返回 400。

没有删除、列表或分页接口，这次只完成计划中的新增、查询、更新闭环。重复姓名允许存在；重复 POST 会创建新编号，不具备幂等去重。

## 2. 启动

IDEA 同步 POM，运行 Day33Application.main，模块 spring-boot-lab、JDK 8，Program arguments：

```text
--spring.profiles.active=local --server.port=8081
```

停止旧进程再启动。服务最初没有 Day40 教师；不要假设前一课固定示例的 10001 已经存在于本课仓库。

## 3. PowerShell 完整操作

在服务运行后，另开 PowerShell，按顺序执行：

```powershell
$base = 'http://127.0.0.1:8081/api/day40/teachers'
$createBody = @{ name = '张老师'; subject = 'Java' } | ConvertTo-Json
$teacher = Invoke-RestMethod -Method Post -Uri $base `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($createBody))
$teacher

# 必须使用实际返回的编号。
$teacherId = $teacher.id
Invoke-RestMethod -Uri "$base/$teacherId"

$updateBody = @{ name = '李老师'; subject = '数学' } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$base/$teacherId" `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($updateBody))

# 再查一次，确认修改已经进入内存仓库。
Invoke-RestMethod -Uri "$base/$teacherId"
```

第一次查询得到张老师/Java；更新后查询得到同一 id 的李老师/数学。不会返回 internalRemark。

已保存 [day40.http](../spring-boot-lab/requests/day40.http)，可在支持 `.http` 的接口客户端运行。若 IDEA 没有 HTTP Client，可直接使用上面的 PowerShell。文件中的 teacherId 需改成 POST 实际返回值。

## 4. 源码调用链

```text
HTTP 请求 → Day40TeacherController → TeacherCrudService
         → MemoryTeacherRepository → TeacherEntity
         → TeacherConverter → TeacherResponse → JSON
```

- [Controller](../spring-boot-lab/src/main/java/learning/boot/day40/Day40TeacherController.java)：接收参数，@Valid 校验，新增返回 201 与 Location。
- [Service](../spring-boot-lab/src/main/java/learning/boot/day40/TeacherCrudService.java)：组织新增/查询/更新，不存在时抛 TeacherNotFoundException。
- [Repository](../spring-boot-lab/src/main/java/learning/boot/day40/MemoryTeacherRepository.java)：保存内存记录。

本课复用 Day35 Entity/Response/Converter、Day36 校验请求类、Day37 异常协议。Day37 Advice 的作用范围扩展到 Day40，其他课程范围不变。历史 Day37 文档描述的是当日范围。

## 5. 内存仓库逐句理解

```java
private final AtomicLong sequence = new AtomicLong(10000);
private final ConcurrentHashMap<Long, TeacherEntity> teachers = new ConcurrentHashMap<>();
```

sequence 负责递增编号，AtomicLong 避免并发递增时重复取号。Map 的 key 是教师 id，value 是教师对象。ConcurrentHashMap 支持并发操作，但不代表自动具有数据库事务能力。

```java
long id = sequence.incrementAndGet();
teachers.put(id, entity);
```

先生成编号，再保存对象。编号由服务端生成，不由请求体决定。

```java
teachers.computeIfPresent(id, (key, old) ->
    new TeacherEntity(key, name, subject, old.getInternalRemark()));
```

意思是：“如果有这个编号，就使用旧对象的信息构造新对象并替换；如果没有，就不创建。”key 是编号，old 是原对象；保留内部备注，只替换允许更新的字段。相同编号的这次 Map 更新是原子的，读者不会看见姓名改了但科目还没改的半更新对象。

并发更新仍采用后写覆盖，不含版本冲突检测。程序重启数据与计数器重置，不同服务进程也不共享这些数据。不要将它当作持久化方案或真实数据库并发模型。

## 6. 错误实验

- POST 空姓名：400，VALIDATION_ERROR，不进入新增业务。
- PUT 已有编号但传 `{}`：400，再 GET，应仍是此前的有效数据。
- PUT/GET `-1`：404，TEACHER_NOT_FOUND，更新不能创建该编号。
- 请求体中多传 id、internalRemark：请求 DTO 不接收它们，服务端决定编号和内部字段。

查看失败响应可沿用 Day36/37 的 HttpClient 示例。PowerShell 的 Invoke-RestMethod 遇到 400/404 会抛出错误，这是请求失败的正常表现。

## 7. 自动化验证

[Day40FlowTest.java](../spring-boot-lab/src/test/java/learning/boot/day40/Day40FlowTest.java) 使用：

```java
@SpringBootTest
@AutoConfigureMockMvc
```

加载真实应用上下文，使用真实 Service 和内存 Repository，不使用 MockBean；MockMvc 仍不监听真实端口。

两个测试覆盖：

1. 创建 201、Location、读取、更新、非法更新被拒绝、再读取确认数据未被非法更新破坏。
2. 更新不存在编号返回 404，随后查询仍为 404。

测试读取创建响应中的 id，不假设每个测试都从 10001 开始。安装 Maven 后运行：

```powershell
Set-Location E:\my\javaProjects\spring-boot-lab
mvn test
mvn package
```

当前 Day39 为 6 个测试、Day40 为 2 个，共 8 个。package 也会执行测试，平时按需要选择命令即可，不必每次连续重复运行。

## 8. 阶段验收

- [ ] 能从 Controller 追到 Service、Repository、转换器。
- [ ] 新增→查询→更新→再查询成功。
- [ ] 请求不合法时返回 400，不破坏已有数据。
- [ ] 查询/更新不存在数据返回 404。
- [ ] 内部备注不会出现在响应里。
- [ ] 能说明切片测试、完整上下文测试和真实 HTTP 验证的区别。
- [ ] 能解释重启为什么丢失数据，以及为什么下一阶段需要数据库。

## 9. 本次验证记录

2026-11-26：JDK 8 + 临时 Maven 3.9.9 执行 package，8 个自动化测试全部通过，无失败、错误或跳过，BUILD SUCCESS。

另启动打包 JAR，在随机空闲端口完成 9 次真实 HTTP 请求，验证新增 201 与 Location、额外字段隔离、查询、更新后再查询、非法更新不改变数据、不存在编号更新与查询均返回 404，以及 Day37 错误处理回归。全部通过，验证进程已停止。未运行真实数据库、未实际操作 IDEA；未做并发负载测试或重启恢复测试。

下一课 Day41：MyBatis 映射，用 Mapper/XML 连接持久化层。目标项目中的 Mapper、数据库配置和真实 SQL 需单独验证，不能用本课内存测试代替。
