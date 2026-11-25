# Day 23：JUnit 单元测试

## 1. 今天解决什么问题

前面的 main 打印结果，需要你自己看是否正确。JUnit 用断言自动判断结果是否符合预期，后续修改代码时可以重新运行，及时发现行为变坏。

本课测试 Day10 的 TeacherDirectory，不连接数据库或外部服务。

- [测试文件 TeacherDirectoryTest.java](../java-basics/tests/TeacherDirectoryTest.java)
- [被测类 TeacherDirectory.java](../java-basics/src/TeacherDirectory.java)
- [Maven 配置](../java-basics/pom.xml)

## 2. 先在 IDEA 运行

1. 将 java-basics/pom.xml 加载为 Maven 项目；已加载时点击 Maven 面板的 Reload。
2. 等待 JUnit 依赖下载完成。
3. 打开 tests/TeacherDirectoryTest.java。
4. 点击类名左侧绿色三角，运行全部测试；也可以点击某个方法旁的三角只运行一个测试。
5. 查看测试面板中的测试数量和失败信息。

测试类不需要 main，也不需要 Scanner 或键盘输入。JUnit 负责发现和调用带 @Test 的方法。

如果 @Test 或 Assertions 标红，先检查 Maven 同步及 tests 是否识别为测试源码目录，不要把测试依赖改成主源码依赖来绕过问题。

## 3. 读懂第一个测试

```java
@Test
void addAndFindTeacher() {
    TeacherDirectory directory = new TeacherDirectory(2);
    directory.add(10001L, " 张老师 ");
    Teacher found = directory.findById(10001L);
    assertNotNull(found);
    assertEquals("张老师", found.getName());
    assertEquals(1, directory.size());
}
```

| 写法 | 含义 |
|---|---|
| @Test | 标记这是一个 JUnit 测试方法 |
| void | 不需要返回值；断言失败时抛出断言错误 |
| new TeacherDirectory(2) | 准备本测试自己的目录 |
| directory.add(...) | 执行要验证的操作 |
| assertNotNull(found) | 自动检查确实找到了教师 |
| assertEquals("张老师", found.getName()) | 期望值在前，实际值在后 |
| assertEquals(1, directory.size()) | 检查新增数量 |

这是 Arrange / Act / Assert：准备数据 → 执行动作 → 断言结果。

每个测试新建目录，不依赖别的测试先执行，也不共享可变静态数据。JUnit 不保证按文件中的书写顺序执行。

## 4. 断言与打印的区别

```java
System.out.println(directory.size()); // 只显示数字
assertEquals(1, directory.size());     // 不是 1 就让测试失败
```

打印出 2 仍可能正常退出；断言发现 2 会使该测试失败，并展示 expected 与 actual。

常用断言：

| 方法 | 判断 |
|---|---|
| assertEquals(expected, actual) | 实际值是否符合期望 |
| assertTrue(condition) | 条件是否为 true |
| assertFalse(condition) | 条件是否为 false |
| assertNull(value) | 是否为 null |
| assertNotNull(value) | 是否非 null |
| assertThrows(Type.class, action) | 执行动作是否抛出指定类型或其子类异常 |

测试源码的 import static 允许直接写 assertEquals，不必每次都写 Assertions.assertEquals。这是静态导入，不是新的断言语法。

## 5. 逐段理解异常断言

```java
assertThrows(IllegalArgumentException.class,
        () -> directory.add(10001L, "李老师"));
```

- IllegalArgumentException.class：传入期望的异常类型信息，不是创建异常对象。
- ()：这个 Lambda 没有输入参数。
- -> 后面：要交给 JUnit 执行的操作。
- assertThrows：执行它，检查是否抛出符合要求的异常。

操作没有抛异常，或抛了不匹配的类型，测试就失败。不要自己 catch 后什么都不检查，那样没抛异常也可能“通过”。

## 6. 为什么异常后还要继续断言

```java
assertThrows(IllegalArgumentException.class,
        () -> directory.add(10001L, "李老师"));
assertEquals(1, directory.size());
assertEquals("张老师", directory.findById(10001L).getName());
```

仅检查“抛了异常”还不够：错误实现可能先覆盖姓名，再抛异常。这两条补充断言证明原有数量和姓名没有被破坏。

本课六个测试覆盖：

| 测试 | 业务行为 |
|---|---|
| addAndFindTeacher | 正常新增，姓名去掉两端空格，数量正确 |
| missingTeacherReturnsNull | 空目录查询返回 null |
| duplicateIdDoesNotReplaceOriginal | 拒绝重复 ID，保留原教师 |
| blankNameDoesNotConsumeCapacity | 空姓名失败不占用容量，随后仍可成功新增 |
| fullDirectoryPreservesExistingData | 满容量拒绝新增，已有数据不变 |
| invalidRenameKeepsOldName | 非法改名保留旧姓名，合法改名仍能成功 |

这些验证的是对外行为，不直接检查 private 数组的内部布局，因此以后改用 Map 仍可以保留同类业务测试。

## 7. Maven 配置与目录

POM 新增 JUnit Jupiter 5.10.3，兼容 Java 8；scope 为 test，只供测试编译和执行使用。Surefire 3.2.5 负责运行 JUnit 测试。

沿用现有自定义目录：

```text
java-basics/
  src/       主源码
  tests/     JUnit 测试源码
  pom.xml
  target/
    classes/
    test-classes/
    surefire-reports/
```

当前 sourceDirectory 是 src，所以将 tests 放在它外面，并显式配置 testSourceDirectory=tests，避免主源码扫描到测试类。标准 Maven 工程一般使用 src/main/java 和 src/test/java，本课不迁移已有文件。

配置 failIfNoTests=true，使本模块未发现测试时失败，避免把“根本没跑测试”误认为通过。它不是所有 Maven 工程的默认设置。

## 8. 命令行运行

Maven 在 PATH 中时：

```powershell
cd E:\my\javaProjects\java-basics
mvn test
mvn '-Dtest=TeacherDirectoryTest' test
```

第二条只选择指定测试类。看日志中的 Tests run、Failures、Errors、Skipped，以及 BUILD SUCCESS；报告位于 target/surefire-reports。

本次当前会话仍不能直接调用 mvn，所以使用下载到系统临时目录的 Maven 3.9.9 验证，不改系统 PATH。临时目录可能被系统清理，平时优先使用 IDEA Maven 面板或自己安装的 Maven。

本次实际执行结果（2026-11-25）：Maven 3.9.9 执行 `-B -f java-basics/pom.xml test`，报告为 `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`，最终 `BUILD SUCCESS`。这是实际执行的 JUnit 测试，不是仅使用 javac 编译。

## 9. 亲手制造一次失败

将第一个测试中数量期望临时改为 2，运行该测试：

```java
assertEquals(2, directory.size());
```

应显示 expected 2、actual 1。确认你能看懂失败后恢复成 1，再运行全部测试。不要为了让测试变绿随意改生产代码；先确认期望是否符合业务规则。

## 10. 验证范围

这批测试是单元测试：验证内存教师目录的业务行为。它们不能证明数据库 SQL、Spring 启动、HTTP 权限或生产部署正确，覆盖率也不能替代业务场景判断。

对照 aic-teaching-service 的测试时，先区分纯 Java 测试和依赖数据库/应用上下文的集成测试，再决定环境和运行方式。

## 11. 今日练习与验收

- [ ] 不用 main，运行六个测试并查看计数。
- [ ] 能解释期望值与实际值的位置。
- [ ] 能解释 () -> 操作怎样交给 assertThrows。
- [ ] 临时制造一个失败并恢复，记录错误信息。
- [ ] 添加“容量为 0 时拒绝创建”的测试。
- [ ] 添加“对不存在教师改名不会改变已有教师”的测试。
- [ ] 能说明单元测试没有验证哪些外部系统行为。

下一课：断点调试与日志，从调用链和异常栈定位问题。
