package io.dropwizard.migrations;

import io.dropwizard.db.DataSourceFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AddColumnGeneratorSQLite;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jdbi.v3.core.Handle;

import java.sql.SQLException;
import java.util.UUID;

class AbstractMigrationTest {

    static {
        SqlGeneratorFactory.getInstance().unregister(AddColumnGeneratorSQLite.class);
    }

    protected static Subparser createSubparser(AbstractLiquibaseCommand<?> command) {
        final Subparser subparser = ArgumentParsers.newFor("db")
            .terminalWidthDetection(false)
            .build()
            .addSubparsers()
            .addParser(command.getName())
            .description(command.getDescription());
        command.configure(subparser);
        return subparser;
    }

    protected static TestMigrationConfiguration createConfiguration(String databaseUrl) {
        final DataSourceFactory dataSource = new DataSourceFactory();
        dataSource.setDriverClass("org.h2.Driver");
        dataSource.setUser("sa");
        dataSource.setUrl(databaseUrl);
        return new TestMigrationConfiguration(dataSource);
    }

    protected static String getDatabaseUrl() {
        return "jdbc:h2:mem:" + UUID.randomUUID() + ";db_close_delay=-1";
    }

    protected boolean tableExists(final Handle handle, final String tableName) throws SQLException {
        return handle.getConnection().getMetaData().getTables(null, null, tableName, null).next();
    }

    protected boolean columnExists(final Handle handle, final String tableName, final String columnName) throws SQLException {
        return handle.getConnection().getMetaData().getColumns(null, null, tableName, columnName).next();
    }
}
