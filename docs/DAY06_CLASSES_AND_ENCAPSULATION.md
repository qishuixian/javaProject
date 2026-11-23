# Day 6：类、构造方法与封装

## 1. 今天解决什么问题

Day2 用独立变量保存教师信息，Day5 用方法处理输入。今天把数据和操作组合成对象：教师管理自己的姓名，课节管理自己的时长，通过构造方法和业务方法保证数据合法。

本课示例规则：教师 ID 必须为正数，姓名和课节名称不能空，课节必须关联教师，时长必须大于 0。这是练习规则，不代表实际业务项目的全部约束。Day5 允许时长为 0，今天对“可开课的课节”使用更严格规则。

## 2. 先运行，再阅读

在 IDEA 中打开 [Day06Objects.java](../java-basics/src/Day06Objects.java)，点击 `main` 左侧绿色三角，选择 Run。

本课有三个源码文件，放在同一个 `src` 目录中：

| 文件 | 职责 |
|---|---|
| [Teacher.java](../java-basics/src/Teacher.java) | 教师数据、构造校验、改名 |
| [Lesson.java](../java-basics/src/Lesson.java) | 课节数据、关联教师、修改时长 |
| [Day06Objects.java](../java-basics/src/Day06Objects.java) | main 入口，演示创建、修改和失败场景 |

Teacher 和 Lesson 不需要 main，它们由 Day06Objects 创建和使用。三个文件均不声明 package，便于当前练习；实际工程通常使用包组织类。

## 3. 类与对象

类定义“有什么数据、可以做什么”，对象是按类创建的具体实例。

```java
Teacher first = new Teacher(10001L, "张老师");
Teacher second = new Teacher(10002L, "李老师");
first.rename("王老师");
System.out.println(second.getName()); // 李老师
```

两次 `new` 创建两个对象，各自有自己的字段。`first` 和 `second` 是保存对象引用的变量，类型都是 Teacher。

## 4. 字段与封装

```java
private final long id;
private String name;
```

- 字段是对象保存的数据，与 Day5 中方法内的局部变量不同。
- `private` 表示外部类不能直接访问该字段。
- `final` 在这里表示 id 初始化后不能再赋值，Day8 会进一步学习。
- `getName()` 提供读取能力；`rename(...)` 提供受控修改能力。

以下写法无法通过编译：

```java
// teacher.name = ""; // name 是 private
```

封装不只是把字段改成 private，再生成所有 setter。它更重要的作用是让修改必须经过规则：外部可以改名，但不能把姓名改成空字符串。

## 5. 构造方法与 this

```java
public Teacher(long id, String name) {
    if (id <= 0) {
        throw new IllegalArgumentException("教师 ID 必须大于 0");
    }
    this.id = id;
    rename(name);
}
```

构造方法与类同名，**没有返回类型，连 void 也不写**。`new Teacher(...)` 时执行构造方法，完成初始化。

`this.id` 是当前对象的字段，右侧 `id` 是构造方法的参数。调用 `rename(name)` 复用姓名校验逻辑。

当类已经声明了这个带参数构造方法，编译器不会再自动提供无参数构造方法，因此 `new Teacher()` 不能通过编译。

## 6. 对象方法与 static 方法

```java
teacher.rename("李老师");
String name = teacher.getName();
```

这些方法没有 static，通过对象调用，可以操作该对象的字段。Day5 的 static 方法属于类，不依赖某个 Teacher 实例。main 是 static，但它可以先 `new` 一个对象，再调用对象方法。

## 7. 先校验，再修改

```java
public void changeMinutes(int minutes) {
    if (minutes <= 0) {
        throw new IllegalArgumentException("课节时长必须大于 0");
    }
    this.minutes = minutes;
}
```

合法输入才会执行赋值。传入 0 或负数时抛出异常，原时长不变。`throw` 表示本次操作失败；示例的 `try/catch` 捕获这种预期失败并打印原因，使后续演示继续执行。今天先理解效果，Day13 再系统学习异常。

构造过程中校验失败，`new` 表达式会抛出异常，不会正常返回这个新对象。

## 8. 对象关联与 Day5 值传递

```java
Teacher teacher = new Teacher(10001L, "张老师");
Lesson lesson = new Lesson("Java 入门", teacher, 45);
teacher.rename("李老师");
System.out.println(lesson.getTeacher().getName()); // 李老师
```

Lesson 保存了同一个 Teacher 对象的引用，没有复制教师对象。通过 teacher 改名后，从 lesson 读到的也是新姓名。

Lesson 中的 `final Teacher teacher` 只限制字段不能改指向，不能阻止 Teacher 对象内部变化。`getTeacher()` 返回实际关联对象，调用方可以通过它调用 rename；本课有意保留这种共享关系，不把它当成不可变快照。

## 9. 预期输出与边界验证

```text
教师：10001 / 张老师
课节：Java 入门 / 45 分钟
修改后教师：李老师
修改后时长：60
拒绝改名：教师姓名不能为空
姓名仍为：李老师
拒绝改时长：课节时长必须大于 0
时长仍为：60
拒绝创建教师：教师 ID 必须大于 0
拒绝创建课节：课节必须关联教师
```

练习时再验证：null 姓名、空课节名称、负数时长、时长为 1。关注是否接受正确输入、拒绝错误输入，以及失败后对象是否保持原值。

## 10. 与目标项目的对应

在 aic-teaching-service 中，DTO、实体和 Service 都是类，但职责不同：DTO 传输数据，实体映射数据，Service 协调业务。此前阅读的 TeacherManagementController 使用 `@RequiredArgsConstructor` 配合 final 字段注入 Service；Lombok 可以生成构造方法，但初学时先手写才能理解它省略了什么。

本课 Teacher/Lesson 是简化练习模型，不直接替换原项目的 DTO 或数据库实体，也不要求所有真实实体都按本例设计构造方法。

## 11. 今日练习

1. 创建第二位教师，修改第一位教师姓名，观察第二位是否变化。
2. 给 Teacher 增加 `private boolean enabled`，默认 true，提供 `disable()` 和 `isEnabled()`，不直接公开字段。
3. 尝试 `new Teacher()`，记录为什么无法编译，再恢复代码。
4. 给 Lesson 增加 `describe()` 方法，返回包含课节名称、教师姓名和时长的字符串。

## 12. 今日验收

- [ ] 能区分类、对象、字段和局部变量。
- [ ] 能手写构造方法并解释 this。
- [ ] 能用 private 和方法维护数据合法性。
- [ ] 能解释为什么不是每个字段都需要 setter。
- [ ] 能验证非法修改不会改变原数据。
- [ ] 能解释为什么教师改名会影响关联课节读到的姓名。

下一课：接口、继承与多态，通过接口替换教师数据的存储实现。
