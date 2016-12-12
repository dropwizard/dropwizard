package io.dropwizard.migrations;

import com.google.common.collect.ImmutableMap;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbMigrateCustomSchemaTest extends AbstractMigrationTest {

    private DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-custom-schema.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @Before
    public void setUp() throws Exception {
        databaseUrl = "jdbc:h2:" + createTempFile();
        conf = createConfiguration(databaseUrl);
    }

    @Test
    public void testRunMigrationWithCustomSchema() throws Exception {
        String schemaName = "customschema";
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            handle.execute("create schema " + schemaName);
        }
        Namespace namespace = new Namespace(ImmutableMap.of("schema", schemaName));
        migrateCommand.run(null, namespace, conf);
        try (Handle handle = new DBI(databaseUrl, "sa", "").open()) {
            final List<Map<String, Object>> rows = handle.select("select * from " + schemaName + ".persons");
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0)).isEqualTo(
                ImmutableMap.of("id", 1, "name", "Bill Smith", "email", "bill@smith.me"));
        }
    }

}
