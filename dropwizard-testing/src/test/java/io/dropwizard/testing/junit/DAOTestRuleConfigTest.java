package io.dropwizard.testing.junit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.junit.Rule;
import org.junit.Test;

public class DAOTestRuleConfigTest {

    @Rule
    public final DAOTestRule database = DAOTestRule.newBuilder()
        .setConnectionUrl("jdbc:h2:mem:")
        .setConnectionDriverClass(org.h2.Driver.class)
        .setConnectionUsername("username")
        .setCurrentSessionContextClass("managed")
        .setHbm2DdlAuto("create")
        .setShowSql(false)
        .addEntityClass(TestEntity.class)
        .build();

    @Test
    public void explicitConfigCreatesSessionFactory() {
        // it yields a valid SessionFactory instance
        assertThat(database.getSessionFactory(), notNullValue());

        final Session currentSession = database.getSessionFactory().getCurrentSession();

        // an instance of an entity contained in the package can be saved
        currentSession.saveOrUpdate(new io.dropwizard.testing.junit.TestEntity("foo"));
    }
}
