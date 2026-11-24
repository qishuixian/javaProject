import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day15Streams {
    public static void main(String[] args) {
        List<Teacher> teachers = Arrays.asList(
                new Teacher(10001L, "张老师"),
                new Teacher(10002L, "李老师"),
                new Teacher(10003L, "张小明"));

        List<Teacher> zhangTeachers = teachers.stream()
                .filter(teacher -> teacher.getName().startsWith("张"))
                .collect(Collectors.toList());
        System.out.println("姓张人数：" + zhangTeachers.size());

        List<String> names = teachers.stream()
                .map(teacher -> teacher.getName())
                .collect(Collectors.toList());
        System.out.println("姓名列表：" + names);

        List<Long> sortedIds = teachers.stream()
                .map(Teacher::getId)
                .sorted()
                .collect(Collectors.toList());
        System.out.println("排序后的 ID：" + sortedIds);

        Map<String, List<Teacher>> grouped = teachers.stream()
                .collect(Collectors.groupingBy(teacher -> teacher.getName().substring(0, 1)));
        System.out.println("张姓分组人数：" + grouped.get("张").size());
        System.out.println("王姓分组是否存在：" + grouped.containsKey("王"));

        long idSum = teachers.stream()
                .mapToLong(Teacher::getId)
                .sum();
        System.out.println("ID 总和：" + idSum);
        System.out.println("原列表人数：" + teachers.size());
    }
}
