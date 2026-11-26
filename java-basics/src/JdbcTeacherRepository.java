import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// 连接由调用方创建、提交/回滚和关闭；仓库只关闭自己创建的语句与结果集。
public class JdbcTeacherRepository implements TeacherRepository {
    private final Connection connection;

    public JdbcTeacherRepository(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("连接不能为 null");
        }
        this.connection = connection;
    }

    @Override
    public Teacher findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("教师 ID 必须大于 0");
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name FROM teacher WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? new Teacher(rows.getLong("id"), rows.getString("name")) : null;
            }
        } catch (SQLException cause) {
            // 原接口没有声明受检异常，包装时保留 cause，不把数据库故障伪装成无结果。
            throw new IllegalStateException("查询教师失败", cause);
        }
    }

    public long add(String number, String name) throws SQLException {
        if (number == null || number.trim().isEmpty() || number.length() > 32) {
            throw new IllegalArgumentException("教师编号不能为空且最多 32 个字符");
        }
        // 复用姓名规则；这里的临时 ID 不是要写入数据库的 ID。
        String validName = new Teacher(1L, name).getName();
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO teacher (teacher_no, name) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, number.trim());
            statement.setString(2, validName);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("新增教师影响行数不为 1");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("未取得新增教师主键");
                }
                return keys.getLong(1);
            }
        }
    }

    public void rename(long id, String name) throws SQLException {
        Teacher teacher = findById(id);
        if (teacher == null) {
            throw new TeacherNotFoundException(id);
        }
        teacher.rename(name);
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE teacher SET name = ? WHERE id = ?")) {
            statement.setString(1, teacher.getName());
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}
