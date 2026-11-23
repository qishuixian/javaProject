# Day 3：运算符与条件分支

## 今日目标

根据审核分数返回教学审核状态，掌握算术、比较、逻辑运算符，以及 `if/else` 和 `switch` 的基本用法。

## 1. 常用运算符

```java
int total = 10 + 2;
boolean passed = total >= 10;
boolean valid = score >= 0 && score <= 100;
```

- 算术：`+`、`-`、`*`、`/`、`%`
- 比较：`>`、`>=`、`<`、`<=`、`==`、`!=`
- 逻辑：`&&`（并且）、`||`（或者）、`!`（取反）
- `=` 是赋值，`==` 才是比较；字符串内容比较使用 `equals`。

整数相除会保留整数结果，例如 `5 / 2` 是 `2`。需要小数时至少一个操作数使用 `double`。

## 2. if/else

```java
if (score < 0 || score > 100) {
    return "INVALID";
} else if (score >= 60) {
    return "PASSED";
} else {
    return "PENDING";
}
```

条件判断顺序会影响结果。先排除非法值，再处理正常区间，避免非法分数被误判为未通过。

## 3. 与目标项目的对应关系

`aic-teaching-service` 中的枚举、请求校验和配置校验都依赖条件判断。例如配置校验会先检查场景，再限制 DIFY/PYTHON 提供方组合。今天先用简单字符串返回值，后续 Day8 再改成枚举。

## 4. 运行示例

文件：[Day03ReviewScore.java](../java-basics/src/Day03ReviewScore.java)

```powershell
cd E:\my\javaProjects
.\java-basics\run-day03.ps1
```

示例会验证 `-1`、`0`、`59`、`60`、`100`、`101` 六个边界值。

## 今日练习

1. 把通过分数从 60 改为 80。
2. 增加“优秀”状态：分数大于等于 90。
3. 增加一个 `enabled` 条件：未启用时返回 `DISABLED`。
4. 用 `switch` 根据 `"PASSED"`、`"PENDING"`、`"INVALID"` 打印中文说明。

## 今日验收

- [ ] 能区分 `=` 和 `==`。
- [ ] 能使用 `&&` 和 `||` 组合条件。
- [ ] 能处理边界值和非法值。
- [ ] 能解释整数除法结果。
- [ ] 能编译运行 Day3 示例。

