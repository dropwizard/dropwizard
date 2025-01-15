package io.dropwizard.migrations;

import liquibase.Scope;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jspecify.annotations.Nullable;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LiquibaseScopingTest implements CustomTaskChange {
    private final DbCommand<TestMigrationConfiguration> dbCommand = new DbCommand<>(
        "db",
        TestMigrationConfiguration::getDataSource,
        TestMigrationConfiguration.class,
        "migrations-custom-change.xml",
        Collections.singletonMap("person", new Person("Bill Smith"))
    );
    private final DbCommand<TestMigrationConfiguration> dbCommandWithoutScopedObjects = new DbCommand<>(
        "db",
        TestMigrationConfiguration::getDataSource,
        TestMigrationConfiguration.class,
        "migrations-custom-change.xml"
    );
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @BeforeEach
    void setUpTest() {
        databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
    }

    private static class Person {
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @Test
    void testCustomChange() throws Exception {
        dbCommand.run(null, new Namespace(Collections.singletonMap("subcommand", "migrate")), conf);
        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            final ResultIterable<Map<String, Object>> rows = handle.select("select * from persons").mapToMap();
            assertThat(rows).hasSize(1);
            Map<String, Object> dbPerson = rows.first();
            assertThat(dbPerson.getOrDefault("name", null))
                .isInstanceOfSatisfying(String.class, name -> assertThat(name).isEqualTo("Bill Smith"));
        }
    }

    @Test
    void testFailingCustomChange() {
        assertThatThrownBy(() ->
            dbCommandWithoutScopedObjects.run(null, new Namespace(Collections.singletonMap("subcommand", "migrate")), conf))
            .isInstanceOf(LiquibaseException.class);
    }


    @Override
    public void execute(Database database) throws CustomChangeException {
        if (!Scope.getCurrentScope().has("person")) {
            throw new CustomChangeException("No Person provided");
        }
        Person person = Scope.getCurrentScope().get("person", Person.class);
        JdbcConnection connection = (JdbcConnection) database.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO persons (name) VALUES (?);");
            statement.setString(1, person.getName());
            statement.execute();
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "";
    }

    @Override
    public void setUp() {
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    @Nullable
    public ValidationErrors validate(Database database) {
        return null;
    }
}
