package com.servicepool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectPoolServicePoolManagerTest {

    private ServicePoolManager manager;

    @BeforeEach
    void setUp() {
        manager = ServicePoolManager.getInstance();
        manager.shutdown();
    }

    @AfterEach
    void tearDown() {
        ServicePoolManager.getInstance().shutdown();
    }

    @Test
    @DisplayName("ObjectPoolModule exposes one shared ServicePoolManager instance")
    void getInstanceReturnsSingleton() {
        ServicePoolManager first = ServicePoolManager.getInstance();
        ServicePoolManager second = ServicePoolManager.getInstance();

        assertSame(first, second);
    }

    @Test
    @DisplayName("ObjectPoolModule registers, borrows, and returns pooled services")
    void registerBorrowAndReturnService() {
        manager.registerService(PooledWidget.class, PooledWidget::new, 2);

        assertTrue(manager.isRegistered(PooledWidget.class));

        PooledWidget widget = manager.borrowService(PooledWidget.class);

        assertNotNull(widget);
        assertEquals("widget", widget.name());

        manager.returnService(PooledWidget.class, widget);
    }

    @Test
    @DisplayName("ObjectPoolModule rejects invalid registrations")
    void invalidRegistrationThrows() {
        assertThrows(NullPointerException.class, () ->
            manager.registerService(PooledWidget.class, null, 1)
        );
        assertThrows(IllegalArgumentException.class, () ->
            manager.registerService(PooledWidget.class, PooledWidget::new, 0)
        );
    }

    @Test
    @DisplayName("ObjectPoolModule rejects duplicate service registration")
    void duplicateRegistrationThrows() {
        manager.registerService(PooledWidget.class, PooledWidget::new, 1);

        assertThrows(IllegalStateException.class, () ->
            manager.registerService(PooledWidget.class, PooledWidget::new, 1)
        );
    }

    @Test
    @DisplayName("ObjectPoolModule reports unregistered services")
    void unregisteredServiceOperationsThrow() {
        assertFalse(manager.isRegistered(PooledWidget.class));

        assertThrows(IllegalStateException.class, () ->
            manager.borrowService(PooledWidget.class)
        );
    }

    @Test
    @DisplayName("ObjectPoolModule invokes custom destroyer on invalidation")
    void invalidateServiceInvokesDestroyer() {
        AtomicInteger destroyed = new AtomicInteger();
        manager.registerService(PooledWidget.class, PooledWidget::new, 2, widget -> {
            Objects.requireNonNull(widget);
            destroyed.incrementAndGet();
        });

        PooledWidget widget = manager.borrowService(PooledWidget.class);
        manager.invalidateService(PooledWidget.class, widget);

        assertEquals(1, destroyed.get());
    }

    @Test
    @DisplayName("ObjectPoolModule closes AutoCloseable services when invalidated")
    void invalidateServiceClosesAutoCloseable() {
        manager.registerService(CloseableWidget.class, CloseableWidget::new, 1);

        CloseableWidget widget = manager.borrowService(CloseableWidget.class);
        manager.invalidateService(CloseableWidget.class, widget);

        assertTrue(widget.closed);
    }

    private static final class PooledWidget {
        String name() {
            return "widget";
        }
    }

    private static final class CloseableWidget implements AutoCloseable {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }
    }
}
