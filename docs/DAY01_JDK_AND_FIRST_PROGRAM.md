# Day 1：JDK、编译与运行第一个 Java 程序

## 今日目标

今天只解决一个问题：Java 源代码如何变成可以运行的程序。完成后，你应能解释 JDK、`javac`、`java`、`.class` 文件和 `main` 方法的关系，并能运行一个教学服务领域的小程序。

## 1. JDK、JRE 与 JVM

- **JVM**：真正执行 Java 字节码的虚拟机。
- **JRE**：JVM 加上运行 Java 程序所需的标准类库。
- **JDK**：JRE 加上开发工具，例如 `javac` 编译器。

本机已验证：JDK `1.8.0_491`，同时可以使用 `java` 和 `javac`。目标项目 `aic-teaching-service` 的 Maven 配置也是 Java 8，因此今天的示例与项目版本一致。

## 2. 第一个程序

文件：[Day01Hello.java](../java-basics/src/Day01Hello.java)

```java
public class Day01Hello {
    public static void main(String[] args) {
        System.out.println("开始学习 Java：aic-teaching-service");
        System.out.println("第一条业务数据：教师管理");
    }
}
```

说明：

- `public class Day01Hello` 定义一个公开类，文件名必须是 `Day01Hello.java`。
- `main` 是 Java 程序默认的入口：`public static void main(String[] args)`。
- `System.out.println` 向控制台输出一行文字。

## 3. 编译与运行

在 PowerShell 中执行：

```powershell
Set-Location E:\my\javaProjects\java-basics\src
javac -encoding UTF-8 Day01Hello.java
java Day01Hello
```

`javac` 会生成 `Day01Hello.class`。`java Day01Hello` 运行的是类名，不要写 `.java` 或 `.class`。

也可以使用脚本一次完成：

```powershell
Set-Location E:\my\javaProjects
\.\java-basics\run-day01.ps1
```

## 4. 与目标项目的对应关系

目标项目的入口文件是：

`E:\code\java\aic-teaching-service\aic-teaching-service-service\src\main\java\cn\vipthink\aic\teaching\Application.java`

它同样有 `public static void main(String[] args)`，只是入口中调用了 `SpringApplication.run(...)`，由 Spring Boot 创建 Web 服务，而不是只打印文本。先掌握今天的入口、编译和运行，再学习 Spring Boot 的 Bean 和 HTTP 请求。

## 5. 今日练习

1. 把输出中的“教师管理”改成“课节管理”，重新编译运行。
2. 增加一行输出：`当前学习阶段：Day 1`。
3. 故意执行 `java Day01Hello.java` 和 `java Day01Hello.class`，记录错误信息，再改回正确命令。
4. 不看示例，重新写出一个 `LessonProgram` 类，输出课节名称和课节时长。

## 6. 今日验收

- [ ] 能执行 `java -version` 和 `javac -version`。
- [ ] 能说明 `.java`、`.class` 和类名之间的关系。
- [ ] 能写出标准 `main` 方法。
- [ ] 能独立编译并运行 `Day01Hello`。
- [ ] 能指出 `Application.java` 为什么也是 Java 程序入口。

## 今日结果

已使用 Java 8 编译并运行 `Day01Hello.java`，输出为：

```text
开始学习 Java：aic-teaching-service
第一条业务数据：教师管理
```
