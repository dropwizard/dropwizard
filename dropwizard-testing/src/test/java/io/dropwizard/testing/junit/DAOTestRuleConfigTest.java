package io.dropwizard.testing.junit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Test;

public class DAOTestRuleConfigTest {
    
    private DAOTestRule database;

    @After
    public void tearDown() {
        database.after();
    }
    
    @Test
    public void explicitConfigCreatesSessionFactory() throws Throwable {
        database = DAOTestRule.newBuilder() //
            .setConnectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1") //
            .setConnectionDriverClass(org.h2.Driver.class) //
            .setConnectionUsername("username") //
            .setCurrentSessionContextClass("managed") //
            .setHbm2DdlAuto("create") //
            .setShowSql(false) //
            .addEntityClass(TestEntity.class) //
            .build();
        
        // when initializing this rule
        database.before();
        
        // it yields a valid SessionFactory instance
        assertThat(database.getSessionFactory(), notNullValue());
        
        final Session currentSession = database.getSessionFactory().getCurrentSession();
        
        // an instance of an entity contained in the package can be saved  
        currentSession.saveOrUpdate(new io.dropwizard.testing.junit.TestEntity("foo"));
    }
}
