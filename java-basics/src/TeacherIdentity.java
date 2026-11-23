// 本课约定：在同一 ID 命名空间内，相同 ID 表示同一个教师身份。
// final 类避免子类改变相等规则；不修改前几课的 Teacher 类。
public final class TeacherIdentity {
    private final long id;

    public TeacherIdentity(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("教师 ID 必须大于 0");
        }
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TeacherIdentity)) {
            return false;
        }
        TeacherIdentity that = (TeacherIdentity) other;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
