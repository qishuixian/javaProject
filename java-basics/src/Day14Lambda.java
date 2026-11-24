import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Day14Lambda {
    // 遍历过程固定，筛选规则由调用方传入。返回新列表，不删除原列表元素。
    public static List<Teacher> filterTeachers(
            List<Teacher> teachers, Predicate<Teacher> condition) {
        if (teachers == null || condition == null) {
            throw new IllegalArgumentException("教师列表和筛选条件不能为 null");
        }
        List<Teacher> result = new ArrayList<>();
        for (Teacher teacher : teachers) {
            if (teacher == null) {
                throw new IllegalArgumentException("教师列表不能包含 null");
            }
            if (condition.test(teacher)) {
                result.add(teacher);
            }
        }
        return result;
    }

    public static void printResult(String label, List<Teacher> teachers) {
        System.out.println(label + "：" + teachers.size() + " 人");
        for (Teacher teacher : teachers) {
            System.out.println("  " + teacher.getId() + " / " + teacher.getName());
        }
    }

    public static void main(String[] args) {
        List<Teacher> teachers = Arrays.asList(
                new Teacher(10001L, "张老师"),
                new Teacher(10002L, "李老师"),
                new Teacher(10003L, "张小明"));

        // 写法一：匿名内部类，明确实现 Predicate 的 test 方法。
        Predicate<Teacher> anonymousCondition = new Predicate<Teacher>() {
            @Override
            public boolean test(Teacher teacher) {
                return teacher.getName().startsWith("张");
            }
        };
        printResult("匿名内部类筛选姓张", filterTeachers(teachers, anonymousCondition));

        // 写法二：同样的规则用 Lambda 表达；Teacher 参数类型可由编译器推断。
        Predicate<Teacher> startsWithZhang = teacher -> teacher.getName().startsWith("张");
        printResult("Lambda 筛选姓张", filterTeachers(teachers, startsWithZhang));

        // Lambda 捕获外部局部变量：此变量初始化后不能再被重新赋值。
        long minimumId = 10002L;
        Predicate<Teacher> idAtLeastMinimum = teacher -> teacher.getId() >= minimumId;
        printResult("姓张且 ID 达标", filterTeachers(teachers, startsWithZhang.and(idAtLeastMinimum)));
        printResult("不姓张", filterTeachers(teachers, startsWithZhang.negate()));

        // 也可以直接在调用参数处传入 Lambda。
        printResult("没有匹配", filterTeachers(teachers, teacher -> teacher.getId() == 99999L));
        printResult("空列表", filterTeachers(new ArrayList<Teacher>(), startsWithZhang));
        System.out.println("原列表人数：" + teachers.size());
    }
}
