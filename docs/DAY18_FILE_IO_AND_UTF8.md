# Day 18：文件 IO 与 UTF-8 编码

## 1. 今日目标与运行

Day10 的教师数据只在内存中，退出后消失。今天把教师 ID 和姓名写入文件，再从文件恢复成 Teacher 对象。

打开 [Day18FileIO.java](../java-basics/src/Day18FileIO.java)，点击 IDEA 中 main 左侧绿色三角运行。依赖前几课的 [Teacher.java](../java-basics/src/Teacher.java)。

程序会在系统临时目录下创建独立的 java-day18-* 文件夹，并打印完整路径。打开其中 teachers.tsv 可以看到保存的数据。文件保留供学习查看，不在项目中新增脚本或数据文件，每次运行生成不同目录。

## 2. IO 是什么

- Input：把文件中的数据读进程序。
- Output：把程序中的数据写到文件。

今天的过程：

```text
List<Teacher> → 转成文本行 → UTF-8 字节 → 文件
文件 → UTF-8 解码 → 文本行 → 解析 ID、姓名 → List<Teacher>
```

文件不会自动理解 Java 对象。我们必须明确怎么把对象变成文本，以及怎么还原。

## 3. 先约定文件格式

每行保存一位教师，两个字段之间用 Tab 分隔：

```text
10001    张老师
10002    李小明
```

这里空隙表示一个 Tab 字符，代码里写成 `"\t"`，不是任意数量的空格。

本课格式要求：ID 为正整数，姓名非空且不能包含 Tab、回车或换行。每行必须两列，空白行算格式错误；真正的空文件没有任何行，读取结果为空列表。没有表头，不处理完整 CSV 转义，Day19 再学习 JSON。

## 4. Path 表示路径，Files 提供操作

```java
Path directory = Files.createTempDirectory("java-day18-");
Path file = directory.resolve("teachers.tsv");
```

逐段看：

| 写法 | 含义 |
|---|---|
| Path | Java 中表示文件或目录路径的类型 |
| Files | 提供文件读写、创建等静态方法的工具类 |
| createTempDirectory | 真正创建一个新的临时目录 |
| resolve("teachers.tsv") | 组合出子文件路径，此时还没有创建文件 |
| toAbsolutePath() | 得到绝对路径 |

需要指定其他目录时可以使用：

```java
Path file = java.nio.file.Paths.get("E:/my/javaProjects/data/teachers.tsv");
```

这是语法示例，程序不会向这个路径写入。父目录必须存在；需要创建目录时用 Files.createDirectories。相对路径以运行时工作目录为基准，不一定是 Java 源文件所在目录。

## 5. 写文件：BufferedWriter

```java
try (BufferedWriter writer = Files.newBufferedWriter(
        file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
    writer.write(teacher.getId() + "\t" + teacher.getName());
    writer.newLine();
}
```

writer 是文本输出工具，Buffered 表示使用缓冲区减少频繁底层写入。write 写内容，newLine 写系统适用的换行符。

CREATE_NEW 要求文件尚不存在；已存在时抛 FileAlreadyExistsException，不会覆盖。一般写文件还可能选择覆盖或追加模式，不能未经确认就混用。本课只创建新文件。

try 后的括号声明资源，离开大括号时自动 close，正常结束和异常退出都适用。关闭 writer 会尝试写出缓冲内容并释放文件资源，关闭过程本身也可能抛 IOException。

## 6. 读文件：BufferedReader

```java
try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
    String line;
    while ((line = reader.readLine()) != null) {
        // 解析这一行
    }
}
```

这句 while 拆开是：

1. 调用 readLine 读取一行。
2. 把结果赋给 line。
3. 判断 line 是否为 null；不是 null 就处理，然后再读下一行。

readLine 不包含行末的换行符。读到文件结尾返回 null；读到空白行则返回空字符串，这两种情况不同。

本课逐行读取，但最终把全部 Teacher 保存进 List，所以总内存仍随数据量增长。大文件需要分批处理，不能仅因为用了 BufferedReader 就认为内存固定。

## 7. 文本还原成对象

```java
String[] fields = line.split("\t", -1);
Teacher teacher = new Teacher(Long.parseLong(fields[0]), fields[1]);
```

split 按 Tab 拆分；第二个参数 -1 保留末尾空字段，避免空姓名在拆分时消失。先检查列数，再将 ID 字符串转成 long，最后交给 Teacher 构造方法校验。

如果 ID 不是数字或对象校验失败，将异常包装成带行号的 IOException，并保存 cause：

```java
throw new IOException("第 " + lineNumber + " 行教师数据不合法", cause);
```

不要遇到坏行就静默跳过，否则读取人数可能悄悄减少。本课任意一行非法就让整个读取操作失败，不返回部分列表。文件内的重复 ID 暂时保留，判重属于后续业务规则。

## 8. 为什么明确指定 UTF-8

字符要先编码成字节才能保存；读回来时需要按同一种编码解码。

```java
StandardCharsets.UTF_8
```

写和读都指定 UTF-8，避免中文依赖机器默认字符集。Java 8 的默认字符集在不同系统上可能不同。

源文件编码、数据文件编码、控制台显示编码是三个环节。终端显示乱码并不直接证明文件坏了：先用支持 UTF-8 的编辑器打开数据文件，再确认控制台编码。IDEA 中也应使用 UTF-8 查看文件。

## 9. IOException 与不存在的文件

saveTeachers 和 loadTeachers 的签名都有：

```java
throws IOException
```

这表示调用方必须捕获或继续声明受检异常。main 在本课也声明 throws IOException，让没有预期到的文件系统失败显示完整异常；对于刻意演示的“文件不存在”，只捕获 NoSuchFileException。

文件不存在不能一律当成空文件，否则会隐藏路径写错、挂载缺失等问题。返回空列表仅代表确实读到了一个合法空文件。

写入中途磁盘失败可能留下部分文件。try-with-resources 保证尝试释放资源，不保证文件操作像数据库事务一样自动回滚；以后再学习临时文件加移动替换等可靠写入方案。

## 10. 预期输出

临时路径每次不同，其他内容应为：

```text
练习目录：<本次临时目录>
读取人数：2
读取教师：10001 / 张老师
读取教师：10002 / 李小明
空文件人数：0
不存在的文件：已捕获 NoSuchFileException
格式错误：第 1 行教师数据不合法
原始原因：NumberFormatException
教师文件：<本次临时目录>/teachers.tsv
```

保存后重新读取会创建新的 Teacher 对象；比较还原是否正确应核对 ID 和姓名，而不是用 == 判断对象相同。

## 11. 与目标项目的联系

教学服务的导入导出同样涉及数据格式、字符编码、资源关闭和失败处理。今天的 TSV 文件只是基础练习，不是 Excel 文件，不能把后缀改成 xlsx 就当成 Excel。真实上传还需要大小限制、格式校验和权限检查。

## 12. 今日练习

1. 增加第三位中文姓名的教师，保存后确认三条数据。
2. 重复调用 saveTeachers 写入同一路径，捕获 FileAlreadyExistsException，验证原文件未被覆盖。
3. 制作末尾姓名为空或多出一列的文件，观察具体错误。
4. 给姓名加入 Tab，确认保存阶段拒绝输入。
5. 扩展重复 ID 校验，并明确选择拒绝还是覆盖，不要静默决定。

## 13. 今日验收

- [ ] 能区分路径对象与实际文件。
- [ ] 能解释写入、读取和字符编码的关系。
- [ ] 能逐段读懂逐行读取的 while 条件。
- [ ] 能使用 try-with-resources 释放读写资源。
- [ ] 能区分空文件、文件不存在和格式错误。
- [ ] 能确认读取后的中文、ID 与原数据一致。

下一课：JSON 序列化，对象与 JSON 文本的相互转换。
