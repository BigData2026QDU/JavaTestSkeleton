import org.bigdata.tool.HibernateUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * HibernateUtil 集成测试
 * 使用 H2 内存数据库
 */
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HibernateUtilIT {

    @BeforeAll
    static void init() {
        // 确保 SessionFactory 已初始化
        HibernateUtil.getSessionFactory();
    }

    @AfterAll
    static void cleanup() {
        HibernateUtil.shutdown();
    }

    // ========================================
    // SessionFactory
    // ========================================

    @Test
    @Order(1)
    @DisplayName("SessionFactory 非空且未关闭")
    void sessionFactoryNotNull() {
        var sf = HibernateUtil.getSessionFactory();
        assertNotNull(sf);
        assertFalse(sf.isClosed());
    }

    // ========================================
    // save + findById
    // ========================================

    @Test
    @Order(2)
    @DisplayName("save 后 findById 能查到")
    void saveAndFindById() {
        TestItem item = new TestItem("test_save", 100);
        HibernateUtil.save(item);

        assertNotNull(item.getId(), "save 后 id 应被填充");

        TestItem found = HibernateUtil.findById(TestItem.class, item.getId());
        assertNotNull(found);
        assertEquals("test_save", found.getName());
        assertEquals(100, found.getValue());
    }

    // ========================================
    // findAll
    // ========================================

    @Test
    @Order(3)
    @DisplayName("findAll 返回所有实体")
    void findAll() {
        // 确保有数据
        HibernateUtil.save(new TestItem("findall_a", 1));
        HibernateUtil.save(new TestItem("findall_b", 2));

        List<TestItem> items = HibernateUtil.findAll(TestItem.class);
        assertThat(items).isNotEmpty();
        assertThat(items).anyMatch(i -> "findall_a".equals(i.getName()));
        assertThat(items).anyMatch(i -> "findall_b".equals(i.getName()));
    }

    // ========================================
    // update
    // ========================================

    @Test
    @Order(4)
    @DisplayName("update 修改实体")
    void updateEntity() {
        TestItem item = new TestItem("update_test", 10);
        HibernateUtil.save(item);

        item.setValue(99);
        HibernateUtil.update(item);

        TestItem found = HibernateUtil.findById(TestItem.class, item.getId());
        assertEquals(99, found.getValue());
    }

    // ========================================
    // delete
    // ========================================

    @Test
    @Order(5)
    @DisplayName("delete 删除实体")
    void deleteEntity() {
        TestItem item = new TestItem("delete_test", 0);
        HibernateUtil.save(item);
        Long id = item.getId();

        HibernateUtil.delete(item);

        TestItem found = HibernateUtil.findById(TestItem.class, id);
        assertNull(found, "删除后应查不到");
    }

    // ========================================
    // executeHQL
    // ========================================

    @Test
    @Order(6)
    @DisplayName("executeHQL 参数化查询")
    void executeHQL() {
        HibernateUtil.save(new TestItem("hql_a", 10));
        HibernateUtil.save(new TestItem("hql_b", 20));

        List<TestItem> result = HibernateUtil.executeHQL(
            "FROM TestItem WHERE value > :val", TestItem.class, 15
        );

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(i -> i.getValue() > 15);
    }

    // ========================================
    // executeUpdate
    // ========================================

    @Test
    @Order(7)
    @DisplayName("executeUpdate 批量更新")
    void executeUpdate() {
        HibernateUtil.save(new TestItem("upd_a", 1));
        HibernateUtil.save(new TestItem("upd_b", 1));

        int updated = HibernateUtil.executeUpdate(
            "UPDATE TestItem SET value = :val WHERE name LIKE :prefix", 999, "upd_%"
        );

        assertTrue(updated >= 2, "应至少更新 2 行");
    }

    // ========================================
    // executeInTransaction 回滚
    // ========================================

    @Test
    @Order(8)
    @DisplayName("executeInTransaction 异常时回滚")
    void transactionRollback() {
        long countBefore = HibernateUtil.findAll(TestItem.class).size();

        try {
            HibernateUtil.executeInTransaction(session -> {
                session.persist(new TestItem("rollback_test", 0));
                throw new RuntimeException("模拟异常");
            });
        } catch (RuntimeException ignored) {}

        long countAfter = HibernateUtil.findAll(TestItem.class).size();
        assertEquals(countBefore, countAfter, "回滚后数据量应不变");
    }

    // ========================================
    // executeQuery
    // ========================================

    @Test
    @Order(9)
    @DisplayName("executeQuery 只读查询")
    void executeQuery() {
        HibernateUtil.save(new TestItem("query_test", 42));

        TestItem result = HibernateUtil.executeQuery(session ->
            session.createQuery("FROM TestItem WHERE name = :name", TestItem.class)
                .setParameter("name", "query_test")
                .uniqueResult()
        );

        assertNotNull(result);
        assertEquals(42, result.getValue());
    }

    // ========================================
    // 边界：空结果
    // ========================================

    @Test
    @Order(10)
    @DisplayName("findById 不存在的 id 返回 null")
    void findByIdNotFound() {
        TestItem found = HibernateUtil.findById(TestItem.class, 999999L);
        assertNull(found);
    }

    @Test
    @Order(11)
    @DisplayName("executeHQL 无匹配结果返回空列表")
    void hqlNoResult() {
        List<TestItem> result = HibernateUtil.executeHQL(
            "FROM TestItem WHERE value > :val", TestItem.class, 999999
        );
        assertTrue(result.isEmpty());
    }
}
