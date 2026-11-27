# Day48：N+1 与批量查询

今天解决：**先查一批课节，再循环查询每个课节的教师，为什么会产生太多 SQL？**

沿用 mybatis-lab 的真实 H2 数据库。本课新增 lesson48 表，不需要安装 MySQL，不提供 HTTP 接口。

## 1. 运行入口

IDEA 选择 **mybatis-lab**、**JDK 8**，打开 [Day48BatchTest.java](../mybatis-lab/src/test/java/learning/persistence/Day48BatchTest.java)，运行整个测试类，预期 3 个测试通过。

安装 Maven 后，在 mybatis-lab 中也可运行：

```powershell
mvn -Dtest=Day48BatchTest test
```

不需要先运行 main。课堂输出应包含 `Day48 SQL次数：6 -> 2`。这里的测试会真正执行 SQL，不使用假 Mapper。

## 2. 实验数据

teacher 表仍是 1=张老师、2=李老师、3=王老师。新增课节表：

| 课节 id | teacher_id | 预期补充姓名 |
| --- | --- | --- |
| 1 | 2 | 李老师 |
| 2 | 1 | 张老师 |
| 3 | 2 | 李老师 |
| 4 | 999 | 教师不存在 |
| 5 | 3 | 王老师 |

故意让两节课使用同一个教师，并保留缺失教师编号，以防优化时错误去重课节或丢弃缺失数据。lesson48 没有教师外键，这是实验安排，不代表真实业务应该允许孤立引用。

## 3. N+1 是什么

[LessonTeacherService.java](../mybatis-lab/src/main/java/learning/persistence/LessonTeacherService.java) 中 naive 方法：

```java
for (LessonRow lesson : mapper.lessons(maxId)) {
    TeacherRow teacher = mapper.teacher(lesson.getTeacherId());
    result.add(label(lesson, teacher));
}
```

先执行 1 次课节查询，拿到 N 节课；每节课再执行 1 次教师查询，总计 N+1 次。这里 N=5，因此是 6 次。

课节只有几条时不明显；几百条时会放大数据库往返成本。给教师主键加索引并不能消除这些重复往返，索引优化和批量优化解决不同问题。

## 4. 批量版的四步

```text
查课节列表
  → 收集教师编号并去重
  → 一条 IN 查询取教师
  → 建 Map，按原课节顺序补充姓名
```

```java
Set<Long> uniqueIds = new LinkedHashSet<>();
for (LessonRow lesson : lessons) {
    uniqueIds.add(lesson.getTeacherId());
}
```

Set 将重复教师编号 2 合并成一次查询参数。但没有对 lessons 去重，所以两节属于李老师的课仍保留。

```java
Map<Long, TeacherRow> byId = new HashMap<>();
for (TeacherRow teacher : teachers) {
    byId.put(teacher.getId(), teacher);
}
```

把 id 作为 key，教师对象作为 value，后续通过 `byId.get(teacherId)` 获取对应教师。不再对每个课节执行 SQL，也不用每节课都遍历完整教师列表。

最后遍历原始 lessons 组装，保证课节顺序不变。**IN 查询不保证按传入编号的顺序返回，不能把两个列表按下标直接配对。** 缺失 id=999 时 Map.get 返回 null，输出“教师不存在”，仍保留这一节课。

## 5. foreach 怎样展开 IN 参数

[Day48BatchMapper.xml](../mybatis-lab/src/main/resources/mapper/Day48BatchMapper.xml)：

```xml
<foreach collection="ids" item="id" open="(" separator="," close=")">
    #{id}
</foreach>
```

- collection：Mapper 的 `@Param("ids")` 参数。
- item：当前遍历元素的名字。
- open/close：生成左右括号。
- separator：元素之间插入逗号。
- `#{id}`：每个元素独立绑定，生成 `(?, ?, ?, ?)`。

不是用字符串拼接 `1,2,3`，也不使用 `${ids}`。Mapper 与 XML 的入口分别是 [Day48BatchMapper.java](../mybatis-lab/src/main/java/learning/persistence/Day48BatchMapper.java) 和上面的 XML。

## 6. 空列表为什么要单独处理

没有课节时，Service 直接返回空结果，因此只执行最初 1 条课节 SQL。

XML 也做防御：ids 为 null 或空集合时生成 `WHERE 1 = 0`，不产生非法 `IN ()`，更不能把 WHERE 整段省略后变成“查全部教师”。测试直接调用 Mapper，验证这个边界。

## 7. SQL 次数是怎样统计的

测试内注册 MyBatis Interceptor，在 `StatementHandler.prepare` 处统计当前线程实际准备的 JDBC 语句。每次运行方法前清零，完成后读取，不计初始化 SQL。

本课每次准备对应一次普通 SELECT 执行，没有批处理或异步查询，因此可以用来验证本实验的 SQL 次数；它不是通用数据库流量监控工具。

naive 没有事务包裹各次 Mapper 调用，没有共享会话的一级缓存，也未开启二级缓存，因此重复教师 id 在本实验仍会发出查询。如果以后增加事务或缓存，实际次数可能变化，应测量，而不是只数 Java 循环次数。

测试使用独立的 day48 H2 数据库名，避免带测试拦截器的新 Spring 上下文与旧测试争用同一个初始化数据库。计数器只在测试配置中注册，不影响正式应用。

## 8. 不只是验证次数

三个测试验证：

1. naive 与 batch 的完整结果一致，顺序、重复教师、缺失教师都保留；SQL 为 6 和 2。
2. 没有课节时，两种方法都只查询一次并返回空列表。
3. 空/null 教师编号集合不会返回全部教师。

本例用字符串表达课节和姓名方便观察；正式 HTTP 接口应返回明确的响应 DTO。

## 9. 批量的边界

本例只有 4 个唯一编号，使用一次 IN。真实场景可能需要分批，例如每批几百个编号；合适大小取决于数据库限制、SQL 长度、网络和业务数据量，不存在通用最佳值。

批量不等于事务：先查课节再查教师期间数据仍可能变化。需要一致性视图时要结合事务与数据库隔离规则。复杂需求也可以考虑 JOIN，但必须验证一对多导致的行数变化，不能为了“一条 SQL”破坏分页或结果语义。

本课没有性能计时基准，6→2 证明往返次数下降，不代表耗时必定变成三分之一。

## 10. 对照目标项目

查看 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\resources\mapper\TeacherProfileMapper.xml` 中的 `selectActiveByTeacherIds`：它按业务线和有效状态过滤，并通过 foreach 绑定教师编号列表。

真实项目的业务线、权限和逻辑删除条件不能在批量优化时丢掉。这里只对照已有 XML，不宣称目标调用方一定存在 N+1，也没有运行目标服务。

## 11. 验收

- [ ] 能解释 N+1 中 N 和 1 各代表什么。
- [ ] 能说明去重教师编号为什么不应去重课节。
- [ ] 能用 Map 按编号补充信息，不依赖 IN 返回顺序。
- [ ] 能处理缺失教师和空列表。
- [ ] 能解释 SQL 次数下降为何不等于固定的耗时收益。

## 12. 本次验证记录

2026-11-27：JDK 8 与临时 Maven 3.9.9 执行 package，新增 3 个测试连同旧测试共 35 个，全部通过，失败/错误/跳过均为 0，BUILD SUCCESS。实际统计逐条查询为 6 次、批量为 2 次，完整结果一致，包含重复教师、缺失教师和原课节顺序；空课节只查询一次。首次全量测试暴露独立测试上下文共用库名的初始化冲突，改为 Day48 专用 H2 库后重跑通过。未操作 IDEA，未验证 MySQL、并发性能或固定耗时提升比例。

下一课 Day49：多数据源与连接池，观察数据源选择、代理和事务边界。
