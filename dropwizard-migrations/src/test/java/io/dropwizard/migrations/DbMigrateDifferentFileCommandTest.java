package io.dropwizard.migrations;

import io.dropwizard.util.Resources;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbMigrateDifferentFileCommandTest extends AbstractMigrationTest {

    private DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-ddl.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @Before
    public void setUp() throws Exception {
        databaseUrl = getDatabaseUrl();
        conf = createConfiguration(databaseUrl);
    }

    @Test
    public void testRun() throws Exception {
        migrateCommand.run(null, new Namespace(Collections.emptyMap()), conf);
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            final List<Map<String, Object>> rows = handle.select("select * from persons");
            assertThat(rows).hasSize(0);
        }
    }

    @Test
    @Ignore("Ignored until https://liquibase.jira.com/browse/CORE-3262 has been solved")
    public void testRunForFileFromFilesystem() throws Exception {
        final String migrationsPath = new File(Resources.getResource("migrations.xml").toURI())
            .getAbsolutePath();
        migrateCommand.run(null, new Namespace(Collections.singletonMap("migrations-file", migrationsPath)), conf);
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            assertThat(handle.select("select * from persons")).hasSize(1);
        }
    }

}
