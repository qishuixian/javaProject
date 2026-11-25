public class ModuleDemo {
    public static void main(String[] args) {
        TeacherSummary summary = new TeacherSummary(10001L, "张老师");
        System.out.println("service 使用 api：" + summary.getId() + " / " + summary.getName());
    }
}
