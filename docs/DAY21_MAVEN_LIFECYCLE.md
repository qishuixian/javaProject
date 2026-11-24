# Day 21：Maven 生命周期与构建验证

## 1. 今日目标

Day20 已添加 POM。今天继续沿用它，理解 compile、test、package 如何衔接，并在 Maven 可用后完成一次编译、测试阶段、打包和运行的闭环。

入口：[Day21BuildLifecycle.java](../java-basics/src/Day21BuildLifecycle.java)。
构建配置：[pom.xml](../java-basics/pom.xml)。

先在 IDEA 点击 main 左侧绿色三角运行，观察它打印的四句话。然后再做下方 Maven 实验，区分 IDE 运行与 Maven 构建。

## 2. 先确认工具和目录

在新打开的 PowerShell 中输入：

```powershell
java -version
mvn -version
cd E:\my\javaProjects\java-basics
```

最后这个目录必须包含 pom.xml。不要在没有 POM 的仓库根目录直接执行 mvn compile。

如果 mvn 不识别，表示当前终端找不到命令，不等于 Java 代码有错。确认安装目录包含 bin/mvn.cmd，bin 已加入 Path，保存后重新打开终端；IDEA 的终端可能需要重启 IDEA 才能获得新环境。

本课检查时，当前会话仍无法识别 mvn。也可以在 IDEA Maven 面板执行阶段，IDEA 的 Maven 和命令行 Maven 可以是不同安装，因此各自要核对配置。

## 3. 什么是生命周期

生命周期是 Maven 组织构建步骤的方式；阶段是其中一个步骤。

默认构建生命周期常见顺序：

```text
validate → compile → test → package → verify → install → deploy
```

这张图省略了资源处理、测试编译等中间阶段。执行某个阶段时，会顺序执行该生命周期中它之前需要的阶段。

| 命令 | 主要效果 | 不代表什么 |
|---|---|---|
| mvn compile | 编译主源码 | 不运行 main |
| mvn test | 包含编译和测试阶段 | 不保证项目存在测试 |
| mvn package | 包含测试阶段并生成包 | 不自动启动 Jar |
| mvn install | 将构建产物安装到本地 Maven 仓库 | 不推送 Git，也不部署服务器 |

clean 属于另一套生命周期，用于清理构建输出。mvn clean package 是先执行清理，再执行默认生命周期到 package。

今天不执行 deploy，它通常涉及发布到远程制品仓库，也不同于 git push。

## 4. POM 怎样决定编译哪些文件

当前配置：

```xml
<sourceDirectory>src</sourceDirectory>
```

所以 src 下的 Java 文件都作为主源码编译，不只是 Day21。前几课留下编译错误，也会导致本次整体构建失败。

标准 Maven 工程通常使用 src/main/java 和 src/test/java。我们的 src 是自定义布局，后续加测试时需要先调整布局或明确编译排除配置，避免把 src/test/java 里的测试误当成主源码。今天不提前搬动已有课程。

maven-compiler-plugin 是执行编译的插件，compile 是生命周期阶段。插件的具体动作称为 goal，例如 compiler:compile；日常先使用 mvn compile，让 Maven 安排前置步骤。

## 5. 动手实验：逐步看输出

Maven 已可用时执行：

```powershell
cd E:\my\javaProjects\java-basics
mvn clean compile
```

成功后应能找到：

```text
target/classes/Day21BuildLifecycle.class
```

接着运行编译后的类：

```powershell
java -Dfile.encoding=UTF-8 -cp target/classes Day21BuildLifecycle
```

如果 PowerShell 对带点的 -D 参数解析异常，使用带引号版本：

```powershell
java '-Dfile.encoding=UTF-8' -cp target/classes Day21BuildLifecycle
```

-cp 指定查找类的路径，后面是类名，不加 .class。编译只是产生字节码，java 命令才执行 main。

## 6. 测试阶段：没有测试也可能成功

```powershell
mvn test
```

目前学习工程没有独立测试目录和 JUnit 测试用例。日志可能出现 No tests to run，最终仍显示 BUILD SUCCESS。

这只能说明构建到测试阶段没有报告失败，不能宣称“测试全部通过”。Day23 再添加真正的单元测试时，要检查测试数量、断言结果和 target/surefire-reports。

普通 main 示例不会因为类名包含 Day 就被自动当作测试执行。

## 7. 打包并运行普通 Jar

```powershell
mvn package
```

默认 packaging 为 jar，按当前坐标通常生成：

```text
target/java-basics-1.0-SNAPSHOT.jar
```

查看包内容：

```powershell
jar tf target/java-basics-1.0-SNAPSHOT.jar
```

从 Jar 中运行本课入口：

```powershell
java '-Dfile.encoding=UTF-8' -cp target/java-basics-1.0-SNAPSHOT.jar Day21BuildLifecycle
```

当前 POM 没有配置 Main-Class，也没有 Spring Boot 重新打包插件。因此不要直接假设 java -jar 能运行它。普通 Jar 可以包含很多 main 类，使用 -cp 加类名可明确选择入口。

预期输出：

```text
Day21：程序已运行
compile 编译源码，不自动执行 main
test 进入测试阶段，不等于已经存在测试用例
package 生成 Jar，不自动启动程序
```

## 8. 常见失败怎样区分

| 现象 | 先检查 |
|---|---|
| mvn 不是可识别命令 | Maven 安装、Path、终端是否重开 |
| 没有 POM | 当前目录是否为 java-basics |
| 插件下载或解析失败 | 网络、镜像、代理和 Maven settings；不要随意删除全部缓存 |
| 编译错误 | 错误中的源文件和行号，包括前几天课程 |
| 找不到主类 | 编译是否成功、-cp 路径是否正确 |
| no main manifest attribute | 普通 Jar 没有入口清单，按本课使用 -cp |
| No tests to run | 尚无测试或未被发现，不等于断言通过 |

构建日志中的第一条具体错误通常比末尾的 BUILD FAILURE 更有价值。记录完整原因，不只复制最后一行。

## 9. 与目标项目的对应

aic-teaching-service 的根 POM 聚合 api、service 模块，执行 package 会按照模块依赖组织构建。服务模块使用 Spring Boot 插件，它的可执行包结构与本课普通 Jar 不同。

公司私有父 POM 解析失败属于依赖解析阶段问题；不能因为学习工程编译通过，就认为公司服务的环境和依赖已经就绪。

## 10. 本次验证记录

本次在当前环境使用 JDK 编译项目全部 Java 源码到临时目录，并运行 Day21 入口；同时用 JDK 的 jar 工具生成临时普通 Jar，再从中运行入口对照结果。

这些是 JDK 编译、打包和运行验证，不是 Maven 生命周期验证。当前终端找不到 mvn，因此 Maven 的 clean/test/package 尚未实测。真实 Maven 结果请按上面步骤运行后记录。

## 11. 今日练习与验收

- [ ] 说明 compile、test、package 的执行关系。
- [ ] 执行 mvn -version，记录 Maven 与它实际使用的 Java 版本。
- [ ] 执行 compile 并确认 class 输出位置。
- [ ] 区分 No tests to run 与实际测试通过。
- [ ] 打包普通 Jar，使用 -cp 运行指定 main。
- [ ] 说明 mvn install 与 Git 提交或推送没有关系。

下一课：Maven 依赖与多模块，理解模块间依赖、传递依赖和版本管理。
