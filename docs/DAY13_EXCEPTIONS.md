# Day 13：异常机制

## 今日目标

区分三类失败：输入不合法、业务结果不满足条件、程序运行时故障。掌握 `throw`、`try/catch`、`finally`、自定义异常和异常继承关系。

入口：[Day13Exceptions.java](../java-basics/src/Day13Exceptions.java)，依赖 [TeacherNotFoundException.java](../java-basics/src/TeacherNotFoundException.java)。在 IDEA 点击 main 左侧绿色三角运行。

## 1. 异常是什么

异常是程序执行过程中出现的非正常情况。方法遇到无法继续处理的问题，可以抛出异常；调用方可以捕获并决定提示、转换、重试或继续向上交给框架。

```java
throw new IllegalArgumentException("教师 ID 必须大于 0");
```

`throw` 立即结束当前方法的正常流程，把异常对象交给调用栈上能处理它的代码。后面的普通语句不会执行。

## 2. try/catch

```java
try {
    findTeacher(directory, 99999L);
} catch (TeacherNotFoundException exception) {
    System.out.println(exception.getMessage());
}
```

try 放可能失败的代码，catch 指定捕获类型。异常类型匹配后执行 catch，程序可以继续执行后面的语句。

catch 应该尽量具体。`TeacherNotFoundException` 是 `RuntimeException` 的子类，若先 catch RuntimeException，后面再 catch TeacherNotFoundException 会编译失败，因为后者已经被前者覆盖。

不要无条件 `catch (Exception)` 后只打印“失败”。这样会隐藏空指针、配置错误和真正的程序缺陷。只有知道如何处理的异常才在当前层捕获；不知道如何恢复时让它继续向上抛出。

## 3. 输入失败、业务失败、运行时失败

本课示例分别演示：

| 场景 | 示例 | 类型 | 常见处理 |
|---|---|---|---|
| 输入不合法 | ID 为 0、仓库为 null | IllegalArgumentException | 返回参数错误，修正请求 |
| 业务结果不存在 | 查询 99999 没有教师 | TeacherNotFoundException | 返回明确的业务提示或接口错误 |
| 程序运行时故障 | Integer null 自动拆箱 | NullPointerException | 修复代码，不能靠吞异常掩盖 |

这些分类是教学示例。真实接口还需要结合项目全局异常处理、错误码和日志策略。

## 4. 自定义业务异常

```java
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(long id) {
        super("教师不存在：" + id);
    }
}
```

`extends RuntimeException` 表示它是运行时异常。构造方法调用 `super(...)`，把错误消息交给父类保存，调用 `getMessage()` 可以读取。

使用：

```java
if (teacher == null) {
    throw new TeacherNotFoundException(id);
}
```

“不存在”不是 JVM 崩溃，而是查询结果不满足本次业务要求。自定义类型让调用方可以只捕获这种业务情况，而不把所有运行时问题都当成“教师不存在”。

## 5. finally

```java
try {
    directory.add(10001L, "李老师");
} catch (IllegalArgumentException exception) {
    System.out.println("新增失败");
} finally {
    System.out.println("新增操作已结束");
}
```

`finally` 通常无论 try 是否成功、是否进入 catch 都会执行，适合释放文件、连接等资源。今天没有真实资源，所以只打印结束信息。

如果 finally 中再次抛异常或 return，可能覆盖 try/catch 中的异常和返回值，不建议在 finally 中写这些控制流程。文件、数据库连接等资源优先使用 try-with-resources，后续 JDBC 课程会学习。

## 6. NullPointerException 与包装类型

```java
Integer count = null;
int value = count;
```

第二行发生自动拆箱，需要调用 `Integer` 的 int 值，但 count 是 null，因此抛出 NullPointerException。解决方式是先判空、提供默认值或重新设计字段含义：

```java
int value = count == null ? 0 : count;
```

不能用 catch 代替空值设计。Day2 已学习基本类型与包装类型的区别，今天把错误表现串起来。

## 7. 运行预期

```text
查询结果：张老师
业务失败：教师不存在：99999
输入失败：教师 ID 必须大于 0
新增失败：教师 ID 已存在
新增操作已结束
系统调用参数失败：查询仓库不能为 null
运行时失败：包装类型 null 不能拆箱
```

注意：catch 只包住当前操作，捕获查询不存在后，main 仍会继续测试后面的场景。

## 8. 与目标项目的联系

aic-teaching-service 有业务异常、全局异常处理和参数校验。阅读 Controller 时关注：参数校验异常在哪里转换为响应；Service 抛出的业务异常是否记录日志、返回什么业务码；未知异常是否保留完整 cause。不要只看 catch 后的提示，还要追踪异常是否被吞掉。

目标项目的事务方法中，如果异常被捕获并不再抛出，事务是否回滚要结合 Spring 事务规则验证；本课不直接修改真实 Service。

## 9. throws、cause 与资源释放

`throw` 是实际抛出一个异常对象，`throws` 写在方法签名上，声明方法可能抛出的异常类型。

```java
public static String readText(java.nio.file.Path path) throws java.io.IOException {
    try (java.io.BufferedReader reader =
            java.nio.file.Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
        return reader.readLine();
    }
}
```

IOException 是受检异常：调用方必须捕获或继续在签名上声明。RuntimeException 及其子类属于非受检异常，不强制声明或捕获。受检与非受检是 Java 的类型规则，不等同于“业务错误与系统错误”的分类。

try-with-resources 会在离开代码块时关闭实现 AutoCloseable 的资源，读取成功或抛异常都适用。若读取和关闭同时失败，关闭异常通常作为 suppressed exception 保存在主要异常上。上面是文件读取语法预览，今天的运行入口没有读写真实文件。

转换底层异常时保留 cause：

```java
try {
    readText(path);
} catch (java.io.IOException cause) {
    throw new IllegalStateException("读取教师文件失败", cause);
}
```

不要只复制 cause.getMessage() 而丢弃原异常对象；完整异常栈中的 Caused by 才能帮助定位原始故障。finally 也不是任何情况下都保证执行，例如进程被强制终止时不能依赖它完成清理。

## 10. 今日练习

1. 给 `TeacherNotFoundException` 增加一个带 cause 的构造方法并测试 `getCause()`。
2. 将非法 ID 的异常消息改成更明确的输入提示。
3. 练习捕获顺序：先写 `catch (RuntimeException)`，再写 `catch (TeacherNotFoundException)`，观察编译错误并恢复。
4. 将 `findTeacher` 的 null 仓库检查和 ID 检查分别测试，记录哪一步先抛出。
5. 把 Day10 控制台中重复的错误处理整理成具体的 catch，避免捕获所有 Exception。

## 11. 今日验收

- [ ] 能解释 throw 会如何改变方法执行流程。
- [ ] 能写 try/catch 捕获指定异常。
- [ ] 能说明为什么 catch 顺序要从具体到一般。
- [ ] 能区分参数错误、业务不存在和程序缺陷。
- [ ] 能说明 finally 的用途及风险。
- [ ] 能解释包装类型 null 拆箱的空指针原因。

下一课：Lambda、函数式接口和 Predicate，把筛选条件作为参数传递。
