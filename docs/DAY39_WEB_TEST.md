# Day39：用 MockMvc 自动检查接口

今天不新增业务接口，给 Day37 的接口写自动化测试。以前用浏览器或 PowerShell 看结果，现在让测试代码判断结果是否正确。

## 1. 从这一段开始读

```java
mvc.perform(get("/api/day37/teachers/10001"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.name").value("张老师"));
```

- `perform`：执行一次模拟 MVC 请求。
- `get(...)`：请求方法和路径。
- `andExpect`：断言“实际结果应该是什么”，不符合就让测试失败。
- `status().isOk()`：要求 HTTP 200。
- `$.name`：JSONPath，`$` 是 JSON 根对象，`.name` 是它的 name 属性。

不是打印“看起来成功”就算通过，而是代码会自动比较。

## 2. 怎么运行

1. IDEA 重新加载 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml)，新增依赖 `spring-boot-starter-test`，scope 为 test。
2. 打开 [Day39WebTest.java](../spring-boot-lab/src/test/java/learning/boot/day39/Day39WebTest.java)。
3. 点击类名左侧绿色三角运行整个测试类，JDK 选择 8。
4. 预期 **6 个测试通过**。不需要先运行 Day33Application，也不需要数据库。

安装 Maven 后可执行：

```powershell
Set-Location E:\my\javaProjects\spring-boot-lab
mvn -Dtest=Day39WebTest test
```

测试位于 `src/test/java`，正式代码位于 `src/main/java`。test 依赖不作为应用运行依赖打进可执行包。

## 3. MockMvc 和 MockBean 不是同一件事

```java
@WebMvcTest(Day37ExceptionController.class)
class Day39WebTest {
    @Autowired private MockMvc mvc;
    @MockBean private Day37TeacherService service;
}
```

`WebMvcTest` 加载 MVC 测试切片，包含本课 Controller、请求转换、校验和相关 Advice；不加载完整业务应用。`MockMvc` 经过 Spring MVC 处理流程，但不监听真实 HTTP 端口。

`MockBean` 把 Service 替换为 Mockito 模拟对象。它不会执行真实 Service 的方法体，需要我们设置行为：

```java
when(service.find(10001L))
    .thenReturn(new TeacherResponse(10001L, "张老师", "Java"));
```

意思是：“测试期间，如果调用这个方法并传入 10001，就返回指定对象。”这不是向数据库插入教师。

```java
verify(service).find(10001L);
```

验证 Controller 确实以该编号调用了 Service。`verifyNoInteractions(service)` 则验证路径参数无法转换时，业务层没有被调用。

## 4. 六个测试分别证明什么

| 测试 | 验证点 |
| --- | --- |
| returnsTeacherJson | 路由、编号绑定、JSON 姓名、无内部备注 |
| convertsBusinessFailure | Service 抛业务异常后返回 404 和业务码 |
| rejectsInvalidField | 空姓名触发 400，errors 中包含 name |
| rejectsInvalidPathBeforeService | abc 无法转 long，返回 400，未调用 Service |
| rejectsBrokenJson | 不完整 JSON 返回 INVALID_BODY |
| hidesUnexpectedDetails | 未知异常返回 500，含 requestId，不泄露内部文本 |

`thenThrow(...)` 用来模拟异常；它不代表真实业务一定会在该输入下抛异常，只验证“如果抛了，Web 层如何处理”。错误顺序不固定，因此使用 `hasItem("name")`，不把 name 必须在第一项写死。

模拟未知异常时控制台出现 ERROR 堆栈是预期实验结果。判断测试是否失败，应看测试断言及 Maven 的 Failures/Errors 汇总。

## 5. 测试失败怎样看

先看失败的测试名，再比较 Expected（期望）与 Actual（实际）。必要时临时加 `.andDo(print())` 打印请求与响应，需静态导入 `MockMvcResultHandlers.print`。

练习：把“张老师”的预期改成“李老师”，运行并观察失败，之后恢复。不要为了让测试变绿而删除断言。

## 6. 能证明什么，不能证明什么

这六个测试证明 Web 层协议在当前测试配置下成立；不验证真实 Service 查询、SQL、事务、数据库驱动、网络监听或浏览器跨域。

Day40 会用 `@SpringBootTest + @AutoConfigureMockMvc` 加载真实 Service 和内存仓库。即使那时测试通过，仍不代表 MySQL 查询正确。

对照目标项目时，可查看 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\test` 的测试目录；本课不调用其私有依赖或宣称其测试通过。

## 7. 验收

- [ ] 能独立运行 6 个测试。
- [ ] 能解释 perform、andExpect 和 jsonPath。
- [ ] 能区分 MockMvc 和 MockBean。
- [ ] 能指出哪些逻辑是真实执行，哪些行为是预设的。
- [ ] 能说明测试通过为何不能证明数据库正确。

## 8. 本次验证记录

2026-11-26：使用 JDK 8 和临时 Maven 3.9.9 执行本模块 package。Day39 的 6 个测试和 Day40 的 2 个测试全部通过：Tests run: 8, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS。未实际操作 IDEA，未验证真实数据库。临时 Maven 可执行文件不代表系统 PATH 已配置 mvn。

下一课：[Day40 HTTP 综合练习](DAY40_HTTP_LAB.md)。
