import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 测试基类 —— 所有测试类继承此类即可获得公共能力
 *
 * 提供：
 *   - H2 内存数据库连接
 *   - 临时目录（JUnit 自动管理）
 *   - 测试配置读取
 *   - 超时断言辅助
 *   - 日志工具
 *
 * 用法：
 *   class MyTest extends BaseTest {
 *       @Test
 *       void example() {
 *           logger.info("可以使用 this.conn 获取数据库连接");
 *           logger.info("可以使用 this.tmpDir 获取临时目录");
 *       }
 *   }
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BaseTest {

    /** 测试日志器，子类直接用 */
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /** 测试配置 */
    protected Properties testConfig;

    /** H2 数据库连接（需要数据库的测试用） */
    protected Connection conn;

    /** JUnit 5 自动管理的临时目录 */
    @TempDir
    protected Path tmpDir;

    /**
     * 初始化：每个测试方法执行前调用
     * 子类可以覆盖，但必须调用 super.setUp()
     */
    @BeforeEach
    void setUp() throws Exception {
        testConfig = loadTestConfig();
        conn = createTestConnection();
    }

    /**
     * 清理：每个测试方法执行后调用
     * 子类可以覆盖，但必须调用 super.tearDown()
     */
    @AfterEach
    void tearDown() throws Exception {
        closeConnection();
    }

    // ========================================
    // 数据库辅助
    // ========================================

    /**
     * 创建 H2 内存数据库连接
     */
    protected Connection createTestConnection() throws SQLException {
        String url = testConfig.getProperty("test.db.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        String user = testConfig.getProperty("test.db.username", "sa");
        String password = testConfig.getProperty("test.db.password", "");
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * 在当前连接上执行 SQL
     */
    protected void executeSql(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 在当前连接上执行多条 SQL
     */
    protected void executeSql(String... sqls) throws SQLException {
        for (String sql : sqls) {
            executeSql(sql);
        }
    }

    /**
     * 关闭数据库连接
     */
    protected void closeConnection() {
        if (conn != null && !conn.isClosed()) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.warning("关闭数据库连接失败: " + e.getMessage());
            }
        }
    }

    // ========================================
    // 文件辅助
    // ========================================

    /**
     * 在临时目录下创建文件
     */
    protected Path createTempFile(String name, String content) throws IOException {
        Path file = tmpDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    /**
     * 在临时目录下创建子目录
     */
    protected Path createTempDir(String name) throws IOException {
        Path dir = tmpDir.resolve(name);
        Files.createDirectories(dir);
        return dir;
    }

    // ========================================
    // 超时辅助
    // ========================================

    /**
     * 断言操作在指定毫秒内完成
     */
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

    /**
     * 断言操作超时
     */
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

    // ========================================
    // 配置读取
    // ========================================

    /**
     * 加载 test.properties
     */
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

    /**
     * 获取配置值（带默认值）
     */
    protected String getConfig(String key, String defaultValue) {
        return testConfig.getProperty(key, defaultValue);
    }

    /**
     * 获取配置值（int）
     */
    protected int getConfigInt(String key, int defaultValue) {
        String val = testConfig.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ========================================
    // 函数式接口
    // ========================================

    @FunctionalInterface
    public interface ExecutableTask {
        void run();
    }
}
