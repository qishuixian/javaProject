# Day 7：接口、继承与多态

## 1. 今日目标

Day6 学会创建 Teacher 对象。今天增加“根据 ID 查询教师”的能力，并把调用方与存储细节分开。

完成后能解释 `interface`、`implements`、`extends`、`@Override` 和 `super`，并通过接口引用切换对象，观察同一个方法调用产生不同的行为。

## 2. 运行入口与文件关系

在 IDEA 打开 [Day07Interfaces.java](../java-basics/src/Day07Interfaces.java)，点击 main 左侧绿色三角运行。所有源码放在同一个 src 目录，无需脚本。

| 文件 | 职责 |
|---|---|
| [Teacher.java](../java-basics/src/Teacher.java) | 复用 Day6 教师对象 |
| [TeacherRepository.java](../java-basics/src/TeacherRepository.java) | 定义查询契约 |
| [InMemoryTeacherRepository.java](../java-basics/src/InMemoryTeacherRepository.java) | 用数组查询教师 |
| [LoggingTeacherRepository.java](../java-basics/src/LoggingTeacherRepository.java) | 继承内存查询，增加日志 |
| [Day07Interfaces.java](../java-basics/src/Day07Interfaces.java) | 切换实现，观察调用结果 |

两个具体类都使用内存数据，本课不连接数据库；后续 JDBC 课程再实现真正的数据库存储。

## 3. 接口：规定能力和契约

```java
public interface TeacherRepository {
    Teacher findById(long id);
}
```

Repository 可以理解为负责读取或保存业务数据的组件。本课接口只定义查询：输入教师 ID，返回 Teacher，不存在则返回 null。

这里的方法没有方法体，也没有实现查找算法。接口中的这种方法隐含为 `public abstract`。普通接口不能直接 `new TeacherRepository()`；需要创建实现类对象。

不存在时返回 null 是本课的约定，因此调用方必须判空。后续可学习 Optional，但现在不提前引入。

## 4. implements：实现接口

```java
public class InMemoryTeacherRepository implements TeacherRepository {
    @Override
    public Teacher findById(long id) {
        // 遍历数组，返回匹配的教师；不存在返回 null。
    }
}
```

这是结构示意，完整可运行代码见源码。`implements` 表示类实现接口。具体类必须实现尚未实现的抽象方法，方法签名和返回类型需要符合接口要求，访问权限不能缩小。

`@Override` 告诉编译器检查这个方法是否真的实现或重写了已有方法。它不负责执行查询；拼错方法名时，编译器可以更早发现问题。

本课沿用 Day4 数组与循环，不使用集合。空数组合法，查找直接返回 null；数组为 null 或包含 null 元素时，构造方法拒绝创建仓库。若同一 ID 重复出现，当前算法返回第一个匹配项，唯一性校验留作练习。

## 5. extends：继承已有实现

```java
public class LoggingTeacherRepository extends InMemoryTeacherRepository {
    public LoggingTeacherRepository(Teacher[] teachers) {
        super(teachers);
    }

    @Override
    public Teacher findById(long id) {
        System.out.println("查询日志：teacherId=" + id);
        return super.findById(id);
    }
}
```

- `extends` 表示子类继承父类。Java 类只能直接继承一个父类，但可以实现多个接口。
- `super(teachers)` 调用父类构造方法，在 Java 8 中必须位于子类构造方法第一条语句。
- 构造方法不会被继承。父类只有带参数构造方法，子类必须安排合适的调用。
- `super.findById(id)` 调用父类的查询实现。若这里误写成 `findById(id)`，会再次调用自己，形成递归。
- 父类 private 数组不能被子类直接访问；子类通过父类方法复用查询能力。

LoggingTeacherRepository 也符合 TeacherRepository 契约，无需重复写 implements。继承适合具有“是一个”关系的扩展，不应仅为了少写几行就把无关类放入同一继承体系。这里用日志扩展做小型演示，不要求实际项目都用继承添加日志。

## 6. 多态：接口引用指向不同实现对象

```java
TeacherRepository repository = new InMemoryTeacherRepository(teachers);
printTeacher(repository, 10001L);

repository = new LoggingTeacherRepository(teachers);
printTeacher(repository, 10001L);
```

变量声明类型始终是 TeacherRepository，实际对象类型不同。执行 `repository.findById(id)` 时，运行时根据实际对象选择实例方法：第二次会先打印日志，再调用父类查询。

`printTeacher` 只调用接口方法，不需要判断具体类名。切换只发生在创建对象的位置，业务调用代码不变。这就是本课的“调用方依赖接口”。

接口引用只能直接使用接口及其可用的公共能力，不能因为实际对象拥有额外方法，就直接通过接口变量调用那些额外方法。今天无需用强制类型转换绕开这个边界。

注意：这里观察的是实例方法重写。方法重载是同名但参数列表不同，由编译器选择匹配签名；static 方法不参与这里的实例方法动态分派。

## 7. 数组复制与对象共享

构造方法复制了输入数组的元素引用，因此调用方后来执行 `teachers[0] = anotherTeacher` 不会替换仓库中的元素。但 Teacher 对象本身没有复制：对原教师执行 rename，仓库查询到的同一个对象也会体现新姓名。

这是 Day5 值传递和 Day6 对象关联的延续。当前仓库不是不可变快照，也不是数据库；程序退出后不会保留数据。

## 8. 预期结果

```text
普通内存实现
教师：10001 / 张老师
未找到教师：99999
带日志的内存实现
查询日志：teacherId=10002
教师：10002 / 李老师
查询日志：teacherId=99999
未找到教师：99999
空仓库
未找到教师：10001
```

观察重点：相同调用方可处理两个实现；日志版仍遵守不存在时返回 null 的契约；空仓库不会发生数组越界。

## 9. 与目标项目的对应

此前阅读 aic-teaching-service 时，TeacherManagementController 中的字段声明为 TeacherManagementService，而具体业务实现在 TeacherManagementServiceImpl 中。可以用本课思路理解：调用方依赖能力接口，实际对象提供实现。

本课手动 new 对象；Spring 项目由容器创建、注入对象，这部分后续学习。接口本身不会自动寻找实现，也不意味着必须继承公共父类。

## 10. 今日练习

1. 把 main 中第一个仓库也换成 LoggingTeacherRepository，验证 printTeacher 无需修改。
2. 新建 EmptyTeacherRepository，直接 implements TeacherRepository，让查询始终返回 null；将它传入同一个 printTeacher。
3. 给 findById 方法故意改一个名字，观察 @Override 和接口实现的编译错误，再恢复代码。
4. 在构造方法中检测重复教师 ID，明确选择拒绝重复数据，而不是依赖“返回第一个”的偶然结果。
5. 分别修改输入数组的元素引用和原 Teacher 的姓名，验证第 7 节描述的区别。

## 11. 今日验收

- [ ] 能解释接口与实现类的区别。
- [ ] 能区分 implements 和 extends。
- [ ] 能解释 @Override 与 super 的用途。
- [ ] 能解释接口变量的声明类型与实际对象类型。
- [ ] 能保持调用方不变，替换查询实现。
- [ ] 能处理不存在的教师和空仓库。

下一课：static、final 与枚举，用明确类型表达审核状态和固定规则。
