# Day45：Spring 事务与回滚边界

今天通过六个真实数据库实验回答：**方法报错了，刚才插入的数据一定会回滚吗？** 答案是不一定，要看事务是否生效、异常类型和异常是否越过事务代理。

沿用 mybatis-lab，使用 H2，不连接 MySQL。今天以测试类作为运行入口，直接检查提交后的数据库状态，比只看日志更明确。

## 1. 怎么运行

IDEA 使用 mybatis-lab 模块、JDK 8，打开 [Day45TransactionTest.java](../mybatis-lab/src/test/java/learning/persistence/Day45TransactionTest.java)，点击类名旁的绿色三角运行，预期 6 个测试通过。无需先启动 main 或数据库服务。

安装 Maven 后也可在 mybatis-lab 运行：

```powershell
mvn -Dtest=Day45TransactionTest test
```

运行全部课程测试可用 `mvn test`。今天没有新增 HTTP 接口，原 main 仍是 Day41～44 的控制台示例。

## 2. 六种结果先看表

每种情况都会先插入一行，再观察方法结束后的数据库：

| 方法 | 情况 | 最终保留行数 |
| --- | --- | --- |
| succeed | 事务方法正常返回 | 1，提交 |
| runtimeFailure | RuntimeException 越过事务代理 | 0，回滚 |
| selfCall | 外层无事务，内部 this 调用事务方法 | 1，没有开启预期事务 |
| caughtInside | 内部抛出并捕获局部异常，正常返回 | 1，提交 |
| checkedFailure | 默认规则下抛受检 Exception | 1，提交 |
| checkedRollback | rollbackFor=Exception.class，抛受检异常 | 0，回滚 |

测试中的 assertThrows 捕获发生在代理调用返回到测试代码时；这不会阻止代理先处理回滚。与业务方法内部捕获异常不同。

## 3. 最简单的事务方法

[TransactionLessonService.java](../mybatis-lab/src/main/java/learning/persistence/TransactionLessonService.java)：

```java
@Transactional
public void runtimeFailure(String name) {
    insert(name);
    throw new IllegalStateException("模拟运行时异常");
}
```

调用路径：

```text
测试 → Spring 事务代理 → 开启事务 → 插入 → 抛异常
                    ← 判断回滚规则 ← 异常向外传播
                    → 回滚 → 异常交给测试
```

Transactional 来自 `org.springframework.transaction.annotation.Transactional`。本工程的数据源和事务管理由 Boot/MyBatis 集成提供，不需要手动操作 Connection.commit/rollback。

默认情况下，RuntimeException 及 Error 触发回滚；受检异常默认不触发。回滚是数据库撤销本次事务修改，不是 Java 自动把所有变量恢复原状，也不会撤销已经发出的邮件或外部 HTTP 请求。

## 4. 为什么 this 调用失效

```java
public void selfCall(String name) {
    this.runtimeFailure(name);
}
```

selfCall 自己没有事务，内部 this 调用没有再次经过代理，因此 runtimeFailure 的事务注解未在这次调用上生效。本实验没有外层事务，MyBatis 的这次插入在事务外完成，后面抛异常也不能撤销已提交的写入。

这正是 Day38 的代理边界。不能概括成“所有同类调用都没有事务”：如果外层已经开启事务，内部仍可能参与外层事务，只是内部方法注解的新增规则不会通过这次 this 调用被拦截。

常见改法是把完整事务边界放在外部调用的 public 方法，或按职责拆成另一个 Bean 后注入调用，不要只靠添加注解猜测效果。

## 5. 捕获异常为什么会提交

本课 caughtInside 先插入，然后在同一个方法中抛出并捕获一个本地 IllegalStateException。最终方法正常返回，事务也没有 rollback-only 标记，代理会提交。

不要因此认为“catch 后永远提交”。如果嵌套事务参与者已经标记 rollback-only，即使外层 catch，也可能在提交时出现 UnexpectedRollbackException；数据库错误也可能改变事务状态。本课只演示简单本地异常。

需要回滚时可让异常继续向外传播，或在确有需要时显式标记回滚；不要吞异常后告诉调用方成功。

## 6. 受检异常与 rollbackFor

```java
@Transactional(rollbackFor = Exception.class)
public void checkedRollback(String name) throws Exception {
    insert(name);
    throw new Exception("显式配置受检异常回滚");
}
```

- `throws Exception`：方法声明可能抛出受检异常。
- `throw new Exception(...)`：实际创建并抛出异常。
- `rollbackFor = Exception.class`：把 Exception 及其子类纳入回滚规则。

rollbackFor 不负责捕获异常或返回错误提示。HTTP 错误转换仍是 Day37 的 Advice 职责；业务事务边界通常在 Service。

## 7. 为什么测试类没有 Transactional

Day41～44 的测试使用测试事务自动回滚，适合隔离普通查询/写入实验。但今天如果给测试也加 Transactional，测试创建的外层事务可能覆盖 selfCall，让它看起来也能回滚，掩盖真实缺陷。

今天测试通过代理调用 Service，然后用另一次查询检查真实提交结果。每个测试使用不同姓名，在 finally 中只清理自己的记录，不清空整张表。默认串行运行，不作为并发事务测试。

```java
assertThrows(IllegalStateException.class, () -> service.runtimeFailure(name));
assertEquals(0, count(name));
```

第一句证明方法抛出了预期异常，第二句证明数据确实没留下；只有异常断言不足以证明回滚成功。测试清理不是业务回滚：断言在清理之前执行。

## 8. 验收与边界

- [ ] 能解释为什么抛异常不一定回滚。
- [ ] 能指出实际调用是否经过 Spring 代理。
- [ ] 能区分业务内部 catch 与测试外部 assertThrows。
- [ ] 能解释默认受检异常与 rollbackFor 的区别。
- [ ] 能说明为什么今天不能用外层测试事务掩盖结果。
- [ ] 能通过写后查询确认数据库状态。

本课验证真实 H2 事务，但不验证 MySQL 存储引擎、锁、隔离级别或跨服务事务。不要将数据库事务理解为能够回滚任意外部副作用。

## 9. 对照目标项目与验证记录

在 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\job\BiSimpleWriteJobHandler.java`，doWrite 方法声明了 `@Transactional(rollbackFor = Exception.class)`。可以用今天的知识理解声明含义；是否实际经过代理、调用方如何捕获异常，还需追踪调用链，不能只看到注解就断言事务有效。本次没有运行该业务任务。

2026-11-26：使用 JDK 8 和临时 Maven 3.9.9 执行 package。新增 6 个事务实验与原有 21 个测试合计 27 个，全部通过，无失败、错误或跳过，BUILD SUCCESS。验证包括提交后真实查询与测试记录清理，不依赖测试外层回滚。未实际操作 IDEA，未验证真实 MySQL。

下一课 Day46：隔离级别与 MVCC，使用两个连接观察读取现象。
