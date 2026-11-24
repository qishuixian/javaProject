import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day11Collections {
    public static void main(String[] args) {
        Teacher first = new Teacher(10001L, "张老师");
        Teacher second = new Teacher(10002L, "李老师");

        // List：按位置保存元素，允许重复。
        List<Teacher> teachers = new ArrayList<>();
        teachers.add(first);
        teachers.add(second);
        teachers.add(first);
        System.out.println("List 元素数：" + teachers.size());
        System.out.println("List 首位教师：" + teachers.get(0).getName());
        for (Teacher teacher : teachers) {
            System.out.println("遍历教师：" + teacher.getName());
        }
        teachers.remove(2); // 删除下标 2 的元素，不是删除 ID 为 2 的教师。
        System.out.println("List 删除后元素数：" + teachers.size());

        // Set：按 ID 去重。long 自动装箱成 Long。
        Set<Long> teacherIds = new HashSet<>();
        System.out.println("Set 首次添加：" + teacherIds.add(first.getId()));
        System.out.println("Set 重复添加：" + teacherIds.add(first.getId()));
        teacherIds.add(second.getId());
        System.out.println("Set 去重后数量：" + teacherIds.size());
        System.out.println("Set 包含 10001：" + teacherIds.contains(10001L));

        // Map：key 是教师 ID，value 是教师对象。
        Map<Long, Teacher> teachersById = new HashMap<>();
        teachersById.put(first.getId(), first);
        teachersById.put(second.getId(), second);
        Teacher found = teachersById.get(10002L);
        if (found != null) {
            System.out.println("Map 按 ID 查询：" + found.getName());
        }
        System.out.println("Map 查询不存在的 ID：" + teachersById.get(99999L));

        // 普通 put 遇到相同 key 会替换 value，并返回旧 value。
        Teacher old = teachersById.put(10001L, new Teacher(10001L, "王老师"));
        System.out.println("Map 替换前姓名：" + old.getName());
        System.out.println("Map 替换后姓名：" + teachersById.get(10001L).getName());
        System.out.println("Map 替换后数量：" + teachersById.size());
        System.out.println("原 List 的首位姓名：" + teachers.get(0).getName());

        // 业务上需要拒绝重复时，显式检查；本示例只用于单线程。
        if (teachersById.containsKey(10001L)) {
            System.out.println("业务校验：拒绝重复教师 ID");
        }
        teachersById.remove(10002L);
        System.out.println("Map 删除后数量：" + teachersById.size());
        System.out.println("新建 List 是否为空：" + new ArrayList<Teacher>().isEmpty());
    }
}
