package io.dropwizard.migrations;

import io.dropwizard.db.ManagedDataSource;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CloseableLiquibaseWithFileSystemMigrationsFile extends CloseableLiquibase implements AutoCloseable {
    CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file
    ) throws LiquibaseException, SQLException {
        this(dataSource,
            database,
            file,
            FileSystems.getDefault());
    }

    CloseableLiquibaseWithFileSystemMigrationsFile(
        ManagedDataSource dataSource,
        Database database,
        String file,
        FileSystem fileSystem
    ) throws LiquibaseException, SQLException {
        super(file,
            new FileSystemResourceAccessor(getRootPaths(fileSystem).toArray(new File[0])),
            database,
            dataSource);
    }

    private static List<File> getRootPaths(FileSystem fileSystem) {
        List<File> rootPaths = new ArrayList<>();
        fileSystem.getRootDirectories().forEach(path -> rootPaths.add(path.toFile()));
        try {
            rootPaths.add(new File(CloseableLiquibase.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        } catch (Exception ignored) {
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
