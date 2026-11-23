// 为演示继承，复用父类的内存查询，增加查询日志。
public class LoggingTeacherRepository extends InMemoryTeacherRepository {
    public LoggingTeacherRepository(Teacher[] teachers) {
        super(teachers);
    }

    @Override
    public Teacher findById(long id) {
        System.out.println("查询日志：teacherId=" + id);
        return super.findById(id);
    }
}
