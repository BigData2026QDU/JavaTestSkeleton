import org.example.Tool.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonUtilTest {

    static class SampleItem {
        public String name;
        public int count;
        public LocalDate createdOn;

        SampleItem() {
        }

        SampleItem(String name, int count, LocalDate createdOn) {
            this.name = name;
            this.count = count;
            this.createdOn = createdOn;
        }
    }

    @Test
    @DisplayName("toJson serializes object fields")
    void toJson_withPojo_shouldSerializeFields() {
        SampleItem item = new SampleItem("alpha", 3, LocalDate.of(2026, 6, 25));

        String json = JsonUtil.toJson(item);

        assertThat(json).contains("\"name\":\"alpha\"");
        assertThat(json).contains("\"count\":3");
        assertThat(json).contains("\"createdOn\":\"2026-06-25\"");
    }

    @Test
    @DisplayName("fromJson deserializes object fields")
    void fromJson_withValidJson_shouldDeserializeFields() {
        String json = "{\"name\":\"beta\",\"count\":5,\"createdOn\":\"2026-06-25\"}";

        SampleItem item = JsonUtil.fromJson(json, SampleItem.class);

        assertThat(item.name).isEqualTo("beta");
        assertThat(item.count).isEqualTo(5);
        assertThat(item.createdOn).isEqualTo(LocalDate.of(2026, 6, 25));
    }

    @Test
    @DisplayName("fromJson wraps malformed JSON errors")
    void fromJson_withInvalidJson_shouldThrowRuntimeException() {
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> JsonUtil.fromJson("{bad-json", SampleItem.class)
        );

        assertThat(exception).hasMessageContaining("JSON");
        assertThat(exception).hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("writeJsonResponse writes JSON content")
    void writeJsonResponse_withObject_shouldSetContentTypeAndWriteBody() throws Exception {
        CapturingResponse responseHandler = new CapturingResponse();
        HttpServletResponse response = responseHandler.createProxy();
        SampleItem item = new SampleItem("gamma", 7, LocalDate.of(2026, 6, 25));

        JsonUtil.writeJsonResponse(response, item);
        responseHandler.flush();

        assertThat(responseHandler.getContentType()).isEqualTo("application/json;charset=UTF-8");
        assertThat(responseHandler.getBody()).contains("\"name\":\"gamma\"");
        assertThat(responseHandler.getBody()).contains("\"count\":7");
    }

    private static final class CapturingResponse implements InvocationHandler {
        private final StringWriter buffer = new StringWriter();
        private final PrintWriter writer = new PrintWriter(buffer);
        private String contentType;

        HttpServletResponse createProxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                this
            );
        }

        void flush() {
            writer.flush();
        }

        String getBody() {
            return buffer.toString();
        }

        String getContentType() {
            return contentType;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if ("getWriter".equals(methodName)) {
                return writer;
            }
            if ("setContentType".equals(methodName)) {
                contentType = args != null && args.length > 0 ? (String) args[0] : null;
                return null;
            }
            if ("getContentType".equals(methodName)) {
                return contentType;
            }
            if ("flushBuffer".equals(methodName)) {
                writer.flush();
                return null;
            }
            return defaultValue(method.getReturnType());
        }

        private Object defaultValue(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0F;
            }
            if (returnType == double.class) {
                return 0D;
            }
            return null;
        }
    }
}
