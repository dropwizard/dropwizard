package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestEntity;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;


class DAOTestExtensionRegisterExtensionTest {

    @RegisterExtension
    static final DAOTestExtension daoTestExtension =
        DAOTestExtension.newBuilder()
            .addEntityClass(TestEntity.class)
            .build();

    @Test
    void shouldProvideSession() {
        assertThat(daoTestExtension.getSessionFactory()).isNotNull();
    }
}
