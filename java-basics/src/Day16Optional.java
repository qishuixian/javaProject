import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Day16Optional {
    public static Optional<Teacher> findTeacher(List<Teacher> teachers, long id) {
        if (teachers == null) {
            throw new IllegalArgumentException("教师列表不能为 null");
        }
        return teachers.stream()
                .filter(Objects::nonNull)
                .filter(teacher -> teacher.getId() == id)
                .findFirst();
    }

    public static void main(String[] args) {
        List<Teacher> teachers = Arrays.asList(
                new Teacher(10001L, "张老师"),
                new Teacher(10002L, "李老师"));

        Optional<Teacher> found = findTeacher(teachers, 10001L);
        if (found.isPresent()) {
            System.out.println("查询成功：" + found.get().getName());
        }

        Optional<Teacher> missing = findTeacher(teachers, 99999L);
        System.out.println("不存在的教师是否有值：" + missing.isPresent());
        System.out.println("orElse 默认姓名：" + missing.map(Teacher::getName).orElse("未找到"));
        System.out.println("orElseGet 默认姓名：" + missing.map(Teacher::getName).orElseGet(() -> "按需生成的默认值"));

        String upperName = found.map(Teacher::getName)
                .map(String::toUpperCase)
                .orElse("UNKNOWN");
        System.out.println("map 后姓名：" + upperName);

        found.ifPresent(teacher -> System.out.println("ifPresent：" + teacher.getId()));
        missing.ifPresent(teacher -> System.out.println("这行不会输出"));

        try {
            missing.orElseThrow(() -> new TeacherNotFoundException(99999L));
        } catch (TeacherNotFoundException exception) {
            System.out.println("orElseThrow：" + exception.getMessage());
        }

        Optional<String> nullOptional = Optional.ofNullable(null);
        System.out.println("ofNullable(null) 是否有值：" + nullOptional.isPresent());
        System.out.println("Objects.equals(null, null)：" + Objects.equals(null, null));
    }
}
