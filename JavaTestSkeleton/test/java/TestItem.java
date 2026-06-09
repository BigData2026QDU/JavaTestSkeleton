import jakarta.persistence.*;

/**
 * 测试用实体类
 * 用于 HibernateUtil 和 DatabaseMetaService 的集成测试
 */
@Entity
@Table(name = "test_item")
public class TestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "\"value\"")
    private Integer value;

    public TestItem() {}

    public TestItem(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }

    @Override
    public String toString() {
        return "TestItem{id=" + id + ", name='" + name + "', value=" + value + "}";
    }
}
