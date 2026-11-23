# Day 4：循环与数组

## 今日目标

用数组保存多个课节数据，用 `for` 循环完成遍历、统计和平均值计算。

## 1. 数组

数组保存固定数量的同类型数据：

```java
int[] lessonMinutes = {30, 45, 60};
System.out.println(lessonMinutes.length); // 3
```

下标从 `0` 开始，最后一个下标是 `length - 1`。访问不存在的下标会抛出 `ArrayIndexOutOfBoundsException`。

## 2. for 循环

```java
int total = 0;
for (int minutes : lessonMinutes) {
    total += minutes;
}
```

增强 `for` 适合遍历全部元素；需要下标时使用普通 `for`：

```java
for (int i = 0; i < lessonMinutes.length; i++) {
    System.out.println(i + ":" + lessonMinutes[i]);
}
```

## 3. 与目标项目的对应关系

教学服务中的查询结果通常是 `List`，底层思路和数组遍历相同。今天先掌握固定数组，Day11 再学习 `List`、`Set` 和 `Map`。

## 4. 运行示例

文件：[Day04LessonStats.java](../java-basics/src/Day04LessonStats.java)

```powershell
cd E:\my\javaProjects\java-basics\src
javac -encoding UTF-8 Day04LessonStats.java
java Day04LessonStats
```

## 今日练习

1. 增加一个课节时长 `90`。
2. 增加一个 `boolean[] completed`，统计已完成课节数。
3. 处理空数组，确保平均值不会除以 0。
4. 故意访问 `lessonMinutes[lessonMinutes.length]`，观察异常并记录原因。

## 今日验收

- [ ] 能创建并初始化数组。
- [ ] 能解释数组下标从 0 开始。
- [ ] 能使用普通 `for` 和增强 `for`。
- [ ] 能统计总数、已完成数和平均值。
- [ ] 能解释数组越界异常。

