package io.dropwizard.testing.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.app.TestEntity;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DAOTestExtensionRegisterExtensionTest {

    @RegisterExtension
    static final DAOTestExtension daoTestExtension =
            DAOTestExtension.newBuilder().addEntityClass(TestEntity.class).build();

    @Test
    void shouldProvideSession() {
        final SessionFactory sessionFactory = daoTestExtension.getSessionFactory();
        assertThat(sessionFactory).isNotNull();
    }
}
