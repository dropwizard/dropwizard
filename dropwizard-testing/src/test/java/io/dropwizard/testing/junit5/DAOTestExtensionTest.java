package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.validation.ConstraintViolationException;
import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(DropwizardExtensionsSupport.class)
class DAOTestExtensionTest {
    private final DAOTestExtension daoTestExtension = DAOTestExtension.newBuilder().addEntityClass(TestEntity.class).build();

    @Test
    void extensionCreatedSessionFactory() {
        final SessionFactory sessionFactory = daoTestExtension.getSessionFactory();

        assertThat(sessionFactory).isNotNull();
    }

    @Test
    void extensionCanOpenTransaction() {
        final Long id = daoTestExtension.inTransaction(() -> persist(new TestEntity("junit 5 description")).getId());

        assertThat(id).isNotNull();
    }

    @Test
    void extensionCanRoundtrip() {
        final Long id = daoTestExtension.inTransaction(() -> persist(new TestEntity("junit 5 description")).getId());

        final TestEntity testEntity = get(id);

        assertThat(testEntity).isNotNull();
        assertThat(testEntity.getDescription()).isEqualTo("junit 5 description");
    }

    @Test()
    void transactionThrowsExceptionAsExpected() {
        Throwable throwable = Assertions.assertThrows(ConstraintViolationException.class, () -> daoTestExtension.inTransaction(() -> persist(new TestEntity(null))));
        Assertions.assertEquals(ConstraintViolationException.class, throwable.getClass());
    }

    @Test
    void rollsBackTransaction() {
        // given a successfully persisted entity
        final TestEntity testEntity = new TestEntity("junit 5 description");
        daoTestExtension.inTransaction(() -> persist(testEntity));

        // when we prepare an update of that entity
        testEntity.setDescription("newDescription");
        // ... but cause a constraint violation during the actual update
        assertThatExceptionOfType(ConstraintViolationException.class)
            .isThrownBy(() -> daoTestExtension.inTransaction(() -> {
                persist(testEntity);
                persist(new TestEntity(null));
            }));
        // ... the entity has the original value
        assertThat(get(testEntity.getId()).getDescription()).isEqualTo("junit 5 description");
    }


    private TestEntity persist(TestEntity testEntity) {
        final Session currentSession = daoTestExtension.getSessionFactory().getCurrentSession();
        currentSession.saveOrUpdate(testEntity);

        return testEntity;
    }

    private TestEntity get(Serializable id) {
        final Session currentSession = daoTestExtension.getSessionFactory().getCurrentSession();
        final TestEntity testEntity = currentSession.get(TestEntity.class, id);
        currentSession.refresh(testEntity);

        return testEntity;
    }
}
