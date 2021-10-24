package io.dropwizard.migrations;

import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
class DbMigrateDifferentFileCommandTest extends AbstractMigrationTest {

    private final DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-ddl.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @BeforeEach
    void setUp() {
        databaseUrl = getDatabaseUrl();
        conf = createConfiguration(databaseUrl);
    }

    @Test
    void testRun() throws Exception {
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);
        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons").mapToMap())
                .isEmpty();
        }
    }

    @Test
    @Disabled("Ignored until https://liquibase.jira.com/browse/CORE-3262 has been solved")
    void testRunForFileFromFilesystem() throws Exception {
        final String migrationsPath = getClass().getResource("/migrations.xml").getPath();
        migrateCommand.run(null, new Namespace(Collections.singletonMap("migrations-file", migrationsPath)), conf);
        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons").mapToMap())
                .hasSize(1);
        }
    }

}
