# Day 2：Java 类型与变量

## 今日目标

用 Java 表达教师和课节数据，理解基本类型、引用类型、包装类型和 `null` 的区别。

## 1. 变量的基本写法

```java
int lessonCount = 12;
long teacherId = 10001L;
boolean enabled = true;
double completionRate = 0.85;
String teacherName = "张老师";
```

格式是：

```text
类型 变量名 = 初始值;
```

变量名使用小驼峰，例如 `lessonCount`。`long` 字面量建议加 `L`，避免和 `int` 混淆。

## 2. 基本类型与引用类型

Java 常见基本类型：

| 类型 | 示例 | 用途 |
|---|---|---|
| `int` | `12` | 普通整数 |
| `long` | `10001L` | 较大的 id、时间戳 |
| `double` | `0.85` | 一般小数 |
| `boolean` | `true` | 开关状态 |
| `char` | `'A'` | 单个字符 |

`String`、数组和自定义对象属于引用类型。引用变量保存的是对象的引用，可以为 `null`：

```java
String remark = null;
```

调用 `remark.length()` 会产生 `NullPointerException`，所以使用前要明确是否允许为空。

## 3. `int`、`Integer`、`long`、`Long`

`int` 是基本类型，不能表示 `null`；`Integer` 是包装类型，可以表示 `null`。数据库字段、请求参数和 DTO 中经常需要包装类型，因为“没有传值”和“传了 0”可能含义不同。

```java
int pageSize = 20;
Integer optionalScore = null;
long teacherId = 10001L;
Long optionalTeacherId = null;
```

自动装箱和拆箱会转换类型，但拆箱 `null` 会报错：

```java
Integer count = null;
// int value = count; // 运行时 NullPointerException
```

## 4. 与目标项目的对应关系

目标项目中的 DTO、实体和枚举大量使用 `Integer`、`Long`、`String`。阅读 `TeacherListRequest` 时，重点观察分页字段和可选筛选字段；阅读 `TeacherManagementController` 时，注意 `String bizLine` 先校验再交给 Service。

## 5. 运行示例

文件：[Day02TeacherData.java](../java-basics/src/Day02TeacherData.java)

执行：

```powershell
cd E:\my\javaProjects
.\java-basics\run-day02.ps1
```

示例会打印教师 id、姓名、课节数、启用状态，并演示 `Integer null` 与 `int` 的区别。

## 6. 今日练习

1. 把教师姓名改成自己的名字。
2. 增加 `Long lessonId`，并输出它。
3. 将 `optionalScore` 改为 `100`，观察输出变化。
4. 思考：为什么教师 id 通常使用 `Long`，而不是 `int`？
5. 写一个 `String remark = null`，先判断不为空再输出长度。

## 今日验收

- [ ] 能区分基本类型和引用类型。
- [ ] 能解释 `int` 与 `Integer` 的区别。
- [ ] 能说明为什么请求字段常使用包装类型。
- [ ] 能解释 `null` 拆箱为什么会报错。
- [ ] 能编译运行 Day2 示例。

