# Day 26：SQL 增删改查与条件筛选

## 1. 今日目标与运行文件

Day25 定义“表长什么样”，今天学习“怎么操作表中的数据”：

| 操作 | SQL | 含义 |
|---|---|---|
| 新增 | INSERT | 增加行 |
| 查询 | SELECT | 读取行 |
| 修改 | UPDATE | 修改命中的行 |
| 删除 | DELETE | 删除命中的行 |

- [只读查询 01_select.sql](../database/day26/01_select.sql)
- [增删改与回滚 02_crud_rollback.sql](../database/day26/02_crud_rollback.sql)
- [Day25 表结构](../database/day25/01_schema.sql)

本课不新增 Java 入口。需要可用的本地 MySQL 和 Day25 的表；只读示例的预期结果还依赖 Day25 示例数据。本次只编写并静态核对 SQL，没有连接数据库执行。

## 2. 如何执行

连接你自己的本地学习库，在 mysql 提示符执行：

```sql
SELECT DATABASE();
SOURCE E:/my/javaProjects/database/day26/01_select.sql;
SOURCE E:/my/javaProjects/database/day26/02_crud_rollback.sql;
```

SQL 中固定 USE java_learning_day25。不要在真实业务连接运行，也不要在已有未提交事务中执行第二个文件：START TRANSACTION 会影响已有事务边界。

第二个文件在同一连接创建独立合成数据，最后 ROLLBACK，不改 Day25 原有记录。需要同时具备 SELECT、INSERT、UPDATE、DELETE 权限。中途有错误时停止并 ROLLBACK，不要继续试着提交。

## 3. SELECT：读哪些列、从哪里读、保留哪些行

```sql
SELECT id, teacher_no, name
FROM teacher
WHERE teacher_no = 'T001';
```

逐段读：查询 id、teacher_no、name 三列；从 teacher 表取；只保留编号为 T001 的行。

SELECT 的列列表不是 Java 的 List 类型，它描述结果包含哪些字段。初学可以用 SELECT * 查看全部列，但接口开发应按需求选择字段，避免依赖全部列。

SQL 字符串通常使用单引号，数字直接写。SQL 用 = 比较；Java 中的 = 是赋值、== 才比较，二者语法不同。

## 4. 条件怎么写

```sql
WHERE duration_minutes >= 45 AND duration_minutes <= 60
WHERE lesson_no IN ('L001', 'L002')
WHERE name LIKE '张%'
```

- AND：同时满足。
- OR：满足至少一个。
- IN：匹配给定候选值之一。
- LIKE 中 %：任意数量字符；_：单个字符。
- BETWEEN 45 AND 60：两端都包含，等价于上述时长条件。

混合条件明确加括号，例如：

```sql
WHERE (lesson_no = 'L001' OR lesson_no = 'L002')
  AND duration_minutes >= 60
```

AND 的优先级高于 OR，省略括号容易写出范围不同的查询。LIKE 的大小写与字符比较还受排序规则影响。

## 5. 日期窗口与稳定排序

```sql
WHERE starts_at >= '2026-11-25 00:00:00'
  AND starts_at < '2026-11-26 00:00:00'
```

左闭右开表示完整的 25 日。不要只写小于等于 23:59:59 来猜测所有时间精度，也不要把下一日零点用 BETWEEN 包含进来。

```sql
ORDER BY name, id
LIMIT 1 OFFSET 0
```

先按姓名排序，同名时按唯一 ID 排序，取第一行。下一页 OFFSET 1。没有 ORDER BY 就不保证顺序；唯一兜底排序减少同值行的不确定性，但数据并发变化时仍可能影响偏移分页，后续再学习一致性。

## 6. NULL、空字符串与不存在的行

三者不同：

| 情形 | 含义 |
|---|---|
| 没有审核行 | 本例尚未审核 |
| 有审核行，remark 为 NULL | 已审核，但没有提供备注 |
| 有审核行，remark 为 '' | 已审核，保存了空文本 |

```sql
WHERE remark IS NULL
WHERE remark IS NOT NULL
WHERE remark = ''
```

不要写 remark = NULL 或 remark <> NULL。与 NULL 的普通比较产生 UNKNOWN，WHERE 只保留 TRUE，所以会查不到预期记录。

```sql
SELECT NULL = NULL;   -- NULL，表示 UNKNOWN
SELECT NULL IS NULL;  -- 1
SELECT '' IS NULL;    -- 0
```

remark <> '' 也不会包含 NULL 行。如果需要“空文本或未提供”，明确写：

```sql
WHERE remark IS NULL OR remark = ''
```

CHAR_LENGTH(NULL) 返回 NULL；CHAR_LENGTH('') 返回 0。纯空格是否等于空字符串可能受 MySQL 排序规则尾部空格比较影响；严格检查零长度可以用 CHAR_LENGTH(remark)=0。不要未经约定就用 TRIM 把“空白”全部视为“未提供”。

## 7. INSERT：新增记录

```sql
INSERT INTO teacher (teacher_no, name)
VALUES ('你的独立练习编号', '练习老师');
```

id 是自增列，因此省略。列名顺序与 VALUES 值一一对应。唯一编号重复会报错，不应该靠 INSERT IGNORE 隐藏失败。

脚本使用会话变量保存新 ID：

```sql
SET @day26_teacher_id = LAST_INSERT_ID();
```

LAST_INSERT_ID 是当前连接最近插入的自增 ID。必须在插入成功后立刻读取，不应通过 MAX(id) 猜测；其他连接也可能在插入。

## 8. UPDATE：修改记录

```sql
SELECT id, name FROM teacher WHERE id = @day26_teacher_id;
UPDATE teacher SET name = 'Day26已改名老师'
WHERE id = @day26_teacher_id;
```

先用同样条件 SELECT，确认将修改哪一行，再 UPDATE。没有 WHERE 的 UPDATE 会作用于全表。

紧接着 SELECT ROW_COUNT() 查看上一条修改的影响行数。更新为相同值时可能为 0，客户端设置也可能影响“命中数/实际改变数”口径；不能单凭 0 就断言记录不存在。应复查记录和实际变化。

## 9. DELETE：删除行，不是删除表

```sql
DELETE FROM lesson_review WHERE id = @day26_review_id;
```

删除的是指定审核行，表结构仍然存在。DROP TABLE 删除整个表结构与数据，TRUNCATE 也不是本课的行级删除，不能替代这里的 DELETE。

Day25 设置了外键限制：有课节引用的教师不能直接删除，有审核引用的课节也不能直接删除。本课只删除刚创建的审核行，再用保存点恢复它。

## 10. 为什么脚本最后使用 ROLLBACK

```sql
START TRANSACTION;
-- 新增、修改
SAVEPOINT before_delete;
-- 删除审核
ROLLBACK TO SAVEPOINT before_delete;
-- 审核恢复，事务继续
ROLLBACK;
```

保存点回滚只撤销保存点之后的操作，不是结束整个事务。最后 ROLLBACK 撤销整个事务中的行数据变化。

InnoDB 支持本课这些 DML 回滚；自增编号可能已经消耗，不保证回到原值。不要在中间加入 CREATE/ALTER 等可能隐式提交的 DDL。完整事务知识在 Day29 继续学习。

## 11. 预期结果与验证范围

Day25 原始数据未改变时：

- 按 T001 查询：1 行。
- 时长 45～60 分钟：L001、L002 两行。
- 25 日日期窗口：L001 一行。
- 姓名以张开头：两位同名教师。
- remark IS NULL：原始审核一行。
- remark = ''：无结果。

增删改脚本预期：

| 检查项 | 预期 |
|---|---|
| 改名后查询 | Day26已改名老师 |
| 更新 NULL 前 | is_null=1，text_length=NULL |
| 更新空字符串后 | is_null=0，text_length=0 |
| 删除审核后的计数 | 0 |
| 回到保存点后的审核计数 | 1 |
| 完整回滚后的练习教师/课节计数 | 均为 0 |

这些是预期结果，不是本次已在服务器验证的输出。若用户已经修改 Day25 数据，只读查询的行数应按实际数据判断，不要直接重跑种子数据制造重复编号。

## 12. 与 Java 和目标项目的联系

以后 JDBC/Mapper 把 Java 参数传给 SQL，应使用参数绑定，不拼接用户输入：

```text
SQL 模板：WHERE teacher_no = ?
参数：用户输入的编号
```

今天 SQL 文件使用固定教学值，不是 Web 接口拼接 SQL 的示范。服务端还必须限制业务线和权限范围，不能因为 SELECT 能返回数据就认为用户有权看到它。

## 13. 今日练习与验收

- [ ] 能拆解 SELECT/FROM/WHERE/ORDER BY/LIMIT。
- [ ] 能使用 AND、OR、IN、LIKE。
- [ ] 能解释为什么 = NULL 查不到 NULL。
- [ ] 能区分无审核行、NULL 备注、空备注。
- [ ] 能先预览目标，再按唯一 ID 更新或删除。
- [ ] 在独立练习连接执行脚本，核对回滚前后计数。
- [ ] 将只读查询改成“时长大于 45 且在 26 日上课”，预测应命中 L002。

下一课：JOIN 与聚合，统计每位教师的课节数量，保留没有授课的教师。
