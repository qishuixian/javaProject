// 教师对象：字段私有，只通过有校验的方法修改姓名。
public class Teacher {
    private final long id;
    private String name;

    public Teacher(long id, String name) {
        if (id <= 0) {
            throw new IllegalArgumentException("教师 ID 必须大于 0");
        }
        this.id = id;
        rename(name);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("教师姓名不能为空");
        }
        // 校验通过后再赋值，失败时原姓名不变。
        this.name = name.trim();
    }
}
