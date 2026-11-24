# Day 16：Optional 与 null

## 今日目标

Day7 的 `findById` 查不到时返回 null，调用方必须自己记得判空。今天用 `Optional<Teacher>` 表示“可能有教师，也可能没有”，让查询结果的可选性出现在方法返回类型中。

入口：[Day16Optional.java](../java-basics/src/Day16Optional.java)。依赖 [Teacher.java](../java-basics/src/Teacher.java) 和 [TeacherNotFoundException.java](../java-basics/src/TeacherNotFoundException.java)，在 IDEA 点击 main 左侧绿色三角运行。

## 1. null 的问题

```java
Teacher teacher = repository.findById(99999L);
System.out.println(teacher.getName()); // 运行时 NullPointerException
```

返回值类型仍然是 Teacher，调用方不容易从签名看出它可能为 null。传统写法需要手动判空：

```java
if (teacher != null) {
    System.out.println(teacher.getName());
} else {
    System.out.println("未找到");
}
```

Optional 不是把 null 变成绝对安全，也不是所有字段都要包装成 Optional；它主要用于表达某个返回结果可能不存在。

## 2. `Optional<T>` 是什么

```java
Optional<Teacher> found = findTeacher(teachers, 10001L);
```

Optional 是一个容器：里面可能有一个 Teacher，也可能为空。它不是 Teacher 本身，不能直接写 `found.getName()`；要先取出或转换里面的值。

```java
public static Optional<Teacher> findTeacher(List<Teacher> teachers, long id)
```

方法签名直接告诉调用方：查询结果可能不存在。`Optional` 的泛型参数是里面的类型，类似 Day12 的 `PageResult<T>`。

## 3. 创建 Optional

| 方法 | 适用情况 |
|---|---|
| `Optional.of(value)` | value 明确不能为 null；传 null 会立刻抛 NullPointerException |
| `Optional.ofNullable(value)` | value 可能为 null；null 会变成空 Optional |
| `Optional.empty()` | 明确表示没有值 |

查询结果通常使用 `ofNullable`，因为数据库或集合查找可能返回 null：

```java
Optional<Teacher> result = Optional.ofNullable(repository.findById(id));
```

本课直接用 Stream 的 `findFirst()`，它找不到时返回 Optional.empty()。

## 4. `isPresent` 与 `get`

```java
if (found.isPresent()) {
    System.out.println(found.get().getName());
}
```

`isPresent()` 判断容器是否有值，`get()` 取出值。空 Optional 调 get 会抛 `NoSuchElementException`，所以不要不判断就调用 get。

更推荐用 map、ifPresent 或 orElse 表达后续动作，避免到处出现“判断再 get”。本课保留这一段是为了理解基础。

## 5. `orElse` 与 `orElseGet`

```java
String name = missing.map(Teacher::getName).orElse("未找到");
```

有值就取姓名，空时使用默认文本。`orElse` 的参数表达式会先计算，即使 Optional 有值也可能执行参数构造。

```java
String name = missing.map(Teacher::getName)
        .orElseGet(() -> "按需生成的默认值");
```

`orElseGet` 接收一个 Supplier，只有为空时才执行。默认值计算昂贵或有副作用时更适合它。简单常量用 orElse 更直观。

## 6. `map`：转换 Optional 内部的值

```java
String upperName = found.map(Teacher::getName)
        .map(String::toUpperCase)
        .orElse("UNKNOWN");
```

类型变化过程：

```text
Optional<Teacher>
  → map getName
Optional<String>
  → map toUpperCase
Optional<String>
  → orElse
String
```

如果任一步没有值，后面的 map 不执行，最后使用默认值。普通 map 函数返回 null 时，Optional.map 会把结果处理为空；不要借此掩盖不该返回 null 的业务错误。

## 7. `ifPresent` 与 `orElseThrow`

```java
found.ifPresent(teacher -> System.out.println(teacher.getId()));
```

有值才执行 Lambda，空时什么也不做，适合可选的附加动作。不要在需要明确“没找到”错误的场景静默跳过。

```java
Teacher teacher = missing.orElseThrow(() -> new TeacherNotFoundException(99999L));
```

有值就返回，无值就创建并抛出业务异常。Supplier 是延迟执行的，只有空时才创建异常对象。这个写法适合“查不到就是业务失败”的 Service 方法。

## 8. Optional 与 null 的边界

```java
Optional<String> value = Optional.ofNullable(null);
```

value 是空 Optional，但变量 value 本身不为 null。通常不要返回 null 的 Optional：返回 `Optional.empty()`，否则调用方还要同时防 Optional 自身为 null。

Optional 不建议作为实体字段、序列化 DTO 字段或方法参数的统一替代品，目标项目是否使用它要看现有接口约定。集合结果通常返回空集合而不是 Optional<List<T>>，避免两层“没有”的语义。

## 9. 运行预期

```text
查询成功：张老师
不存在的教师是否有值：false
orElse 默认姓名：未找到
orElseGet 默认姓名：按需生成的默认值
map 后姓名：张老师
ifPresent：10001
orElseThrow：教师不存在：99999
ofNullable(null) 是否有值：false
Objects.equals(null, null)：true
```

## 10. 与目标项目的联系

阅读 aic-teaching-service 的查询 Service 时，先确认现有契约是返回 null、空集合、业务异常还是响应中的空字段。学习项目用 Optional 练习表达“可能没有”，不应直接把真实接口改成 Optional 而破坏 JSON、调用方或公共 API 约定。

## 11. 今日练习

1. 将 findTeacher 改成查询姓名，返回 `Optional<Teacher>`。
2. 用 `filter(teacher -> teacher.getId() > 10001L)` 接在 Optional 后面。
3. 比较 orElse 与 orElseGet：让默认值方法打印日志，观察有值时哪个会执行。
4. 将 missing.get() 放进 try/catch，观察空 Optional 的异常类型。
5. 选择一个 Day10 的查询分支，说明它更适合返回空提示还是抛出 TeacherNotFoundException。

## 12. 今日验收

- [ ] 能说出 Optional 表示“可能有一个值，也可能没有”。
- [ ] 能区分 of、ofNullable 和 empty。
- [ ] 能安全使用 map、ifPresent、orElse 和 orElseThrow。
- [ ] 能解释为什么不应无条件调用 get。
- [ ] 能说明 Optional 不等于所有字段都必须包装。
- [ ] 能先查看原有接口契约，再决定是否使用 Optional。

下一课：日期与数值，使用 java.time 和 BigDecimal 处理时间及精确金额/比例。
