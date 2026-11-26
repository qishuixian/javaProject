# Day33：Spring Boot 启动与配置

今天的目标：启动一个真正的 HTTP 服务，在浏览器查看教师信息，并通过配置切换运行环境。数据只放在内存，不连接 MySQL。

## 1. 从昨天到今天

Day32 手动创建 Spring 容器；今天由 Spring Boot 启动容器，并根据 Web 依赖启动内嵌 Tomcat。main 运行后会一直等待请求，这是正常现象，停止时点击 IDEA 的红色方块。

可以类比前端开发：启动开发服务后，在浏览器访问地址；但这里运行的是 Java 后端，返回 JSON，而不是页面。

本课使用独立模块 [spring-boot-lab](../spring-boot-lab/pom.xml)，沿用目标项目的 Java 8、Boot 2.3.10.RELEASE 学习背景。这是历史版本学习示例，不是新生产项目的版本建议。

## 2. 先运行，再看代码

1. 在 IDEA 找到 `spring-boot-lab/pom.xml`，右键 **Add as Maven Project（添加为 Maven 项目）**。若已导入，使用 Maven 的重新加载按钮。
2. 等待依赖下载完；选择 JDK 8，运行配置的模块 classpath 选择 **spring-boot-lab**。
3. 打开 [Day33Application.java](../spring-boot-lab/src/main/java/learning/boot/Day33Application.java)，点击 main 左边的绿色三角。
4. 看到 `Started Day33Application` 后，打开 <http://127.0.0.1:8080/api/lesson>。

预期结果（JSON 显示顺序或排版不重要）：

```json
{"day":33,"environment":"base","teacherName":"BaseTeacher"}
```

先不要打开根地址 `/`，本课只配置了 `/api/lesson`。访问 `/` 得到 404 不代表启动失败。

## 3. 启动代码逐句读

```java
@SpringBootApplication
public class Day33Application {
    public static void main(String[] args) {
        SpringApplication.run(Day33Application.class, args);
    }
}
```

- `main`：Java 程序的入口。
- `Day33Application.class`：表示这个类本身的 Class 对象，不是手动读取磁盘上的 class 文件。
- `args`：启动程序时传入的参数，例如 `--spring.profiles.active=local`。
- `SpringApplication.run(...)`：创建并启动 Spring 应用。
- `@SpringBootApplication`：包含配置、自动配置和组件扫描等能力。默认扫描当前类所在包及其子包。

所以本课把 Controller 和 Service 都放在 `learning.boot` 包内。随便移到其他包，可能无法扫描到。

POM 的 `spring-boot-starter-web` 带来 Web、JSON 转换和内嵌服务器相关依赖，不需要额外安装 Tomcat。`spring-boot-maven-plugin` 用于生成能通过 `java -jar` 启动的包。

## 4. 一次请求经过哪里

```text
浏览器 GET /api/lesson
  → LessonController.lesson()
  → TeacherService.findTeacherName()
  → Map 结果
  → Spring 转换为 JSON
  → 浏览器显示
```

打开 [LessonController.java](../spring-boot-lab/src/main/java/learning/boot/LessonController.java)：

- `@RestController`：注册为 Spring Bean，方法返回值用作 HTTP 响应内容。
- `@GetMapping("/api/lesson")`：GET 请求路径与这个方法对应。
- 构造方法的 `TeacherService teacherService`：Spring 提供 Service 对象，这就是昨天学的构造器注入。
- `Map<String, Object>`：键是字符串，值可以是整数、字符串等对象。

打开 [TeacherService.java](../spring-boot-lab/src/main/java/learning/boot/TeacherService.java)：`@Service` 让它被扫描并注册为 Bean。只有一个构造方法时，可以省略 `@Autowired`。

今天没有数据库仓库，教师名直接来自配置，集中理解启动与配置。参数绑定、POST 和请求体明天再学。

## 5. 配置值怎样进入 Java

公共文件 [application.properties](../spring-boot-lab/src/main/resources/application.properties)：

```properties
server.address=127.0.0.1
server.port=8080
spring.application.name=day33-boot-lab
lesson.environment=base
lesson.teacher-name=BaseTeacher
```

`server.port` 是 Boot 认识的配置，决定监听端口；`lesson.teacher-name` 是本课自定义配置。只写自定义键不会自动产生业务效果，需要代码读取。

```java
public TeacherService(@Value("${lesson.teacher-name}") String teacherName) {
    this.teacherName = teacherName;
}
```

一步一步理解：

1. `${lesson.teacher-name}` 表示查找这个配置键。
2. `@Value` 让 Spring 把找到的值提供给参数。
3. 本次值是 `BaseTeacher`。
4. `this.teacherName = teacherName` 把参数保存到当前对象字段里。

它不是 Java 字符串自己支持的模板语法，而是 Spring 在创建 Bean 时解析的。缺失这个键且没有默认值时，启动会失败。

## 6. 切换 local 与 test

profile 可以理解为一组有名字的环境配置。文件存在，不代表自动启用。

IDEA 顶部运行配置 → **Edit Configurations（编辑配置）** → 选择 `Day33Application` → **Program arguments（程序实参）**。若看不到，在 **Modify options（修改选项）** 中显示该项。

填入：

```text
--spring.profiles.active=local
```

停止旧进程，重新运行，再打开 <http://127.0.0.1:8081/api/lesson>：

```json
{"day":33,"environment":"local","teacherName":"LocalTeacher"}
```

将程序实参改为：

```text
--spring.profiles.active=test
```

重新启动，打开 <http://127.0.0.1:8082/api/lesson>：

```json
{"day":33,"environment":"test","teacherName":"TestTeacher"}
```

本课两个 profile 文件都放在 `src/main/resources`，用于直接启动应用。`test` 只是名字，不代表自动执行单元测试，也不等同于 Maven 的 test 阶段。

## 7. 亲自验证配置优先级

将程序实参改为：

```text
--spring.profiles.active=local --server.port=8083 --lesson.teacher-name=CommandLineTeacher
```

重新启动，打开 <http://127.0.0.1:8083/api/lesson>：

```json
{"day":33,"environment":"local","teacherName":"CommandLineTeacher"}
```

对于本课这三种来源，同名键的优先级是：

```text
命令行参数 > 已激活 profile 的配置文件 > 公共 application.properties
```

公共配置仍然会读取，只是同名键被覆盖。例如 local 没有定义 `server.address`，仍沿用公共值。这里只说明本课涉及的来源，不是 Spring Boot 所有配置来源的完整排序。

`lesson.environment` 是演示标签，不是激活 profile 的开关。真正选择 profile 的是 `spring.profiles.active`。

修改文件后重新启动才能观察新配置；本课没有配置热更新。

## 8. 已安装 Maven 时的命令

```powershell
Set-Location E:\my\javaProjects\spring-boot-lab
mvn package
java -jar target/spring-boot-lab-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

本模块暂未添加自动化测试，构建成功不代表测试通过。运行后在另一个终端请求：

```powershell
Invoke-RestMethod http://127.0.0.1:8081/api/lesson
```

如果 `mvn` 找不到，可先用上面的 IDEA 步骤。IDEA 的 Maven 支持与 PowerShell 的 PATH 是两回事。

## 9. 常见错误

| 现象 | 检查方法 |
| --- | --- |
| `Port ... was already in use` | 停止自己之前的服务，或用 `--server.port=8083` 更换端口 |
| `SpringApplication` 标红、找不到类 | 确认 POM 已导入并同步，模块选 spring-boot-lab |
| 浏览器连接失败 | 检查启动是否报错、当前 profile 和实际监听端口 |
| 404 | 检查路径是否为 `/api/lesson` |
| 改了 local 文件却仍显示 base | 检查 Program arguments 是否激活 local，并重新启动 |
| 配置一直被覆盖 | 检查运行参数中是否还保留上一次的覆盖值 |

本课不需要 MySQL 驱动、数据库账号、Apollo 或公司私有依赖。

## 10. 对照真实项目

阅读 `E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\Application.java`：同样有 `@SpringBootApplication` 与 `SpringApplication.run(Application.class, args)`。

原项目还有 Feign、拦截器导入和数据源自动配置排除，不要直接复制到学习项目。原项目的 `src/main/resources` 中有公共、local、prod 配置；测试配置在 `src/test/resources/application-test.properties`，目录位置与本课不同。

今天只对照入口与配置结构，不启动真实业务服务，也不改动原项目。

## 11. 验收

- [ ] 能从 main 找到应用启动语句。
- [ ] 浏览器能访问 `/api/lesson` 并看到 JSON。
- [ ] 能解释 Controller 怎样拿到 Service。
- [ ] 能切换 local/test，访问各自端口。
- [ ] 能解释命令行为什么覆盖文件中的教师名。
- [ ] 能区分 profile 名字 test 与 Maven test 阶段。

## 12. 本次验证记录

2026-11-26：使用 JDK 8u491 和临时目录中的 Maven 3.9.9 执行本模块 `package`，结果为 `BUILD SUCCESS`。系统 PATH 中仍未找到 mvn；临时工具验证不等于已全局安装 Maven。

实际启动打包后的 JAR 并请求接口，以下四种情况均返回预期 JSON：默认配置（8080）、local（8081）、test（8082）、local 加命令行覆盖（8083）。验证进程已停止。这是 HTTP 启动与配置验证，未新增或执行单元测试，也未实际操作 IDEA 启动。

下一课：Day34 MVC 参数绑定，学习路径参数、查询参数与 JSON 请求体。
