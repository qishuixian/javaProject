package learning.persistence;

import java.sql.*;
import java.util.*;
import java.util.regex.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Day47IndexTest {
    private static final String QUERY = "SELECT id, teacher_name FROM teacher "
            + "WHERE subject='Java' ORDER BY subject, id LIMIT 20";

    private String plan(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("EXPLAIN ANALYZE " + QUERY)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private long scans(String plan) {
        // H2 特有的实际扫描计数，不是 MySQL EXPLAIN 的 rows 估计值。
        Matcher matcher = Pattern.compile("scanCount: (\\d+)").matcher(plan);
        assertTrue(matcher.find(), "执行计划缺少 scanCount: " + plan);
        return Long.parseLong(matcher.group(1));
    }

    private List<String> rows(Connection connection) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(QUERY)) {
            while (result.next()) { rows.add(result.getLong(1) + ":" + result.getString(2)); }
        }
        return rows;
    }

    @Test void compositeIndexReducesScansWithoutChangingResults() throws Exception {
        // 独立临时内存库，最后一个连接关闭即删除，不碰前几课的数据。
        String url = "jdbc:h2:mem:day47_" + UUID.randomUUID();
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE teacher (id BIGINT PRIMARY KEY, teacher_name VARCHAR(40), subject VARCHAR(20))");
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO teacher VALUES (?, ?, ?)")) {
                for (int id = 1; id <= 10000; id++) {
                    insert.setInt(1, id); insert.setString(2, "Teacher-" + id);
                    insert.setString(3, id % 100 == 0 ? "Java" : "Other");
                    insert.addBatch();
                }
                insert.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
            statement.execute("ANALYZE");
            String before = plan(connection);
            List<String> expected = rows(connection);
            statement.execute("CREATE INDEX idx_teacher_subject_id ON teacher(subject, id)");
            statement.execute("ANALYZE");
            String after = plan(connection);
            assertEquals(expected, rows(connection));
            assertEquals(20, expected.size());
            assertEquals("100:Teacher-100", expected.get(0));
            assertEquals("2000:Teacher-2000", expected.get(19));
            assertTrue(after.toUpperCase(Locale.ROOT).contains("IDX_TEACHER_SUBJECT_ID"), after);
            assertTrue(scans(after) < scans(before), "索引后扫描量应降低");
            System.out.println("Day47 BEFORE\n" + before);
            System.out.println("Day47 AFTER\n" + after);
            System.out.println("Day47 扫描量：" + scans(before) + " -> " + scans(after));
            System.out.println("Day47 结果一致：20条，首ID=100，末ID=2000");
        }
    }
}
