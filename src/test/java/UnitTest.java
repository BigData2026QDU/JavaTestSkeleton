import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 单元测试模板 —— 无需数据库，纯逻辑测试
 *
 * 覆盖：
 *   - 基本断言（JUnit 5）
 *   - AssertJ 流式断言
 *   - 参数化测试
 *   - Mockito mock
 *   - 超时检测
 *   - 异常检测
 *
 * 使用时删除本类，创建你自己的测试类
 */
class UnitTest {

    // ========================================
    // 基本断言
    // ========================================

    @Test
    @Order(1)
    @DisplayName("JUnit 5 基本断言示例")
    void basicAssertions() {
        // assertEquals
        assertEquals(2, 1 + 1, "1 + 1 应该等于 2");

        // assertTrue / assertFalse
        assertTrue("hello".startsWith("h"));
        assertFalse("hello".startsWith("x"));

        // assertNull / assertNotNull
        String s = null;
        assertNull(s);
        assertNotNull("not null");

        // assertAll —— 分组断言，全部执行后才报错
        assertAll("字符串操作",
            () -> assertEquals("HELLO", "hello".toUpperCase()),
            () -> assertEquals(5, "hello".length()),
            () -> assertTrue("hello".contains("ell"))
        );
    }

    // ========================================
    // AssertJ 流式断言
    // ========================================

    @Test
    @Order(2)
    @DisplayName("AssertJ 流式断言示例")
    void assertjAssertions() {
        // 字符串
        assertThat("hello")
            .startsWith("hel")
            .endsWith("llo")
            .hasSize(5)
            .isNotEmpty();

        // 集合
        assertThat(java.util.List.of(1, 2, 3))
            .hasSize(3)
            .contains(1, 2)
            .doesNotContain(4)
            .isSorted();

        // 数字
        assertThat(42)
            .isGreaterThan(0)
            .isLessThan(100)
            .isEven();
    }

    // ========================================
    // 参数化测试
    // ========================================

    @ParameterizedTest
    @Order(3)
    @DisplayName("参数化测试 —— 多组输入")
    @ValueSource(strings = {"racecar", "radar", "level"})
    void palindromes(String input) {
        String reversed = new StringBuilder(input).reverse().toString();
        assertEquals(input, reversed, input + " 应该是回文");
    }

    @ParameterizedTest
    @Order(4)
    @DisplayName("参数化测试 —— CSV 输入")
    @CsvSource({
        "1, 1, 2",
        "2, 3, 5",
        "10, 20, 30",
        "0, 0, 0"
    })
    void addition(int a, int b, int expected) {
        assertEquals(expected, a + b);
    }

    // ========================================
    // Mockito mock
    // ========================================

    @Test
    @Order(5)
    @DisplayName("Mockito mock 示例")
    void mockExample() {
        // 创建 mock
        java.util.List<String> mockList = mock(java.util.List.class);

        // 行为定义
        when(mockList.size()).thenReturn(42);
        when(mockList.get(0)).thenReturn("hello");

        // 验证
        assertEquals(42, mockList.size());
        assertEquals("hello", mockList.get(0));

        // 验证调用次数
        verify(mockList, times(1)).size();
        verify(mockList, times(1)).get(0);
        verifyNoMoreInteractions(mockList);
    }

    // ========================================
    // 异常检测
    // ========================================

    @Test
    @Order(6)
    @DisplayName("异常检测示例")
    void exceptionDetection() {
        // assertThrows
        assertThrows(IllegalArgumentException.class, () -> {
            Integer.parseInt("not a number");
        });

        // AssertJ 异常
        assertThatThrownBy(() -> {
            int[] arr = new int[0];
            arr[1] = 1;
        })
            .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    // ========================================
    // 超时检测
    // ========================================

    @Test
    @Order(7)
    @DisplayName("超时检测示例")
    void timeoutDetection() {
        assertTimeout(Duration.ofSeconds(1), () -> {
            // 模拟快速操作
            int sum = 0;
            for (int i = 0; i < 1000; i++) sum += i;
            assertTrue(sum > 0);
        });
    }

    // ========================================
    // 嵌套测试
    // ========================================

    @Nested
    @DisplayName("嵌套测试组")
    class NestedTests {

        @Test
        @DisplayName("A. 组内测试 1")
        void test1() {
            assertThat(1).isPositive();
        }

        @Test
        @DisplayName("B. 组内测试 2")
        void test2() {
            assertThat("test").isNotBlank();
        }
    }
}
