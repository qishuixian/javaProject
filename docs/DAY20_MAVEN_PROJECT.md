# Day 20：Maven 工程与依赖管理

## 今日目标

把前 1～19 天的 Java 源码放进一个可重复构建的 Maven 工程，理解 `pom.xml`、坐标、生命周期、编译版本和依赖。今天不再创建运行脚本。

项目文件：[java-basics/pom.xml](../java-basics/pom.xml)。当前项目的源码目录仍是 `java-basics/src`，POM 用 `<sourceDirectory>src</sourceDirectory>` 明确告诉 Maven 这一点。

## 1. Maven 是什么

Maven 是 Java 项目构建工具，负责约定或配置源码位置、调用编译器、管理依赖、运行测试和生成打包产物。它不是 JDK：JDK 提供 javac/java，Maven 负责组织这些步骤。

目标项目 `aic-teaching-service` 也是 Maven 多模块工程，根 POM 管理版本和模块，api/service 子模块分别编译。今天的 java-basics 是单模块、无第三方依赖的小工程，先理解基础。

## 2. pom.xml 的关键部分

```xml
<groupId>cn.vipthink.learning</groupId>
<artifactId>java-basics</artifactId>
<version>1.0-SNAPSHOT</version>
```

三者组成项目坐标：组织、项目名、版本。`SNAPSHOT` 表示开发中的版本，不代表源码自动上传仓库。

```xml
<maven.compiler.source>8</maven.compiler.source>
<maven.compiler.target>8</maven.compiler.target>
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
```

本练习和目标项目都按 Java 8 编译。IDEA 使用的 JDK、Maven 使用的 JDK、POM 的目标版本需要分别核对；编译目标为 8 不会自动把 JDK 17 的 API 变成 Java 8 可用 API。

```xml
<sourceDirectory>src</sourceDirectory>
```

标准 Maven 目录通常是 `src/main/java`，本练习为了保留前几课的平面目录显式配置为 `src`。真实项目建议遵循标准布局，便于工具识别。

## 3. 生命周期与常用命令

在有 Maven 的终端中：

```powershell
cd E:\my\javaProjects\java-basics
mvn clean
mvn compile
mvn package
```

大致关系：

```text
clean   删除 target
compile 编译主源码
test    编译并运行测试
package 生成 jar
```

后一个阶段包含前面需要的阶段；执行 `mvn package` 通常会先编译和测试。`clean` 只删除 Maven 的 target，不会删除 src 下的 Java 文件。

当前电脑此前验证过 JDK 8，但命令行没有识别 `mvn`，所以本课没有声称 Maven 构建成功。可在 IDEA Maven 面板点击 Lifecycle 的 compile/package，前提是 IDEA 配置了 Maven 和可解析的插件仓库。也可以先安装 Maven 并把 bin 加入 PATH，再运行命令。

## 4. 依赖是什么

依赖是项目编译或运行所需的外部库。今天项目没有业务第三方依赖，JDK 类库由 JDK 提供，不需要写成 dependency。若后续加入 JUnit 测试，应在 POM 声明测试作用域依赖，不能把 jar 手动复制到 src。

依赖可能传递引入其他依赖，版本冲突要用 `mvn dependency:tree` 查看。目标项目的 POM 依赖 Spring Boot、MyBatis-Plus、MySQL 等公司和开源库，另有私有父 POM；不能直接把它们复制到入门工程就认为环境已准备好。

## 5. 生成目录和源码目录

Maven 默认生成：

```text
java-basics/target/classes/  编译后的 class
java-basics/target/test-classes/ 测试 class
java-basics/target/*.jar     打包产物
```

这些都是可删除的构建产物，应加入 `.gitignore`。本项目根部已忽略 `target/` 和 `*.class`。源码、文档和 pom.xml 才是学习成果。

## 6. 在 IDEA 中使用

1. 打开 `java-basics/pom.xml`，选择 Load Maven Project。
2. 右侧 Maven 面板展开 Lifecycle。
3. 先运行 clean，再运行 compile。
4. 运行源码中的 main 仍可直接点击绿色三角。
5. 如果 IDEA 报 JDK 或 Maven 错误，先检查 Settings → Build Tools → Maven 的 Maven home、JDK 和 User settings。

Maven compile 只负责编译，不能替代 main 运行。IDEA 运行 main 可能使用 IDE 自己的输出目录，命令行 Maven 使用 target/classes，二者都应指向同一份源码。

## 7. 与 aic-teaching-service 的对照

目标项目根 POM 是 packaging=pom，并声明 api/service 两个 modules；服务模块再配置 Spring Boot、Surefire、JaCoCo 等插件。学习时先回答：

- 根 POM 的 `<modules>` 如何找到子模块？
- `aic-teaching-service-api` 为什么可以独立打包？
- service 为什么依赖 api？
- 私有 parent POM 无法解析时，错误属于代码问题还是依赖仓库问题？
- `skipTests=false` 与 `maven.test.skip=false` 分别影响什么？

不要在无法解析私有依赖时反复改 Java 代码；先从错误中的 artifact、仓库和 settings.xml 定位环境问题。

## 8. 今日验证

由于当前命令行 `mvn` 不在 PATH，本课进行等价的 JDK 编译检查：使用 Java 8 编译 `java-basics/src` 的全部 `.java` 文件到临时目录，并运行 Day20 之前的代表性入口。Maven 构建需在 Maven 可用后另行执行；这两种验证不能混为一谈。

在 Maven 可用时保存实际命令和结果：

```powershell
mvn clean package
```

成功标准是退出码 0、测试实际执行、target 中存在产物。不能只看到 IDE 没有红线就断言 Maven package 成功。

## 9. 今日练习

1. 在 IDEA Maven 面板执行 compile，找到 target/classes 中的 class。
2. 将 sourceDirectory 改成不存在的目录，观察编译结果，再恢复。
3. 运行 `mvn dependency:tree`，解释当前无业务依赖的输出。
4. 为工程增加标准 `src/main/java` 目录，比较显式 sourceDirectory 与 Maven 默认约定。
5. 记录 JDK 版本、Maven 版本、IDEA 使用的 Maven 和实际构建结果。

## 10. 今日验收

- [ ] 能解释 JDK 与 Maven 的职责区别。
- [ ] 能读懂项目坐标和 Java 8 编译配置。
- [ ] 能解释 clean、compile、test、package 的关系。
- [ ] 能区分源码、target 构建产物和依赖缓存。
- [ ] 能在 IDEA 找到 Maven Lifecycle。
- [ ] 能判断“IDEA 能运行 main”和“Maven package 成功”不是同一个证据。

下一阶段：Day21～Day30 工程基础、测试与数据库。
