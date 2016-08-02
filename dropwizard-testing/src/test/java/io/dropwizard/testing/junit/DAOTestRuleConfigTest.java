package io.dropwizard.testing.junit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.junit.Rule;
import org.junit.Test;

public class DAOTestRuleConfigTest {

    @Rule
    public final DAOTestRule database = DAOTestRule.newBuilder()
        .setUrl("jdbc:h2:mem:rule-config-test")
        .setDriver(org.h2.Driver.class)
        .setUsername("username")
        .useSqlComments(true)
        .setHbm2DdlAuto("create")
        .setShowSql(true)
        .addEntityClass(TestEntity.class)
        .setProperty("hibernate.format_sql", "true")
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
