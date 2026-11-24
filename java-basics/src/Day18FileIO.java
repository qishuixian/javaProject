import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day18FileIO {
    // 教学格式：每行 ID + Tab + 姓名。不是完整 CSV 或 JSON。
    public static void saveTeachers(Path file, List<Teacher> teachers) throws IOException {
        if (file == null || teachers == null) {
            throw new IllegalArgumentException("文件路径和教师列表不能为 null");
        }
        // 先校验数据，避免校验失败时已经创建部分文件。
        for (Teacher teacher : teachers) {
            if (teacher == null || teacher.getName().contains("\t")
                    || teacher.getName().contains("\n") || teacher.getName().contains("\r")) {
                throw new IllegalArgumentException("教师不能为空，姓名不能含 Tab 或换行");
            }
        }
        // CREATE_NEW：目标已存在则报错，避免意外覆盖。
        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            for (Teacher teacher : teachers) {
                writer.write(teacher.getId() + "\t" + teacher.getName());
                writer.newLine();
            }
        }
    }

    public static List<Teacher> loadTeachers(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("文件路径不能为 null");
        }
        List<Teacher> teachers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\t", -1);
                if (fields.length != 2) {
                    throw new IOException("第 " + lineNumber + " 行必须包含 ID 和姓名两列");
                }
                try {
                    teachers.add(new Teacher(Long.parseLong(fields[0]), fields[1]));
                } catch (IllegalArgumentException cause) {
                    throw new IOException("第 " + lineNumber + " 行教师数据不合法", cause);
                }
            }
        }
        return teachers;
    }

    public static void main(String[] args) throws IOException {
        Path directory = Files.createTempDirectory("java-day18-");
        Path dataFile = directory.resolve("teachers.tsv");
        System.out.println("练习目录：" + directory.toAbsolutePath());

        List<Teacher> source = Arrays.asList(
                new Teacher(10001L, "张老师"),
                new Teacher(10002L, "李小明"));
        saveTeachers(dataFile, source);
        List<Teacher> loaded = loadTeachers(dataFile);
        System.out.println("读取人数：" + loaded.size());
        for (Teacher teacher : loaded) {
            System.out.println("读取教师：" + teacher.getId() + " / " + teacher.getName());
        }

        Path emptyFile = directory.resolve("empty.tsv");
        saveTeachers(emptyFile, new ArrayList<Teacher>());
        System.out.println("空文件人数：" + loadTeachers(emptyFile).size());

        try {
            loadTeachers(directory.resolve("missing.tsv"));
        } catch (NoSuchFileException exception) {
            System.out.println("不存在的文件：已捕获 NoSuchFileException");
        }

        Path invalidFile = directory.resolve("invalid.tsv");
        Files.write(invalidFile, Arrays.asList("abc\t王老师"),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        try {
            loadTeachers(invalidFile);
        } catch (IOException exception) {
            System.out.println("格式错误：" + exception.getMessage());
            System.out.println("原始原因：" + exception.getCause().getClass().getSimpleName());
        }
        // 保留临时文件供查看；每次运行创建新目录，不写入项目 src。
        System.out.println("教师文件：" + dataFile.toAbsolutePath());
    }
}
