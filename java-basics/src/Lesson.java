// 课节关联一个教师对象，而不是单独保存一份教师姓名。
public class Lesson {
    private final String title;
    private final Teacher teacher;
    private int minutes;

    public Lesson(String title, Teacher teacher, int minutes) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("课节名称不能为空");
        }
        if (teacher == null) {
            throw new IllegalArgumentException("课节必须关联教师");
        }
        this.title = title.trim();
        this.teacher = teacher;
        changeMinutes(minutes);
    }

    public String getTitle() {
        return title;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public int getMinutes() {
        return minutes;
    }

    public void changeMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("课节时长必须大于 0");
        }
        this.minutes = minutes;
    }
}
