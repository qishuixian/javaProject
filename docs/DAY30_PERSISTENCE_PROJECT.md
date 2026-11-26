# Day 30：持久化综合练习

## 1. 本阶段目标

把 Day7 的接口、Day10 的教师管理、Day25 的表、Day28 的 JDBC 和 Day29 的事务合在一起：将查询实现换成数据库，完成新增、查询、改名、失败验证和回滚清理。

- [Day30Persistence.java：运行入口](../java-basics/src/Day30Persistence.java)
- [JdbcTeacherRepository.java：数据库实现](../java-basics/src/JdbcTeacherRepository.java)
- [TeacherRepository.java：已有查询接口](../java-basics/src/TeacherRepository.java)

旧内存实现和前几课源码不变。本课是连接真实 MySQL 的集成练习，不是纯内存单元测试。

## 2. 运行配置

先完成 Day25 建表，确认自己的本地学习库可用。在 IDEA 新建 Application：

| 配置项 | 值 |
|---|---|
| Main class | Day30Persistence |
| Use classpath of module | java-basics |
| JRE | JDK 8 |
| Environment variables | DAY28_DB_USER、DAY28_DB_PASSWORD，按需设置 DAY28_DB_URL |

环境变量沿用 Day29。**Day28 源码里手动填写的密码不会自动传给新课程。** 应在本次 IDEA 运行配置中填写，不保存密码到 Java 文件。

默认 URL 连接本地 java_learning_day25，程序检查库名。每次生成独立 D30 编号，不依赖教师 ID 恰好为 1。成功和失败路径都默认回滚练习写入，不执行 COMMIT。

## 3. “替换 Repository”是什么意思

原来的调用方：

```java
TeacherRepository repository = new InMemoryTeacherRepository(teachers);
Teacher teacher = repository.findById(id);
```

数据库版：

```java
TeacherRepository repository = new JdbcTeacherRepository(connection);
Teacher teacher = repository.findById(id);
```

调用方仍使用 findById；变化的是对象如何取得数据。verifyName 方法只依赖接口，能够使用数据库实现。

现有接口只有查询，所以 add、rename 是 JdbcTeacherRepository 的额外能力，本课没有强行修改所有旧实现。实际工程需要统一读写接口时，应进一步设计职责与异常契约。

## 4. JDBC 查询结果怎样变成对象

```java
return rows.next()
        ? new Teacher(rows.getLong("id"), rows.getString("name"))
        : null;
```

查询有行就读取列，创建新的 Teacher；没有行返回 null。数据库出错则抛异常，不能返回 null 掩盖故障。

每次查询创建新对象，因此数据库里的同一行不意味着两次查询得到同一个 Java 引用。校验看字段值，不用 ==。

旧 TeacherRepository 没有 throws SQLException，重写方法不能凭空增加这个受检异常。因此 findById 包装为 IllegalStateException 并保留 cause。新增、改名方法当前直接声明 SQLException；这是为兼容已有教学接口做的简化，不是最终企业异常设计。

## 5. 自增 ID 的正确获取

```java
connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
statement.executeUpdate();
ResultSet keys = statement.getGeneratedKeys();
```

告诉驱动需要返回生成的主键，然后读取它。不要用 MAX(id) 或猜测下一个 ID，并发写入时它们可能对应别人的记录。

add 为复用 Day6 姓名校验，创建了临时 Teacher(1L, name)，只读取校验后的姓名；数据库实际 id 仍由自增列生成。真实系统可以进一步提取独立校验器，本课先不扩大重构。

## 6. 谁负责关闭和提交

| 资源/操作 | 负责人 |
|---|---|
| Connection 创建、关闭 | Day30 入口 |
| commit/rollback | 入口决定事务边界 |
| PreparedStatement、ResultSet | Repository 方法内 try-with-resources |

Repository 不关闭传入的连接，也不偷偷 commit。这样新增、查询和改名才能属于同一事务。

rename 先查对象、调用 Teacher.rename 校验，再发送 UPDATE。它没有版本号、行锁或并发删除检测，本课只验证独立单线程练习数据；真实并发业务要另行设计乐观锁、影响行数检查等机制。

## 7. 集成验证流程

```text
同一连接、关闭自动提交
  → 新增教师并拿到自增 ID
  → 查询并确认姓名已去除两端空格
  → 修改姓名，再从数据库查询
  → 空姓名应拒绝，原数据不变
  → 重复编号应触发 1062，原数据不变
  → 回滚全部练习写入
  → 原连接查询为空
关闭连接
  → 新连接再次确认没有残留
```

检查失败会抛异常，不能只打印“成功”后继续。只有预期的重复键错误才算重复编号验证成功；权限、网络或 SQL 错误会向外传播。

MySQL InnoDB 普通重复键错误通常只让当前语句失败，整个事务不会自动回滚。本例最后显式 rollback 撤销第一条插入。不要把这种行为推断到死锁等所有数据库错误。

## 8. 预期输出

```text
练习编号：D30...
新增并查询：通过
修改后重新查询：通过
非法改名不改变数据：通过
重复编号被唯一约束拒绝：通过
回滚后查询为空：通过
新连接确认无残留：通过
```

本次编写后进行 Maven 编译和已有 JUnit 测试回归，未连接你的数据库运行 Day30。以上是待验证的数据库预期，不是已完成的服务器测试。

## 9. “持久化”为什么这里还要回滚

数据库存储实现可以在 commit 后保留记录，但本课默认是可重复集成实验，所以刻意回滚。

原连接中能查询到新增行，不代表已经持久提交。若要练习重启后仍可查询，应在自己的学习库另行设计提交实验并记录生成 ID，而不是随意把本例 rollback 换成 commit：后面的“无残留”断言会因此失败。

回滚不保证自增 ID 连续无空洞，也不会撤销文件或外部网络请求。

## 10. 对照 aic-teaching-service

目前调用链是 main → Repository → PreparedStatement → MySQL。真实项目通常是 Controller → Service → Mapper → 数据库，连接池和 Spring 管理资源与事务。

先理解本课的连接共享、参数绑定和异常传播，再学习框架注解，会更容易判断事务究竟保护了哪些操作。

## 11. 阶段验收与练习

- [ ] 能保持 TeacherRepository 查询接口不变，换成 JDBC 实现。
- [ ] 能解释新对象与同一数据库记录的关系。
- [ ] 能获取自增 ID，不依赖固定主键。
- [ ] 能说明数据库错误为什么不能伪装成查询无结果。
- [ ] 实际运行并核对六个“通过”，以及异常时的原始 cause。
- [ ] 在 Workbench 按本次 D30 编号查询，确认无残留。
- [ ] 解释连接关闭、事务提交和回滚的不同作用。

可选练习：增加按教师编号查询的方法；为输入非法编号添加验证；设计一个明确提交并能按编号安全清理的独立实验。

下一阶段从 Day31 开始：HTTP 与接口契约，逐步进入 Spring Boot Web 服务。
