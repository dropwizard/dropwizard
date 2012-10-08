package com.yammer.dropwizard.hibernate.tests;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.hibernate.HibernateBundle;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HibernateBundleTest {
    private final ObjectMapperFactory objectMapperFactory = mock(ObjectMapperFactory.class);
    private final Bootstrap<?> bootstrap = mock(Bootstrap.class);
    private final HibernateBundle bundle = new HibernateBundle();

    @Before
    public void setUp() throws Exception {
        when(bootstrap.getObjectMapperFactory()).thenReturn(objectMapperFactory);
    }

    @Test
    public void enablesJacksonSupportForHibernate() throws Exception {
        bundle.initialize(bootstrap);

        final ArgumentCaptor<Hibernate4Module> captor = ArgumentCaptor.forClass(Hibernate4Module.class);
        verify(objectMapperFactory).registerModule(captor.capture());

        final Hibernate4Module module = captor.getValue();

        assertThat(module.isEnabled(Hibernate4Module.Feature.FORCE_LAZY_LOADING))
                .isTrue();

        assertThat(module.isEnabled(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION))
                .isTrue();
    }
}
