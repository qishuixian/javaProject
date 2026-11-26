# Day 32：IoC 与依赖注入

## 1. 今天只解决一个问题

TeacherService 要查询教师，必须使用 TeacherRepository。仓库对象由谁创建、怎样交给业务类？

- DI（Dependency Injection，依赖注入）：把依赖对象从外部传进来。
- IoC（Inversion of Control，控制反转）：把对象创建和组装等控制权交给外部容器。
- Bean：由 Spring 容器管理的对象。

先看普通 Java 构造方法，再看 Spring；依赖注入本身不要求 Spring。

## 2. 独立工程与运行步骤

- [spring-ioc-lab/pom.xml](../spring-ioc-lab/pom.xml)
- [Day32Injection.java](../spring-ioc-lab/src/main/java/learning/day32/Day32Injection.java)

在 IDEA 右键 spring-ioc-lab/pom.xml → Add as Maven Project，等待同步，然后运行 Day32Injection.main。

**运行配置模块选 spring-ioc-lab，JRE 选 JDK 8**，不是 javaProjects 或 java-basics。这与 Day28 驱动必须在正确模块 classpath 中是同一个道理。

本课使用 Java 8 兼容的 Spring Framework 5.2.14.RELEASE，服务于旧项目学习；不作为新生产项目版本推荐。没有 Spring Boot、Web 端口或数据库。旧课程不受此独立工程影响。

所有小类放在一个文件中，用 public static 嵌套类简化跳转。static 嵌套类可以独立创建，不需要先 new 外层 Day32Injection；它不意味着所有对象或字段自动共享。真实工程通常拆成独立文件。

## 3. 不注入时的问题

如果业务类内部写死：

```java
private final TeacherRepository repository = new MemoryTeacherRepository();
```

那么换成数据库仓库就要修改业务类，单元测试也不容易传入假仓库。

改成构造器注入：

```java
public TeacherService(TeacherRepository repository) {
    this.repository = repository;
}
```

参数 repository 是外部传来的依赖，this.repository 是当前对象字段。这仍是 Day6 的构造方法语法。

调用方手动组装：

```java
TeacherService service = new TeacherService(new MemoryTeacherRepository());
```

这已经是依赖注入，不需要注解。业务类只调用接口，具体实现由外部决定。

## 4. Spring 怎样接管组装

```java
@Configuration
public static class AppConfig {
    @Bean
    public TeacherRepository teacherRepository() {
        return new MemoryTeacherRepository();
    }

    @Bean
    public TeacherService teacherService(TeacherRepository repository) {
        return new TeacherService(repository);
    }
}
```

逐段解释：

| 写法 | 作用 |
|---|---|
| @Configuration | 告诉 Spring 这是配置类 |
| @Bean | 方法返回的对象交给容器管理 |
| teacherRepository() | 定义仓库 Bean 的创建方式 |
| teacherService(TeacherRepository repository) | Spring 按参数类型找仓库 Bean，提供参数 |
| new TeacherService(repository) | 将容器提供的仓库注入业务对象 |

new 并没有消失，而是集中在组装位置。你不用在每个业务调用处重复创建对象。接口本身不会“自动找到实现”，这里能找到是因为配置显式注册了一个符合类型的 Bean。

## 5. 创建容器、取得 Bean

```java
try (AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(AppConfig.class)) {
    TeacherService service = context.getBean(TeacherService.class);
}
```

AppConfig.class 是配置类的类型信息。容器启动时读取配置、创建 Bean 并完成依赖组装。getBean 取得容器管理的实例。

try-with-resources 退出时调用 close，释放容器管理的资源。这个 main 不会启动 HTTP 服务，所以执行完就退出。

getBean 在本课只是入口演示；真实 Controller/Service 通常继续使用构造器注入，不在每个方法里到处调用 getBean。

## 6. singleton 是什么意思

默认情况下同一容器里的同一 Bean 定义使用 singleton 作用域，重复 getBean 得到同一个实例。因此示例比较结果为 true。

它不代表全 JVM 永远只有一个对象，也不意味着线程安全。自己 new 的对象或另一个容器中的对象仍可能不同。不要把请求相关的可变状态随意存在共享业务 Bean 字段里。

## 7. 为什么有利于测试

```java
TeacherRepository fake = id -> "测试老师";
TeacherService service = new TeacherService(fake);
```

这个接口只有一个抽象方法，可以用 Day14 的 Lambda 提供假实现。TeacherService 不变，结果来自新仓库。此假实现对所有 ID 都返回同一名字，只用于演示替换，不能用它验证真实数据库行为。

构造器中的 null 校验也让“依赖没准备好”的错误提早暴露。

## 8. 常见配置失败

- 没有注册仓库 Bean：创建 TeacherService 时无法满足参数，容器启动失败。
- 注册两个相同接口的候选：单值注入无法确定选谁，通常需要 @Qualifier 或 @Primary，后续再学习。
- 业务类自己 new 了仓库：绕过容器，配置中的替换不会影响它。
- 手动 new 一个标注了注解的普通类：注解不会自动帮这个对象完成装配。
- 运行提示找不到 Spring 类：先检查是否选择 spring-ioc-lab 模块。

本课使用 @Configuration/@Bean 明确注册。@Component/@Service 加组件扫描是另一种注册方式，不是说所有类只要名字叫 Service 就自动成为 Bean。

## 9. 预期输出

```text
手动注入：教师：张老师
Spring 注入：教师：张老师
查询无结果：教师不存在
默认共享同一个 Bean：true
替换仓库：教师：测试老师
Spring 容器已关闭
```

示例包含结果检查，结果不正确会抛异常。它证明容器组装和替换思路，不验证 Web 请求、SQL 或事务。

## 10. 对照目标项目

此前阅读 aic-teaching-service 时，Controller 通过字段与构造方法依赖 Service，Service 又依赖 Mapper。Lombok 的 @RequiredArgsConstructor 可以生成构造器，但核心依然是“外部提供依赖”。

本课没有复制私有父 POM 或业务依赖。Spring Boot 后续会简化容器启动、自动配置等步骤，但不会改变接口和构造器注入的基本思想。

## 11. 今日练习与验收

- [ ] 用自己的话区分 IoC、DI 和 Bean。
- [ ] 能指出依赖在哪里创建、在哪里传入。
- [ ] 能解释 @Configuration 与 @Bean。
- [ ] 修改假仓库返回值，不修改 TeacherService，观察新结果。
- [ ] 暂时去掉仓库方法的 @Bean，观察启动错误，然后恢复。
- [ ] 能解释默认 singleton 不等于线程安全。

下一课：Spring Boot 启动与配置，使用 local/test 环境配置。
