# Day 28：JDBC 与参数化查询

## 今日目标

用 Java 连接 Day25 的本地 MySQL 学习库，执行一条按教师 ID 查询的 SQL。重点理解 Connection、PreparedStatement、ResultSet 和 try-with-resources。

入口：[Day28Jdbc.java](../java-basics/src/Day28Jdbc.java)。POM 已增加 MySQL Connector/J 依赖。运行前必须先准备自己的数据库和账号；本课不连接 aic-teaching-service 的真实数据库。

## 1. JDBC 调用链

```text
DriverManager.getConnection
  → Connection
  → PreparedStatement
  → executeQuery
  → ResultSet.next / getXxx
  → 自动关闭资源
```

- Connection：一次数据库连接。
- PreparedStatement：带参数的 SQL 语句。
- ResultSet：查询结果游标。

## 2. 为什么使用 PreparedStatement

```java
String sql = "SELECT id, teacher_no, name FROM teacher WHERE id = ?";
PreparedStatement statement = connection.prepareStatement(sql);
statement.setLong(1, teacherId);
```

`?` 是参数占位符，`setLong(1, teacherId)` 给第一个占位符绑定值。不要把用户输入直接拼接到 SQL：

```java
// 不要这样写："... WHERE id = " + userInput
```

参数绑定同时处理类型和字符串转义，降低 SQL 注入风险；它不能替代权限校验、业务范围校验或 SQL 设计。

## 3. ResultSet 为什么先 next

```java
if (resultSet.next()) {
    long id = resultSet.getLong("id");
    String name = resultSet.getString("name");
}
```

ResultSet 初始位于第一行之前。`next()` 移到下一行并返回是否存在；查询没有结果时返回 false。多行查询要 while (resultSet.next()) 循环。列名读取比硬编码下标更容易检查，但列别名变更时也要同步。

## 4. try-with-resources

```java
try (Connection connection = ...;
     PreparedStatement statement = ...;
     ResultSet resultSet = ...) {
    // 使用资源
}
```

Connection、PreparedStatement、ResultSet 都实现 AutoCloseable，离开代码块时按创建的反向顺序关闭。正常完成和异常都尝试关闭，避免连接泄漏。关闭不代表事务自动提交或回滚；本课只读查询，Day29 专门学习事务。

## 5. 配置连接信息

示例默认尝试：

```text
jdbc:mysql://127.0.0.1:3306/java_learning_day25
用户：root
密码：空字符串
```

不要假设这一定适合你的机器。推荐使用环境变量：

```powershell
$env:DAY28_DB_URL = 'jdbc:mysql://127.0.0.1:3306/java_learning_day25?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai'
$env:DAY28_DB_USER = '你的本地账号'
$env:DAY28_DB_PASSWORD = '你的本地密码'
```

本机发现有 `D:\mysql9.6\bin\mysql.exe` 客户端，但没有确认 MySQL 服务是否启动、端口、账号或数据库是否已创建。客户端存在不等于 JDBC 可以连接。

先执行 Day25 的建表和示例数据，再运行 Java。命令行 Maven 可用后：

```powershell
cd E:\my\javaProjects\java-basics
mvn compile
java '-Dfile.encoding=UTF-8' -cp 'target/classes;target/dependency/*' Day28Jdbc 1
```

普通 Maven package 不会自动把依赖复制到 target/dependency；IDEA 运行配置会使用 Maven 依赖。初学推荐直接在 IDEA 中加载 POM 后运行 main，连接器由 IDE classpath 提供。

## 6. SQL 异常怎么读

代码捕获 SQLException，打印 message 和 SQLState。连接失败、认证失败、数据库不存在、表不存在和 SQL 错误的处理方式不同，应保留完整异常信息；示例只为控制台练习，没有接入项目统一日志。

不要把异常 message 和数据库密码写入日志。生产代码还要使用连接池、超时、最小权限账号和统一异常转换，不应每次请求都直接 DriverManager 建立新连接。

## 7. 与目标项目的联系

aic-teaching-service 使用 MyBatis-Plus、Mapper XML、Hikari 和动态数据源，日常业务通常不手写 DriverManager。JDBC 是理解它们底层连接、参数绑定、结果映射和资源边界的基础。阅读 Mapper 时，可以把 `#{}` 参数绑定与 PreparedStatement 的安全边界对应起来。

## 8. 今日练习与验收

- [ ] 能解释 Connection、PreparedStatement、ResultSet 的职责。
- [ ] 能写 `?` 占位符并绑定 long 参数。
- [ ] 能解释为什么 ResultSet 要先调用 next。
- [ ] 能用 try-with-resources 关闭 JDBC 资源。
- [ ] 能区分连接失败、查询无结果和 SQL 执行失败。
- [ ] 能说明客户端存在不代表数据库服务和账号可用。
- [ ] 不在代码或 Git 中写真实数据库密码。

下一课：JDBC 事务，用提交、回滚和连接的 autoCommit 验证一致性。
