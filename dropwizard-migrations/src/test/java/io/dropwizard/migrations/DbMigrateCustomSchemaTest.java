package io.dropwizard.migrations;

import io.dropwizard.util.Maps;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
class DbMigrateCustomSchemaTest {

    private final DbMigrateCommand<TestMigrationConfiguration> migrateCommand = new DbMigrateCommand<>(
        TestMigrationConfiguration::getDataSource, TestMigrationConfiguration.class, "migrations-custom-schema.xml");
    private TestMigrationConfiguration conf;
    private String databaseUrl;

    @BeforeEach
    void setUp() {
        databaseUrl = MigrationTestSupport.getDatabaseUrl();
        conf = MigrationTestSupport.createConfiguration(databaseUrl);
    }

    @Test
    void testRunMigrationWithCustomSchema() throws Exception {
        String schemaName = "customschema";
        Jdbi dbi = Jdbi.create(databaseUrl, "sa", "");
        dbi.useHandle(h -> h.execute("create schema " + schemaName));
        Namespace namespace = new Namespace(Maps.of("schema", schemaName, "catalog", "public"));
        migrateCommand.run(null, namespace, conf);
        dbi.useHandle(handle ->
            assertThat(handle
                .select("select * from " + schemaName + ".persons")
                .mapToMap())
                .hasSize(1)
                .containsExactly(Maps.of("id", 1, "name", "Bill Smith", "email", "bill@smith.me"))
        );
    }

}
