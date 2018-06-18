package io.dropwizard.migrations;

import io.dropwizard.util.Maps;
import net.jcip.annotations.NotThreadSafe;
import net.sourceforge.argparse4j.inf.Namespace;
import org.assertj.core.data.Index;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;

import static org.assertj.core.api.Assertions.assertThat;

@NotThreadSafe
public class DbMigrateCustomSchemaTest extends AbstractMigrationTest {

    private DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-custom-schema.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @Before
    public void setUp() throws Exception {
        databaseUrl = getDatabaseUrl();
        conf = createConfiguration(databaseUrl);
    }

    @Test
    public void testRunMigrationWithCustomSchema() throws Exception {
        String schemaName = "customschema";
        DBI dbi = new DBI(databaseUrl, "sa", "");
        dbi.useHandle(h -> h.execute("create schema " + schemaName));
        Namespace namespace = new Namespace(Maps.of("schema", schemaName, "catalog", "public"));
        migrateCommand.run(null, namespace, conf);
        dbi.useHandle(handle ->
            assertThat(handle
                .select("select * from " + schemaName + ".persons"))
                .hasSize(1)
                .contains(Maps.of("id", 1, "name", "Bill Smith", "email", "bill@smith.me"), Index.atIndex(0))
        );
    }

}
