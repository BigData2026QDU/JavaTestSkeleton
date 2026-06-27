import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    protected final Logger logger = Logger.getLogger(getClass().getName());
    protected Properties testConfig;
    protected Connection conn;

    @TempDir
    protected Path tmpDir;

    @BeforeEach
    void setUp() throws Exception {
        testConfig = loadTestConfig();
        conn = createTestConnection();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeConnection();
    }

    protected Connection createTestConnection() throws SQLException {
        String url = testConfig.getProperty("test.db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        String user = testConfig.getProperty("test.db.username", "sa");
        String password = testConfig.getProperty("test.db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    protected void executeSql(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    protected void executeSql(String... sqls) throws SQLException {
        for (String sql : sqls) {
            executeSql(sql);
        }
    }

    protected void closeConnection() {
        if (conn == null) {
            return;
        }
        try {
            if (!conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.warning("关闭数据库连接失败: " + e.getMessage());
        }
    }

    protected Path createTempFile(String name, String content) throws IOException {
        Path file = tmpDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    protected Path createTempDir(String name) throws IOException {
        Path dir = tmpDir.resolve(name);
        Files.createDirectories(dir);
        return dir;
    }

    protected void assertCompletesWithin(long millis, ExecutableTask task) {
        long start = System.currentTimeMillis();
        task.run();
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > millis) {
            throw new AssertionError(
                String.format("操作耗时 %dms，超过限制 %dms", elapsed, millis)
            );
        }
    }

    protected void assertTimesOut(long millis, ExecutableTask task) {
        long start = System.currentTimeMillis();
        task.run();
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed < millis) {
            throw new AssertionError(
                String.format("操作耗时 %dms，未达到预期超时 %dms", elapsed, millis)
            );
        }
    }

    private Properties loadTestConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.warning("加载 test.properties 失败: " + e.getMessage());
        }
        return props;
    }

    protected String getConfig(String key, String defaultValue) {
        return testConfig.getProperty(key, defaultValue);
    }

    protected int getConfigInt(String key, int defaultValue) {
        String value = testConfig.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @FunctionalInterface
    public interface ExecutableTask {
        void run();
    }
}
