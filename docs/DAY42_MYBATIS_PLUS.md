# Day42：MyBatis-Plus 的 BaseMapper 与自定义 SQL

Day41 自己写 SQL。今天的问题是：**简单的单表新增、按主键查询、更新、删除，能不能少写重复 SQL？**

MyBatis-Plus 基于 MyBatis 提供通用能力，不是另一套数据库，也不会让复杂 SQL 自动消失。

## 1. 仍运行 mybatis-lab

按 [Day41](DAY41_MYBATIS.md) 导入工程，运行 MybatisLabApplication.main，模块 mybatis-lab，JDK 8。无需数据库服务，也没有 HTTP 接口。

main 先执行 Day41 查询，再执行 Day42 新增、更新、统计和删除，最后退出。关键输出包含：

```text
Day42 新增编号：某个新编号
Day42 更新后：赵老师（更新）
Day42 科目统计：Java=2
Day42 科目统计：数学=1
Day42 科目统计：英语=1
Day42 删除后为空：true
```

统计行顺序由数据库排序决定，不要依赖此处展示顺序；编号以实际返回为准。

## 2. extends BaseMapper 到底是什么意思

[Day42TeacherMapper.java](../mybatis-lab/src/main/java/learning/persistence/Day42TeacherMapper.java)：

```java
public interface Day42TeacherMapper extends BaseMapper<TeacherRow> {
    List<SubjectCount> countBySubject();
}
```

逐个解释：

- interface：声明 Mapper 接口。
- extends：当前接口继承另一个接口。
- BaseMapper：Plus 提供的通用 Mapper 接口。
- `<TeacherRow>`：泛型参数，告诉它操作的是哪种实体。
- countBySubject：我们额外声明的自定义聚合查询。

为什么接口里没写 insert 也能调用？因为继承了 BaseMapper 的方法声明；运行时由 Plus/MyBatis 集成生成并执行对应 SQL。不是 Java 编译器自动理解数据库表。

## 3. 表名、列名和主键

```java
@TableName("teacher")
public class TeacherRow {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("teacher_name")
    private String name;
}
```

- TableName 指定表名。
- TableId 指定主键；AUTO 使用数据库自动生成编号。
- TableField 指定 name 对应 teacher_name。

数据库本身还必须配置自增/identity 主键，本课 schema.sql 已配置。TableId 不会自动把普通列改成自增列。

## 4. 四个最常用方法

```java
TeacherRow row = new TeacherRow();
row.setName("赵老师");
row.setSubject("英语");
int inserted = mapper.insert(row);
Long id = row.getId();

TeacherRow saved = mapper.selectById(id);
row.setName("赵老师（更新）");
int updated = mapper.updateById(row);
int deleted = mapper.deleteById(id);
```

insert/updateById/deleteById 返回受影响行数，不是生成的 id。新增后从 row.getId() 读取回填主键。

selectById 返回实体或 null；更新/删除不存在编号通常返回 0。若 HTTP 接口需要 404，应在 Service 明确处理，不要把 0 行修改当成成功。

updateById 需要对象中的 id。字段更新还受 Plus 字段策略影响，不能假定把属性设成 null 就一定能清空数据库列。本课更新明确的非空姓名，不演示清空字段。

## 5. 为什么统计仍然写 XML

按科目统计人数，结果不是 teacher 表的一行，而是 `subject + teacherCount`。因此使用独立的 [SubjectCount.java](../mybatis-lab/src/main/java/learning/persistence/SubjectCount.java)。

[Day42TeacherMapper.xml](../mybatis-lab/src/main/resources/mapper/Day42TeacherMapper.xml)：

```sql
SELECT subject, COUNT(*) AS teacher_count
FROM teacher
GROUP BY subject
ORDER BY subject
```

resultMap 把 teacher_count 映射为 teacherCount。COUNT 使用 Long 接收。这里没有 WHERE 输入参数，因此没有字符串拼接。

BaseMapper 可以解决常见单表 CRUD，但聚合、复杂 JOIN、窗口函数、权限范围等查询应根据需求明确设计 SQL 与结果对象，不能因为用了 Plus 就强行套 selectById 或返回 Entity。

## 6. 两套 Mapper 能否共存

可以。本工程 Day41 使用普通接口和 XML，Day42 继承 BaseMapper，同时仍有自定义 XML。两者查询同一张表。

测试会先通过 Plus insert，再用 Day41 XML 查询新增数据，证明它们操作的是同一个数据库，不是两套独立数据。

## 7. 与目标项目对照

目标 `E:\code\java\aic-teaching-service` 根 POM 的 mybatis-plus-boot-starter 版本为 3.3.1；本课固定为 3.4.3.4，不宣称与目标版本完全一致。不要把 generator 的版本误当作运行 starter 的版本。

查看服务模块的 TeacherProfileMapper.java、TeacherProfileMapper.xml 和 TeacherProfileEntity.java，分别关注通用 Mapper 继承、定制查询，以及表/主键注解。只比较源码结构，本次没有构建或执行目标项目。

## 8. 自动化验收

运行 [MapperIntegrationTest.java](../mybatis-lab/src/test/java/learning/persistence/MapperIntegrationTest.java)，共 5 个测试：Day41 三个映射/绑定测试，Day42 两个 CRUD/统计测试。

CRUD 测试检查影响行数、主键回填、更新后数据、删除后查不到以及重复删除返回 0。统计测试检查 Java 科目计数为 2。

这些是真实 H2 集成测试，不是 MySQL 测试。默认测试修改回滚，程序 main 则实际新增和删除演示记录。

- [ ] 能解释 BaseMapper 泛型参数。
- [ ] 能区分 insert 返回行数和 row.id。
- [ ] 能说明 TableField 与 XML resultMap 分别在哪里使用。
- [ ] 能对照控制台 SQL 理解通用 CRUD。
- [ ] 能解释为什么聚合查询返回 SubjectCount 而不是 TeacherRow。

## 9. 本次验证记录

2026-11-26：5 个 H2 集成测试全部通过，Maven package 成功；实际启动 JAR，确认新增回填编号 4、更新后姓名、科目统计 Java=2/数学=1/英语=1，以及删除后查询为 null。程序与连接池正常关闭。没有验证 MySQL 方言、真实业务数据库或并发性能。

控制台可能出现 `will not be managed by Spring` 的 JDBC 事务提示：本例 main 未开启跨多个方法的 Spring 事务，各次操作按当前连接设置执行；这行调试信息本身不是 SQL 执行失败。测试类则使用 Spring 测试事务并默认回滚。

下一课 Day43：动态 SQL，可选条件、参数绑定与排序白名单。
