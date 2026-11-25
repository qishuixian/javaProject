# Day 25：关系建模与 DDL

## 1. 今日目标

前面的 Teacher 对象和文件学习了“如何保存一条数据”。数据库还要解决多条数据之间的关系、重复编号和不存在的关联。

今天设计三个实体：教师、课节、审核。重点是表结构与约束，不急着写 Java 连接数据库，Day28 再学习 JDBC。

文件：

- [01_schema.sql：建库建表](../database/day25/01_schema.sql)
- [02_sample_data.sql：合成数据](../database/day25/02_sample_data.sql)
- [03_inspect.sql：只读检查](../database/day25/03_inspect.sql)

本课只面向独立本地库 java_learning_day25，不使用 aic-teaching-service 的数据库或表。

## 2. 从业务规则推导关系

先规定规则，再写 SQL：

1. 一位教师可以授课多次，每个课节只有一位授课教师。
2. 教师可以同名，但教师业务编号不能重复。
3. 课节业务编号不能重复。
4. 每个课节可以没有审核，或有一份当前审核结果。
5. 本课不记录审核历史，不建登录账号或审核员体系。

关系：

```text
teacher 1 ───── 多 lesson 1 ───── 0或1 lesson_review
```

如果将来需要多位教师合作授课，应新增关联表；如果要保留多次审核历史，应调整审核唯一约束或新增历史表。不要在规则变化时强行保留旧结构。

## 3. 表、行、列对应什么

| Java 概念 | 数据库中的近似对应 |
|---|---|
| 类定义的字段结构 | 表结构 |
| 一个对象的数据 | 一行记录 |
| 一个字段 | 一列 |
| 对象关联的 ID | 外键列 |

这是帮助理解的对应关系，不代表 Java 对象会自动变成表。数据库表没有 rename 等 Java 方法，需要 SQL 或后续数据访问框架操作。

三张表的核心字段：

| 表 | 字段 | 意义 |
|---|---|---|
| teacher | id、teacher_no、name | 主键、教师编号、姓名 |
| lesson | id、lesson_no、teacher_id、title、starts_at、duration_minutes | 课节身份、关联教师和上课信息 |
| lesson_review | id、lesson_id、score、remark、reviewed_at | 审核身份、关联课节、分数和备注 |

审核状态不另外保存：本例“没有审核行”表示未审核，“有行”表示已有当前结果，避免同时维护状态与行存在性产生矛盾。

## 4. 逐行理解建表

```sql
CREATE TABLE teacher (
    id BIGINT NOT NULL AUTO_INCREMENT,
    teacher_no VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_no (teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- CREATE TABLE：创建表，是 DDL（数据定义语言）。
- BIGINT：整数列，适合存较大的 ID，可对应 Java long/Long。
- AUTO_INCREMENT：省略该 ID 插入时由数据库分配；不保证连续无空洞。
- VARCHAR(32)：最多 32 个字符的可变长度字符串。
- NOT NULL：禁止 SQL NULL，**不禁止空字符串或空格**，姓名非空白仍要校验。
- PRIMARY KEY：每行唯一身份，不能重复或为 NULL。
- UNIQUE KEY：另一个不能重复的业务标识。
- InnoDB：本课使用的存储引擎，支持事务和外键。
- utf8mb4：用于完整 Unicode 字符，包括中文及许多表情字符。

实际脚本还设置 utf8mb4_unicode_ci 排序规则，通常不区分大小写。因此 T001 与 t001 会被视为重复编号；本课统一使用大写 ASCII 编号，不把大小写当成不同身份。

DDL 与 DML 不同：INSERT/UPDATE/DELETE 改行数据，CREATE/ALTER 改结构。MySQL 的这些建表语句可能隐式提交，不能指望用普通 ROLLBACK 撤回建表。

## 5. 主键与业务唯一键

teacher.id 是数据库内部身份，teacher_no 是业务编号：

```text
id=1  teacher_no=T001  name=张老师
id=2  teacher_no=T002  name=张老师
```

姓名允许重复，不适合做唯一键。外键关联稳定的 id，不复制姓名。老师改名后课节仍关联同一教师。

唯一约束在数据库层阻止重复，而不是只靠 Java 先查再插。并发请求可能同时查到“没有”，最终仍需唯一约束兜底。主键和唯一键并不等于业务权限校验。

## 6. 外键为什么重要

```sql
FOREIGN KEY (teacher_id) REFERENCES teacher (id)
    ON DELETE RESTRICT ON UPDATE RESTRICT
```

这意味着课节的 teacher_id 必须对应真实教师；存在关联课节时，不能直接删除教师或修改其关联主键。

同理，审核必须关联已存在课节。lesson_review.lesson_id 上的 UNIQUE KEY 保证一个课节最多一份当前审核。

创建顺序是教师 → 课节 → 审核，因为子表引用父表。插入数据也先父后子。不能通过关闭外键检查来“解决”设计或数据错误。

本课选择数据库外键，并限制级联删除。真实服务有时因跨库、同步或历史数据采用逻辑关联；若没有数据库外键，就必须另行保证一致性，不能假设错误关联会被自动拦截。

## 7. 字段类型与约束边界

- duration_minutes 用 INT，但不是天然只能正数。
- score 用 DECIMAL(5,2)：总共 5 位数字，其中 2 位小数，可表示 100.00，但也能表示负数或大于 100 的值。
- remark 允许 NULL；NULL 代表没有提供备注，空字符串是另一个值。
- DATETIME 不包含时区信息。本课约定录入 Asia/Shanghai 本地时间，跨时区服务需另行统一协议。
- created_at 默认 CURRENT_TIMESTAMP 会受数据库会话时间影响，不能当成自动携带时区的字段。

为兼容 MySQL 5.7，本课不依赖 CHECK 保护分数与时长范围：5.7 不执行这类检查约束。应用必须校验分数 0～100、时长大于 0、名称非空白；MySQL 8.0.16+ 可另外设计 CHECK，本课脚本不宣称已经强制这些范围。

## 8. 索引与外键不是一回事

lesson 的 (teacher_id, starts_at) 是普通联合索引，支持按教师、时间查课节的常见方向；它不限制同一教师只能有一个时间值，也不自动禁止课程时间重叠。

外键负责关联存在性，唯一索引负责判重，普通索引主要帮助查询。索引不是越多越好，后续结合实际 SQL 与 EXPLAIN 学习。

## 9. 如何执行——先确认本地连接

本次发现本机有 mysql.exe 客户端（位于 D:/mysql9.6/bin），但没有连接数据库，也没有核验服务端版本、账号或密码。客户端安装不等于服务器已启动。

使用你自己的本地学习账号连接，例如：

```powershell
mysql -h 127.0.0.1 -P 3306 -u 你的本地账号 -p --default-character-set=utf8mb4
```

-p 让客户端交互式询问密码，不把密码写在命令或文档里。连接成功后，在 **mysql 提示符**中依次执行，不能把 SOURCE 当成 PowerShell 命令：

```sql
SELECT VERSION();
SOURCE E:/my/javaProjects/database/day25/01_schema.sql;
SOURCE E:/my/javaProjects/database/day25/02_sample_data.sql;
SOURCE E:/my/javaProjects/database/day25/03_inspect.sql;
```

确认连接的是本地学习环境，且账号有创建学习库权限。也可以在数据库客户端里打开 SQL 文件逐段执行。

先看建表是否全部成功，再执行数据文件。任一数据写入报错立即停止并 ROLLBACK，不要继续提交；客户端的批量错误处理行为可能不同，不能以最后 COMMIT 成功推断前面都成功。

本课没有 DROP/TRUNCATE，也不使用 INSERT IGNORE 或覆盖式写入。第一次建表成功后重复执行会提示表已存在；种子数据也只对空练习表执行一次。遇到重跑问题先检查数据，不要删除真实库或绕过唯一约束。

## 10. 验证结果

首次完整执行后，行数应为：

| 表 | 行数 |
|---|---|
| teacher | 2 |
| lesson | 2 |
| lesson_review | 1 |

查看最后的关联查询，应有 L001、L002 两行，均关联 T001 张老师。L001 分数为 88.50，L002 因未审核而分数为 NULL。T002 同名教师存在，但本例没有授课。

SHOW CREATE TABLE 用于核对数据库实际保存的主键、唯一键、外键和索引，不仅看 SQL 文件。

样例使用 LAST_INSERT_ID() 捕获当前会话刚插入的自增 ID，不假定 ID 一定从 1 开始。数据文件需要在同一连接执行。

03_inspect.sql 中的 JOIN/LEFT JOIN 是观察关系的预览，Day27 再详细学习。

## 11. 约束练习

仅在自己的练习库中单独尝试并观察错误：

1. 插入第二个 teacher_no=T001 的教师：应被唯一约束拒绝。
2. 插入同名但编号不同的教师：允许，姓名不是唯一键。
3. 使用确认不存在的 teacher_id 新增课节：应被外键拒绝。
4. 给 L001 再插入一份审核：应被课节审核唯一约束拒绝。
5. 删除已被课节引用的 T001：应被 RESTRICT 拒绝。
6. 对比 NULL 备注与空字符串备注，两者是否表达相同业务含义。

这些是待执行的数据库实验，文档没有声称它们已在本机运行通过。

## 12. 与目标项目的联系及验收

aic-teaching-service 包含教师、课节、审核与数据批次等更复杂关系。本课是独立简化模型，不复制业务表，也不代表原项目采用相同外键或唯一规则。

- [ ] 能解释一对多和一对零或一的业务含义。
- [ ] 能指出三张表的主键、业务唯一键与关联列。
- [ ] 能说明姓名为什么不适合作为唯一身份。
- [ ] 能解释建表、插入顺序，以及删除限制。
- [ ] 能区分类型、NOT NULL、唯一约束与范围校验。
- [ ] 执行后用 SHOW CREATE TABLE 和行数查询记录实际结果。

下一课：SQL 增删改查与条件，理解 NULL 和空字符串。
