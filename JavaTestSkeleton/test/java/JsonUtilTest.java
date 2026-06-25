import org.example.Tool.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        PrintWriter writer = new PrintWriter(body);
        when(response.getWriter()).thenReturn(writer);
        SampleItem item = new SampleItem("gamma", 7, LocalDate.of(2026, 6, 25));

        JsonUtil.writeJsonResponse(response, item);
        writer.flush();

        verify(response).setContentType("application/json;charset=UTF-8");
        assertThat(body.toString()).contains("\"name\":\"gamma\"");
        assertThat(body.toString()).contains("\"count\":7");
    }
}
