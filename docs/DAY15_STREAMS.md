# Day 15：Stream

## 今日目标

把 Day14 的 Lambda 放进 Stream，完成筛选、转换、排序、分组和统计。先记住：Stream 是一条处理数据的流程，不是一个新的集合，也不会因为写了 Stream 就自动异步。

入口：[Day15Streams.java](../java-basics/src/Day15Streams.java)。在 IDEA 点击 main 左侧绿色三角运行。

## 1. 先对照普通循环

筛选姓张的教师，普通写法是：

```java
List<Teacher> result = new ArrayList<>();
for (Teacher teacher : teachers) {
    if (teacher.getName().startsWith("张")) {
        result.add(teacher);
    }
}
```

Stream 写法：

```java
List<Teacher> result = teachers.stream()
        .filter(teacher -> teacher.getName().startsWith("张"))
        .collect(Collectors.toList());
```

两者完成同一件事。Stream 把“从哪里取数据、筛选规则、最后怎么收集”串成了流程；普通循环更容易逐步调试。不要为了使用 Stream 而强行改写难以理解的代码。

## 2. 每一段是什么意思

```java
teachers.stream()
```

从 List 创建 Stream，数据还没有真正输出。

```java
.filter(teacher -> teacher.getName().startsWith("张"))
```

保留条件返回 true 的教师。filter 是中间操作，可以继续接其他操作。

```java
.collect(Collectors.toList())
```

执行并收集结果，生成新的 List。collect 是终止操作；没有终止操作时，前面的 filter/map 通常不会执行。

可以把它读成：

```text
教师列表 → 变成流程 → 只保留姓张 → 收集成新列表
```

## 3. filter：筛选，不修改原列表

```java
List<Teacher> zhangTeachers = teachers.stream()
        .filter(teacher -> teacher.getName().startsWith("张"))
        .collect(Collectors.toList());
```

filter 的 Lambda 接收一项，返回 boolean。结果列表保存原 Teacher 对象的引用，不会创建教师副本，也不会从原列表删除元素。空结果是空列表，不是 null。

## 4. map：把一种数据变成另一种数据

```java
List<String> names = teachers.stream()
        .map(teacher -> teacher.getName())
        .collect(Collectors.toList());
```

输入是 Teacher，map 后输出是 String，所以结果类型从 `Stream<Teacher>` 变成 `Stream<String>`。map 不是筛选，三位教师会得到三个姓名。

方法引用是 Lambda 的简写：

```java
.map(Teacher::getId)
```

等价于：

```java
.map(teacher -> teacher.getId())
```

看到 `类名::方法名`，先把它翻译成“把每项交给这个方法”。

## 5. sorted：排序

```java
List<Long> sortedIds = teachers.stream()
        .map(Teacher::getId)
        .sorted()
        .collect(Collectors.toList());
```

先 map 成 ID，再按 Long 的自然顺序排序。sorted 默认升序；复杂对象需要 Comparator，后续再学习。排序可能需要暂存数据，不能把它当成数据库 ORDER BY 的替代品，真实查询应确认分页前排序是否在数据库完成。

## 6. groupingBy：分组

```java
Map<String, List<Teacher>> grouped = teachers.stream()
        .collect(Collectors.groupingBy(
                teacher -> teacher.getName().substring(0, 1)));
```

key 是姓名首字，value 是属于该组的教师列表：

```text
张 → [张老师, 张小明]
李 → [李老师]
```

`grouped.get("王")` 可能返回 null，所以示例用 `containsKey` 判断王姓分组是否存在。真实代码中还要处理空姓名，否则 substring 可能抛异常；Teacher 构造方法已经拒绝空姓名，但外部模型不一定有相同保证。

## 7. 统计与执行时机

```java
long idSum = teachers.stream()
        .mapToLong(Teacher::getId)
        .sum();
```

mapToLong 将对象流转换成 LongStream，sum 返回总和。统计为空流时 sum 是 0。

常见终止操作包括 `collect`、`count`、`sum`、`findFirst`、`anyMatch`、`allMatch`。`anyMatch` 和 `findFirst` 等可能短路，不必处理全部元素。

Stream 通常只能消费一次：

```java
// Stream<Teacher> stream = teachers.stream();
// stream.count();
// stream.count(); // 运行时 IllegalStateException
```

需要另一条流程时重新调用 `teachers.stream()`。List 本身仍可重复遍历。

## 8. 不要在 Stream 中制造副作用

不建议在 map/filter 中修改外部 List、教师姓名或共享计数器。Stream 的表达重点是数据转换；副作用会让顺序、异常和并行化更难推理。`forEach` 可以用于最终输出，但不要把所有复杂业务都塞进一个长 Lambda。

本课使用顺序 Stream。不要把 `.parallelStream()` 当作性能开关；线程安全、顺序、连接池和下游服务容量都需要单独验证。

## 9. 预期输出

```text
姓张人数：2
姓名列表：[张老师, 李老师, 张小明]
排序后的 ID：[10001, 10002, 10003]
张姓分组人数：2
王姓分组是否存在：false
ID 总和：30006
原列表人数：3
```

## 10. 与目标项目的联系

aic-teaching-service 中的 Service、Mapper 结果和选项处理可能使用 Stream 做映射、过滤或分组。阅读时先确定 Stream 的输入元素类型，再逐个标出 filter/map 的输出类型，最后找到终止操作。Stream 内存处理不等于 SQL 过滤，也不自动继承数据库分页、权限范围或稳定排序规则。

## 11. 今日练习

1. 用 filter 找出 ID 大于等于 10002 的教师。
2. 用 map 生成姓名长度列表，并计算最长姓名长度。
3. 用 groupingBy 按姓名长度分组，打印长度为 3 的教师数。
4. 用 count 统计教师人数，用 anyMatch 判断是否存在 10003。
5. 把一条 Stream 改写成普通 for 循环，对照输入、输出和空列表行为。

## 12. 今日验收

- [ ] 能解释 stream、filter、map、collect 的顺序。
- [ ] 能区分 filter（筛选）和 map（转换）。
- [ ] 能读懂方法引用 `Teacher::getId`。
- [ ] 能用 groupingBy 得到分组 Map。
- [ ] 能说明 Stream 需要终止操作才会执行。
- [ ] 能说明顺序 Stream 不等于异步或并行执行。

下一课：Optional 与 null，安全表达可能不存在的查询结果。
