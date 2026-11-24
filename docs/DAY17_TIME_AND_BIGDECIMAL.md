# Day 17：日期时间与 BigDecimal

## 今日目标

使用 Java 8 的 java.time 处理课程日期和时间，使用 BigDecimal 处理完成率等精确小数计算。

入口：[Day17TimeAndDecimal.java](../java-basics/src/Day17TimeAndDecimal.java)。在 IDEA 点击 main 左侧绿色三角运行，无需脚本。

## 1. 选对时间类型

| 类型 | 表示什么 |
|---|---|
| LocalDate | 只有日期，没有时分秒和时区 |
| LocalTime | 只有时间，没有日期和时区 |
| LocalDateTime | 日期加时间，没有时区 |
| Instant | 时间线上的瞬时点，常用于跨服务事件 |
| Duration | 时分秒层面的时间长度 |
| Period | 按年、月、日表达的日期周期 |

本课使用 LocalDate、LocalTime、LocalDateTime 和 Duration。LocalDateTime 不是跨时区绝对时间，真实接口要确认时区协议。

## 2. 日期计算

```java
LocalDate startDate = LocalDate.of(2026, 11, 24);
LocalDate endDate = startDate.plusDays(6);
```

java.time 对象不可变，plusDays 返回新日期，不修改原对象。日期比较用 isBefore、isAfter、isEqual，不用 ==。`ChronoUnit.DAYS.between(start, end)` 返回日期间隔，本例是 6 天；若业务要求首尾都包含，则需要明确是否加 1。

## 3. 时间、格式化与解析

```java
LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(19, 30));
LocalDateTime end = start.plusMinutes(45);
long minutes = Duration.between(start, end).toMinutes();
```

DateTimeFormatter 用于展示与解析。月份是 MM，分钟是 mm，大小写不能混淆。`LocalDate.parse("2026-12-01")` 使用标准 ISO 日期格式；自定义格式需要传 formatter。

时间区间应明确端点，例如 `[start, end)` 包含开始、不包含结束，可以避免相邻区间重复计算。月份、闰年和时区不能简单当成固定毫秒数。

## 4. BigDecimal 精确小数

```java
BigDecimal result = new BigDecimal("0.1").add(new BigDecimal("0.2"));
```

double 的 0.1 + 0.2 会显示 0.30000000000000004，因为二进制浮点数不能精确表示很多十进制小数。BigDecimal 用字符串构造可保留十进制含义。避免 `new BigDecimal(0.1)`，它会带入 double 的近似值；需要从 double 转换时可考虑 valueOf，但仍应明确数据来源。

## 5. 除法与舍入

```java
BigDecimal rate = new BigDecimal("7")
        .divide(new BigDecimal("9"), 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"));
```

除法可能无限循环，所以指定小数位数和舍入方式。HALF_UP 是常见四舍五入，实际财务业务可能要求其他规则。展示时再用 setScale(2, HALF_UP)，得到 77.78%。

BigDecimal 也是不可变对象，add、divide、setScale 返回新对象。equals 同时比较数值和 scale，因此 1.0 与 1.00 的 equals 为 false；只比较数值使用 compareTo(...) == 0。

## 6. 预期输出

```text
课程开始日期：2026-11-24
课程结束日期：2026-11-30
日期间隔天数：6
课程时间：2026-11-24 19:30 ~ 2026-11-24 20:15
课程时长：45 分钟
完成率：77.78%
double 的 0.1 + 0.2：0.30000000000000004
BigDecimal 的 0.1 + 0.2：0.3
解析后日期加一天：2026-12-02
```

## 7. 与目标项目的联系

aic-teaching-service 的日期筛选、回放窗口、时区输出和数值范围需要明确类型、时区、区间端点与舍入口径。不能只根据展示字符串猜测数据库时间语义，也不能用内存格式化代替 SQL 查询边界。

## 今日练习

1. 将课程开始时间改为 23:30，验证加 45 分钟会进入第二天。
2. 解析日期并计算该月最后一天。
3. 比较 BigDecimal 的 1.0 与 1.00 的 equals 和 compareTo 结果。
4. 将完成数改为 1、总数改为 3，明确计算与展示精度。
5. 日期窗口开始晚于结束时，抛出 IllegalArgumentException。

## 今日验收

- [ ] 能区分常用 java.time 类型。
- [ ] 能使用 plusDays、plusMinutes、between。
- [ ] 能解释格式中的 MM 与 mm。
- [ ] 能解释 double 与 BigDecimal 精度差异。
- [ ] 能为除法指定精度与舍入。
- [ ] 能说明时间端点和时区需要业务约定。

下一课：文件 IO 与编码，读取和保存 UTF-8 教师数据。
