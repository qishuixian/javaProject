# Day 22：Maven 依赖与多模块

## 今日目标

理解父 POM、模块聚合和依赖方向。新增的 `java-basics/maven-lab` 包含 `api` 与 `service` 两个模块：api 提供公共模型，service 依赖 api。

## 1. 目录关系

```text
maven-lab/
├── pom.xml       packaging=pom，聚合两个模块
├── api/          TeacherSummary 公共模型
└── service/      使用 TeacherSummary 的入口
```

在 IDEA 打开 [maven-lab/pom.xml](../java-basics/maven-lab/pom.xml)，选择 Load Maven Project，然后运行 [ModuleDemo.java](../java-basics/maven-lab/service/src/main/java/ModuleDemo.java)。

## 2. 父 POM 与 modules

父 POM 的 `<modules>` 表示一次构建哪些子项目。`packaging=pom` 表示它本身不是业务 Jar。父 POM 还可以统一 Java 版本和插件配置。

聚合是“一起构建哪些项目”；`parent` 是“子项目继承哪些配置”。两者常一起出现，但不是同一个概念。

## 3. 依赖方向

service 的 POM 声明依赖 api，所以 service 编译时可以使用 `TeacherSummary`。api 不依赖 service，避免循环依赖。

```text
api：公共 DTO / 模型
 ↑
service：业务入口和实现
```

在根目录执行：

```powershell
cd E:\my\javaProjects\java-basics\maven-lab
mvn clean package
```

Maven 会先构建 api，再构建 service。只构建 service 时，使用 `mvn -pl service -am package`，`-am` 表示同时构建它依赖的模块。

当前环境此前仍未识别 `mvn`，所以命令需要在 Maven 配置完成后实测。IDEA Maven 面板也可以执行 package。

## 4. dependencies 与 dependencyManagement

`dependencies` 表示本模块实际需要的依赖，service 因此可以 import api 类。`dependencyManagement` 只统一版本，子模块仍要在 `dependencies` 中声明依赖；只写 dependencyManagement 不能直接使用类。

默认 compile 依赖会参与编译、测试和运行。`test` 依赖只在测试阶段使用。`mvn dependency:tree` 可查看直接和传递依赖。

## 5. 对照 aic-teaching-service

目标项目根 POM 同样聚合 `aic-teaching-service-api` 和 `aic-teaching-service-service`：api 放 DTO、枚举等公共模型，service 放 Spring Boot 业务实现。它还依赖私有 parent 和多个公司公共包；学习工程构建成功不能证明私有仓库可用。

## 6. 今日验证

本地先用 JDK 8 分别编译 api 与 service，并把 api 的 classpath 交给 service，模拟 Maven 的依赖方向：

```powershell
javac -encoding UTF-8 -d api-out api/src/main/java/TeacherSummary.java
javac -encoding UTF-8 -cp api-out -d service-out service/src/main/java/ModuleDemo.java
java -cp service-out;api-out ModuleDemo
```

预期输出：

```text
service 使用 api：10001 / 张老师
```

这证明 Java 类路径和依赖方向正确，不等于 Maven package 已成功。

## 7. 今日练习与验收

- [ ] 能解释父 POM 的 modules。
- [ ] 能说明 service 为什么依赖 api。
- [ ] 能区分 dependencies 与 dependencyManagement。
- [ ] 能解释 `-pl` 和 `-am`。
- [ ] 能用 classpath 编译并运行两个模块。
- [ ] 能对照 aic-teaching-service 的 api/service 结构。

下一课：JUnit 单元测试，用断言验证方法行为。
