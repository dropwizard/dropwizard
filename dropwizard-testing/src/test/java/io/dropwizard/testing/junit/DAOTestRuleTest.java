package io.dropwizard.testing.junit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.Serializable;

import javax.validation.ConstraintViolationException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Rule;
import org.junit.Test;

public class DAOTestRuleTest {

    @Rule
    public final DAOTestRule daoTestRule = DAOTestRule.newBuilder().addEntityClass(TestEntity.class).build();

    @Test
    public void ruleCreatedSessionFactory() {
        final SessionFactory sessionFactory = daoTestRule.getSessionFactory();

        assertThat(sessionFactory, notNullValue());
    }

    @Test
    public void ruleCanOpenTransaction() {
        final Long id = daoTestRule.transaction(() -> persist(new TestEntity("description")).getId());

        assertThat(id, notNullValue());
    }

    @Test
    public void ruleCanRoundtrip() {
        final Long id = daoTestRule.transaction(() -> persist(new TestEntity("description")).getId());

        final TestEntity testEntity = get(id);

        assertThat(testEntity, notNullValue());
        assertThat(testEntity.getDescription(), equalTo("description"));
    }

    @Test(expected = ConstraintViolationException.class)
    public void transcationThrowsExceptionAsExpected() {
        daoTestRule.transaction(() -> persist(new TestEntity(null)));
    }

    @Test
    public void rollsBackTransaction() {
        // given a successfully persisted entity
        final TestEntity testEntity = new TestEntity("description");
        daoTestRule.transaction(() -> {
            persist(testEntity);
            return true;
        });

        // when we prepare an update of that entity
        testEntity.setDescription("newDescription");
        try {
            // ... but cause a constraint violation during the actual update
            daoTestRule.transaction(() -> {
                persist(testEntity);
                persist(new TestEntity(null));
                return true;
            });
            fail("Expected a constraint violation");
        } catch (ConstraintViolationException ignoredException) {
            // keep calm and carry on
        }

        // ... the entity has the original value
        final TestEntity sameTestEntity = get(testEntity.getId());
        assertThat(sameTestEntity.getDescription(), equalTo("description"));
    }


    private TestEntity persist(TestEntity testEntity) {
        final Session currentSession = daoTestRule.getSessionFactory().getCurrentSession();
        currentSession.saveOrUpdate(testEntity);

        return testEntity;
    }

    private TestEntity get(Serializable id) {
        final Session currentSession = daoTestRule.getSessionFactory().getCurrentSession();
        final TestEntity testEntity = currentSession.get(TestEntity.class, id);
        currentSession.refresh(testEntity);

        return testEntity;
    }
}
