package com.codahale.dropwizard.testing.jdbi;

import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.testing.Person;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * A simple example of an integration test for a JDBI data access object.
 */
public class JdbiPeopleStoreIntegrationTests extends JdbiIntegrationTest {

    private static final int EXPECTED_ROWS_AFFECTED = 1;
    private final Person person = new Person("blah", "blah@example.com");

    private JdbiPeopleStore personStore;

    @Test
    public void daoShouldAddPeople() throws Exception {
        int rowsAffected = personStore.addPerson(person);
        assertThat(rowsAffected, is(equalTo(EXPECTED_ROWS_AFFECTED)));
    }

    @Test
    public void daoShouldFetchPeople() throws Exception {
        personStore.addPerson(person);
        Person fetchedPerson = personStore.fetchPerson("blah");
        assertThat(fetchedPerson, is(equalTo(person)));
    }

    @Override
    protected DataSourceFactory getDatabaseConfiguration() {
        DataSourceFactory databaseConfig = new DataSourceFactory();
        databaseConfig.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        databaseConfig.setUser("sa");
        databaseConfig.setPassword("");
        databaseConfig.setDriverClass("org.h2.Driver");
        return databaseConfig;
    }

    @Override
    protected void setUpDataAccessObjects() {
        personStore = onDemandDao(JdbiPeopleStore.class);
    }
}
