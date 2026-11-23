# Java 入门到高级学习计划：以 aic-teaching-service 为实战参照

制定日期：2026-11-17。学习工作目录：`E:/my/javaProjects`。

本计划参考 MyApplication 的“分阶段路线、每日一个核心主题、可运行 Demo、中文笔记、源码对照、验证步骤”组织方式，把练习主线替换为 Java 教学管理后端。目标是从独立编写 Java 程序，逐步达到读懂、调试、测试并维护 aic-teaching-service 的能力。

这是一份待执行的学习计划，不代表练习工程、每日教程或测试已经完成。建议主线 100 个学习日，现代 Java 拓展 10 个学习日；每天 1.5～2 小时，每周 5 天，约 22 周。复杂实验可以拆成两天，阶段验收优先于赶进度。“高级”以能解释原理、复现故障并给出证据为标准，需要后续真实项目持续练习。

## 1. 参考依据与版本边界

### 1.1 从 MyApplication 借鉴什么

- [Java 入门课](E:/my/MyApplication/docs/DAY1_JAVA_BASICS.md)：目标、知识解释、代码示例的组织方式。
- [Day9～Day79 路线](E:/my/MyApplication/docs/DAY9_TO_DAY79_ROADMAP.md)：在已有能力上逐步增加功能。
- [Day31～Day55 计划](E:/my/MyApplication/docs/DAY31_TO_DAY55_LEARNING_PLAN.md)：真实源码调用链、独立 Demo 和验证相结合。

本文以 Java 后端为主，Android 的 Repository、接口回调等只作理解辅助；不安排 Android UI 或前端基础课程。已掌握的 Java 基础可以先做验收题，通过后缩短时间。

### 1.2 目标项目的当前依据

参照项目：[aic-teaching-service](E:/code/java/aic-teaching-service)。核对了根 POM、服务模块 POM、Controller/Service、权限与同步代码、Mapper 和测试目录。README 开头仍有“仅基础脚手架”的早期说明，不能据此判断当前实现范围。

| 方面 | 已看到的源码或配置 | 学习侧重点 |
|---|---|---|
| Java 与构建 | 根 POM 声明 Java 1.8、Maven、多模块、公司私有父 POM | JDK/JRE/JVM、依赖解析、模块边界 |
| Web 框架 | Spring Boot 2.3.10.RELEASE；部分 Starter 另有版本声明 | MVC、依赖注入、配置、代理；最终版本需看 effective POM/依赖树 |
| 模块 | api 模块已有 DTO/枚举；service 模块已有大量业务 | DTO、实体、接口和实现的职责 |
| 数据访问 | MyBatis-Plus 3.3.1、Mapper XML、PageHelper、动态数据源依赖 | SQL、分页、事务、映射、多数据源 |
| 管理业务 | 教师管理、班级/学员、个人备课、人工审核、导入导出 | 从简单查询走向完整业务闭环 |
| 权限 | TeacherAccessService、TeacherRelationManager | 身份、数据范围、拒绝策略、一致的查询口径 |
| 同步任务 | SyncCoordinator、BatchPublisher、PublishedBatchReader、Job Handler | 并发任务、批次、发布快照、故障恢复 |
| 外部集成 | DifyAnalysisManager、OaOrganizationFeignClient、回放回调 Controller | HTTP、超时、重试、状态机、幂等 |
| 运维与测试 | Surefire、JaCoCo、测试配置、Actuator、日志、Docker/CI 文件 | 可复现测试、诊断、发布与回滚 |

Redis/Redisson 有依赖和配置及业务引用；Caffeine、AMQP 等也有依赖声明，但依赖存在不等于所有能力已在业务链路启用。缓存、MQ 课程中新增的案例按学习实验处理，阅读时继续核对实际调用点。

**版本策略：**Day1～Day100 的兼容主线使用 Java 8 语法与 API，便于对照目标项目。Java 17/21、records、虚拟线程、Spring Boot 3 放到独立拓展工程；不把这些写法直接移入 Java 8 项目。MySQL Connector 的版本不等于数据库服务端版本，应核对本地数据库及目标环境，再选择 SQL 语法。

## 2. 学习方法与练习工程

每天按以下节奏推进：20 分钟理解概念，20 分钟阅读指定源码，40～60 分钟实现练习，20 分钟验证和记录。初学阶段先写小程序，遇到企业级大类只阅读当天相关方法。

练习主线命名为 **teaching-lab**，从教师与课节管理逐步成长为“小型教学质检后台”：

1. 控制台维护教师、课节和审核状态。
2. 用集合及文件保存数据，再替换为 MySQL。
3. 提供教师查询、课节查询和审核 HTTP 接口。
4. 增加权限、导入导出、事务、缓存和异步任务。
5. 模拟外部分析回调、批次发布与查询，再做故障诊断。

建议后续按下列结构逐步创建，本次仅交付计划：

```text
E:/my/javaProjects/
  JAVA_LEARNING_PLAN.md
  java-basics/           # Java 小程序与基础测试
  teaching-lab/          # Java 8 学习项目
    teaching-lab-api/    # DTO、响应模型、公共枚举
    teaching-lab-service/
  modern-java-lab/       # JDK 17/21 独立实验
  docs/                 # DAY01_...、阶段总结、故障记录
```

不要一开始复制整个公司工程及其私有父 POM。基础练习使用公共依赖和合成数据；外部组织、Dify、回放等先用本地假服务替代。学习项目不默认连接真实业务数据库或启动真实同步任务。

每日笔记模板：

```markdown
# DayXX：主题
## 今天要解决的问题
## 核心概念与最小 Java 示例
## 目标项目源码：文件、方法、输入、输出、调用关系
## 今天的 Demo 与运行命令
## 验证：正常输入、边界输入、失败场景、实际结果
## 易错点与排查记录
## 自测：不用看答案解释三个问题
```

## 3. 总体路线与里程碑

| 阶段 | 学习日 | 能力目标 | 阶段产物 |
|---|---|---|---|
| 一 | 1～10 | Java 语法与对象建模 | 控制台教学管理器 |
| 二 | 11～20 | 集合、泛型、Java 8、IO | 可保存、筛选的数据程序 |
| 三 | 21～30 | Maven、测试、SQL、JDBC | 有测试的数据库应用 |
| 四 | 31～40 | Spring、Boot、MVC、分层 | 内存版教学 HTTP 服务 |
| 五 | 41～50 | MyBatis、分页、事务、索引 | 数据库版服务 |
| 六 | 51～60 | 权限、业务口径、导入导出 | 可验收的管理业务闭环 |
| 七 | 61～70 | 并发、线程池、缓存、锁 | 可复现并发实验 |
| 八 | 71～80 | 外部调用、任务、幂等、快照 | 模拟分析和同步系统 |
| 九 | 81～90 | JVM、性能、日志、发布 | 故障复盘与优化报告 |
| 十 | 91～100 | 原理、设计、综合交付 | 完整毕业项目 |
| 拓展 | 101～110 | 现代 Java 与迁移思路 | 独立现代版本实验 |

## 4. 每日学习计划

### 阶段一：Java 语言与面向对象（Day1～Day10）

源码对照：先看 api 模块中的 TeacherListRequest、TeacherListItemResponse、TeachingAccountRoleEnum；暂不追踪完整 Service。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day1 | JDK、编译与运行 | 用 javac/java 输出教师信息；解释源文件、class、main 的关系 |
| Day2 | 类型与变量 | 建模 id、姓名、课节数、状态；说明 int/Integer、long、null 的区别 |
| Day3 | 运算与分支 | 根据审核分数返回枚举状态；验证临界值和非法值 |
| Day4 | 循环与数组 | 遍历课节数组、统计已审核数量；空数组结果为 0 |
| Day5 | 方法与参数 | 提取校验及统计方法；解释 Java 总是值传递，包括对象引用的传递 |
| Day6 | 类、构造器、封装 | 编写 Teacher/Lesson；通过构造器和方法维护合法状态 |
| Day7 | 接口、继承、多态 | 写 TeacherRepository 接口与内存实现；调用方只依赖接口 |
| Day8 | static、final、枚举 | 定义审核状态与常量；解释 final 引用不代表对象内容不可变 |
| Day9 | String、equals、hashCode | 对比字符串和值对象；验证相同 id 对象的集合去重行为 |
| Day10 | 入门综合练习 | 完成控制台新增、查询、更新；至少演示正常、重复 id、空姓名三类输入 |

阶段验收：从空白文件写出类和接口，不依靠 Lombok；能解释一个 DTO 的字段、构造方法和访问修饰符。

### 阶段二：常用类库与 Java 8（Day11～Day20）

源码对照：TeacherManagementServiceImpl 的集合处理、SyncCoordinator 中的 EnumMap/Atomic 类型先认名称；时间处理结合 TeachingTimeTest 阅读。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day11 | List/Set/Map | 用 Map 按 id 存教师，用 Set 去重；解释查找与遍历复杂度 |
| Day12 | 泛型与通配符 | 编写 PageResult<T>；解释类型安全、擦除和 extends/super 的使用方向 |
| Day13 | 异常机制 | 区分非法输入与存储失败；保留 cause，验证 finally 与资源释放 |
| Day14 | Lambda、函数式接口 | 用 Predicate 筛选教师；与匿名内部类写法对照 |
| Day15 | Stream | 实现 filter/map/groupingBy；与循环输出一致，处理重复 key |
| Day16 | null 与 Optional | 查询不存在的教师；明确返回空集合、Optional、抛异常各自用途 |
| Day17 | 日期与数值 | 用 java.time 处理日期区间，用 BigDecimal 计算比例；验证跨日与舍入 |
| Day18 | 文件 IO 与编码 | UTF-8 保存并加载教师信息；验证中文、空文件、路径不存在 |
| Day19 | JSON 序列化 | 对象与 JSON 互转；区分字段缺失、null、空字符串和未知枚举 |
| Day20 | 类库综合练习 | 完成文件版筛选、排序、分页；重启后数据恢复，边界页行为明确 |

阶段验收：不把 Stream 当作必须使用的语法；能选择更容易理解的循环，说明集合顺序、重复值及空值策略。

### 阶段三：工程基础、测试与数据库（Day21～Day30）

源码对照：根 POM、两个子模块 POM、TeacherProfileMapper.xml；原项目测试只作为样例，先在独立项目形成测试习惯。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day21 | Maven 生命周期 | 建立 Maven 基础工程，执行 test/package；说明 compile/test/package 的关系 |
| Day22 | 依赖与多模块 | 拆分 api/service；查看 dependency:tree，解释版本管理和传递依赖 |
| Day23 | 单元测试 | 用 JUnit 测统计、分页、校验；断言结果而非只看没有异常 |
| Day24 | 调试与日志 | 断点追踪一次查询；从完整异常栈找到最内层 cause |
| Day25 | 关系建模与 DDL | 设计教师、课节、审核三张练习表，定义主键、唯一键及外键关系策略 |
| Day26 | SQL 查询基础 | 完成增删改查与参数条件；解释 NULL 与空字符串的差异 |
| Day27 | JOIN 与聚合 | 统计每位教师课节数；验证无课节教师、重复关联行不会误计数 |
| Day28 | JDBC | 用 PreparedStatement 查询；用 try-with-resources 关闭资源 |
| Day29 | 数据库事务基础 | 两次写入中第二次故意失败；rollback 后第一条不残留 |
| Day30 | 持久化综合练习 | 将内存 Repository 替换为 JDBC；测试数据可初始化、可清理 |

阶段验收：能解释唯一约束与程序判重的不同作用；至少包含查询成功、无数据、约束冲突和事务回滚测试。

### 阶段四：Spring Boot 与 HTTP 服务（Day31～Day40）

源码对照：Application、TeacherManagementController、TeacherManagementService、GlobalExceptionHandler、TeachingWebConfiguration。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day31 | HTTP 与接口契约 | 设计教师查询请求/响应；区分状态码、业务码、分页字段 |
| Day32 | IoC 与依赖注入 | 把 Repository/Service 注册为 Bean，用构造器注入；解释为何可替换实现 |
| Day33 | Boot 启动和配置 | 启动内存版服务；切换 local/test 配置并验证优先级 |
| Day34 | MVC 参数绑定 | 实现 GET/POST；验证路径参数、查询参数、JSON body |
| Day35 | DTO/Entity 分层 | 不直接返回数据库实体；控制输出字段并完成对象转换 |
| Day36 | 参数校验 | 用 Bean Validation 校验名称、页大小；非法请求返回清晰错误 |
| Day37 | 统一异常处理 | 分别返回校验错误、业务错误、未知异常；未知异常保留服务端堆栈 |
| Day38 | AOP 与代理 | 写一个耗时切面；通过同类调用与跨 Bean 调用对照代理边界 |
| Day39 | Web 测试 | 用 MockMvc 验证状态码、JSON、非法输入；了解 Mock 不代表真实数据库正确 |
| Day40 | HTTP 综合练习 | 教师新增、查询、更新形成闭环；用 curl 或接口工具保存复现请求 |

阶段验收：画出请求经过 Controller → Service → Repository 的顺序；能解释注解由谁处理，而非仅记住注解名字。

### 阶段五：MyBatis、事务与查询性能（Day41～Day50）

源码对照：TeacherProfileMapper、TeacherProfileMapper.xml、TeacherManagementServiceImpl、PageQueryExecutor、DataSourceConstants。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day41 | MyBatis 映射 | JDBC 查询替换为 Mapper/XML；验证列名、属性名及返回类型 |
| Day42 | MyBatis-Plus | 对照 BaseMapper 与自定义 SQL；明确复杂查询不强行套通用 CRUD |
| Day43 | 动态 SQL | 添加多个可选筛选；值使用参数绑定，排序字段按白名单映射 |
| Day44 | 分页与总数 | 分清当前页条数、筛选后总数、库内总数；固定排序含唯一兜底键 |
| Day45 | Spring 事务 | 故意抛异常验证回滚；测试同类调用、异常被捕获等失效场景 |
| Day46 | 隔离级别与 MVCC | 两个数据库连接复现读现象；说明一致性读与锁定读区别 |
| Day47 | 索引与 EXPLAIN | 对筛选排序建立候选索引，记录扫描行数与执行计划变化 |
| Day48 | 批量与 N+1 | 将逐条补充教师信息改为批量查询；结果相同且 SQL 次数下降 |
| Day49 | 多数据源与连接池 | 两个本地库演示 @DS；验证代理、事务边界及不存在的数据源 |
| Day50 | 数据访问验收 | 为组合筛选、分页、回滚建立数据库集成测试，保留执行记录 |

阶段验收：不能用 H2 通过代替 MySQL 特有 SQL 的正确性；普通本地事务不自动覆盖跨库或外部 HTTP 操作。优化必须先保证结果集合与顺序不变。

### 阶段六：业务权限与可维护接口（Day51～Day60）

源码对照：TeacherAccessService、DataScopeProvider、TeacherRelationManager、TeacherImportServiceImpl、TeacherExportServiceImpl，以及 ConversationDatabaseTotalTest。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day51 | 身份认证与授权 | 在学习服务建立可信测试身份上下文；解释“是谁”和“能看谁” |
| Day52 | 数据范围 | 实现老师/管理员两类范围；禁止请求参数直接授予管理员权限 |
| Day53 | 查询权限一致性 | 列表、详情、候选、导出使用同一范围；逐一验证越权访问 |
| Day54 | 权限失败处理 | 模拟权限提供方异常、空范围、禁用账号；不能异常后放开数据 |
| Day55 | 业务计数口径 | 设计 databaseTotal/total；筛选改变 total，完整授权范围仍受约束 |
| Day56 | Excel 导入 | 读取小型合成表格，验证重复教师、空列、非法格式；生成逐行错误 |
| Day57 | 大文件导出 | 分批写出数据；验证中文文件名、行数、权限和输出流关闭 |
| Day58 | 审核状态与历史 | 保存审核动作和修改记录；定义合法状态迁移及重复提交规则 |
| Day59 | 接口契约与安全输入 | 验证未知字段策略、超大分页、非法排序、越权 id；避免直接拼 SQL |
| Day60 | 业务综合验收 | 用角色×接口×成功/失败场景矩阵验收教学管理后台 |

阶段验收：至少覆盖正常授权、无权限、跨业务线、禁用账号、权限依赖失败五类情形，并说明接口计数单位是报告、教师还是消息。

### 阶段七：Java 并发与缓存（Day61～Day70）

源码对照：SyncCoordinator、SyncExecutionConfiguration、TeachingRedissonConfiguration；缓存案例是学习扩展，不能推断原项目所有查询使用缓存。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day61 | 线程与共享状态 | 多线程递增复现丢失更新；说明竞态条件 |
| Day62 | JMM、volatile | 用停止标记理解可见性；解释 volatile 不保证复合操作原子性 |
| Day63 | synchronized、Lock、CAS | 对比三种计数保护方式；测试异常路径仍能释放锁 |
| Day64 | 线程池 | 设置核心线程、上限、队列和拒绝策略；用饱和实验记录结果 |
| Day65 | Future/CompletableFuture | 并行请求两个假服务；处理超时、部分失败和资源关闭 |
| Day66 | ThreadLocal 与上下文 | 线程池复用下复现身份/MDC 污染，再用 finally 清理修复 |
| Day67 | 本地缓存与 Redis | 给低敏字典查询加缓存；验证 key 维度、TTL、更新后失效 |
| Day68 | 缓存常见故障 | 模拟空值穿透、热点失效、批量过期；观察后端请求次数 |
| Day69 | 分布式锁与幂等 | 两实例抢同一任务；测试锁过期、非持有者释放及数据库唯一约束兜底 |
| Day70 | 并发综合实验 | 并发提交同一审核请求；验证结果唯一、无身份串用、线程池可关闭 |

阶段验收：能区分 JVM 内锁和跨进程锁；不把“加锁成功”当作业务绝对只执行一次，必须考虑超时、崩溃和持久化状态。

### 阶段八：分布式集成、任务与批次（Day71～Day80）

源码对照：OaOrganizationFeignClient、DifyAnalysisManager、ReplayEvaluationCallbackController、DataSyncJobHandler、SyncCoordinator、BatchPublisher、PublishedBatchReader。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day71 | HTTP/Feign 客户端 | 调用本地组织服务；验证正常、非 2xx、空响应和错误 JSON |
| Day72 | 超时与重试 | 设置连接/读取超时、重试上限和退避；只对可安全重试操作重试 |
| Day73 | 分析任务状态机 | 模拟待处理、执行中、成功、失败；拒绝非法状态跳转 |
| Day74 | 回调与幂等 | 假服务发送重复/乱序回调；校验来源、任务关联，最终状态不倒退 |
| Day75 | XXL-JOB 与定时任务 | 先直接调用任务入口，再接本地调度；重复触发不重复发布 |
| Day76 | 批次与快照 | 构建写入批次和已发布指针；写入未完成时查询仍读取旧版本 |
| Day77 | 发布与失败恢复 | 模拟写到一半失败；记录失败批次，成功后再原子切换可见版本 |
| Day78 | 策略模式 | 为两种数据集注册不同同步策略；新增策略不扩张大型 if/else |
| Day79 | MQ 基础扩展 | 本地实验重复消费、确认和失败重试；说明至少一次投递与业务幂等 |
| Day80 | 集成综合验收 | 跑通提交分析→假服务回调→结果保存→发布查询，并注入超时/重复事件 |

阶段验收：明确 MQ 是拓展实验；能解释数据库事务与外部调用不能天然原子提交，提出本地消息表/补偿方案并分析代价，而不是宣称获得“绝对一次”。

### 阶段九：JVM、性能与工程交付（Day81～Day90）

源码对照：ConversationSqlTimingInterceptor、日志配置、Actuator 配置、Dockerfile、entrypoint.sh、CI 文件；原项目性能测试数据不作为学习环境基准。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day81 | JVM 内存与类加载 | 画出堆、栈、元空间；解释对象分配、类初始化及类路径问题 |
| Day82 | GC 与内存诊断 | 限制学习程序堆大小并制造持续引用；分析 GC 日志和堆转储 |
| Day83 | 线程诊断 | 在小程序复现死锁，用 jstack 定位等待关系；给出修复 |
| Day84 | 链路与阶段耗时 | 为请求记录 requestId 和阶段耗时；异步链路正确传递与清理上下文 |
| Day85 | SQL 性能排查 | 拆分 COUNT、分页、明细补充耗时；不能把单 SQL 耗时当接口总耗时 |
| Day86 | 压测与容量 | 记录数据量、并发、p50/p95、错误率和连接池；建立可重复基线 |
| Day87 | 性能优化实验 | 只改一个变量，重复测量；确认结果一致，再比较延迟和资源占用 |
| Day88 | 健康检查与配置 | 区分存活、就绪；模拟数据库不可用，检查返回及日志 |
| Day89 | 打包、容器与 CI | 为学习工程编写测试→构建→镜像流程；配置外置并验证优雅停止 |
| Day90 | 发布与回滚演练 | 本地部署两个版本，验证旧接口兼容、数据变更策略及回滚步骤 |

阶段验收：提交一份有复现步骤、原始指标、根因、修复和复测的报告；不靠“感觉更快”判断优化。JVM 命令和 GC 参数按实际 JDK 版本选择。

### 阶段十：原理深化与毕业项目（Day91～Day100）

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day91 | 反射与注解 | 定义一个校验注解，用反射读取；解释运行时保留策略及反射代价 |
| Day92 | 动态代理与 Spring 原理 | 用 JDK Proxy 包装接口调用；追踪事务/AOP 拦截的进入位置 |
| Day93 | 集合内部原理 | 阅读 HashMap/ConcurrentHashMap 关键实现；解释哈希、扩容、线程安全边界 |
| Day94 | 设计与重构 | 将重复校验提取为明确职责；用回归测试证明行为不变，避免过度抽象 |
| Day95 | 毕业项目设计 | 固定需求、接口、表结构、状态机、权限矩阵和验收数据 |
| Day96 | 查询业务交付 | 完成教师/课节分页、筛选、排序和详情；验证空结果及越权 |
| Day97 | 写入业务交付 | 完成审核、历史、导入和事务；验证重复提交与失败回滚 |
| Day98 | 异步业务交付 | 完成分析任务、假回调和快照发布；验证超时、乱序、恢复 |
| Day99 | 测试与诊断 | 跑完整回归和定量压测，修复阻断问题；记录仍未验证的场景 |
| Day100 | 演示与复盘 | 从空数据库启动演示，提交操作说明、测试证据、性能报告和改进清单 |

阶段验收：能在目标项目定位一个需求影响的 Controller、DTO、Service、Mapper、SQL 和测试；说明改动风险及验证策略。学习计划本身不要求直接修改原业务项目。

### 现代 Java 拓展（Day101～Day110）

本阶段在 modern-java-lab 中进行，先确认依赖兼容再选择 Boot 3 的具体版本。Java 17 是 Spring Boot 3 的最低 Java 基线，具体版本的支持范围另查对应文档。

| 天数 | 核心主题 | 当天练习与验收 |
|---|---|---|
| Day101 | JDK 多版本 | 对比 Java 8/17/21 的编译运行，解释字节码目标版本与 API 兼容 |
| Day102 | 现代语言语法 | 对照 var、文本块、switch 表达式；记录各自引入版本 |
| Day103 | record 与 sealed | 重写纯响应模型和状态类型；说明 record 的浅不可变边界 |
| Day104 | Java 21 模式匹配 | 简化状态分支；验证 null 与覆盖范围 |
| Day105 | Java 21 虚拟线程 | 对 IO 等待任务比较平台线程和虚拟线程；不将其等同 CPU 加速 |
| Day106 | 现代并发边界 | 测试取消、背压、外部连接池容量；虚拟线程不意味着无限下游并发 |
| Day107 | Boot 3 迁移 | 在小服务迁移 javax→jakarta，检查验证、Servlet、测试依赖 |
| Day108 | 依赖兼容分析 | 列出私有库、旧 Spring Cloud、Swagger、插件等迁移阻碍 |
| Day109 | 双版本契约测试 | Java 8 版与现代版输出同一组业务样例，比较接口差异 |
| Day110 | 迁移提案 | 输出收益、风险、依赖清单、验证范围和回退方案；不直接升级原仓库 |

## 5. 原项目源码导航与阅读顺序

以下入口均基于制定计划时的本地文件。阅读时先用 IDE 跳转方法、查找调用方，再按主题扩展；不要求逐行读完整个大类。

| 顺序 | 源码入口 | 要回答的问题 |
|---|---|---|
| 1 | [根 POM](E:/code/java/aic-teaching-service/pom.xml) | Java 版本、父 POM、模块和版本管理分别在哪里？ |
| 2 | [TeacherListRequest](E:/code/java/aic-teaching-service/aic-teaching-service-api/src/main/java/cn/vipthink/aic/teaching/api/dto/TeacherListRequest.java) | 请求哪些字段可为空？继承了什么公共分页规则？ |
| 3 | [TeacherManagementController](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/controller/TeacherManagementController.java) | 路由、校验、身份来源和响应封装分别在哪一层？ |
| 4 | [TeacherManagementServiceImpl](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/service/impl/TeacherManagementServiceImpl.java) | listTeachers 怎样构造条件、分页、批量补充动态字段？ |
| 5 | [TeacherProfileMapper.xml](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/resources/mapper/TeacherProfileMapper.xml) | SQL 条件、排序、计数怎样与 Java 参数对应？ |
| 6 | [TeacherAccessService](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/security/TeacherAccessService.java) | provider 缺失、返回 null 或空集合的语义是否相同？ |
| 7 | [TeacherRelationManager](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/manager/TeacherRelationManager.java) | 组织关系、账号角色与数据查询范围如何关联？ |
| 8 | [SyncCoordinator](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/sync/SyncCoordinator.java) | 任务如何分发、收集结果、传播失败和触发发布？ |
| 9 | [BatchPublisher](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/sync/BatchPublisher.java) | 数据什么时候变为读请求可见？发布失败如何处理？ |
| 10 | [ConversationSqlTimingInterceptor](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/main/java/cn/vipthink/aic/teaching/config/ConversationSqlTimingInterceptor.java) | SQL 执行耗时覆盖哪些阶段？与请求耗时如何对应？ |
| 11 | [测试目录](E:/code/java/aic-teaching-service/aic-teaching-service-service/src/test/java/cn/vipthink/aic/teaching) | 哪些是纯单测、SQL 验证、集成测试？各自能证明什么？ |

第一条完整阅读链建议从 `POST /teaching-web/management/teachers/query` 开始：

```text
TeacherListRequest
  → TeacherManagementController.listTeachers
  → TeacherManagementServiceImpl.listTeachers
  → 查询条件转换 / 分页路径选择
  → TeacherProfileMapper + XML
  → 动态字段批量补充
  → TeacherListItemResponse / ReportQueryResult
  → Result.SUCCESS
```

先逐层记下输入输出，不急着理解所有分支。随后再单独追踪组织权限链，最后进入同步和分析任务。不要假设所有 Controller 都经过同一个权限类，应按实际调用逐个验证。

## 6. 环境准备与验证命令

### 6.1 开始前的检查

安装或确认 IDE、JDK 8、Maven、Git；数据库在 Day25 前准备，Redis 在 Day67 前准备，MQ 在 Day79 前按需准备。可以使用本地容器，但不把 Docker 作为入门第一天的前置要求。

```powershell
java -version
javac -version
mvn -version
```

重点核对 Maven 显示的 Java 路径与 IDE 使用的 JDK 是否一致。目标项目存在私有父 POM 和公司公共包；无法解析时核对有权限的 Maven 配置、仓库和网络，学习主线继续用独立工程。不要把凭据写入教程或提交到代码库。

### 6.2 学习工程的日常检查

以下命令是后续创建 Maven 工程后的示例，本次没有执行构建：

```powershell
Set-Location E:\my\javaProjects\teaching-lab
mvn test
mvn package
mvn dependency:tree
```

运行学习服务前，应明确端口、local 配置、数据库名、测试身份及外部假服务地址，关闭真实调度和外部分析调用。每次记录命令、退出码、测试数量和失败原因。

### 6.3 原项目的测试注意事项

原项目根 POM 明确设置 `skipTests=false` 和 `maven.test.skip=false`；服务模块使用 Surefire 2.22.2，配置 test profile、测试配置文件及关闭 Apollo bootstrap。运行前仍须读测试配置，确认需要的数据库或其他基础设施。

依赖和测试基础设施就绪后，可在原项目根目录尝试：

```powershell
Set-Location E:\code\java\aic-teaching-service
mvn -pl aic-teaching-service-service -am test
```

这不是本次已验证成功的命令。若失败，区分依赖解析、编译、无匹配测试、测试断言及基础设施故障；不能通过跳过测试声称验证完成。源码目录中存在测试文件也不代表当前全部通过。

## 7. 毕业项目验收清单

用合成数据准备两个业务线、两类角色、至少三位教师及多个课节；各业务线有同形数据用于检验隔离。性能数据另行生成，报告具体数量，不以固定吞吐量作为所有机器的统一及格线。

- [ ] 从干净数据库和文档说明可以完成初始化、启动及基本请求。
- [ ] 查询支持分页、筛选、稳定排序；空结果、边界页和非法页大小有定义。
- [ ] 列表、详情、候选、导出权限一致；异常和无权限不会退化为查全部。
- [ ] 审核写入及历史记录保持事务一致；重复请求不产生重复业务结果。
- [ ] 导入错误能定位到行；导出行数与授权范围一致。
- [ ] 假分析服务覆盖正常、超时、重复回调、乱序回调和非法任务。
- [ ] 同步中途失败不会发布半成品；请求使用明确的已发布批次。
- [ ] 单元测试、Web 测试和数据库集成测试职责清楚，记录实际执行结果。
- [ ] 至少完成一次 SQL 性能优化和一次 JVM/线程故障定位，有前后证据。
- [ ] README 包含启动、测试、接口样例、环境依赖、已知限制和回滚办法。

## 8. 时间不足时的优先级与后续深化

最先完成 Day1～Day50，形成 Java→SQL→HTTP→事务的闭环。若需尽快读业务代码，再优先完成 Day51～Day55 的权限与计数口径，以及 Day71～Day77 的外部调用和批次概念；导入导出、缓存和 JVM 深化随后补齐，不能把压缩路线当作已掌握高级能力。

遇到三个典型卡点时这样处理：

- **看不懂 Service：**退回类、接口、集合和泛型；只追一个方法，不整文件硬读。
- **启动不了公司项目：**先定位私有依赖与环境问题；用纯 Java 或独立 Boot Demo 继续当天知识点。
- **会写 CRUD 但不会排错：**增加失败注入、越权、回滚、并发和慢查询实验，而非继续堆接口数量。

完成主线后再围绕实际职责深化：Java 集合与并发源码、Spring 事务源码、MySQL 锁和索引、分布式一致性、应用安全及容量规划。每个方向至少产出一个可复现问题和一份有证据的解决记录。

## 9. 官方资料索引

按当天问题查对应章节，优先选择与项目匹配的历史版本；下列为资料入口，不代表本次已逐页核验其内容。

- [Oracle Java Tutorials（JDK 8 体系）](https://docs.oracle.com/javase/tutorial/)
- [Java 8 API](https://docs.oracle.com/javase/8/docs/api/)
- [Maven Guides](https://maven.apache.org/guides/)
- [Spring Boot 2.3.10 参考文档](https://docs.spring.io/spring-boot/docs/2.3.10.RELEASE/reference/html/)
- [MyBatis 3 中文文档](https://mybatis.org/mybatis-3/zh_CN/)
- [MySQL 官方文档](https://dev.mysql.com/doc/)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Java 21 文档](https://docs.oracle.com/en/java/javase/21/)

建议读法：带着“这段代码为什么这样写”的问题查文档，然后回到 Demo 验证，再回到原项目解释实现与边界。
