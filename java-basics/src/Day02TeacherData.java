public class Day02TeacherData {
    public static void main(String[] args) {
        long teacherId = 100101L;
        String teacherName = "张老师";
        int lessonCount = 12;
        boolean enabled = true;
        Integer optionalScore = null;

        System.out.println("教师 ID：" + teacherId);
        System.out.println("教师姓名：" + teacherName);
        System.out.println("课节数量：" + lessonCount);
        System.out.println("是否启用：" + enabled);
        System.out.println("可选评分：" + optionalScore);
    }
}
