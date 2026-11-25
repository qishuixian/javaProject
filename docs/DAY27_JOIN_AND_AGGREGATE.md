# Day 27：JOIN 与聚合

## 今日目标

用 Day25 的 teacher、lesson、lesson_review 三张表回答：每位教师有几节课、没有课的教师是否保留、平均课时和审核分是多少。

SQL 文件：[01_join_aggregate.sql](../database/day27/01_join_aggregate.sql)。本课只读，需先完成 Day25 学习库和示例数据；本次只编写并静态核对 SQL，未连接数据库执行。

## 1. JOIN 的基本读法

```sql
SELECT t.teacher_no, t.name, l.lesson_no
FROM teacher t
JOIN lesson l ON l.teacher_id = t.id;
```

`t` 和 `l` 是表别名，ON 表示外键关联。JOIN 只生成查询结果，不修改表。

## 2. INNER JOIN 与 LEFT JOIN

INNER JOIN 只返回有匹配课节的教师；没有课节的 T002 不出现。LEFT JOIN 保留左表的每一行，T002 会出现，但右表课节字段是 NULL。

选择取决于问题：只看有课教师用 INNER JOIN；统计全部教师及课节数通常用 LEFT JOIN。

一个教师有多节课时，JOIN 明细会出现多行。这不是重复数据，而是一对多关系展开。要变成每位教师一行，需要 GROUP BY。

## 3. COUNT(*) 与 COUNT(column)

```sql
COUNT(l.id)
```

只统计非 NULL 的 l.id。LEFT JOIN 的无匹配占位行中 l.id 为 NULL，所以 T002 的课节数是 0。

如果写 COUNT(*)，占位行也会被统计，T002 会错误显示为 1。统计子表实体数量时通常 COUNT 子表主键。

## 4. GROUP BY 与 HAVING

GROUP BY 把同一教师的明细组成一组。SELECT 中没有聚合的列应放进 GROUP BY，避免依赖宽松 SQL 模式。

常见聚合函数：COUNT 数量、AVG 平均、SUM 求和、MIN/MAX 最小最大。AVG 会忽略 NULL。

WHERE 在分组前过滤明细，HAVING 在分组后过滤结果：

```text
FROM/ON → WHERE → GROUP BY → HAVING → SELECT → ORDER BY
```

不能写 WHERE COUNT(l.id) >= 2；分组结果还没有在 WHERE 阶段产生。

## 5. NULL 与 COALESCE

没有审核时 AVG(r.score) 为 NULL。`COALESCE(AVG(r.score), 0)` 把它显示为 0，但“无审核”和“平均分为 0”可能是不同业务语义，真实接口不应无条件替换。

## 6. ON 与 WHERE 的区别

```sql
LEFT JOIN lesson l ON l.teacher_id = t.id
WHERE l.duration_minutes >= 45
```

无课教师的右表字段为 NULL，WHERE 不成立，结果接近 INNER JOIN。若要保留全部教师，只连接满足时长的课节，把条件放 ON：

```sql
LEFT JOIN lesson l
  ON l.teacher_id = t.id AND l.duration_minutes >= 45
```

## 7. 预计结果

按 Day25 原始数据：INNER JOIN 只有 T001 的 L001、L002；LEFT JOIN 还保留 T002；课节聚合为 T001=2、T002=0；HAVING >=2 只有 T001；审核汇总中 T001 已审核 1、平均分 88.50，T002 为 0。

这些是样例数据下的预期，不是本次数据库实测结果。

## 8. 与目标项目的联系

阅读 aic-teaching-service 的 Mapper XML 时，先确定结果一行代表教师、课节、报告还是消息，再看 COUNT 的字段。权限条件必须进入正确的 JOIN/WHERE 范围，不能因为 LEFT JOIN 看到更多数据就放宽权限。

## 今日验收

- [ ] 能区分 INNER JOIN 和 LEFT JOIN。
- [ ] 能解释一对多为何产生多行。
- [ ] 能用 COUNT(子表主键) 统计无子数据为 0。
- [ ] 能区分 WHERE 与 HAVING。
- [ ] 能解释 AVG、NULL、COALESCE。
- [ ] 能说明 ON 条件与 WHERE 条件对 LEFT JOIN 的影响。

下一课：JDBC，用 Java 连接本地学习数据库并执行参数化查询。
