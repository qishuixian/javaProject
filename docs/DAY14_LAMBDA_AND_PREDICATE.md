# Day 14：Lambda、函数式接口与 Predicate

## 1. 今日目标与入口

假设教师列表有三人，我们想筛选“姓张的老师”，后来又想筛选“ID 大于等于 10002 的老师”。遍历过程相同，只有判断条件不同。今天让方法接收一个筛选条件，从而复用遍历代码。

运行 [Day14Lambda.java](../java-basics/src/Day14Lambda.java)：在 IDEA 点击 main 左侧绿色三角，无需输入。复用 [Teacher.java](../java-basics/src/Teacher.java)，不修改前几课。

今天不用 Stream，继续使用你熟悉的 for 循环。先看懂筛选条件如何传递，再学习简写。

## 2. Predicate 是什么

Predicate 是 Java 8 标准库中的接口，位于 java.util.function 包。它最重要的方法可以概括为：

```java
boolean test(T value);
```

意思是：接收一个 T 类型的数据，判断是否符合条件，返回 true 或 false。

```java
Predicate<Teacher> condition;
```

拆开读：Predicate 是接口，Teacher 是泛型参数，condition 是变量名。这与 Day7 的 TeacherRepository 接口、Day12 的泛型是同样的基础。

`Predicate<Teacher>` 的核心方法相当于 `boolean test(Teacher value)`。这只是理解上的类型替换，不需要你重新定义 Predicate。

## 3. 先看不使用 Lambda 的完整写法

```java
Predicate<Teacher> condition = new Predicate<Teacher>() {
    @Override
    public boolean test(Teacher teacher) {
        return teacher.getName().startsWith("张");
    }
};
```

这是匿名内部类：在创建对象的位置直接提供接口实现，不单独起实现类名。这里并不是直接创建一个没有实现的接口，后面的大括号提供了 test 的实现。

逐段理解：

| 写法 | 含义 |
|---|---|
| Predicate<Teacher> condition | 用接口类型保存筛选规则对象 |
| new Predicate<Teacher>() { ... } | 临时定义实现接口的匿名类并创建实例 |
| @Override | 检查方法是否正确实现接口约定 |
| test(Teacher teacher) | 每次接收一位教师 |
| startsWith("张") | 判断姓名是否以“张”开头 |
| return ... | 返回这个教师是否符合条件 |

使用方式：

```java
Teacher teacher = new Teacher(10001L, "张老师");
boolean matched = condition.test(teacher); // true
```

condition 是规则，matched 是执行一次规则之后得到的结果，不要把两者混淆。

## 4. Lambda 是相同规则的简洁表达

```java
Predicate<Teacher> condition = teacher -> teacher.getName().startsWith("张");
```

把这行读成：**接收一个 teacher，返回它的姓名是否以“张”开头。**

```text
teacher       ->       teacher.getName().startsWith("张")
输入参数      箭头      判断表达式，也就是返回结果
```

左边的 teacher 只是形参名称，可以改成 t，但右边要同步改。编译器从 Predicate<Teacher> 推断出参数类型是 Teacher、返回值必须是 boolean。

以下三种写法在本例中表达相同规则：

```java
Predicate<Teacher> a = (Teacher teacher) -> teacher.getName().startsWith("张");
Predicate<Teacher> b = teacher -> teacher.getName().startsWith("张");
Predicate<Teacher> c = teacher -> {
    return teacher.getName().startsWith("张");
};
```

单个推断类型的参数可省略括号；表达式形式直接产生结果；有大括号的形式要显式 return boolean 结果。不是看到任何代码都能改成 Lambda，它需要匹配一个函数式接口目标类型。

## 5. 什么是函数式接口

只有一个抽象方法需要实现的接口，可以作为函数式接口使用；这里的“一个”不把 Object 的公共方法计入。接口还可以包含 default 或 static 方法。

Predicate 的唯一核心抽象方法是 test，所以编译器知道 Lambda 表达的是 test 的实现，不需要再写方法名。

自己定义时可以使用 `@FunctionalInterface` 让编译器检查：

```java
@FunctionalInterface
interface TeacherCondition {
    boolean matches(Teacher teacher);
}
```

注解并非使用 Lambda 的必要条件，但能检查你是否不小心添加了第二个抽象方法。这段是语法示例，本课运行代码使用标准库 Predicate。

Lambda 和匿名内部类在本例功能等价，但不能认为所有语义都完全相同。例如 Lambda 中的 this 沿用外层上下文，而匿名类中的 this 指向匿名类实例。今天不用依赖 this 完成筛选。

## 6. 把规则传给筛选方法

```java
public static List<Teacher> filterTeachers(
        List<Teacher> teachers, Predicate<Teacher> condition)
```

第一个参数是数据，第二个参数是判断规则。返回值是筛选后的新列表。

```java
for (Teacher teacher : teachers) {
    if (condition.test(teacher)) {
        result.add(teacher);
    }
}
```

每次遍历将当前教师交给规则，true 才保留。调用时可以先创建规则变量，也可以直接传入：

```java
filterTeachers(teachers, teacher -> teacher.getId() >= 10002L);
```

创建 Lambda 时不会遍历教师。真正执行规则是在 filterTeachers 内调用 test 的时候。这里是普通同步执行，不会因为写了箭头就自动创建线程。

## 7. 组合条件

```java
Predicate<Teacher> startsWithZhang = teacher -> teacher.getName().startsWith("张");
Predicate<Teacher> idMatches = teacher -> teacher.getId() >= 10002L;
Predicate<Teacher> both = startsWithZhang.and(idMatches);
```

| 写法 | 意思 |
|---|---|
| a.and(b) | 同时满足 a 和 b；a 为 false 时不执行 b |
| a.or(b) | 满足 a 或 b；a 为 true 时不执行 b |
| a.negate() | 将 a 的结果取反 |

组合方法返回新的规则，不会立刻返回筛选列表，也不会修改原来的规则变量。

## 8. 使用外部变量的限制

```java
long minimumId = 10002L;
Predicate<Teacher> rule = teacher -> teacher.getId() >= minimumId;
```

Lambda 使用外部局部变量 minimumId，这叫捕获。该变量必须为 final 或 effectively final，也就是初始化后没有被重新赋值。

在同一作用域内再写 `minimumId = 10003L;` 会导致捕获代码无法编译。限制针对变量重新赋值，不意味着引用对象的内容不可变；不要在筛选条件中随意修改 Teacher 姓名或外部列表，避免难以理解的副作用。

## 9. 边界与预期输出

本课拒绝 null 列表、null 规则和 null 教师元素。空列表或无匹配结果返回空列表。新结果列表保存原 Teacher 引用，不是深拷贝；筛选过程本身不删除或修改原数据。

```text
匿名内部类筛选姓张：2 人
  10001 / 张老师
  10003 / 张小明
Lambda 筛选姓张：2 人
  10001 / 张老师
  10003 / 张小明
姓张且 ID 达标：1 人
  10003 / 张小明
不姓张：1 人
  10002 / 李老师
没有匹配：0 人
空列表：0 人
原列表人数：3
```

本例按列表遍历顺序保留结果。规则内部抛出的异常会向上传播，不能偷偷把规则执行失败当作“不匹配”。

## 10. 与目标项目的联系

阅读 aic-teaching-service 中集合处理或 Stream 的 filter 时，会遇到类似 `item -> ...` 的写法。先找出箭头左边是什么类型，右边返回什么，再理解该 API 何时执行这个规则。本课是本地内存筛选，不会自动生成数据库 WHERE，也不能替代服务端权限范围查询。

## 11. 今日练习

1. 使用匿名内部类和 Lambda 各写一个“ID 等于 10002”的规则，对比结果。
2. 使用 or 组合“姓张”或“ID 等于 10002”，应得到三位教师。
3. 将姓名条件改成 contains("老师")，预测和验证结果。
4. 给外部局部变量 minimumId 重新赋值，观察编译错误，然后恢复。
5. 用 null 条件调用筛选方法，捕获 IllegalArgumentException，确认不是静默返回空结果。

## 12. 今日验收

- [ ] 能解释 Predicate<Teacher> 的输入和输出。
- [ ] 能逐段读懂匿名内部类和 Lambda 两种写法。
- [ ] 能解释 `teacher -> ...` 中 teacher 只是参数名。
- [ ] 能说明创建规则与执行 test 的区别。
- [ ] 能组合 and/or/negate 条件。
- [ ] 能解释捕获局部变量的限制，以及 Lambda 不会自动异步执行。

下一课：Stream，组合 filter、map 和 groupingBy 处理教师数据。
