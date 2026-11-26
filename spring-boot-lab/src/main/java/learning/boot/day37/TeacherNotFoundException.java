package learning.boot.day37;

// 可预期的业务情况，不是程序崩溃。
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException() {
        super("教师不存在");
    }
}
