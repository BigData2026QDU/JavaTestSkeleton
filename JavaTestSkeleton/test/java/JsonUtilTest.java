import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

@DisplayName("JsonUtil 工具类测试")
public class JsonUtilTest {

    @Test
    @DisplayName("对象序列化为 JSON 字符串")
    void testToJson() {
        TestItem item = new TestItem();
        item.setId(1);
        item.setName("测试项目");
        item.setActive(true);

        String json = JsonUtil.toJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"测试项目\""));
        assertTrue(json.contains("\"active\":true"));
    }

    @Test
    @DisplayName("JSON 字符串反序列化为对象")
    void testFromJson() {
        String json = "{\"id\":1,\"name\":\"测试项目\",\"active\":true}";

        TestItem item = JsonUtil.fromJson(json, TestItem.class);

        assertNotNull(item);
        assertEquals(1, item.getId());
        assertEquals("测试项目", item.getName());
        assertTrue(item.isActive());
    }

    @Test
    @DisplayName("null 值不序列化")
    void testNullValuesNotSerialized() {
        TestItem item = new TestItem();
        item.setId(1);
        // name 和 active 为 null

        String json = JsonUtil.toJson(item);

        assertNotNull(json);
        assertTrue(json.contains("\"id\":1"));
        assertFalse(json.contains("\"name\""));
        assertFalse(json.contains("\"active\""));
    }

    @Test
    @DisplayName("日期时间格式化")
    void testDateTimeFormat() {
        String json = "{\"timestamp\":\"2026-06-25 10:30:00\"}";

        TestItem item = JsonUtil.fromJson(json, TestItem.class);

        assertNotNull(item);
    }

    @Test
    @DisplayName("JSON 反序列化失败抛出异常")
    void testFromJsonInvalidJson() {
        String invalidJson = "not a json";

        assertThrows(RuntimeException.class, () -> {
            JsonUtil.fromJson(invalidJson, TestItem.class);
        });
    }

    @Test
    @DisplayName("空对象序列化")
    void testEmptyObjectToJson() {
        TestItem item = new TestItem();

        String json = JsonUtil.toJson(item);

        assertNotNull(json);
        assertEquals("{}", json);
    }
}
