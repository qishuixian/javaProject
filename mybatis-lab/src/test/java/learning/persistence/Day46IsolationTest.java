package learning.persistence;

import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// 每个测试使用独立 H2 数据库；两个真实 JDBC 连接，不使用测试外层事务。
class Day46IsolationTest {
    private String database() throws SQLException {
        String url = "jdbc:h2:mem:day46_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=300";
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE account (id INT PRIMARY KEY, balance INT NOT NULL)");
            statement.executeUpdate("INSERT INTO account VALUES (1, 100)");
        }
        return url;
    }

    private Connection transaction(String url, int isolation) throws SQLException {
        Connection connection = DriverManager.getConnection(url, "sa", "");
        connection.setTransactionIsolation(isolation);
        connection.setAutoCommit(false);
        return connection;
    }

    private int read(Connection connection, boolean lock) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT balance FROM account WHERE id=1" + (lock ? " FOR UPDATE" : ""))) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private void write(Connection connection, int value) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE account SET balance=? WHERE id=1")) {
            statement.setInt(1, value);
            assertEquals(1, statement.executeUpdate());
        }
    }

    private void shutdown(String url) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }

    @Test void readCommittedDoesNotSeeUncommittedUpdate() throws Exception {
        String url = database();
        try (Connection a = transaction(url, Connection.TRANSACTION_READ_COMMITTED);
             Connection b = transaction(url, Connection.TRANSACTION_READ_COMMITTED)) {
            write(b, 200);
            assertEquals(100, read(a, false));
            b.rollback();
            assertEquals(100, read(a, false));
            a.rollback();
            System.out.println("Day46 RC未提交更新不可见：100 → 100");
        } finally { shutdown(url); }
    }

    @Test void readCommittedCanObserveNonRepeatableRead() throws Exception {
        String url = database();
        try (Connection a = transaction(url, Connection.TRANSACTION_READ_COMMITTED);
             Connection b = transaction(url, Connection.TRANSACTION_READ_COMMITTED)) {
            assertEquals(100, read(a, false));
            write(b, 200); b.commit();
            assertEquals(200, read(a, false));
            a.rollback();
            System.out.println("Day46 RC同一事务两次读：100 → 200");
        } finally { shutdown(url); }
    }

    @Test void repeatableReadKeepsValueUntilTransactionEnds() throws Exception {
        String url = database();
        try (Connection a = transaction(url, Connection.TRANSACTION_REPEATABLE_READ);
             Connection b = transaction(url, Connection.TRANSACTION_READ_COMMITTED)) {
            assertEquals(100, read(a, false));
            write(b, 200); b.commit();
            assertEquals(100, read(a, false));
            a.commit();
            assertEquals(200, read(a, false));
            a.rollback();
            System.out.println("Day46 RR同一事务/下一事务：100 → 100 → 200");
        } finally { shutdown(url); }
    }

    @Test void lockingReadBlocksWriterUntilLockIsReleased() throws Exception {
        String url = database();
        try (Connection a = transaction(url, Connection.TRANSACTION_READ_COMMITTED);
             Connection b = transaction(url, Connection.TRANSACTION_READ_COMMITTED)) {
            assertEquals(100, read(a, true));
            SQLException timeout = assertThrows(SQLException.class, () -> write(b, 200));
            // H2 特定错误码，不能拿来判断 MySQL 错误。
            assertEquals(50200, timeout.getErrorCode());
            b.rollback();
            a.commit();
            write(b, 200); b.commit();
            assertEquals(200, read(a, false));
            a.rollback();
            System.out.println("Day46 FOR UPDATE：写入超时 → 释放锁 → 重试成功");
        } finally { shutdown(url); }
    }
}
