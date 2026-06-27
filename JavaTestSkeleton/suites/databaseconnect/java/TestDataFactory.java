import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class TestDataFactory {

    private static final AtomicLong COUNTER = new AtomicLong(0);

    private TestDataFactory() {
    }

    public static TestItem createItem(String name, Integer value) {
        return new TestItem(name, value);
    }

    public static TestItem createItem() {
        long id = COUNTER.incrementAndGet();
        return new TestItem("item_" + id, (int) id);
    }

    public static List<TestItem> createItems(int count) {
        List<TestItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long id = COUNTER.incrementAndGet();
            items.add(new TestItem("item_" + id, (int) id));
        }
        return items;
    }

    public static List<TestItem> createItems(int count, int startValue) {
        List<TestItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long id = COUNTER.incrementAndGet();
            items.add(new TestItem("item_" + id, startValue + i));
        }
        return items;
    }

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

    public static String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }

    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static TestItem randomItem() {
        return new TestItem(
            "rand_" + randomString(8),
            randomInt(0, 1000)
        );
    }
}
