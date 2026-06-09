import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试数据工厂 —— 快速创建测试数据
 *
 * 用法：
 *   TestItem item = TestDataFactory.createItem("Alice", 90);
 *   List<TestItem> items = TestDataFactory.createItems(5);
 *   TestItem item = new ItemBuilder().name("Bob").value(100).build();
 */
public final class TestDataFactory {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    private TestDataFactory() {}

    // ========================================
    // TestItem 工厂方法
    // ========================================

    /**
     * 创建单个 TestItem
     */
    public static TestItem createItem(String name, Integer value) {
        return new TestItem(name, value);
    }

    /**
     * 创建带自动编号的 TestItem（避免名字重复）
     */
    public static TestItem createItem() {
        long id = COUNTER.incrementAndGet();
        return new TestItem("item_" + id, (int) id);
    }

    /**
     * 创建批量 TestItem
     */
    public static List<TestItem> createItems(int count) {
        List<TestItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long id = COUNTER.incrementAndGet();
            items.add(new TestItem("item_" + id, (int) id));
        }
        return items;
    }

    /**
     * 创建指定范围的 TestItem（value 从 start 到 start+count-1）
     */
    public static List<TestItem> createItems(int count, int startValue) {
        List<TestItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long id = COUNTER.incrementAndGet();
            items.add(new TestItem("item_" + id, startValue + i));
        }
        return items;
    }

    // ========================================
    // Builder 模式
    // ========================================

    /**
     * TestItem Builder
     *
     * 用法：
     *   TestItem item = new ItemBuilder()
     *       .name("Alice")
     *       .value(90)
     *       .build();
     */
    public static class ItemBuilder {
        private String name = "default_" + COUNTER.incrementAndGet();
        private Integer value = 0;

        public ItemBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ItemBuilder value(Integer value) {
            this.value = value;
            return this;
        }

        public TestItem build() {
            return new TestItem(name, value);
        }
    }

    // ========================================
    // 随机数据生成
    // ========================================

    /**
     * 生成随机字符串（字母 + 数字）
     */
    public static String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

    /**
     * 生成随机整数
     */
    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    /**
     * 生成随机 TestItem
     */
    public static TestItem randomItem() {
        return new TestItem(
            "rand_" + randomString(8),
            randomInt(0, 1000)
        );
    }
}
