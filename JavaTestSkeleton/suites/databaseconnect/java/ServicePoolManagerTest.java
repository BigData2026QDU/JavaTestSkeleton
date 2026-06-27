import org.bigdata.tool.ServicePoolManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicePoolManagerTest {

    private ServicePoolManager pool;

    @BeforeEach
    void setUp() {
        pool = ServicePoolManager.getInstance();
        pool.shutdown();
    }

    @AfterAll
    static void tearDown() {
        ServicePoolManager.getInstance().shutdown();
    }

    @Test
    @DisplayName("单例：getInstance 返回同一实例")
    void singleton() {
        ServicePoolManager a = ServicePoolManager.getInstance();
        ServicePoolManager b = ServicePoolManager.getInstance();
        assertSame(a, b, "getInstance 应返回同一实例");
    }

    @Test
    @DisplayName("注册服务并借出")
    void registerAndBorrow() {
        pool.registerService(StringBuilder.class, StringBuilder::new, 4);

        StringBuilder sb = pool.borrowService(StringBuilder.class);
        assertNotNull(sb, "借出的对象不应为 null");
        pool.returnService(StringBuilder.class, sb);
    }

    @Test
    @DisplayName("借出的对象是独立实例")
    void borrowCreatesDistinctInstances() {
        pool.registerService(StringBuilder.class, StringBuilder::new, 4);

        StringBuilder a = pool.borrowService(StringBuilder.class);
        StringBuilder b = pool.borrowService(StringBuilder.class);
        assertNotSame(a, b, "两次借出应返回不同实例");

        pool.returnService(StringBuilder.class, a);
        pool.returnService(StringBuilder.class, b);
    }

    @Test
    @DisplayName("借出后归还，再借出同一个对象")
    void borrowReturnReuse() {
        pool.registerService(StringBuilder.class, StringBuilder::new, 4);

        StringBuilder first = pool.borrowService(StringBuilder.class);
        int identityHash = System.identityHashCode(first);
        pool.returnService(StringBuilder.class, first);

        StringBuilder second = pool.borrowService(StringBuilder.class);
        assertEquals(identityHash, System.identityHashCode(second),
            "归还后再借出应复用同一对象");

        pool.returnService(StringBuilder.class, second);
    }

    @Test
    @DisplayName("重复注册抛出 IllegalStateException")
    void duplicateRegisterThrows() {
        pool.registerService(Integer.class, () -> 42, 2);

        assertThrows(IllegalStateException.class, () ->
            pool.registerService(Integer.class, () -> 99, 2)
        );
    }

    @Test
    @DisplayName("registerService: creator 为 null 抛 NPE")
    void registerNullCreatorThrows() {
        assertThrows(NullPointerException.class, () ->
            pool.registerService(String.class, null)
        );
    }

    @Test
    @DisplayName("registerService: poolSize <= 0 抛 IAE")
    void registerInvalidPoolSizeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            pool.registerService(String.class, String::new, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            pool.registerService(String.class, String::new, -1)
        );
    }

    @Test
    @DisplayName("borrowService: 未注册的类抛 ISE")
    void borrowUnregisteredThrows() {
        assertThrows(IllegalStateException.class, () ->
            pool.borrowService(CopyOnWriteArrayList.class)
        );
    }

    @Test
    @DisplayName("returnService: null 抛 NPE")
    void returnNullThrows() {
        pool.registerService(String.class, String::new, 2);
        assertThrows(NullPointerException.class, () ->
            pool.returnService(String.class, null)
        );
    }

    @Test
    @DisplayName("销毁时调用自定义 destroyer")
    void destroyerCallback() {
        AtomicInteger destroyCount = new AtomicInteger(0);

        pool.registerService(StringBuilder.class, StringBuilder::new, 2, sb ->
            destroyCount.incrementAndGet()
        );

        StringBuilder a = pool.borrowService(StringBuilder.class);
        pool.invalidateService(StringBuilder.class, a);

        assertEquals(1, destroyCount.get(), "destroyer 应被调用一次");
    }

    @Test
    @DisplayName("AutoCloseable 对象自动关闭")
    void autoCloseableDestroy() {
        class CloseableThing implements AutoCloseable {
            boolean closed = false;

            @Override
            public void close() {
                closed = true;
            }
        }

        pool.registerService(CloseableThing.class, CloseableThing::new, 2);

        CloseableThing thing = pool.borrowService(CloseableThing.class);
        pool.invalidateService(CloseableThing.class, thing);

        assertTrue(thing.closed, "AutoCloseable 应被自动关闭");
    }

    @Test
    @DisplayName("isRegistered 正确判断")
    void isRegisteredCheck() {
        pool.registerService(Long.class, () -> 0L, 2);

        assertTrue(pool.isRegistered(Long.class));
        assertFalse(pool.isRegistered(Float.class));
    }

    @Test
    @DisplayName("shutdown 后借出抛异常")
    void shutdownAndBorrowThrows() {
        pool.registerService(Character.class, () -> 'a', 2);
        pool.shutdown();

        assertThrows(IllegalStateException.class, () ->
            pool.borrowService(Character.class)
        );
    }

    @Test
    @DisplayName("并发借还不丢失对象")
    void concurrentBorrowReturn() throws Exception {
        pool.registerService(StringBuilder.class, StringBuilder::new, 8);

        int threadCount = 8;
        int iterations = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await();
                    for (int j = 0; j < iterations; j++) {
                        StringBuilder sb = pool.borrowService(StringBuilder.class);
                        assertNotNull(sb);
                        pool.returnService(StringBuilder.class, sb);
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(0, errors.get(), "并发操作不应有异常");
    }
}
