# Day 24：断点调试与日志

## 1. 今日目标

Day23 用测试判断结果对不对，今天用调试器观察“程序怎么走到这个结果”，用日志和异常栈解释失败原因。

打开 [Day24Debugging.java](../java-basics/src/Day24Debugging.java)，复用 [TeacherDirectory.java](../java-basics/src/TeacherDirectory.java) 和原有 Teacher/TeacherRepository，不修改旧课程。

运行三种固定输入：10002 查询成功、99999 无匹配、abc 解析失败。没有键盘交互、数据库或外部服务。

## 2. 先 Run，观察输出

点击 main 左侧绿色三角，选择 Run。控制台依次包含：

- INFO 开始查询、查询成功。
- WARNING 没有匹配教师。
- SEVERE 演示异常及完整异常栈。
- 最后一行：Day24 演示结束：成功、无结果和非法输入均已处理。

日期、时间、堆栈行号因运行环境或编辑而变化。红色日志不一定表示程序崩溃：本例刻意触发并捕获错误，之后仍正常结束。

日志一般写 stderr，println 写 stdout，两条输出通道混合显示时顺序可能变化，不要仅凭控制台颜色或最终一行判断根因。

## 3. 第一次设置断点

1. 在 queryTeacher 中找到 `long id = parseTeacherId(idText);`。
2. 点击这一行左侧行号旁的空白区域，出现红点。
3. 点击 main 左侧三角，选择 **Debug**，不是 Run。
4. 程序暂停后，看下方 Debug 工具窗口的 Variables。
5. 找到 idText，此次应该是字符串 "10002"。

断点停在某行通常表示该行尚未执行。因此此时不要期待局部变量 id 已经存在。输入字符串和解析后的 long 是两个不同变量。

若不停：确认使用 Debug、断点未禁用、运行入口正确，并确认编辑的源码已重新编译。

## 4. Step Over、Step Into、Step Out

| 操作 | 常见 Windows 默认快捷键 | 作用 |
|---|---|---|
| Step Over | F8 | 执行当前行，不进入其中的方法 |
| Step Into | F7 | 进入当前调用的方法 |
| Step Out | Shift+F8 | 执行到当前方法返回，回到调用方 |
| Resume Program | F9 | 继续直到下一个断点或程序结束 |

IDEA 快捷键受 Keymap 和笔记本 Fn 键影响，找不到时直接使用调试工具栏上对应名称按钮。

在 parseTeacherId 调用处按 F8，返回后应能看到 id=10002。想观察 Long.parseLong 之前的校验逻辑，就进入自己的 parseTeacherId；暂时不必钻进 JDK 内部实现。

## 5. 追踪一次查询

再在 `Teacher teacher = repository.findById(id);` 设置断点，停住后按 Step Into。

进入 TeacherDirectory.findById 时观察：

- id：10002。
- size：2。
- teachers：两个 Teacher 对象。
- i：循环下标，从 0 开始。

第一轮 ID 10001 不匹配；第二轮 ID 10002 匹配，返回李老师对象。按 Step Out 回到 queryTeacher，展开 teacher 查看 id 和 name，再观察 handleQuery 输出成功日志。

调用链：

```text
main
  → handleQuery
    → queryTeacher
      → TeacherDirectory.findById
```

左侧 Frames/调用栈展示当前暂停位置的调用关系。点击上层帧可查看上层局部变量，不代表程序倒着执行。

## 6. 条件断点与变量观察

只想看“查询不到”的场景：右键 queryTeacher 入口断点，在 Condition 填写：

```java
"99999".equals(idText)
```

继续执行，只有满足条件时才暂停。可在 Watches 添加 idText 或 directory.size() 等只读表达式。避免在 Evaluate Expression 或 Watches 中调用 add、rename 等修改数据的方法，否则调试过程会改变待观察状态。

不需要条件后清除它，防止下一次以为断点失效。

## 7. 怎样读异常栈

abc 输入会产生类似结构：

```text
java.lang.IllegalStateException: 教师查询未完成（教学故障演示）
    at Day24Debugging.handleQuery(...)
Caused by: java.lang.IllegalArgumentException: 教师 ID 解析或校验失败
    at Day24Debugging.parseTeacherId(...)
Caused by: java.lang.NumberFormatException: For input string: "abc"
    at java.lang.Long.parseLong(...)
```

从外到内分别是：查询失败的上下文、ID 校验失败的上下文、无法把 abc 转成 long 的直接原因。

先找到最内层 Caused by，再向上找自己的第一个源码位置，回看输入如何传到那里。最内层异常说明直接失败点，但业务根因还要结合上下文，例如错误数据来自哪里；不能只看异常类名就结束排查。

`... N more` 表示重复的栈帧被省略，不代表日志丢失。点击控制台中的文件名和行号通常可以跳转到源码。

可在 IDEA 的 View Breakpoints 中添加 Java Exception Breakpoint，选择 NumberFormatException，并启用 caught 异常暂停。这样即使稍后被 catch，也能在最初抛出时观察现场；练习后禁用，避免影响别的课程。

## 8. 日志代码逐段解释

```java
private static final Logger LOGGER =
        Logger.getLogger(Day24Debugging.class.getName());
```

使用 JDK 自带 java.util.logging，无需新增依赖。class.getName() 提供日志来源名称；static final 表示共享且不能重新赋值的日志器引用。

本课使用的等级：

| 等级 | 本例用途 |
|---|---|
| FINE | 查看解析后的 ID；默认控制台常不显示 |
| INFO | 查询开始和成功 |
| WARNING | 预期但需要关注的无匹配情况 |
| SEVERE | 为教学展示完整失败堆栈 |

日志等级由场景和项目约定决定，真实接口的非法输入不一定需要 SEVERE。JUL 的 FINE/SEVERE 与常见 SLF4J 的 DEBUG/ERROR 名字不同，不要直接混写。

```java
LOGGER.log(Level.INFO, "查询成功，teacherId={0}", teacher.getId());
LOGGER.log(Level.SEVERE, "演示异常", failure);
```

第一句使用 JUL 的 {0} 占位符，不是 SLF4J 的 {}。第二句把 Throwable 传进去，才会输出完整堆栈。只打印 failure.getMessage() 看不到 cause 和调用位置。

日志中避免放密码、Token、真实学生隐私或完整请求内容；本例只使用合成教师 ID。看到参数化日志中的数字被按地区格式分组时，不代表 ID 数值变化。

## 9. 保留 cause，在哪一层记录

```java
throw new IllegalArgumentException("教师 ID 解析或校验失败", cause);
```

第二个参数保存原始异常。上层补充上下文但仍保留链条，最后只在 handleQuery 输出完整日志，避免每层重复打印同一个错误。

这里为了展示异常链，额外构造了 IllegalStateException；它不是要求所有查询都包装两层异常。真实项目应遵守现有错误码、异常类型和全局处理约定。

## 10. 调试、日志、测试各做什么

- 调试器：在本地暂停程序，观察一次执行中的变量与分支。
- 日志：记录运行现场，帮助分析无法随意暂停的环境。
- 测试：自动验证预期行为，防止修复后再次出错。

本课没有新增模拟输出内容的测试，沿用 Day23 的业务测试回归。打印“成功”不能替代断言，测试通过也不能替代具体故障的现场证据。

## 11. 与目标项目的联系

阅读 aic-teaching-service 时，可以按 Controller → Service → Mapper 跟踪调用，在请求校验、权限范围和查询参数处观察变量。真实 HTTP、数据库调用受环境影响，本地单步暂停也会影响超时，不要把调试时耗时当作性能结果。

出现 Spring 启动失败时，查看完整异常链，定位具体配置、Bean 或依赖失败；最后一个 BUILD FAILURE 不是 Java 异常的具体原因。

## 12. 今日练习与验收

- [ ] 在 queryTeacher 设置断点，用 Debug 启动。
- [ ] 观察字符串 idText 如何变成 long id。
- [ ] 进入数组查询，观察两轮循环和返回对象。
- [ ] 用条件断点只停在 99999 场景。
- [ ] 从完整堆栈找到 NumberFormatException 与自己的调用位置。
- [ ] 解释日志等级过滤为何可能隐藏 FINE。
- [ ] 将输入 abc 改成 0，比较最内层原因：解析失败与数值校验失败。
- [ ] 运行 Day23 的测试，确认旧目录行为保持正确。

下一课：关系建模与 DDL，设计教师、课节、审核三张练习表。
