package io.dropwizard.hibernate;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnitOfWorkTest {
    private static class Example {
        @UnitOfWork
        public void example() {

        }
    }

    private UnitOfWork unitOfWork;

    @BeforeEach
    void setUp() throws Exception {
        this.unitOfWork = Example.class.getDeclaredMethod("example")
                                       .getAnnotation(UnitOfWork.class);
    }

    @Test
    void defaultsToReadWrite() throws Exception {
        assertThat(unitOfWork.readOnly())
                .isFalse();
    }

    @Test
    void defaultsToTransactional() throws Exception {
        assertThat(unitOfWork.transactional())
                .isTrue();
    }

    @Test
    void defaultsToNormalCaching() throws Exception {
        assertThat(unitOfWork.cacheMode())
                .isEqualTo(CacheMode.NORMAL);
    }

    @Test
    void defaultsToAutomaticFlushing() throws Exception {
        assertThat(unitOfWork.flushMode())
                .isEqualTo(FlushMode.AUTO);
    }
}
