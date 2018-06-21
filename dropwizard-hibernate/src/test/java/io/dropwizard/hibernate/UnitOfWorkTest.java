package io.dropwizard.hibernate;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitOfWorkTest {
    private UnitOfWork unitOfWork;

    @Before
    public void setUp() throws Exception {
        this.unitOfWork = ClassWithUnitOfWork.class
                .getDeclaredMethod("defaultUnitOfWork")
                .getAnnotation(UnitOfWork.class);
    }

    @Test
    public void defaultsToReadWrite() throws Exception {
        assertThat(unitOfWork.readOnly())
                .isFalse();
    }

    @Test
    public void defaultsToTransactional() throws Exception {
        assertThat(unitOfWork.transactional())
                .isTrue();
    }

    @Test
    public void defaultsToNormalCaching() throws Exception {
        assertThat(unitOfWork.cacheMode())
                .isEqualTo(CacheMode.NORMAL);
    }

    @Test
    public void defaultsToAutomaticFlushing() throws Exception {
        assertThat(unitOfWork.flushMode())
                .isEqualTo(FlushMode.AUTO);
    }
}
