# Day 29：JDBC 事务——两次写入一起成功或一起撤销

## 1. 今天做什么

Day28 已通过 IDEA 成功查询教师。今天在同一条 Connection 上执行两次 INSERT：第一次成功，第二次故意使用相同教师编号，触发唯一约束，然后显式 rollback，确认第一条也被撤销。

入口：[Day29Transactions.java](../java-basics/src/Day29Transactions.java)。依赖 Day25 的 InnoDB teacher 表和 teacher_no 唯一键，不修改已有教师。每次运行生成独立编号，默认演示没有 commit，不保留新教师行。

本次只完成编译和已有单元测试回归；真实 MySQL 写入与回滚需你按下面步骤运行，不能把 Day28 查询成功当作本课事务验证。

## 2. IDEA 怎样配置

沿用已经成功的 Day28 配置方式，新建 Application：

- Name：Day29Transactions
- Main class：Day29Transactions
- Use classpath of module：java-basics
- JRE：JDK 8
- Environment variables：DAY28_DB_USER、DAY28_DB_PASSWORD；必要时填写 DAY28_DB_URL

本课复用 Day28 的环境变量名。注意：**在 Java 文件中改过 Day28 的默认密码，不会自动传给 Day29**。在 IDEA 的 Run → Edit Configurations 中填写环境变量；没有该字段时通过 Modify options 显示 Environment variables。

DAY28_DB_URL 默认连接 127.0.0.1:3306/java_learning_day25，只允许此库名。密码不写入源码。已启动的 IDEA 不会自动获得另一个 PowerShell 窗口新设置的环境变量，应在运行配置中设置。

确认是自己的本地学习库，teacher 使用 InnoDB 且存在 uk_teacher_no。程序不建表、不删除已有数据。

## 3. 自动提交是什么

新建 JDBC 连接通常默认 autoCommit=true：

```text
执行第一条 INSERT → 自动提交
执行第二条 INSERT → 失败
第一条已经提交，不能再靠 rollback 撤销
```

要把两次操作组成同一个事务，先写：

```java
connection.setAutoCommit(false);
```

然后根据结果选择：

```java
connection.commit();   // 提交整个事务
connection.rollback(); // 撤销当前事务未提交的修改
```

事务不是 Java 的 try/catch 本身。捕获异常只改变 Java 执行流程，必须显式调用 rollback 才能撤销数据库里的事务修改。

## 4. 理解完整模板

以下是常见成功/失败模板，**不作为本课自动执行的写入路径**：

```java
connection.setAutoCommit(false);
try {
    // 第一条写入，使用同一个 connection
    // 第二条写入，仍使用同一个 connection
    connection.commit();
} catch (SQLException failure) {
    try {
        connection.rollback();
    } catch (SQLException rollbackFailure) {
        failure.addSuppressed(rollbackFailure);
    }
    throw failure;
}
```

失败时保留最初的异常；回滚失败作为附加异常，而不是用一个新错误把原始原因覆盖掉。运行时异常也可能发生，真实代码要把所有需要回滚的失败路径考虑进去；本课入口同时处理 SQLException 和 RuntimeException。

使用 try-with-resources 关闭连接只是资源管理，不能代替明确提交或回滚。恢复 autoCommit=true 可能提交尚未完成的事务，因此不能在未回滚的情况下盲目放入 finally。

## 5. 本课的执行顺序

```text
生成新的 D29 开头编号
  → 建立连接，确认学习库
  → setAutoCommit(false)
  → INSERT 第一个教师
  → 当前连接 COUNT=1
  → INSERT 相同编号，唯一约束报错
  → rollback()
  → 恢复自动提交
  → COUNT=0
```

第一次 COUNT=1 只说明当前事务能看到自己写入的数据，不表示已经提交给其他连接。

错误码 1062 是 MySQL 重复键错误。只有第一次插入成功且收到这个预期错误，才继续验证“故意重复写入”的场景。认证失败、表不存在、权限不足等不能被当成实验成功。

如果唯一约束被移除，第二次 INSERT 可能成功；程序会主动报错并回滚，而不是把两条记录提交。

## 6. executeUpdate 与 executeQuery

```java
statement.executeUpdate(); // INSERT/UPDATE/DELETE，返回影响行数
statement.executeQuery();  // SELECT，返回 ResultSet
```

两次 INSERT 继续使用 PreparedStatement 的 ? 占位符，事务与参数化查询解决不同问题：前者保证操作边界，后者安全绑定数据。

插入和计数方法都接收 connection 参数，而不是自己再创建一个连接。否则两次写入可能处于不同事务，一边 rollback 不能撤销另一条连接的提交。

## 7. 预期输出

编号每次不同，其余应类似：

```text
本次练习编号：D29...
第一次写入后，当前连接可见数量：1
第二次写入被唯一约束拒绝，MySQL 错误码：1062
已回滚整个事务
回滚后教师数量：0
验证通过：第一条写入也没有保留
```

运行后也可在 Workbench 用日志里的完整编号检查：

```sql
USE java_learning_day25;
SELECT id, teacher_no, name
FROM teacher
WHERE teacher_no = '替换为刚才的完整 D29 编号';
```

应没有结果。自增 ID 可能出现空洞：回滚行数据不保证回收已分配的 ID，不能用 ID 是否连续判断事务失败。

## 8. 事务能保护什么

本课只涉及同一 MySQL 连接上的 InnoDB DML。它不会自动撤销：

- 已提交的事务；
- 另一条独立连接已经提交的数据；
- 文件写入、发消息、外部 HTTP 请求；
- 某些会隐式提交的 MySQL DDL，例如 CREATE/ALTER TABLE。

不要把 Day25 建表语句放进本课事务来测试回滚。网络故障导致 commit 响应丢失时，也不能仅凭异常断言数据库没有提交，真实服务需要进一步查证或幂等设计。

## 9. 与目标项目的联系

Spring 的 @Transactional 让框架管理连接与提交/回滚，本质上仍有事务边界。它不是简单等于“方法加注解就永远安全”；异常是否继续抛出、连接是否相同、是否跨库都影响结果。后续 Spring 课程再对照真实 Service 学习，不在此处修改业务项目。

## 10. 今日练习与验收

- [ ] 使用 java-basics 模块运行 Day29，而不是旧的 javaProjects 模块。
- [ ] 看到当前连接第一次计数为 1，回滚后为 0。
- [ ] 在 Workbench 按练习编号查不到记录。
- [ ] 能解释捕获异常为什么不等于自动回滚。
- [ ] 能解释两次写入为什么必须使用同一连接。
- [ ] 能区分 commit、rollback 与 close。
- [ ] 能解释回滚后自增编号出现空洞是正常现象。

下一课：持久化综合练习，整合数据库存储、查询和可重复验证。
