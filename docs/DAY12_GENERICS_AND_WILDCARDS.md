# Day 12：泛型与通配符

## 今日目标

用同一个 `PageResult<T>` 表示教师、课节或字符串的分页结果，并理解 `? extends` 与 `? super` 的安全边界。

入口：[Day12Generics.java](../java-basics/src/Day12Generics.java)。在 IDEA 点击 main 左侧绿色三角运行；不需要脚本。

## 1. 泛型是什么

```java
List<Teacher> teachers = new ArrayList<>();
```

`<Teacher>` 是编译期类型约束：只能添加 Teacher，取出时不需要强制转换。`List<Teacher>` 不能当成 `List<Object>`，否则可以通过 Object 引用塞入字符串，破坏类型安全。

## 2. PageResult<T>

```java
public class PageResult<T> {
    private final List<T> records;
    private final long total;
}
```

`T` 是类型占位符，使用时决定具体类型：`PageResult<Teacher>` 或 `PageResult<String>`。同一个类可以复用。`total` 是符合条件的总数，可能大于当前页 `records.size()`。构造方法复制并包装记录，调用方不能通过返回列表修改分页对象。

## 3. `? extends Teacher`：安全读取

```java
public static long totalTeacherIds(List<? extends Teacher> teachers)
```

它表示某种 Teacher 或子类的列表，具体类型未知，但取出的元素一定可以当作 Teacher。不能安全向其中添加 Teacher，因为实际传入的可能是 `List<SpecialTeacher>`。口诀：**extends 生产者，适合读取。**

## 4. `? super Teacher`：安全写入

```java
public static void addDefaultTeacher(List<? super Teacher> target)
```

实际类型可能是 `List<Teacher>` 或 `List<Object>`，无论哪种都能安全写入 Teacher。读取时只能按 Object 接收，因为具体列表类型未知。口诀：**super 消费者，适合写入。** PECS 就是 Producer Extends、Consumer Super。

## 5. 泛型方法

```java
public static <T> PageResult<T> firstPage(List<T> source, int pageSize)
```

返回类型前的 `<T>` 声明这是泛型方法。传入字符串列表时 T 推断为 String，传入教师列表时 T 推断为 Teacher。

## 6. 预期输出

```text
教师总数：5
教师记录：10001 / 张老师
教师记录：10002 / 李老师
教师 ID 总和：20003
写入后最后一项类型：Teacher
写入后的教师数：1
标题记录数：2
第一个标题：Java
教师页面为空：false
```

## 今日练习

1. 创建 `PageResult<Lesson>` 并打印课节标题。
2. 编写 `copyTeachers(List<? extends Teacher> source, List<? super Teacher> target)`。
3. 尝试向 `List<? extends Teacher>` 写入 Teacher，记录编译器错误。
4. 解释当前页记录数与 total 总数为什么不是同一个值。

## 今日验收

- [ ] 能创建 `PageResult<Teacher>` 和 `PageResult<String>`。
- [ ] 能解释 T 是类型占位符。
- [ ] 能按 PECS 选择 extends 或 super。
- [ ] 能说明 extends 主要安全读取、super 主要安全写入。
- [ ] 能区分当前页记录数量和 total 总数。

下一课：异常机制，区分非法输入、业务失败和系统故障。
