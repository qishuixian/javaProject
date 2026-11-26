# Day38：AOP 与代理

今天只解决一个问题：**为什么方法上写了注解，有时生效，有时不生效？**

我们给教师查询方法添加耗时日志，再对照四种调用方式。不连接数据库，沿用 `spring-boot-lab`。

## 1. 先理解 AOP 想解决什么

假设很多 Service 方法都要记录耗时。如果每个方法都手写“开始计时、执行业务、结束计时”，代码会重复。AOP（面向切面编程）可以把这类公共逻辑集中起来，在指定方法执行前后运行。

可以类比前端给函数包一层计时包装函数。但 Spring AOP 通常通过代理对象完成，不是把注解自动翻译成方法体中的代码。

```text
调用方 → Spring 代理 → 计时开始 → 真正的方法 → 记录耗时 → 返回结果
```

代理可以理解为站在真实对象前面的入口，调用经过它时，才有机会执行额外逻辑。

## 2. 启动步骤

1. IDEA 重新加载 [spring-boot-lab/pom.xml](../spring-boot-lab/pom.xml)，本课增加 `spring-boot-starter-aop`。
2. 仍运行 [Day33Application.main()](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)，模块 **spring-boot-lab**，JRE **JDK 8**。
3. Program arguments 设置 `--spring.profiles.active=local --server.port=8081`。
4. 停止旧进程，重新启动，打开 IDEA 控制台观察日志。

Boot 的 AOP 自动配置会启用本课所需代理，不需要再手动添加 EnableAspectJAutoProxy。只新增依赖或修改注解后未重新同步、启动，可能仍在运行旧代码。

## 3. 四个接口，返回相同教师名

逐个打开，每次观察是否增加 `DAY38_TIMING` 日志：

| 地址 | 调用方式 | 每次新增计时日志 |
| --- | --- | --- |
| <http://127.0.0.1:8081/api/day38/direct> | Controller 调用注入 Service 的 findName | 1 条 |
| <http://127.0.0.1:8081/api/day38/self> | Service 内部 this.findName | 0 条 |
| <http://127.0.0.1:8081/api/day38/cross> | 另一个 Service 调用注入的目标 Service | 1 条 |
| <http://127.0.0.1:8081/api/day38/manual> | 手动 new Service 后调用 | 0 条 |

direct 示例响应：

```json
{"mode":"direct","teacherName":"张老师","injectedServiceIsProxy":true}
```

`injectedServiceIsProxy` 描述的是 Controller 注入的那个对象；manual 实际使用另一个手动创建的对象，所以这个字段在 manual 响应中仍是 true，不表示手动对象也有代理。

计时日志类似：

```text
DAY38_TIMING method=TimedTeacherService.findName() success=true elapsedMs=0.123
```

数值每次不同，可能非常小，不要把示例数字作为验收标准。这里测的是方法调用耗时，不是整个 HTTP 请求耗时，也不是性能基准测试。

## 4. 自定义注解只是一个标记

[Timed.java](../spring-boot-lab/src/main/java/learning/boot/day38/Timed.java)：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed { }
```

- `@interface`：声明一个注解类型，不是普通 interface。
- `Target(METHOD)`：允许写在方法上。
- `Retention(RUNTIME)`：运行时仍能读取这个注解。
- 空的大括号：这个注解没有自定义参数。

写上 `@Timed` 本身不会计时，真正执行计时代码的是下面的切面。

## 5. 切面逐行读

[TimingAspect.java](../spring-boot-lab/src/main/java/learning/boot/day38/TimingAspect.java)：

```java
@Aspect
@Component
public class TimingAspect { ... }
```

`@Aspect` 声明切面；`@Component` 注册为 Spring Bean。这里只有 Aspect 而没有注册到容器，不足以让本例生效。

```java
@Around("@annotation(learning.boot.day38.Timed)")
public Object measure(ProceedingJoinPoint point) throws Throwable
```

- `Around`：环绕通知，可以在目标方法前后执行代码。
- `@annotation(...)`：切点表达式，选择带 Timed 注解的方法。
- `point`：本次方法调用的信息，以及继续调用目标方法的入口。
- `Object`：接收并返回目标方法结果，本课实际为 String。
- `throws Throwable`：允许把目标调用抛出的异常继续传出去。

核心逻辑：

```java
long start = System.nanoTime();
boolean success = false;
try {
    Object result = point.proceed();
    success = true;
    return result;
} finally {
    double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
    // 记录方法名、success 和 elapsedMs
}
```

`proceed()` 执行后续拦截链及目标方法。忘记调用会导致业务方法不执行；重复调用可能使业务执行两次。`return result` 保留原返回值。

`finally` 在正常返回或抛异常时都会执行，所以失败也会记录耗时。代码不 catch 后吞掉异常，不影响上层的异常处理。success 只代表本次方法调用正常返回，不等于业务一定成功。

`nanoTime` 用于测量经过时间；除以一百万换算为毫秒。它不是日期时间戳。

## 6. 为什么 self 没有日志

[TimedTeacherService.java](../spring-boot-lab/src/main/java/learning/boot/day38/TimedTeacherService.java)：

```java
@Timed
public String findName() { return "张老师"; }

public String selfCall() {
    return this.findName();
}
```

self 接口先经过代理调用 selfCall，但 selfCall 没有 Timed 标记，因此不匹配计时切面。随后真实对象内部的 `this.findName()` 不再经过代理，所以 findName 的计时也不会触发。

```text
Controller → 代理 → 真实对象.selfCall() → this.findName()
                                            ↑ 没有再次进入代理
```

不是方法没执行：返回的教师名证明它执行了，只是额外计时逻辑没有执行。

如果把 Timed 加到 selfCall 上，外层调用就会计时一次，但内部 findName 仍不会单独计时。

## 7. 为什么 cross 可以

[TeacherCallerService.java](../spring-boot-lab/src/main/java/learning/boot/day38/TeacherCallerService.java) 注入 TimedTeacherService，调用的是代理：

```text
Controller → TeacherCallerService → TimedTeacherService 的代理 → findName
```

需要独立拦截的职责可以拆到另一个 Bean，由外部调用它。这是基于职责的拆分，不是要求所有内部方法都拆成 Bean。

手动 `new TimedTeacherService()` 则绕过了 Spring 容器；即使同一个类上有 Service、方法上有 Timed，也不会自动获得 Spring 代理。

## 8. 常见边界

- Spring AOP 是基于代理的方案，不等于 AspectJ 编译期或加载期织入；本课虽然使用 AspectJ 注解语法，仍是 Spring 代理机制。
- 常见代理方式有 JDK 接口代理与 CGLIB 类代理。本课没有 Service 接口，使用类代理。
- 类代理不能覆盖 final 方法，也不能代理 final 类；private 方法不能作为这里的外部代理入口。
- 有注解、对象在容器里，还要确认实际调用路径经过代理且匹配切点。
- 不要把用户请求体、密码等参数直接全部写入计时日志。本例只记录方法名、状态和耗时。

## 9. 对照真实项目

本次检索 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java`，未找到显式的 `@Aspect` 或 `@Around` 标记。不能据此断言整个应用没有 AOP：依赖库与框架功能也可能提供代理。

今天的耗时切面是独立教学实现，不是复制目标项目已有切面。同类调用边界也是后续理解 Spring 代理模式下事务注解的重要基础；但不能只看注解就断言某段事务失效，还要确认实际调用链和配置。

## 10. 今日验收

- [ ] 能解释代理在调用链中的位置。
- [ ] direct/cross 各增加一条日志，self/manual 不增加。
- [ ] 四个接口仍返回张老师，切面没有改变返回值。
- [ ] 能解释 proceed、return result 和 finally 的作用。
- [ ] 给 selfCall 添加 Timed 后，理解为什么只有外层一次计时；实验后恢复。
- [ ] 能说明“方法执行了”和“切面执行了”不是一回事。

## 11. 本次验证记录

2026-11-26：JDK 8 与临时 Maven 3.9.9 执行 `package`，结果 `BUILD SUCCESS`。本模块没有单元测试，日志显示 `No tests to run`。

实际启动 JAR，在随机空闲端口按 direct、self、cross、manual、self、direct 顺序请求，逐次核对返回值及计时日志增量，分别为 1、0、1、0、0、1；同时确认注入对象为代理，日志包含方法名、成功标记和耗时。另一次请求验证 Day37 正常查询，总计 7 次 HTTP 检查通过。验证进程已停止，未实际操作 IDEA。异常时 finally 计时的分支仅做源码说明，本次没有故障注入验证。

下一课 Day39：Web 测试，用 MockMvc 验证状态码、JSON 与非法输入。
