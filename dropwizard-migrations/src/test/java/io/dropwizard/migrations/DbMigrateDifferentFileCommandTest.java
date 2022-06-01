package io.dropwizard.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.util.Collections;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

@Execution(SAME_THREAD)
class DbMigrateDifferentFileCommandTest {

    private final DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
            TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-ddl.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @BeforeEach
    void setUp() {
        databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
    }

    @Test
    void testRun() throws Exception {
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);
        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons").mapToMap()).isEmpty();
        }
    }

    @Test
    void testRunForFileFromFilesystem() throws Exception {
        final String migrationsPath = getClass().getResource("/migrations.xml").getPath();
        migrateCommand.run(null, new Namespace(Collections.singletonMap("migrations-file", migrationsPath)), conf);
        try (Handle handle = Jdbi.create(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons").mapToMap()).hasSize(1);
        }
    }
}
