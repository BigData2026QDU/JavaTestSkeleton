import org.example.Service.DatabaseMetaService;
import org.example.Tool.HibernateUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * DatabaseMetaService 集成测试
 * 使用 H2 内存数据库
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseMetaServiceIT {

    private static DatabaseMetaService metaService;

    @BeforeAll
    static void init() {
        HibernateUtil.getSessionFactory();
        metaService = new DatabaseMetaService();

        // 创建测试表并插入数据
        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("""
                CREATE TABLE IF NOT EXISTS meta_test (
                    id    INT AUTO_INCREMENT PRIMARY KEY,
                    name  VARCHAR(100) NOT NULL,
                    score INT DEFAULT 0
                )
            """).executeUpdate();
        });

        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery(
                "INSERT INTO meta_test (name, score) VALUES ('Alice', 90)"
            ).executeUpdate();
            session.createNativeQuery(
                "INSERT INTO meta_test (name, score) VALUES ('Bob', 85)"
            ).executeUpdate();
            session.createNativeQuery(
                "INSERT INTO meta_test (name, score) VALUES ('Charlie', 95)"
            ).executeUpdate();
        });
    }

    @AfterAll
    static void cleanup() {
        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("DROP TABLE IF EXISTS meta_test").executeUpdate();
        });
        HibernateUtil.shutdown();
    }

    // ========================================
    // getAllTableNames
    // ========================================

    @Test
    @Order(1)
    @DisplayName("getAllTableNames 包含测试表")
    void getAllTableNames() {
        List<String> tables = metaService.getAllTableNames();
        assertThat(tables).isNotEmpty();
        assertThat(tables).anyMatch(t -> t.equalsIgnoreCase("META_TEST"));
    }

    // ========================================
    // getTableColumns
    // ========================================

    @Test
    @Order(2)
    @DisplayName("getTableColumns 返回正确列名")
    void getTableColumns() {
        List<String> columns = metaService.getTableColumns("meta_test");
        assertThat(columns).hasSize(3);
        assertThat(columns).containsExactlyInAnyOrder("ID", "NAME", "SCORE");
    }

    // ========================================
    // previewTable — 全列
    // ========================================

    @Test
    @Order(3)
    @DisplayName("previewTable 全列预览")
    void previewTableAllColumns() {
        Map<String, Object> result = metaService.previewTable("meta_test", 10);

        assertThat(result).containsKey("columns");
        assertThat(result).containsKey("rows");
        assertThat(result).containsKey("tableName");
        assertThat(result).containsKey("rowCount");

        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) result.get("columns");
        assertThat(columns).hasSize(3);

        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");
        assertThat(rows).hasSize(3);

        assertEquals("meta_test", result.get("tableName"));
    }

    // ========================================
    // previewTable — 指定列
    // ========================================

    @Test
    @Order(4)
    @DisplayName("previewTable 指定列预览")
    void previewTableSelectedColumns() {
        Map<String, Object> result = metaService.previewTable(
            "meta_test", 10, List.of("NAME", "SCORE")
        );

        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) result.get("columns");
        assertThat(columns).containsExactly("NAME", "SCORE");
        assertThat(columns).doesNotContain("ID");
    }

    // ========================================
    // previewTable — 行数限制
    // ========================================

    @Test
    @Order(5)
    @DisplayName("previewTable 行数限制生效")
    void previewTableLimit() {
        Map<String, Object> result = metaService.previewTable("meta_test", 2);

        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");
        assertThat(rows).hasSize(2);
    }

    // ========================================
    // previewTable — 空表
    // ========================================

    @Test
    @Order(6)
    @DisplayName("previewTable 空表返回空 rows")
    void previewTableEmpty() {
        // 创建空表
        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("CREATE TABLE IF NOT EXISTS empty_test (id INT)").executeUpdate();
        });

        Map<String, Object> result = metaService.previewTable("empty_test", 10);

        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");
        assertTrue(rows.isEmpty());

        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("DROP TABLE IF EXISTS empty_test").executeUpdate();
        });
    }

    // ========================================
    // 参数校验
    // ========================================

    @Test
    @Order(7)
    @DisplayName("previewTable: null 表名抛异常")
    void previewNullTableName() {
        assertThrows(IllegalArgumentException.class, () ->
            metaService.previewTable(null, 10)
        );
    }

    @Test
    @Order(8)
    @DisplayName("previewTable: 空表名抛异常")
    void previewEmptyTableName() {
        assertThrows(IllegalArgumentException.class, () ->
            metaService.previewTable("", 10)
        );
    }

    @Test
    @Order(9)
    @DisplayName("previewTable: SQL 注入表名被拦截")
    void previewSqlInjectionTableName() {
        assertThrows(IllegalArgumentException.class, () ->
            metaService.previewTable("meta_test; DROP TABLE meta_test", 10)
        );
    }

    @Test
    @Order(10)
    @DisplayName("previewTable: 不存在的列抛异常")
    void previewNonExistentColumn() {
        assertThrows(IllegalArgumentException.class, () ->
            metaService.previewTable("meta_test", 10, List.of("NONEXISTENT"))
        );
    }

    @Test
    @Order(11)
    @DisplayName("getTableColumns: null 表名抛异常")
    void columnsNullTableName() {
        assertThrows(IllegalArgumentException.class, () ->
            metaService.getTableColumns(null)
        );
    }

    // ========================================
    // limit 边界
    // ========================================

    @Test
    @Order(12)
    @DisplayName("previewTable limit 限制在 1-100 范围内")
    void previewLimitBounds() {
        // limit 过小应被修正为 1
        Map<String, Object> result1 = metaService.previewTable("meta_test", 0);
        @SuppressWarnings("unchecked")
        List<List<Object>> rows1 = (List<List<Object>>) result1.get("rows");
        assertThat(rows1).hasSize(1);

        // limit 过大应被修正为 100
        Map<String, Object> result2 = metaService.previewTable("meta_test", 999);
        @SuppressWarnings("unchecked")
        List<List<Object>> rows2 = (List<List<Object>>) result2.get("rows");
        assertThat(rows2).hasSize(3); // 实际只有 3 行
    }
}
