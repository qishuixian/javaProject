import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day12Generics {
    public static void printTeacherPage(PageResult<Teacher> page) {
        System.out.println("教师总数：" + page.getTotal());
        for (Teacher teacher : page.getRecords()) System.out.println("教师记录：" + teacher.getId() + " / " + teacher.getName());
    }
    public static long totalTeacherIds(List<? extends Teacher> teachers) {
        long total = 0;
        for (Teacher teacher : teachers) total += teacher.getId();
        return total;
    }
    public static void addDefaultTeacher(List<? super Teacher> target) {
        target.add(new Teacher(10003L, "王老师"));
        Object last = target.get(target.size() - 1);
        System.out.println("写入后最后一项类型：" + last.getClass().getSimpleName());
    }
    public static <T> PageResult<T> firstPage(List<T> source, int pageSize) {
        if (source == null || pageSize <= 0) throw new IllegalArgumentException("数据或页大小不合法");
        int end = Math.min(pageSize, source.size());
        return new PageResult<>(source.subList(0, end), source.size(), 1, pageSize);
    }
    public static void main(String[] args) {
        List<Teacher> teachers = Arrays.asList(new Teacher(10001L, "张老师"), new Teacher(10002L, "李老师"));
        PageResult<Teacher> teacherPage = new PageResult<>(teachers, 5, 1, 2);
        printTeacherPage(teacherPage);
        System.out.println("教师 ID 总和：" + totalTeacherIds(teachers));
        List<Teacher> writableTeachers = new ArrayList<>();
        addDefaultTeacher(writableTeachers);
        System.out.println("写入后的教师数：" + writableTeachers.size());
        PageResult<String> titlePage = firstPage(Arrays.asList("Java", "Spring", "MySQL"), 2);
        System.out.println("标题记录数：" + titlePage.getRecords().size());
        System.out.println("第一个标题：" + titlePage.getRecords().get(0));
        System.out.println("教师页面为空：" + teacherPage.isEmpty());
    }
}
