import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Day28Jdbc {
    private static final String URL = System.getenv().getOrDefault(
            "DAY28_DB_URL", "jdbc:mysql://127.0.0.1:3306/java_learning_day25?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
    private static final String USER = System.getenv().getOrDefault("DAY28_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DAY28_DB_PASSWORD", "123456");

    public static void main(String[] args) {
        long teacherId = args.length == 0 ? 1L : Long.parseLong(args[0]);
        String sql = "SELECT id, teacher_no, name FROM teacher WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, teacherId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("查询教师：" + resultSet.getLong("id") + " / "
                            + resultSet.getString("teacher_no") + " / "
                            + resultSet.getString("name"));
                } else {
                    System.out.println("未找到教师：" + teacherId);
                }
            }
        } catch (SQLException exception) {
            System.out.println("数据库操作失败：" + exception.getMessage());
            System.out.println("SQLState：" + exception.getSQLState());
        }
    }
}
