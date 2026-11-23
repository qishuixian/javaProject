public class InMemoryTeacherRepository implements TeacherRepository {
    private final Teacher[] teachers;

    public InMemoryTeacherRepository(Teacher[] teachers) {
        if (teachers == null) {
            throw new IllegalArgumentException("教师数组不能为 null");
        }
        // 复制数组，避免调用方替换数组元素影响仓库；Teacher 对象仍共享。
        this.teachers = new Teacher[teachers.length];
        for (int i = 0; i < teachers.length; i++) {
            if (teachers[i] == null) {
                throw new IllegalArgumentException("教师数组不能包含 null");
            }
            this.teachers[i] = teachers[i];
        }
    }

    @Override
    public Teacher findById(long id) {
        for (Teacher teacher : teachers) {
            if (teacher.getId() == id) {
                return teacher;
            }
        }
        return null;
    }
}
