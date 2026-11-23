# Day 8：static、final 与枚举

## 1. 今天先理解三个问题

1. 为什么有些方法不用 new 对象就能调用？——static。
2. 怎样规定变量赋值后不能再改？——final。
3. 怎样把审核状态限制成几个固定选项？——enum。

今天在 Day3 审核分数例子上增加能力，不修改原来的 Day3 文件。接口和继承暂时不增加新内容，先把这三个概念理解清楚。

## 2. 文件与运行

- [ReviewStatus.java](../java-basics/src/ReviewStatus.java)：审核状态枚举。
- [Day08ConstantsAndEnums.java](../java-basics/src/Day08ConstantsAndEnums.java)：运行入口与常量、方法演示。
- [Teacher.java](../java-basics/src/Teacher.java)：复用 Day6 的教师对象。

在 IDEA 打开 Day08ConstantsAndEnums.java，点击 main 左侧绿色三角运行。不要单独运行枚举文件，它没有 main。无需 .ps1 脚本；IDEA 会管理编译输出。

## 3. static：属于类，不属于某个对象

先对比两种方法调用：

```java
// 实例方法：先有一个教师对象，再对这位教师改名。
Teacher teacher = new Teacher(10001L, "张老师");
teacher.rename("李老师");

// 静态方法：直接用类名调用，不用 new Day08ConstantsAndEnums()。
ReviewStatus status = Day08ConstantsAndEnums.reviewStatus(80);
```

reviewStatus 只需要传入的分数，不需要某个对象的字段，因此本课将它定义为 static。

```java
public static ReviewStatus reviewStatus(int score)
```

逐段阅读：public 允许其他类调用；static 表示静态方法；ReviewStatus 是返回类型；reviewStatus 是方法名；int score 是参数。

在同一个类中，`reviewStatus(80)` 可以省略前面的类名。静态方法内没有代表当前实例的 this；如果要访问 Teacher 的姓名，仍需要先取得 Teacher 对象。

static 字段属于类，实例共享这份类状态，而实例字段属于各自对象。**static 本身不表示只读，也不保证线程安全。** 不要因为 main 是 static，就给所有业务字段和方法加 static。

## 4. final：赋值后不能重新赋值

### 基本类型变量

```java
final int passScore = 60;
// passScore = 80; // 编译失败
```

final 变量可以先声明，再在合法的初始化路径上赋值一次；本课直接在声明时赋值，容易阅读。

### 引用类型变量

```java
final Teacher teacher = new Teacher(10001L, "张老师");
teacher.rename("李老师"); // 可以：修改对象内部数据
// teacher = new Teacher(10002L, "王老师"); // 不可以：变量改指向
```

可以把 teacher 理解为“固定指向这位教师的变量”。固定的是引用，不是对象内部的姓名。

数组也是对象，所以：

```java
final int[] minutes = {30, 45};
minutes[0] = 60; // 可以修改元素
// minutes = new int[] {90}; // 不能重新赋值数组引用
```

这延续了 Day5 的“引用与对象内容不同”的概念。final 引用不等于不可变对象。

## 5. static final：定义类常量

```java
public static final int PASS_SCORE = 60;
```

| 部分 | 意思 |
|---|---|
| public | 其他类可以访问 |
| static | 通过类名访问，不需要创建对象 |
| final | 初始化后不能重新赋值 |
| int | 整数类型 |
| PASS_SCORE | 常量名，通常使用大写和下划线 |
| 60 | 初始值 |

使用方式：

```java
if (score >= PASS_SCORE) {
    return ReviewStatus.PASSED;
}
```

比起到处写数字 60，PASS_SCORE 更清楚地表达“通过分数”，也方便统一调整。这里的调整是改源码再编译，不是运行中重新给 final 字段赋值。

不要把会随环境或业务动态变化的配置都写成常量。`static final` 如果修饰可变对象引用，也不意味着对象内容不能变化。

## 6. enum：定义有限的状态选项

Day3 用字符串表示状态：

```java
String status = "PASSED";
String typo = "PASSSED"; // 拼错了，但仍是合法字符串，编译器不会报错。
```

现在新增一个类型：

```java
public enum ReviewStatus {
    INVALID,
    PENDING,
    PASSED
}
```

逐段阅读：public 允许其他类使用；enum 表示枚举；ReviewStatus 是自己定义的类型名；大括号内是预先定义的三个枚举常量，用逗号分隔。

本例没有给枚举添加字段或方法，最后一个常量后可以不写分号。不能使用 `new ReviewStatus()` 创建任意新状态。

```java
ReviewStatus status = ReviewStatus.PASSED;
```

左边的 ReviewStatus 是变量类型，右边的 ReviewStatus.PASSED 是该类型定义好的一个值。这和 `Teacher teacher = ...` 一样，都是“类型 + 变量名 + 初始值”。

```java
// ReviewStatus status = "PASSED";         // 编译失败：字符串不是枚举
// ReviewStatus status = ReviewStatus.PASSSED; // 编译失败：不存在这个常量
```

本课沿用 Day3 语义：INVALID 表示非法分数，PENDING 表示低于通过线，PASSED 表示通过。这是教学示例约定；实际业务中“待审核”和“审核未通过”往往需要分开建模。

枚举也是引用类型，变量仍可能为 null；类型限制了可用状态，不会自动消除所有空值问题。

## 7. 返回枚举与比较枚举

```java
public static ReviewStatus reviewStatus(int score) {
    if (score < MIN_SCORE || score > MAX_SCORE) {
        return ReviewStatus.INVALID;
    }
    if (score >= PASS_SCORE) {
        return ReviewStatus.PASSED;
    }
    return ReviewStatus.PENDING;
}
```

判断顺序与 Day3 一样：先非法区间，再通过区间，剩下的是低分区间。变化是返回类型从 String 变成 ReviewStatus。

```java
ReviewStatus status = reviewStatus(80);
if (status == ReviewStatus.PASSED) {
    System.out.println("审核已通过");
}
```

枚举常量可以直接用 `==` 比较。这里即使 status 是 null，比较结果也是 false；但调用 null 的方法仍会产生空指针异常。不要把这种枚举比较方式套到字符串内容比较上。

## 8. final 与 Day7 的继承有什么关系

final 还可以修饰类和方法：

| 写法 | 含义 |
|---|---|
| final class Example | 这个类不能被 extends |
| public final void execute() | 子类不能重写这个方法 |
| final Teacher teacher | 这个变量不能重新赋值 |

今天先能区分这三种含义即可，不需要修改 Day7。若把 InMemoryTeacherRepository 改为 final，LoggingTeacherRepository 就不能继续继承它。

## 9. 预期输出与边界

```text
通过分数：60
分数 -1：INVALID
分数 0：PENDING
分数 59：PENDING
分数 60：PASSED
分数 100：PASSED
分数 101：INVALID
审核已通过
final 引用的对象改名后：李老师
final 数组的首个元素：60
```

枚举打印时默认显示常量名。六个分数覆盖非法下界、合法最小值、通过线两侧、合法最大值和非法上界。

## 10. 与目标项目的联系

此前阅读 aic-teaching-service 时，api 模块已有 TeachingAccountRoleEnum 等枚举，数据源名称则使用 DataSourceConstants 等常量。阅读这些代码时分别关注“有哪些合法状态”和“哪些名称是固定的”。本课 ReviewStatus 是独立练习，不代表目标项目实际审核状态协议。

真实接口或数据库经常保存状态码，需要显式设计状态码与枚举之间的转换。不要把枚举的声明顺序编号当作稳定业务协议；枚举更复杂的字段、构造方法和转换逻辑后续再学习。

## 11. 今日练习

1. 将 PASS_SCORE 改成 80，补充 79 和 80 的输入，重新运行。原来的 60 应变为 PENDING。
2. 增加 EXCELLENT 枚举常量，让 90～100 返回它。注意必须先排除非法分数，再判断优秀，再判断通过。
3. 每次只取消一条非法赋值语句的注释，观察 final 的编译错误，然后恢复。
4. 声明 `ReviewStatus status = null`，比较它与 PASSED，解释为什么结果为 false。
5. 不看代码，解释 `public static final int PASS_SCORE = 60` 中每个部分的作用。

## 12. 今日验收

- [ ] 能通过类名调用 static 方法和读取常量。
- [ ] 能说明 static 不等于不能修改。
- [ ] 能说明 final 引用不等于对象内容不可变。
- [ ] 能声明枚举变量并从方法返回枚举。
- [ ] 能解释枚举相比随意字符串减少了哪些错误。
- [ ] 能解释 final 类、final 方法与 final 变量的区别。

下一课：String、equals 和 hashCode，理解内容相等与对象身份的区别。
