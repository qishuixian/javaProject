# Day 5：方法、参数与返回值

## 1. 今天要解决的问题

Day4 把统计代码集中写在 main 中。今天将校验、求和、平均值和输出拆成方法，便于复用，同时解释：为什么方法修改整数变量没有效果，修改数组元素却有效果？

源码：[Day05Methods.java](../java-basics/src/Day05Methods.java)。先运行，再逐个阅读方法。无需修改 Day4 代码。

## 2. 方法的结构

```java
public static long totalMinutes(int[] minutes) {
    long total = 0;
    for (int value : minutes) {
        total += value;
    }
    return total;
}
```

| 部分 | 含义 |
|---|---|
| public | 允许其他类访问 |
| static | 属于类，今天可以直接从 static main 调用；对象方法后续学习 |
| long | 返回结果的类型 |
| totalMinutes | 方法名 |
| int[] minutes | 形参：方法内部接收输入的变量 |
| return total | 返回计算结果，并结束本次调用 |

调用示例：

```java
int[] lessons = {30, 45};
long result = totalMinutes(lessons);
System.out.println(result); // 75
```

这里 `lessons` 是实参，`minutes` 是形参。名字不必相同，类型必须兼容。形参和方法内声明的局部变量只在各自作用域内可用。

返回值可以接收、打印或参与其他计算。`System.out.println` 只是在控制台显示内容，不等于将结果返回给调用方。

## 3. void 和提前返回

`printSummary` 只负责输出，返回类型使用 `void`，不能写 `long result = printSummary(...)`。

```java
if (!isValidMinutes(minutes)) {
    System.out.println("时长不合法");
    return;
}
```

这里 `return;` 没有返回值，只结束当前方法；main 后面的语句仍会继续执行。非 void 方法的正常返回路径必须返回与声明兼容的值。

## 4. 如何拆分方法

| 方法 | 输入 | 返回值或效果 |
|---|---|---|
| isValidMinutes | 时长数组 | boolean：是否非 null 且没有负数 |
| totalMinutes | 已校验的数组 | long：总时长；空数组返回 0 |
| averageMinutes | 已校验的数组 | double：平均时长；空数组返回 0.0 |
| printSummary | 任意时长数组，包括 null | 先校验，再输出统计或错误提示 |

`totalMinutes` 和 `averageMinutes` 的前提是输入已校验。单独调用它们时也需要先校验；它们不是可以接受任意输入的公共工具。后续异常课程再学习让方法自行拒绝非法输入。

平均值使用 `(double) totalMinutes(minutes) / minutes.length`，避免整数除法截断。空数组先返回，避免除以零。

## 5. Java 始终是值传递

**调用方法时，复制实参的值给形参。** 基本类型复制数值，引用类型复制引用值，不会自动复制整个对象。

### 基本类型：修改副本

```java
int number = 30;
changeNumber(number);
System.out.println(number); // 30
```

方法内 `number = 99` 改的是形参。调用方的变量仍为 30。

### 数组：两份引用指向同一个对象

```java
int[] minutes = {30, 45};
changeFirst(minutes);
System.out.println(minutes[0]); // 99
```

数组是对象。形参持有复制过来的引用，仍指向原数组，因此 `minutes[0] = 99` 改的是双方共同访问的数组元素。这不表示 Java 是“引用传递”。

### 重新赋值形参：不会替换调用方的引用

```java
replaceArray(minutes);
System.out.println(minutes[0]); // 仍然是 99，不是 999
```

方法内 `minutes = new int[] {999}` 只改变形参指向。调用方的变量仍指向原来的数组。自定义对象的引用参数也遵循相同规则。

## 6. 如何运行

在 IDEA 打开 `Day05Methods.java`，点击 main 方法左侧绿色三角，选择 Run。IDEA 会自动编译，生成的字节码由 IDE 管理；不需要手动建立 `.class` 文件或运行脚本。

预期输出：

```text
课节数：4
总时长：225
平均时长：56.25
课节数：0
总时长：0
平均时长：0.0
时长不合法：数组不能为 null，元素不能为负数
时长不合法：数组不能为 null，元素不能为负数
修改基本类型形参后：30
修改数组元素后：99
给数组形参重新赋值后：99
```

正常数组、空数组、负数数组和 null 是四个独立输入场景。`changeFirst` 仅用于非空数组演示，传入空数组会越界，传入 null 会空指针。

## 7. 与目标项目的联系

参考此前阅读的 `TeacherManagementController.listTeachers(TeacherListRequest request)`：请求对象是输入，返回类型描述接口结果，内部调用 Service 方法获取结果。今天的 `main → printSummary → totalMinutes` 也是方法之间传递输入、获取结果的调用链。

真实项目的 Service 方法通常通过对象调用，不应因为今天示例使用 static 就把业务方法都改成 static。今天只关注方法签名、输入、输出和职责划分。

## 8. 今日练习

1. 编写 `maxMinutes(int[] minutes)`，返回最长时长，约定空数组返回 0；先校验再调用。
2. 将 `{30, 45}` 传给 `averageMinutes`，确认结果是 37.5，而不是 37。
3. 编写 `addOne(int value)`，返回 `value + 1`。比较只调用 `addOne(number)` 与 `number = addOne(number)` 的区别。
4. 在调用 `changeFirst` 和 `replaceArray` 前后分别打印数组第一个元素，解释每一步结果。

## 9. 今日验收

- [ ] 能指出方法名、形参、实参和返回类型。
- [ ] 能解释打印结果与返回结果的区别。
- [ ] 能把 Day4 中的一段统计逻辑提取成方法。
- [ ] 能解释空数组与 null 的差别。
- [ ] 能独立预测值传递示例的三个输出：30、99、99。
- [ ] 能说明修改对象内容与重新赋值形参的区别。

下一课：类、构造器和封装，把教师与课节数据组织成对象。
