# Day43：动态 SQL、可选筛选与排序白名单

今天解决：**有时按姓名查，有时按科目查，有时两个条件一起查，难道要写很多个方法吗？**

沿用 Day41/42 的 `mybatis-lab`。SQL 在真实 H2 内存数据库执行，不需要安装 MySQL，没有 HTTP 端口。

## 1. 先运行

IDEA 同步 `mybatis-lab/pom.xml`，模块选 **mybatis-lab**、JDK 8，运行 [MybatisLabApplication.main()](../mybatis-lab/src/main/java/learning/persistence/MybatisLabApplication.java)，程序实参留空。

在 Day41/42 输出之后，会出现：

```text
Day43 无条件数量：3
Day43 Java且编号>=2：2 / 李老师
Day43 非法排序已拒绝：排序字段只允许 id、name、subject
```

Day42 临时新增的记录已经删除，所以 Day43 仍查询三条初始记录。main 执行完会关闭连接池并退出。

## 2. 今天的查询规则

| 参数 | 含义 | 不传时 |
| --- | --- | --- |
| name | 教师姓名精确匹配 | 不筛选姓名 |
| subject | 科目精确匹配 | 不筛选科目 |
| minId | 编号下限，包含边界 | 不筛选编号 |
| sort | id、name、subject 三选一 | id |
| direction | asc 或 desc | asc |

字符串参数先 trim，空串和普通全空格按未传处理。姓名是**精确匹配**，本课不是模糊搜索；只传“张”不会匹配“张老师”。多个筛选之间是 AND，必须同时满足。

## 3. 三个文件怎样合作

```text
main → TeacherSearchService（整理输入与白名单检查）
     → Day43TeacherMapper（参数命名）
     → Day43TeacherMapper.xml（组合 SQL）
     → H2 → List<TeacherRow>
```

- [TeacherSearchService.java](../mybatis-lab/src/main/java/learning/persistence/TeacherSearchService.java)
- [Day43TeacherMapper.java](../mybatis-lab/src/main/java/learning/persistence/Day43TeacherMapper.java)
- [Day43TeacherMapper.xml](../mybatis-lab/src/main/resources/mapper/Day43TeacherMapper.xml)

这里参数不多，先用普通方法参数；复杂业务可以封装成 Query 对象。XML 复用 Day41 的 resultMap，仍将 teacher_name 映射到 name。

## 4. if：条件存在才添加 SQL

```xml
<if test="subject != null and subject != ''">
    AND subject = #{subject}
</if>
```

逐句理解：

1. test 是 MyBatis 使用的 OGNL 条件表达式，不是 SQL。
2. 判断 subject 不是 null，也不是空字符串。
3. 成立时把 AND subject = ? 放进 SQL。
4. `#{subject}` 的实际值通过 JDBC 参数绑定提供。
5. 条件不成立，这一整段 SQL 不加入。

动态 SQL 不是修改 XML 文件，而是每次请求根据参数生成最终语句。

## 5. where：处理开头多余的 AND

```xml
<where>
    <if test="name != null and name != ''">AND teacher_name = #{name}</if>
    <if test="subject != null and subject != ''">AND subject = #{subject}</if>
    <if test="minId != null">AND id &gt;= #{minId}</if>
</where>
```

`where` 有两个作用：有内容才添加 WHERE；去掉内容开头多余的 AND/OR。无条件时不会生成一个空的 WHERE。

`&gt;` 是 XML 中 `>` 的实体写法，生成的 SQL 仍是 `>=`。

传 subject="Java"、minId=2，大致生成：

```sql
SELECT id, teacher_name, subject FROM teacher
WHERE subject = ? AND id >= ?
ORDER BY id DESC
```

绑定值是 Java 与 2。不要把 Preparing 日志里的问号误认为“没有填入参数”，它代表预编译占位符。

## 6. 为什么排序不能直接用参数绑定

查询值可以使用 `WHERE subject = #{subject}`。但 `ORDER BY #{sort}` 通常会把参数当值，不会把字符串 name 变成数据库列名。

同样不能直接写 `ORDER BY ${sort}` 来拼接用户输入：`${...}` 是文本替换，可能让用户输入进入 SQL 结构。

本课没有使用 `${...}`。Service 先限定输入为 id、name、subject，再由 XML 映射到固定 SQL 片段：

```xml
<choose>
    <when test='sort == "name"'>teacher_name</when>
    <when test='sort == "subject"'>subject</when>
    <otherwise>id</otherwise>
</choose>
```

`choose` 类似 if / else if / else，只选择一个分支。外部参数 name 对应真实列 teacher_name，不要求前端知道数据库列名。

排序方向也是固定分支：desc 选择 DESC，其余分支输出 ASC。Service 会拒绝未知方向；即使有人绕过 Service 直接调用 Mapper，XML 也只输出这些固定片段，不拼接未知文本。直接调用 Mapper 时未知字段回退 id、未知方向回退 ASC，这与 Service 明确拒绝非法输入是两层不同职责。

## 7. 为什么后面还加 id ASC

按 subject 排序时，张老师和李老师的 subject 都是 Java，仅按科目不能确定它们之间的顺序。所以非 id 排序会追加：

```sql
ORDER BY subject DESC, id ASC
```

先按科目降序，同科目按编号升序。这是固定的并列顺序，不是 subject 降序就所有列都降序。稳定顺序是下一课分页的重要基础。

数据库字符串排序受排序规则影响，本课 H2 默认排序不代表 MySQL 的中文拼音顺序。

## 8. 在 main 中动手修改

```java
search.search("张老师", null, null, null, null); // 姓名筛选
search.search(null, "Java", null, "id", "desc"); // Java 科目，编号降序
search.search(null, "Java", 2L, "id", "asc"); // 科目和编号同时满足
search.search("张老师", "数学", null, null, null); // 空列表
```

minId 是 Long，可以为 null；`2L` 表示 long 字面值，会装箱成 Long。

尝试 sort="teacher_name" 或 direction="desc; --"，Service 会抛 IllegalArgumentException；不要删除白名单判断来让错误输入通过。main 对演示的非法排序做了捕获，因此程序仍正常退出。

## 9. 自动化测试

运行 [Day43DynamicSqlTest.java](../mybatis-lab/src/test/java/learning/persistence/Day43DynamicSqlTest.java)，新增 8 个测试覆盖：

- 无筛选、空白筛选，以及三个条件分别生效。
- 多条件 AND、去除两端空格、无匹配数据。
- 编号降序、科目并列顺序、name 到真实列的映射。
- 包含引号/SQL 片段的筛选值仍作为普通值。
- 未知排序字段、非法方向、非法编号下限被拒绝。
- 绕过 Service 时，XML 仍不拼接未知排序文本。

安装 Maven 后在 mybatis-lab 中执行 `mvn test`；连同 Day41/42 原有 5 个测试，共 13 个。测试执行真实 H2 SQL，修改在测试事务内默认回滚。没有验证 MySQL 方言、性能、真实业务权限或所有可能的 SQL 注入路径。

## 10. 对照目标项目

查看 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\resources\mapper\TeacherProfileMapper.xml`，可以看到 `if` 判断可选列表，再用 `foreach` 绑定 IN 条件中的各个值。

本课先用三个简单条件理解动态拼装；目标项目还有业务线、逻辑删除等必需限制，不应把所有过滤条件都改成可选。学习 Demo 的“无条件返回全部三条数据”不能直接搬到需要权限隔离的业务接口。

## 11. 验收

- [ ] 能解释 if 和 where 各自做什么。
- [ ] 能写出 subject 与 minId 同时传入时的 SQL 结构。
- [ ] 能区分参数值绑定与 SQL 结构选择。
- [ ] 能说明为什么不能把用户排序直接放入 `${...}`。
- [ ] 能解释白名单键 name 如何映射为 teacher_name。
- [ ] 能说明并列排序为什么需要 id 兜底。

## 12. 本次验证记录

2026-11-26：JDK 8 配合临时 Maven 3.9.9 执行 package，13 个 H2 集成测试全部通过，失败/错误/跳过均为 0，BUILD SUCCESS。随后实际启动打包 JAR，确认无条件为 3 条、Java 且编号至少 2 返回李老师、非法排序被拒绝，程序正常退出。git diff --check 通过。未实际操作 IDEA，未连接 MySQL；临时 Maven 不代表系统已配置全局 mvn。

下一课 Day44：分页与总数，区分当前页数量、筛选后总数与表内总数。
