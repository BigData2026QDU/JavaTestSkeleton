import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试模板 —— 需要 H2 数据库的测试
 *
 * 命名规则：类名以 IT 结尾（Maven Failsafe 自动识别）
 * 或使用 @Tag("integration") 标记
 *
 * mvn verify          → 运行所有测试
 * mvn test            → 只运行单元测试（跳过 IT）
 * mvn verify -DskipTests → 只运行集成测试
 *
 * 使用时删除本类，创建你自己的集成测试类
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest extends BaseTest {

    @BeforeAll
    static void initDatabase() throws SQLException {
        // 创建测试表
        try (Statement stmt = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "").createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS test_user (
                    id      INT AUTO_INCREMENT PRIMARY KEY,
                    name    VARCHAR(100) NOT NULL,
                    email   VARCHAR(200),
                    active  BOOLEAN DEFAULT TRUE
                )
            """);
        }
    }

    @AfterAll
    static void cleanupDatabase() throws SQLException {
        try (Statement stmt = DriverManager.getConnection(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "").createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS test_user");
        }
    }

    // ========================================
    // 数据库 CRUD 测试
    // ========================================

    @Test
    @Order(1)
    @DisplayName("INSERT —— 插入数据")
    void insertData() throws SQLException {
        executeSql("INSERT INTO test_user (name, email) VALUES ('张三', 'zhangsan@test.com')");
        executeSql("INSERT INTO test_user (name, email) VALUES ('李四', 'lisi@test.com')");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    @Order(2)
    @DisplayName("SELECT —— 查询数据")
    void selectData() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_user WHERE name = '张三'")) {
            assertTrue(rs.next());
            assertEquals("张三", rs.getString("name"));
            assertEquals("zhangsan@test.com", rs.getString("email"));
            assertTrue(rs.getBoolean("active"));
            assertFalse(rs.next());
        }
    }

    @Test
    @Order(3)
    @DisplayName("UPDATE —— 更新数据")
    void updateData() throws SQLException {
        executeSql("UPDATE test_user SET email = 'new@test.com' WHERE name = '张三'");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email FROM test_user WHERE name = '张三'")) {
            assertTrue(rs.next());
            assertEquals("new@test.com", rs.getString("email"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("DELETE —— 删除数据")
    void deleteData() throws SQLException {
        executeSql("DELETE FROM test_user WHERE name = '李四'");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    // ========================================
    // 事务测试
    // ========================================

    @Test
    @Order(5)
    @DisplayName("事务 —— 提交")
    void transactionCommit() throws SQLException {
        conn.setAutoCommit(false);
        try {
            conn.createStatement().executeUpdate(
                "INSERT INTO test_user (name, email) VALUES ('王五', 'wangwu@test.com')");
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user WHERE name = '王五'")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    @Order(6)
    @DisplayName("事务 —— 回滚")
    void transactionRollback() throws SQLException {
        conn.setAutoCommit(false);
        try {
            conn.createStatement().executeUpdate(
                "INSERT INTO test_user (name, email) VALUES ('临时', 'temp@test.com')");
            conn.rollback();
        } catch (Exception e) {
            conn.rollback();
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_user WHERE name = '临时'")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }
    }

    // ========================================
    // 边界测试
    // ========================================

    @Test
    @Order(7)
    @DisplayName("边界 —— 空表查询")
    void emptyTableQuery() throws SQLException {
        executeSql("DELETE FROM test_user");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM test_user")) {
            assertFalse(rs.next(), "空表应无结果");
        }
    }

    @Test
    @Order(8)
    @DisplayName("边界 —— NULL 值处理")
    void nullValueHandling() throws SQLException {
        executeSql("INSERT INTO test_user (name, email) VALUES ('空邮箱', NULL)");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email FROM test_user WHERE name = '空邮箱'")) {
            assertTrue(rs.next());
            assertNull(rs.getString("email"));
        }
    }

    // ========================================
    // 临时目录测试
    // ========================================

    @Test
    @Order(9)
    @DisplayName("临时目录 —— 文件读写")
    void tempDirectoryUsage() throws Exception {
        java.nio.Path file = createTempFile("test.txt", "hello world");
        assertTrue(java.nio.Files.exists(file));
        assertEquals("hello world", java.nio.Files.readString(file));
    }
}
