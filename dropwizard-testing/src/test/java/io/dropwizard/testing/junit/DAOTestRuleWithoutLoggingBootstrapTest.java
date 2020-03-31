package io.dropwizard.testing.junit;

import io.dropwizard.testing.app.TestEntity;
import org.hibernate.SessionFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DAOTestRuleWithoutLoggingBootstrapTest {
    @SuppressWarnings("deprecation")
    @Rule
    public final DAOTestRule daoTestRule = DAOTestRule.newBuilder()
            .addEntityClass(TestEntity.class)
            .bootstrapLogging(false)
            .build();

    @Test
    public void ruleCreatedSessionFactory() {
        final SessionFactory sessionFactory = daoTestRule.getSessionFactory();

        assertThat(sessionFactory).isNotNull();
    }
}
