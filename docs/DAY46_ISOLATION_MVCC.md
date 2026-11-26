# Day46：两个连接看隔离级别与 MVCC

昨天学习一个事务何时提交、回滚；今天观察：**连接 B 改了数据，连接 A 什么时候能看到？**

本课使用两个真实 JDBC 连接执行交错事务。默认使用 H2 内存数据库，不需要安装 MySQL。H2 的结果只能证明当前 H2 行为，不能替代 MySQL/InnoDB 验证。

## 1. 运行入口

打开 [Day46IsolationTest.java](../mybatis-lab/src/test/java/learning/persistence/Day46IsolationTest.java)，IDEA 选择 **mybatis-lab** 模块、**JDK 8**，运行整个测试类，预期 4 个测试通过。

不需要运行 main，没有 HTTP 接口。安装 Maven 后可以在 mybatis-lab 执行：

```powershell
mvn -Dtest=Day46IsolationTest test
```

每个测试建立独立的随机命名 H2 数据库，并创建 account 表，只有一行 `id=1, balance=100`。测试结束关闭连接并执行 SHUTDOWN；不修改之前的 teacher 表。

## 2. 先理解两个连接

```java
connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
connection.setAutoCommit(false);
```

第一句选择隔离级别，第二句关闭自动提交，让多条语句可以处于同一个事务。之后显式调用 commit 或 rollback。

A 和 B 是两个独立连接，拥有不同事务。两次调用同一个 Mapper 方法不一定就是两个连接，因此今天直接使用 JDBC，把连接身份写清楚。

我们按 A读→B写→B提交→A读 的顺序交错执行，不需要靠多个线程随机竞争来制造现象。锁实验中的 B 更新则会真实等待并触发超时。

## 3. RC：看不到未提交的数据

RC 是 READ COMMITTED（读已提交）。实验顺序：

| 步骤 | 连接 B | 连接 A |
| --- | --- | --- |
| 1 | 更新 balance=200，但不提交 | |
| 2 | | 普通 SELECT 得到 100 |
| 3 | rollback，撤销更新 | |
| 4 | | 再读仍为 100 |

如果 A 看到了 B 尚未提交的 200，就属于脏读。本实验验证 RC 没有发生脏读。B 执行 UPDATE 返回成功，并不等于已经 commit。

## 4. RC：同一个事务可以两次读到不同结果

```text
A：读到 100，事务仍未结束
B：更新为 200，并提交
A：再次读到 200
```

这叫不可重复读：同一事务内对同一行的两次读取结果不同。RC 禁止脏读，不保证同一行重复读取不变。

## 5. RR：本次事务继续看到原来的值

RR 是 REPEATABLE READ（可重复读）。只把 A 的隔离级别改成 RR：

```text
A：读到 100
B：更新为 200，并提交
A：再次普通查询，仍读到 100
A：提交，结束本次事务
A：新事务查询，读到 200
```

这不表示 B 的提交失败，也不表示 A 永远看不到新值，而是不同事务读取视图的时间边界不同。本课验证这一个同一行重复读取场景，不据此概括所有数据库的范围查询、幻读或锁实现。

## 6. MVCC 可以怎样理解

MVCC 是多版本并发控制。可以先理解为：数据库保留可供事务判断的不同版本，让读取在很多场景下不用一直阻塞写入。

例如 B 提交了新版本 200，A 仍可能根据自己的可见性规则读取旧版本 100。实际版本存储、清理和可见性规则由数据库实现，不能把 H2 内部实现当成 MySQL 的 undo log/Read View。

对 MySQL InnoDB 的一般学习模型：RC 普通一致性读通常每条语句建立新快照；RR 普通一致性读通常复用事务中首次一致性读建立的快照。快照并不总是在 BEGIN 一执行就创建，显式一致性快照等方式还会影响建立时机。

这些是 InnoDB 概念说明，本次自动化实验只运行 H2，没有执行 MySQL 验证。

## 7. 普通查询和锁定查询的区别

普通查询：

```sql
SELECT balance FROM account WHERE id=1;
```

锁定查询：

```sql
SELECT balance FROM account WHERE id=1 FOR UPDATE;
```

本课在 RC 下运行锁定实验：

1. A 关闭自动提交，执行 FOR UPDATE 并保持事务不结束。
2. B 尝试更新同一行，由于 A 持锁而等待。
3. 达到实验配置的锁等待超时，B 得到异常。
4. B rollback，清理本次失败事务。
5. A commit，释放锁。
6. B 在新的事务中重试更新并提交成功。
7. A 普通查询得到 200。

本课配置 `LOCK_TIMEOUT=300` 毫秒用于快速复现，不以精确等待时长作断言。测试检查 H2 锁超时错误码 50200，不把任意 SQLException 都当作“锁生效”。该错误码不能直接用于 MySQL。

锁超时不等于死锁：这里只是 B 等待 A，没有构造循环等待。

## 8. 一致性读、锁定读不要混为一谈

在 InnoDB 术语中，普通 SELECT 常用于一致性读；FOR UPDATE 属于锁定读，按锁定读规则处理当前数据，并不简单地沿用普通快照查询的结果。

本课分别验证重复普通读取和 RC 下的锁冲突，没有验证 H2 RR 下所有“快照读与锁定读混用”的行为，也没有验证 MySQL 的间隙锁、next-key lock 或幻读。

FOR UPDATE 的意义通常是为后续依赖该行的修改保护事务边界。若查询后马上结束事务，锁也会释放，不能指望之后的另一次请求还受保护。

不要将“不用 FOR UPDATE”理解为数据库完全不使用任何锁；这里讨论的是读取已有行时对并发修改的可观察影响。

## 9. 为什么测试不用外层事务

测试没有 Spring 的测试级 Transactional，而是自己管理 A/B 两个连接。每个实验通过 assertEquals 验证读值；锁实验还用 assertThrows 验证特定错误码。

try-with-resources 关闭连接，finally 中关闭独立数据库。正常路径显式提交/回滚，让事务边界可见。测试没有验证生产连接池配置、跨服务并发或高负载性能。

## 10. 对照目标项目

查看 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\service\ConversationPageService.java`，可以看到 `@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ, timeout = 120)`。

可以据此识别它声明的只读、隔离级别与事务超时要求；实际是否经过代理、使用什么数据库和如何建立读取视图，还需检查运行环境与调用链。本次没有执行该业务服务。readOnly 不是应用层权限校验，也不保证每种数据库都绝对禁止写入。

## 11. 验收

- [ ] 能解释 UPDATE 成功与 commit 的区别。
- [ ] 能按顺序复述 RC 的 100→200 与 RR 的 100→100→200。
- [ ] 能区分脏读和不可重复读。
- [ ] 能解释 FOR UPDATE 为什么需要明确的事务边界。
- [ ] 能说明锁超时为什么不是死锁。
- [ ] 能指出哪些结果经过 H2 测试，哪些 MySQL 概念尚未实测。

## 12. 本次验证记录

2026-11-26：JDK 8 与临时 Maven 3.9.9 执行 package，新增 4 个双连接实验连同原有测试共 31 个，全部通过，失败/错误/跳过均为 0，BUILD SUCCESS。实际验证了未提交更新不可见、RC 不可重复读、RR 同一事务值不变及新事务读取新值、FOR UPDATE 锁超时与释放后重试成功。未实际操作 IDEA，未验证 MySQL。测试数据库已关闭。

下一课 Day47：索引与 EXPLAIN，观察查询执行计划和扫描工作量。
