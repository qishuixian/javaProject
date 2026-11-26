# Day41：用 Mapper 和 XML 查询数据库

今天把 JDBC 中“准备 SQL、绑定参数、执行查询、读取 ResultSet”的重复工作交给 MyBatis。SQL 仍然需要理解和编写。

## 1. 今天使用独立工程

新模块：[mybatis-lab/pom.xml](../mybatis-lab/pom.xml)。沿用 Java 8、Boot 2.3 的历史项目学习背景，使用 MyBatis-Plus 3.4.3.4 提供 MyBatis 集成，两天共用工程。Day41 的接口不继承 BaseMapper，只使用普通 MyBatis XML 映射。不要额外混装 mybatis-spring-boot-starter。

默认数据库是 **H2 内存数据库**：SQL 会真实执行，有表、记录和数据库连接，但无需另装服务。不是 Day40 的 Java Map，也不是 MySQL。程序退出后数据消失；每次独立启动重新初始化。

此模块是控制台程序，没有 Web 端口。旧 spring-boot-lab 不用修改数据库配置。版本用于历史工程教学，不作为新生产项目版本建议。

## 2. IDEA 启动步骤

1. 右键 `mybatis-lab/pom.xml` → Add as Maven Project，等待同步。
2. 打开 [MybatisLabApplication.java](../mybatis-lab/src/main/java/learning/persistence/MybatisLabApplication.java)。
3. 运行 main，模块选 **mybatis-lab**，JRE 选 **JDK 8**，程序实参留空。
4. 控制台会打印 SQL 及 Day41/42 结果，执行后自动关闭连接池并退出。

Day41 关键输出：

```text
Day41 编号1姓名：张老师
Day41 Java教师数：2
```

SQL 日志中的 `Preparing` 是带占位符的 SQL，`Parameters` 是绑定值，`Total` 是返回行数。不需要打开浏览器，也不要找 Maven 面板里的 MySQL 驱动，本课没有依赖 MySQL。

## 3. 数据从哪里来

- [application.properties](../mybatis-lab/src/main/resources/application.properties)：配置 H2 数据源及 XML 位置。
- [schema.sql](../mybatis-lab/src/main/resources/schema.sql)：创建 teacher 表。
- [data.sql](../mybatis-lab/src/main/resources/data.sql)：插入张老师/Java、李老师/Java、王老师/数学。

当前 SQL 使用 H2 语法，不要直接拿去覆盖已有 MySQL 表。`initialization-mode=always` 仅适合这个独立内存实验；切换外部数据库前必须重新设计初始化方式，不能直接复用自动建表配置。

## 4. 一次查询的调用链

```text
main → Mapper 代理.findById(1L)
     → XML 中的 findById SQL
     → JDBC / H2
     → ResultSet
     → resultMap 转 TeacherRow
     → 返回 Java 对象
```

[Day41TeacherMapper.java](../mybatis-lab/src/main/java/learning/persistence/Day41TeacherMapper.java)：

```java
TeacherRow findById(@Param("id") long id);
```

- 接口里没有方法体，因为实现由 MyBatis 的映射代理提供。
- `@MapperScan("learning.persistence")` 扫描 Mapper 接口并注册代理 Bean。
- `@Param("id")` 给 SQL 参数取名。
- 返回类型 TeacherRow 表示预期一条记录对应一个对象。

这也是代理，但不是 Day38 的计时切面。接口没有手写实现，不代表业务不需要执行逻辑：实际查询定义在 XML 中。

## 5. XML 怎样找到接口方法

[Day41TeacherMapper.xml](../mybatis-lab/src/main/resources/mapper/Day41TeacherMapper.xml)：

```xml
<mapper namespace="learning.persistence.Day41TeacherMapper">
    <select id="findById" resultMap="teacherMap">
        SELECT id, teacher_name, subject FROM teacher WHERE id = #{id}
    </select>
</mapper>
```

三个对应关系：

1. namespace = Mapper 接口的完整类名。
2. select 的 id = 接口方法名。
3. `#{id}` = `@Param("id")` 指定的参数名。

`mapper-locations=classpath:/mapper/*.xml` 让框架加载这些 XML。放错目录、namespace 拼错、方法名不匹配，常导致 `Invalid bound statement (not found)`。

`#{id}` 会作为预编译参数绑定，不是把文本直接拼到 SQL。不要把用户输入改成 `${...}` 字符串替换；动态排序等结构后续需白名单处理。

## 6. teacher_name 为什么能变成 name

```xml
<resultMap id="teacherMap" type="learning.persistence.TeacherRow">
    <id column="id" property="id"/>
    <result column="teacher_name" property="name"/>
    <result column="subject" property="subject"/>
</resultMap>
```

column 是数据库列名，property 是 Java 属性名。`teacher_name → name` 不是简单下划线转驼峰，因此这里显式指定映射。`<id>` 表示映射主键属性，不会自动生成数据库主键或建表。

`resultType` 适用于能直接匹配的结果类型；`resultMap` 指向详细映射规则。本例选择 resultMap 展示不同列名与属性名。

[TeacherRow.java](../mybatis-lab/src/main/java/learning/persistence/TeacherRow.java) 中的 TableName、TableField 等 Plus 注解供 Day42 通用 SQL 使用，不应认为它们自动替代 Day41 XML 的 resultMap。

## 7. 返回一个对象与返回 List

```java
TeacherRow findById(@Param("id") long id);
List<TeacherRow> findBySubject(@Param("subject") String subject);
```

- 单对象查询：没有结果返回 null；意外查到多行通常抛 TooManyResultsException，不能把多行 SQL 声明成单对象。
- List 查询：没有结果返回空列表；多行则每行映射成一个对象。
- 本课列表 SQL 写了 ORDER BY id，测试和学习时可观察稳定顺序。

不要在可能为 null 的查询结果上直接 getName。main 查询的是已初始化的固定数据；实际接口需要像 Day40 一样处理不存在的情况。

## 8. 测试与边界

运行 [MapperIntegrationTest.java](../mybatis-lab/src/test/java/learning/persistence/MapperIntegrationTest.java)。Day41 相关的三个测试覆盖：列映射、空结果/多结果、包含 SQL 特殊文本的绑定参数。

这些测试通过真实 Mapper 执行真实 H2 SQL，没有 MockBean。测试类的 Transactional 使每个测试的修改默认回滚，减少相互影响；不要依赖回滚会重置自增序列。

安装 Maven 后：

```powershell
Set-Location E:\my\javaProjects\mybatis-lab
mvn test
```

H2 通过不代表 MySQL 的方言、索引、锁或事务隔离表现相同，也没有验证公司项目的 SQL。

## 9. 对照目标项目

`E:\code\java\aic-teaching-service\aic-teaching-service-service` 中可对照：

- `src/main/java/cn/vipthink/aic/teaching/mapper/TeacherProfileMapper.java`
- `src/main/resources/mapper/TeacherProfileMapper.xml`

先找 XML 的 namespace，再找接口同名方法、参数和返回类型。一次只追一个查询，不必马上读完复杂业务 SQL。目标工程依赖环境与本课不同，不复制它的私有父 POM。

## 10. 验收

- [ ] 能指出 namespace、select id、Param 三者如何对应。
- [ ] 能解释列 teacher_name 怎样进入 name。
- [ ] 能区分 null 与空列表。
- [ ] 能解释 Mapper 仍然依赖 JDBC，却省去了哪些手写步骤。
- [ ] 能说明 H2 验证与真实 MySQL 验证的区别。

## 11. 本次验证记录

2026-11-26：JDK 8 与临时 Maven 3.9.9 执行 package，5 个 H2 集成测试全部通过，无失败、错误或跳过，BUILD SUCCESS。随后实际执行打包 JAR，确认中文姓名、查询数量与 Day42 输出，进程正常退出。

首次测试暴露 SQL 初始化编码问题，已通过 `spring.datasource.sql-script-encoding=UTF-8` 修复并重跑成功。未操作 IDEA，也未连接真实 MySQL。临时 Maven 不代表系统 PATH 已配置 mvn。

继续：[Day42 MyBatis-Plus](DAY42_MYBATIS_PLUS.md)。
