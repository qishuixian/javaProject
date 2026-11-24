public class Day13Exceptions {
    public static void requirePositiveId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("教师 ID 必须大于 0");
        }
    }

    public static Teacher findTeacher(TeacherRepository repository, long id) {
        if (repository == null) {
            throw new IllegalArgumentException("查询仓库不能为 null");
        }
        requirePositiveId(id);
        Teacher teacher = repository.findById(id);
        if (teacher == null) {
            throw new TeacherNotFoundException(id);
        }
        return teacher;
    }

    public static void main(String[] args) {
        TeacherDirectory directory = new TeacherDirectory(2);
        directory.add(10001L, "张老师");
        try {
            System.out.println("查询结果：" + findTeacher(directory, 10001L).getName());
            findTeacher(directory, 99999L);
        } catch (TeacherNotFoundException exception) {
            System.out.println("业务失败：" + exception.getMessage());
        }

        try {
            findTeacher(directory, 0L);
        } catch (IllegalArgumentException exception) {
            System.out.println("输入失败：" + exception.getMessage());
        }

        try {
            directory.add(10001L, "李老师");
        } catch (IllegalArgumentException exception) {
            System.out.println("新增失败：" + exception.getMessage());
        } finally {
            System.out.println("新增操作已结束");
        }

        try {
            findTeacher(null, 10001L);
        } catch (IllegalArgumentException exception) {
            System.out.println("系统调用参数失败：" + exception.getMessage());
        }

        try {
            Integer count = null;
            int value = count;
            System.out.println(value);
        } catch (NullPointerException exception) {
            System.out.println("运行时失败：包装类型 null 不能拆箱");
        }
    }
}
