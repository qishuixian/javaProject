import java.util.logging.Level;
import java.util.logging.Logger;

public class Day24Debugging {
    private static final Logger LOGGER = Logger.getLogger(Day24Debugging.class.getName());

    public static Teacher queryTeacher(TeacherRepository repository, String idText) {
        // 断点一：观察字符串输入，尚未执行下面的转换。
        long id = parseTeacherId(idText);
        LOGGER.log(Level.FINE, "解析后的教师 ID：{0}", id);
        // 断点二：Step Into 进入 TeacherDirectory.findById。
        Teacher teacher = repository.findById(id);
        if (teacher == null) {
            LOGGER.log(Level.WARNING, "没有匹配的教师，teacherId={0}", id);
        }
        return teacher;
    }

    private static long parseTeacherId(String idText) {
        try {
            long id = Long.parseLong(idText);
            if (id <= 0) {
                throw new IllegalArgumentException("教师 ID 必须大于 0");
            }
            return id;
        } catch (IllegalArgumentException cause) {
            // 保留原始异常，而不是只复制 message。
            throw new IllegalArgumentException("教师 ID 解析或校验失败", cause);
        }
    }

    private static void handleQuery(TeacherRepository repository, String idText) {
        LOGGER.info("开始教师查询");
        try {
            Teacher teacher = queryTeacher(repository, idText);
            if (teacher != null) {
                LOGGER.log(Level.INFO, "查询成功，teacherId={0}", teacher.getId());
            }
        } catch (IllegalArgumentException cause) {
            IllegalStateException failure =
                    new IllegalStateException("教师查询未完成（教学故障演示）", cause);
            // Throwable 作为第三个参数，输出完整堆栈和 Caused by。
            LOGGER.log(Level.SEVERE, "演示异常：以下错误为预期输入场景", failure);
        }
    }

    public static void main(String[] args) {
        TeacherDirectory directory = new TeacherDirectory(2);
        directory.add(10001L, "张老师");
        directory.add(10002L, "李老师");
        handleQuery(directory, "10002");
        handleQuery(directory, "99999");
        handleQuery(directory, "abc");
        System.out.println("Day24 演示结束：成功、无结果和非法输入均已处理");
    }
}
