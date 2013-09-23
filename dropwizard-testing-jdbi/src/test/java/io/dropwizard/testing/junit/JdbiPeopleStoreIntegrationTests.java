package io.dropwizard.testing.junit;

import com.codahale.dropwizard.db.DataSourceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * A simple example of an integration test for a JDBI data access object.
 */
public class JdbiPeopleStoreIntegrationTests {

    private static final int EXPECTED_ROWS_AFFECTED = 1;

    private JdbiPeopleStore personStore;

    private final Person person = new Person("blah", "blah@example.com");

    @Rule
    public JdbiIntegrationRule jdbiIntegrationRule = new JdbiIntegrationRule() {

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
    };

    @Before
    public void migrateDatabase() {
        Handle handle = jdbiIntegrationRule.getHandle();
        handle.createCall("DROP TABLE People IF EXISTS").invoke();
        handle.createCall("CREATE TABLE People (name VARCHAR(100) PRIMARY KEY, email VARCHAR(100))").invoke();
    }

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
}
