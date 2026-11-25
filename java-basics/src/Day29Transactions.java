import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Day29Transactions {
    private static Connection openConnection() throws SQLException {
        String password = System.getenv("DAY28_DB_PASSWORD");
        if (password == null) {
            throw new IllegalArgumentException("请在 IDEA 运行配置中设置 DAY28_DB_PASSWORD");
        }
        return DriverManager.getConnection(
                System.getenv().getOrDefault("DAY28_DB_URL",
                        "jdbc:mysql://127.0.0.1:3306/java_learning_day25?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"),
                System.getenv().getOrDefault("DAY28_DB_USER", "root"), password);
    }

    private static void insertTeacher(Connection connection, String number) throws SQLException {
        String sql = "INSERT INTO teacher (teacher_no, name) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            statement.setString(2, "Day29事务练习老师");
            if (statement.executeUpdate() != 1) {
                throw new SQLException("预期插入一行");
            }
        }
    }

    private static int countTeacher(Connection connection, String number) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM teacher WHERE teacher_no = ?")) {
            statement.setString(1, number);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        // 编号不超过 teacher_no 的 32 字符限制，避免重复运行依赖固定数据。
        String number = "D29" + UUID.randomUUID().toString().replace("-", "").substring(0, 29);
        System.out.println("本次练习编号：" + number);
        try (Connection connection = openConnection()) {
            if (!"java_learning_day25".equals(connection.getCatalog())) {
                throw new IllegalArgumentException("本课只允许使用 java_learning_day25 学习库");
            }
            connection.setAutoCommit(false);
            boolean firstInserted = false;
            try {
                insertTeacher(connection, number);
                firstInserted = true;
                System.out.println("第一次写入后，当前连接可见数量：" + countTeacher(connection, number));
                // 同一个业务编号再次插入，触发 Day25 的唯一约束。
                insertTeacher(connection, number);
                throw new IllegalStateException("第二次写入没有失败，请检查唯一约束");
            } catch (SQLException failure) {
                // rollback 失败时保留原异常，不能继续宣称回滚成功。
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                    throw failure;
                }
                if (!firstInserted || failure.getErrorCode() != 1062) {
                    throw failure;
                }
                System.out.println("第二次写入被唯一约束拒绝，MySQL 错误码：1062");
                System.out.println("已回滚整个事务");
            } catch (RuntimeException failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
            // 只在明确回滚后恢复；不能在失败的事务尚未回滚时打开自动提交。
            connection.setAutoCommit(true);
            int remaining = countTeacher(connection, number);
            System.out.println("回滚后教师数量：" + remaining);
            if (remaining != 0) {
                throw new IllegalStateException("回滚后仍有数据，验证失败");
            }
            System.out.println("验证通过：第一条写入也没有保留");
        }
    }
}
