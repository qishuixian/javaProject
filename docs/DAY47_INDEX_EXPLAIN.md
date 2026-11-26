# Day47：索引与 EXPLAIN

今天解决：**SQL 能查出正确结果，还需要检查它怎样找到这些结果。**

通过 10,000 条数据，对比同一个查询在创建索引前后的执行计划和扫描量，同时检查返回记录完全一致。数据库是独立 H2 内存库，不连接 MySQL，不修改前几课的 teacher 表。

## 1. 如何运行

IDEA 选择 **mybatis-lab** 模块、**JDK 8**，打开 [Day47IndexTest.java](../mybatis-lab/src/test/java/learning/persistence/Day47IndexTest.java)，运行整个测试类。

不需要启动 main 或数据库服务。安装 Maven 后，在 mybatis-lab 执行：

```powershell
mvn -Dtest=Day47IndexTest test
```

控制台打印 BEFORE、AFTER 两份计划和扫描计数。测试结束关闭连接，临时数据库销毁。测试只建一个新索引，不对现有数据库做 DDL。

## 2. 数据为什么这样准备

表中有 id 主键、teacher_name、subject。生成 10,000 行，每隔 100 行设置 subject=Java，其他为 Other，所以 Java 有 100 行，占 1%。

如果只有前三课的 3 行数据，数据库可能认为扫描全表更便宜，难以观察索引收益。本课的数据分布是人为设置的教学场景，不代表真实业务分布。

## 3. 要优化的查询

```sql
SELECT id, teacher_name
FROM teacher
WHERE subject = 'Java'
ORDER BY subject, id
LIMIT 20;
```

先筛选 Java，再按科目和编号排列，取前 20 条。科目在筛选后都是 Java，因此 id 决定这批结果的内部顺序。应返回 id=100、200……2000。

本课固定 SQL 中的 Java 是教学常量，不是用户输入。业务输入仍应按 Day43 使用参数绑定。

## 4. 联合索引如何设计

```sql
CREATE INDEX idx_teacher_subject_id ON teacher(subject, id);
```

可以把它理解成“先按科目分类，同科目内再按编号排序”的目录。本查询按科目等值筛选，然后需要编号顺序，索引列顺序与此相吻合。

表原本已有 id 主键索引，但仅靠 id 不便直接定位所有 Java 记录。**没有这个联合索引，不等于表完全没有索引。**

联合索引不是两个独立索引相加，列顺序有意义。通常应先从实际筛选、连接、排序需求出发设计候选索引，再检查计划；不要机械地给每个字段都建索引。

本例还选择 teacher_name，而候选索引没有它，因此不能称为覆盖所有返回列的索引，也不能由扫描计数推断“完全没有额外行读取”。

## 5. EXPLAIN 与 EXPLAIN ANALYZE

```sql
EXPLAIN ANALYZE
SELECT id, teacher_name FROM teacher
WHERE subject = 'Java'
ORDER BY subject, id LIMIT 20;
```

普通 EXPLAIN 通常用于查看优化器选择的执行计划；EXPLAIN ANALYZE 会实际执行查询并附加运行信息。不同数据库语法和输出格式不同。不要把 ANALYZE 当作永远不执行业务语句的只读说明工具；本课只分析 SELECT。

本例还执行独立的 `ANALYZE` 命令更新 H2 统计信息。它与 `EXPLAIN ANALYZE SELECT ...` 是两种用途不同的语句。

## 6. 本次真实观察

在当前 H2 版本与本课固定数据上：

| 指标 | 建索引前 | 建索引后 |
| --- | --- | --- |
| H2 scanCount | 10001 | 20 |
| 计划中使用候选索引 | 否 | IDX_TEACHER_SUBJECT_ID |
| 返回数量 | 20 | 20 |
| 首/末编号 | 100 / 2000 | 100 / 2000 |

scanCount 是 H2 计划里的实际扫描计数，可能包含游标扫描结束等内部行为，所以不是看到 10001 就认为表有 10001 行。它不等于所有物理 IO，也不是 MySQL EXPLAIN 的 rows 字段。

测试断言“索引后计数低于索引前”，不把 10001 和 20 写死为所有版本都必须满足的常数；同时检查计划确实包含目标索引名，并逐条比较查询结果。

扫描工作量下降不等于耗时必然按同样倍数下降。本课没有做预热、重复采样、并发或 IO 基准测试，不给出性能提升倍数。

## 7. 怎样读 MySQL 的计划

迁移到 MySQL 时要在独立测试库重新准备数据，并运行实际计划，不能直接使用 H2 的输出结论。常见 EXPLAIN 字段可这样理解：

| 字段 | 关注点 |
| --- | --- |
| possible_keys | 可能使用的候选索引 |
| key | 优化器实际选择的索引 |
| type | 访问方式，如 ALL、range、ref；结合查询理解 |
| rows | 通常是预计扫描行数，不是已验证的实际值 |
| filtered | 预计筛选保留比例 |
| Extra | 是否涉及额外排序、临时表、覆盖索引等 |

`Using filesort` 不一定代表真正落盘，`Using index` 也不等于“所有出现索引名的查询”。版本支持时，可用 MySQL 的 EXPLAIN ANALYZE 获取实际执行信息，但需要按该版本语法和输出阅读。

## 8. 索引的代价与常见误区

- 索引占空间，新增、删除和更新相关列时需要维护。
- 条件返回大部分数据时，优化器可能选择扫描，不必强制它使用某个索引。
- 对列套函数、隐式类型转换、前导通配符等可能影响某些索引使用，要看实际表达式与执行计划。
- 唯一排序键有助于稳定结果，但不解决 Day44 提到的并发快照问题。
- count 查询和列表查询可能有不同计划，应分别分析，不能只看分页列表很快就认为总数也快。

## 9. 对照目标项目的方法

目标项目路径：`E:\code\java\aic-teaching-service`。可以从之前读过的 TeacherProfileMapper.xml 中选择一个具体查询，整理 WHERE、ORDER BY、返回列与现有表索引，然后在授权的测试数据库运行 EXPLAIN。

本次对仓库 SQL 文件的有限检索没有取得可用建索引定义，不能据此断言数据库没有索引，也不能直接建议给真实表添加本课的索引。未访问目标数据库，未修改其表结构。

## 10. 验收与验证记录

- [ ] 能说明主键索引与本课联合索引的区别。
- [ ] 能解释为什么列顺序选 subject、id。
- [ ] 能在 AFTER 计划中找到索引名和 scanCount。
- [ ] 能证明优化前后结果一致。
- [ ] 能区分估算行数、实际扫描计数和查询耗时。
- [ ] 能说明索引并非越多越好。

2026-11-26：JDK 8 配合临时 Maven 3.9.9 执行 package。新增 1 个完整对照实验，连同旧测试共 32 个，全部通过，失败/错误/跳过均为 0，BUILD SUCCESS。扫描计数和结果见上表；临时数据库已关闭。未操作 IDEA，未验证 MySQL 或生产性能。

下一课 Day48：批量与 N+1，把逐条查询改成批量查询，比较结果与 SQL 次数。
