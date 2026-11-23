public class Day06Objects {
    public static void main(String[] args) {
        Teacher teacher = new Teacher(10001L, " 张老师 ");
        Lesson lesson = new Lesson("Java 入门", teacher, 45);

        System.out.println("教师：" + teacher.getId() + " / " + teacher.getName());
        System.out.println("课节：" + lesson.getTitle() + " / " + lesson.getMinutes() + " 分钟");

        teacher.rename("李老师");
        lesson.changeMinutes(60);
        System.out.println("修改后教师：" + lesson.getTeacher().getName());
        System.out.println("修改后时长：" + lesson.getMinutes());

        // 暂时用 try/catch 观察校验失败；异常体系将在后续课程系统学习。
        try {
            teacher.rename("   ");
        } catch (IllegalArgumentException exception) {
            System.out.println("拒绝改名：" + exception.getMessage());
        }
        System.out.println("姓名仍为：" + teacher.getName());

        try {
            lesson.changeMinutes(0);
        } catch (IllegalArgumentException exception) {
            System.out.println("拒绝改时长：" + exception.getMessage());
        }
        System.out.println("时长仍为：" + lesson.getMinutes());

        try {
            new Teacher(0L, "王老师");
        } catch (IllegalArgumentException exception) {
            System.out.println("拒绝创建教师：" + exception.getMessage());
        }
        try {
            new Lesson("Java 入门", null, 45);
        } catch (IllegalArgumentException exception) {
            System.out.println("拒绝创建课节：" + exception.getMessage());
        }
    }
}
