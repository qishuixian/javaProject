// 固定容量数组版：Day11 再学习集合。
public class TeacherDirectory implements TeacherRepository {
    private final Teacher[] teachers;
    private int size;

    public TeacherDirectory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("容量必须大于 0");
        }
        teachers = new Teacher[capacity];
    }

    public void add(long id, String name) {
        // 先完成所有校验，最后才修改数组和数量。
        Teacher teacher = new Teacher(id, name);
        if (findById(id) != null) {
            throw new IllegalArgumentException("教师 ID 已存在");
        }
        if (size == teachers.length) {
            throw new IllegalArgumentException("目录已满");
        }
        teachers[size] = teacher;
        size++;
    }

    @Override
    public Teacher findById(long id) {
        for (int i = 0; i < size; i++) {
            if (teachers[i].getId() == id) {
                return teachers[i];
            }
        }
        return null;
    }

    public void rename(long id, String name) {
        Teacher teacher = findById(id);
        if (teacher == null) {
            throw new IllegalArgumentException("教师不存在");
        }
        teacher.rename(name);
    }

    public int size() {
        return size;
    }
}
