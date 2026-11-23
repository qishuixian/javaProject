public class Day07Interfaces {
    // 调用方只依赖接口，不判断具体实现类，也不直接访问存储数组。
    public static void printTeacher(TeacherRepository repository, long id) {
        Teacher teacher = repository.findById(id);
        if (teacher == null) {
            System.out.println("未找到教师：" + id);
            return;
        }
        System.out.println("教师：" + teacher.getId() + " / " + teacher.getName());
    }

    public static void main(String[] args) {
        Teacher[] teachers = {
            new Teacher(10001L, "张老师"),
            new Teacher(10002L, "李老师")
        };

        TeacherRepository repository = new InMemoryTeacherRepository(teachers);
        System.out.println("普通内存实现");
        printTeacher(repository, 10001L);
        printTeacher(repository, 99999L);

        // 换一个对象，同一个 printTeacher 方法不需要修改。
        repository = new LoggingTeacherRepository(teachers);
        System.out.println("带日志的内存实现");
        printTeacher(repository, 10002L);
        printTeacher(repository, 99999L);

        System.out.println("空仓库");
        printTeacher(new InMemoryTeacherRepository(new Teacher[0]), 10001L);
    }
}
