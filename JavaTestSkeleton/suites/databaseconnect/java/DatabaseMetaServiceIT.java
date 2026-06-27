import org.assertj.core.api.Assertions;
import org.bigdata.service.DatabaseMetaService;
import org.bigdata.tool.HibernateUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseMetaServiceIT {

    private static DatabaseMetaService metaService;

    @BeforeAll
    static void init() {
        HibernateUtil.getSessionFactory();
        metaService = new DatabaseMetaService();

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

    @Test
    @Order(1)
    @DisplayName("getAllTableNames 包含测试表")
    void getAllTableNames() {
        List<String> tables = metaService.getAllTableNames();
        Assertions.assertThat(tables).isNotEmpty();
        Assertions.assertThat(tables).anyMatch(t -> t.equalsIgnoreCase("META_TEST"));
    }

    @Test
    @Order(2)
    @DisplayName("getTableColumns 返回正确列名")
    void getTableColumns() {
        List<String> columns = metaService.getTableColumns("META_TEST");
        Assertions.assertThat(columns).hasSize(3);
        Assertions.assertThat(columns).containsExactlyInAnyOrder("ID", "NAME", "SCORE");
    }

    @Test
    @Order(3)
    @DisplayName("previewTable 全列预览")
    void previewTableAllColumns() {
        Map<String, Object> result = metaService.previewTable("META_TEST", 10);

        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) result.get("columns");
        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");

        Assertions.assertThat(result).containsKeys("columns", "rows", "tableName", "rowCount");
        Assertions.assertThat(columns).hasSize(3);
        Assertions.assertThat(rows).hasSize(3);
        assertEquals("META_TEST", result.get("tableName"));
    }

    @Test
    @Order(4)
    @DisplayName("previewTable 指定列预览")
    void previewTableSelectedColumns() {
        Map<String, Object> result = metaService.previewTable(
            "META_TEST", 10, List.of("NAME", "SCORE")
        );

        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) result.get("columns");
        Assertions.assertThat(columns).containsExactly("NAME", "SCORE");
        Assertions.assertThat(columns).doesNotContain("ID");
    }

    @Test
    @Order(5)
    @DisplayName("previewTable 行数限制生效")
    void previewTableLimit() {
        Map<String, Object> result = metaService.previewTable("META_TEST", 2);

        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");
        Assertions.assertThat(rows).hasSize(2);
    }

    @Test
    @Order(6)
    @DisplayName("previewTable 空表返回空 rows")
    void previewTableEmpty() {
        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("CREATE TABLE IF NOT EXISTS empty_test (id INT)").executeUpdate();
        });

        Map<String, Object> result = metaService.previewTable("EMPTY_TEST", 10);

        @SuppressWarnings("unchecked")
        List<List<Object>> rows = (List<List<Object>>) result.get("rows");
        assertTrue(rows.isEmpty());

        HibernateUtil.executeInTransaction(session -> {
            session.createNativeQuery("DROP TABLE IF EXISTS empty_test").executeUpdate();
        });
    }

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
        assertThrows(RuntimeException.class, () ->
            metaService.previewTable("META_TEST", 10, List.of("NONEXISTENT"))
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

    @Test
    @Order(12)
    @DisplayName("previewTable limit 限制在 1-100 范围内")
    void previewLimitBounds() {
        Map<String, Object> result1 = metaService.previewTable("META_TEST", 0);
        @SuppressWarnings("unchecked")
        List<List<Object>> rows1 = (List<List<Object>>) result1.get("rows");
        Assertions.assertThat(rows1).hasSize(1);

        Map<String, Object> result2 = metaService.previewTable("META_TEST", 999);
        @SuppressWarnings("unchecked")
        List<List<Object>> rows2 = (List<List<Object>>) result2.get("rows");
        Assertions.assertThat(rows2).hasSize(3);
    }
}
