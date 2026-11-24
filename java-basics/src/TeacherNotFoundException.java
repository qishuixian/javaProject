// 业务异常：查询动作本身正常执行，只是没有符合条件的教师。
public class TeacherNotFoundException extends RuntimeException {
    public TeacherNotFoundException(long id) {
        super("教师不存在：" + id);
    }
}
