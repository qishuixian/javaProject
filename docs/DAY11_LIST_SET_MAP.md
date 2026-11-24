# Day 11：List、Set、Map

## 1. 今天解决什么问题

Day10 用固定数组保存教师，自己维护 size，并逐个遍历 ID。今天学习集合：让 List 管理可变数量的数据，用 Set 去重，用 Map 按 ID 查询。

在 IDEA 打开 [Day11Collections.java](../java-basics/src/Day11Collections.java)，点击 main 左侧绿色三角即可运行。示例复用 [Teacher.java](../java-basics/src/Teacher.java)，不修改前几课代码，不需要输入或脚本。

## 2. 先记住区别

| 需求 | 类型 | 本课实现类 | 特点 |
|---|---|---|---|
| 依次保存教师列表 | List | ArrayList | 有位置、允许重复 |
| 只保留不重复的 ID | Set | HashSet | 没有下标，不保证遍历顺序 |
| 根据 ID 找到教师 | Map | HashMap | key 唯一，key 对应 value，不保证遍历顺序 |

List/Set/Map 是接口，ArrayList/HashSet/HashMap 是实现类，正好对应 Day7 的写法。Map 不继承 Collection 接口，但也是集合框架的重要组成部分。

## 3. 逐段拆解声明

```java
List<Teacher> teachers = new ArrayList<>();
```

| 部分 | 含义 |
|---|---|
| List | 左边用接口描述能力 |
| <Teacher> | 里面保存 Teacher 对象 |
| teachers | 变量名 |
| new ArrayList<>() | 创建具体实现对象；右边尖括号中的类型由编译器推断 |

尖括号是泛型，今天先理解成“限定元素类型”，Day12 再详细学习。不能往 List<Teacher> 中直接添加字符串。

```java
Set<Long> teacherIds = new HashSet<>();
Map<Long, Teacher> teachersById = new HashMap<>();
```

Set 的一个类型参数描述元素；Map 的两个类型参数分别描述 key 和 value。这里 key 是 Long，value 是 Teacher。泛型不能写基本类型 long，要写包装类型 Long，传入 10001L 时会自动装箱。

## 4. List：按位置管理数据

```java
teachers.add(first);
teachers.add(second);
teachers.add(first);
```

同一个对象可以放入多次，所以 size 是 3。List 保持元素的位置顺序，并不是自动排序。

```java
Teacher teacher = teachers.get(0); // 第一个元素
teachers.remove(2);               // 删除第三个元素
int count = teachers.size();      // 当前元素数量
```

数组用 length，集合用 size()。ArrayList 会在内部容量不足时扩容，不必像 Day10 那样手动维护数组容量，但仍受内存和性能约束。

空 List 的 size 为 0，可以安全遍历，但 get(0) 会越界。遍历方式与数组相似：

```java
for (Teacher teacher : teachers) {
    System.out.println(teacher.getName());
}
```

不要在增强 for 中随意调用集合的 add/remove 来改变结构，可能引发 ConcurrentModificationException；后续学习迭代器安全删除。

### remove 的一个陷阱

```java
List<Integer> numbers = new ArrayList<>();
numbers.add(10);
numbers.add(20);
numbers.remove(1);                  // 按下标删除 20
numbers.remove(Integer.valueOf(10)); // 按元素值删除 10
```

这是方法重载：remove(int index) 与 remove(Object value) 不是同一含义。

## 5. Set：不保留相等的重复元素

```java
Set<Long> teacherIds = new HashSet<>();
teacherIds.add(10001L); // true：新增
teacherIds.add(10001L); // false：已经存在
```

HashSet 使用 Day9 的 hashCode/equals 规则判断重复。Long 已经按数值实现相等比较，因此适合存 ID。

若改成 HashSet<Teacher>，前几课 Teacher 没有重写 equals/hashCode，两次 new 出来的相同 ID 教师仍会被当作两个对象。可以存 Long ID，或使用 Day9 的 TeacherIdentity；不能以为所有对象都自动按 ID 去重。

HashSet 不保证添加顺序，也不支持 get(0)。需要按插入顺序遍历时可以了解 LinkedHashSet，但不是今天的主线。

## 6. Map：通过 key 找 value

```java
Map<Long, Teacher> teachersById = new HashMap<>();
teachersById.put(first.getId(), first);
Teacher teacher = teachersById.get(10001L);
```

可以理解成保存一对数据：`10001 → 张老师这个对象`。这里 10001 是业务 ID，不是数组下标；Map 不需要为了这个数字预留 10002 个数组位置。

| 方法 | 作用 |
|---|---|
| put(key, value) | 保存映射；相同 key 会替换旧 value，返回旧 value |
| get(key) | 返回对应 value，不存在时返回 null |
| containsKey(key) | 判断 key 是否存在 |
| remove(key) | 删除映射，返回旧 value |
| size() | key-value 映射数量 |

HashMap 允许 null value，因此一般情况下 get 返回 null 不足以区分“没有 key”和“有 key 但值为 null”，要用 containsKey。本课只保存非 null Teacher。

**注意类型一致：**Map<Long, Teacher> 查询用 `get(10001L)`，不要写 `get(10001)`。get 接收 Object，后者会装箱成 Integer，虽然能编译，但 Integer 与 Long 不相等，可能导致查不到。

### 相同 key 会替换，不会自动报错

```java
teachersById.put(10001L, new Teacher(10001L, "王老师"));
```

这一操作替换映射，数量不增加。若业务要求“新增遇到重复 ID 必须失败”，需要显式校验，不能直接将 Day10 的新增逻辑替换为 put。

```java
if (teachersById.containsKey(id)) {
    throw new IllegalArgumentException("教师 ID 已存在");
}
teachersById.put(id, teacher);
```

这里还需要保证 key 与 teacher.getId() 一致，并保持原有 ID、姓名校验。这段先判断再写入仅适用于本课单线程场景，不构成并发安全保障；三个实现类本身也都不是线程安全集合。

## 7. 集合保存引用，不会自动复制教师

List 和 Map 最初存入同一个 first 对象，对 first 调用 rename，两边都能看到改名。

但示例随后给 Map 的同一个 key 放入了新 Teacher 对象，此时 Map 指向新对象，List 仍指向旧对象，所以 Map 查询是王老师，List 读取仍是张老师。这与 Day5 的引用概念一致。

## 8. 查找与遍历成本

| 操作 | 本课实现的典型成本 |
|---|---|
| ArrayList 按下标 get | O(1) |
| ArrayList 末尾 add | 均摊 O(1)，扩容的一次可能 O(n) |
| ArrayList 按 ID 搜索或 contains | O(n)，可能逐个比较 |
| ArrayList 中间插入/删除 | O(n)，可能移动后续元素 |
| HashSet add/contains、HashMap put/get | 哈希分布良好时平均 O(1)，不是任何情况下都保证常数时间 |
| ArrayList 遍历 | O(n) |
| HashMap/HashSet 遍历 | 与元素数和底层容量有关，通常可理解为 O(n + 容量) |

n 是元素数量。O(1) 不是“1 毫秒”，O(n) 也不是“n 毫秒”，它描述输入规模增长时工作量如何变化。HashMap 常数、内存开销及哈希冲突也会影响实际表现，不是数据越少就一定比数组快。

## 9. 预期输出

```text
List 元素数：3
List 首位教师：张老师
遍历教师：张老师
遍历教师：李老师
遍历教师：张老师
List 删除后元素数：2
Set 首次添加：true
Set 重复添加：false
Set 去重后数量：2
Set 包含 10001：true
Map 按 ID 查询：李老师
Map 查询不存在的 ID：null
Map 替换前姓名：张老师
Map 替换后姓名：王老师
Map 替换后数量：2
原 List 的首位姓名：张老师
业务校验：拒绝重复教师 ID
Map 删除后数量：1
新建 List 是否为空：true
```

本例没有依赖 HashSet/HashMap 的遍历顺序；若自行遍历，不要以打印顺序判断数据是否正确。

## 10. 与目标项目的联系

此前阅读 aic-teaching-service 时，教师分页查询使用 List 承载当前页记录，权限逻辑使用 Set 表达不重复的教师 ID。本课 Map 按 ID 索引的思路也适合在内存中匹配查询结果，减少反复嵌套遍历；是否替换真实代码仍要看业务规则、数据规模和验证结果。

List 元素个数只是当前列表大小，不能自动当成数据库全部记录数或分页总数。

## 11. 今日练习

1. 向 List 添加两次同一教师，再向 Set 添加两次同一 ID，比较数量。
2. 查询 Map 中不存在的 ID，先判空再调用 getName，避免空指针。
3. 分别使用 get(10001) 和 get(10001L)，解释结果为什么不同。
4. 可选：新增一个独立 Map 版教师目录，保留 Day10 的接口与校验规则，用 containsKey 拒绝重复 ID，不改原数组版。
5. 思考三个需求分别选什么：保留查询结果顺序、判断是否有教师权限、按 ID 取教师对象。

## 12. 今日验收

- [ ] 能读懂 List<Teacher>、Set<Long>、Map<Long, Teacher> 的声明。
- [ ] 能选择 List、Set 或 Map 解决对应问题。
- [ ] 能说明 Set 的去重依据和 Map 的相同 key 替换行为。
- [ ] 能区分数组下标与 Map 的业务 key。
- [ ] 能处理空集合与不存在的 key。
- [ ] 能解释数组线性查询与哈希查询的平均复杂度差异。

下一课：泛型与通配符，编写可复用的 PageResult<T>。
