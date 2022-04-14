package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CloseableLiquibaseWithFileSystemMigrationsFile extends CloseableLiquibase implements AutoCloseable {

    CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file
    ) throws LiquibaseException, SQLException {
        super(file,
            new FileSystemResourceAccessor(getRootPaths().toArray(new File[0])),
            database,
            dataSource);
    }

    private static List<File> getRootPaths() {
        List<File> rootPaths = new ArrayList<>();
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win");
        if (isWindows) {
            rootPaths.add(new File("C:\\"));
        } else {
            rootPaths.add(new File("/"));
        }
        try {
            rootPaths.add(new File(CloseableLiquibase.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (URISyntaxException ignored) {
            // if we cannot acquire the path of the executing jar, skip it
        }
        return rootPaths;
    }

    public CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        String file
    ) throws LiquibaseException, SQLException {
        this(dataSource,
            DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection())),
            file);
    }
}
