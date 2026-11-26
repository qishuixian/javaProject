# Day44：分页与总数

今天只解决：**一页只有 1 条数据，为什么 total 却是 2，数据库又明明有 3 条？**

沿用 mybatis-lab 的 H2 数据库，初始数据为张老师/Java、李老师/Java、王老师/数学。不需要 MySQL，没有 HTTP 端口。

## 1. 先分清三个数字

筛选 Java 教师，每页 1 条，查询第一页：

| 数量 | 值 | 意义 |
| --- | --- | --- |
| 当前页条数 currentCount | 1 | 本次 records 实际有几条 |
| 筛选后总数 total | 2 | 所有满足 Java 条件的记录数，不受页码影响 |
| 表内总数 | 3 | teacher 表一共有几行，不带筛选 |

pageSize 是每页最多返回几条，不一定等于当前页条数。total 不能直接用 records.size() 代替，否则前端无法知道还有多少页。

## 2. 运行方式

IDEA 使用 **mybatis-lab** 模块、**JDK 8**，运行 [MybatisLabApplication.main()](../mybatis-lab/src/main/java/learning/persistence/MybatisLabApplication.java)，程序实参留空。首次导入需将 mybatis-lab/pom.xml 添加为 Maven 项目。

控制台在 Day42 示例之后打印：

```text
Day44 当前页条数/筛选总数/表内总数：1/2/3
Day44 总页数：2
Day44 超出末页：条数=0，总数=2
```

同时会看到执行的 SQL 和参数。程序运行完退出，数据不会保留到下次启动。

## 3. 两条 SQL 分别做什么

[Day44TeacherMapper.xml](../mybatis-lab/src/main/resources/mapper/Day44TeacherMapper.xml)：

```sql
SELECT COUNT(*) FROM teacher WHERE subject = ?;

SELECT id, teacher_name, subject FROM teacher
WHERE subject = ?
ORDER BY subject ASC, id ASC
LIMIT ? OFFSET ?;
```

第一条统计筛选后的所有记录，第二条只取当前页。若 offset 已经达到或超过 total，直接返回空列表，不再执行第二条 SQL。COUNT 查询不加 LIMIT/OFFSET；列表也不是先查全部再在 Java 中截取。

`COUNT(*)` 在没有匹配记录时返回数字 0，不是 null。Mapper 用 long 接收总数，列表返回 List<TeacherRow>。

为了讲清分页，本课只保留可选 subject 精确筛选，排序固定为 subject ASC、id ASC。Day43 的多条件和排序白名单示例仍可单独运行。

## 4. offset 怎么算

```java
long offset = ((long) page - 1) * pageSize;
```

页码从 1 开始，数据库偏移量从 0 开始。

| page | pageSize | offset | 含义 |
| --- | --- | --- | --- |
| 1 | 2 | 0 | 跳过 0 条，最多取 2 条 |
| 2 | 2 | 2 | 跳过 2 条，最多取 2 条 |
| 3 | 2 | 4 | 跳过 4 条，最多取 2 条 |

必须先转 long 再乘；如果两个 int 已经乘法溢出，再赋值给 long 也无法恢复正确值。

本例校验 page >= 1、pageSize 在 1～100。支持超大合法页码返回空页，但不代表深分页性能好；生产接口通常还要限制深分页或采用游标分页。

LIMIT/OFFSET 都通过 `#{...}` 绑定，不拼接用户文本。

## 5. count 和列表如何保证筛选口径一致

```xml
<sql id="filters">
    <where>
        <if test="subject != null">subject = #{subject}</if>
    </where>
</sql>
```

两个查询都使用 `<include refid="filters"/>`。sql 定义公共 SQL 片段，include 引用它，避免一个查询加了筛选、另一个忘记加。

[TeacherPageService.java](../mybatis-lab/src/main/java/learning/persistence/TeacherPageService.java) 在调用 Mapper 前对 subject 做 trim，空白按未传处理。不要绕过 Service 后又假设空字符串仍会被视为未传。

如果未来增加姓名、业务线或权限条件，要同时影响 count 与列表。真实项目中的总数可能暴露数据范围，因此不能把不带权限的 COUNT 当作筛选总数。

## 6. 为什么 ORDER BY 里需要 id

两位教师的科目都是 Java，只有 `ORDER BY subject` 无法确定谁排前面。追加唯一编号：

```sql
ORDER BY subject ASC, id ASC
```

这样静态数据下，第一页 id=1、第二页 id=2，不会因为同科目顺序不确定而重复或漏看。

但稳定排序不等于并发快照：两次请求之间有新增或删除，offset 分页仍可能移动。本课 Service 的 count 和列表是两次独立查询，没有开启一致性快照事务，期间发生写入时总数和列表也可能不同。公共筛选片段只能保证条件口径，不保证时间一致性。事务与隔离后续再学。

## 7. 空页不代表总数为零

- Java 共 2 条，每页 1 条，查第 3 页：records=[]，total=2，totalPages=2。
- 查询不存在的科目：records=[]，total=0，totalPages=0。
- 不筛选，共 3 条，每页 2 条，查第 2 页：records 有 1 条，total=3，totalPages=2。

本课不自动把超出末页的请求改成最后一页，也不把它当作异常。前端可根据 totalPages 提示或调整页码，但后端应明确自己的协议。

## 8. 分页结果类

[TeacherPage.java](../mybatis-lab/src/main/java/learning/persistence/TeacherPage.java) 包含 page、pageSize、total、records，并提供派生的 currentCount、totalPages。

```java
total / pageSize + (total % pageSize == 0 ? 0 : 1)
```

先整数除法求完整页数；如果还有余数，再加一页。例如 3/2 得到 1，余数为 1，因此共 2 页。这里省去 `total + pageSize - 1`，也避免该加法在极大总数下溢出。

本课 TeacherPage 是供已校验的 Service 构造的结果对象，pageSize 合法性在 Service 保证。它不是 MyBatis-Plus 的 Page，也不需要配置 Plus 分页插件；分页 SQL 是我们手写的。

## 9. 测试与目标项目对照

运行 [Day44PaginationTest.java](../mybatis-lab/src/test/java/learning/persistence/Day44PaginationTest.java)，新增 8 个测试覆盖三种数量、稳定顺序、末页不足、超出末页、无匹配、空白与特殊文本、非法页参数、大 offset 不发生 int 溢出。

连同旧测试总计 21 个。安装 Maven 后在 mybatis-lab 运行 `mvn test`。这些使用真实 H2 SQL，但不验证 MySQL 排序规则、深分页性能或并发快照。

对照 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\resources\mapper\TeacherProfileMapper.xml`，查找 LIMIT 和 countForExport。目标项目有导出和其他查询场景，不要仅看 LIMIT 就当作同一个分页接口；需沿 Mapper 和 Service 确认筛选及总数口径。本次未运行目标项目。

## 10. 验收

- [ ] 能解释 currentCount、total、表内总数的区别。
- [ ] 能算出 page=3、pageSize=20 的 offset 为 40。
- [ ] 能解释公共过滤片段为何同时被两条 SQL 引用。
- [ ] 能解释超出末页为什么 total 仍然大于 0。
- [ ] 能说明唯一排序键解决什么，以及不能解决什么。
- [ ] 能指出 LIMIT/OFFSET 在数据库执行，而不是 Java 截取全部结果。

## 11. 本次验证记录

2026-11-26：使用 JDK 8 和临时 Maven 3.9.9 执行 package，21 个 H2 集成测试全部通过，失败、错误、跳过均为 0，BUILD SUCCESS。实际运行 JAR，确认数量 1/2/3、总页数 2、超出末页仍保留 total=2，程序正常退出。

首次边界测试发现当前 H2 对超大 OFFSET 的限制；已在 Service 统计后对超出末页直接返回空列表，再重跑测试与程序通过。未操作 IDEA，未验证 MySQL、并发快照或深分页性能。

下一课 Day45：Spring 事务，故意抛异常验证回滚，并观察代理与异常捕获的影响。
