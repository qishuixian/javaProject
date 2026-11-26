import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class Day30Persistence {
    private static Connection openConnection() throws SQLException {
        String password = System.getenv("DAY28_DB_PASSWORD");
        if (password == null) {
            throw new IllegalArgumentException("请在 IDEA 配置 DAY28_DB_PASSWORD");
        }
        return DriverManager.getConnection(
                System.getenv().getOrDefault("DAY28_DB_URL",
                        "jdbc:mysql://127.0.0.1:3306/java_learning_day25?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"),
                System.getenv().getOrDefault("DAY28_DB_USER", "root"), password);
    }

    // 和 Day7 一样只依赖查询接口，不关心具体存储方式。
    private static void verifyName(TeacherRepository repository, long id, String expected) {
        Teacher teacher = repository.findById(id);
        if (teacher == null || !expected.equals(teacher.getName())) {
            throw new IllegalStateException("教师查询结果不符合预期");
        }
    }

    public static void main(String[] args) throws SQLException {
        String number = "D30" + UUID.randomUUID().toString().replace("-", "").substring(0, 29);
        System.out.println("练习编号：" + number);
        long id;
        try (Connection connection = openConnection()) {
            if (!"java_learning_day25".equals(connection.getCatalog())) {
                throw new IllegalArgumentException("只允许 java_learning_day25 学习库");
            }
            connection.setAutoCommit(false);
            JdbcTeacherRepository repository = new JdbcTeacherRepository(connection);
            try {
                id = repository.add(number, " 张老师 ");
                verifyName(repository, id, "张老师");
                System.out.println("新增并查询：通过");

                repository.rename(id, "李老师");
                verifyName(repository, id, "李老师");
                System.out.println("修改后重新查询：通过");

                try {
                    repository.rename(id, "   ");
                    throw new IllegalStateException("空姓名没有被拒绝");
                } catch (IllegalArgumentException expected) {
                    verifyName(repository, id, "李老师");
                    System.out.println("非法改名不改变数据：通过");
                }

                try {
                    repository.add(number, "王老师");
                    throw new IllegalStateException("重复编号没有被拒绝");
                } catch (SQLException expected) {
                    if (expected.getErrorCode() != 1062) {
                        throw expected;
                    }
                    verifyName(repository, id, "李老师");
                    System.out.println("重复编号被唯一约束拒绝：通过");
                }
                // 默认只做回滚实验，不持久提交练习行。
                connection.rollback();
            } catch (SQLException | RuntimeException failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
            connection.setAutoCommit(true);
            if (repository.findById(id) != null) {
                throw new IllegalStateException("回滚后仍找到练习教师");
            }
            System.out.println("回滚后查询为空：通过");
        }

        // 换一条新连接确认无残留，而不是只看原事务的读取结果。
        try (Connection verification = openConnection()) {
            TeacherRepository repository = new JdbcTeacherRepository(verification);
            if (repository.findById(id) != null) {
                throw new IllegalStateException("新连接仍找到练习教师");
            }
            System.out.println("新连接确认无残留：通过");
        }
    }
}
